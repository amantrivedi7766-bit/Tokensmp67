package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.VersionCompatibility;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import com.tokensmp.token.TokenTier;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains the passive state of every player's ACTIVE (claimed) token:
 * infinite potion effects, extra hearts, permanent survival flight, wall
 * climbing, cloud jumps, bow damage bonuses and damage immunities.
 * Server data is authoritative - passives apply only to claimed tokens.
 */
public final class PassiveManager implements Listener {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final Map<UUID, Long> climbBoost = new HashMap<>();
    private final Map<UUID, Long> cloudJump = new HashMap<>();

    public PassiveManager(TokenSMP plugin, TokenRegistry registry, TokenDataManager data) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
    }

    /** The tier definition of the player's ACTIVE claimed token, or null. */
    public TokenTier activeTier(Player player) {
        String activeId = data.getActiveToken(player);
        if (activeId == null || !data.isClaimed(player, activeId)) {
            return null;
        }
        Token token = registry.get(activeId);
        return token == null ? null : token.tier(data.getTier(player, activeId));
    }

    /** (Re-)applies every passive of the active token. */
    public void applyPassives(Player player) {
        TokenTier tier = activeTier(player);
        clearPassives(player);
        if (tier == null) {
            return;
        }
        Attribute maxHealth = VersionCompatibility.maxHealthAttribute();
        if (tier.getExtraHearts() > 0 && maxHealth != null) {
            AttributeInstance instance = player.getAttribute(maxHealth);
            if (instance != null) {
                NamespacedKey key = new NamespacedKey(plugin, "extra_hearts");
                if (instance.getModifier(key) == null) {
                    instance.addModifier(new AttributeModifier(key,
                            tier.getExtraHearts() * 2.0, AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }
        for (TokenTier.PotionPassive passive : tier.getPotions()) {
            player.addPotionEffect(new PotionEffect(passive.type(), PotionEffect.INFINITE_DURATION,
                    passive.amplifier(), true, false));
        }
        if (tier.hasFlight() && player.getGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(true);
        }
    }

    /** Removes every passive granted by this plugin. */
    public void clearPassives(Player player) {
        clearPotions(player);
        Attribute maxHealth = VersionCompatibility.maxHealthAttribute();
        if (maxHealth != null) {
            VersionCompatibility.removeModifier(player, maxHealth, new NamespacedKey(plugin, "extra_hearts"));
        }
        if (player.getGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(false);
        }
    }

    private void clearPotions(Player player) {
        for (String[] names : new String[][]{{"STRENGTH", "INCREASE_DAMAGE"}, {"SPEED", null},
                {"NIGHT_VISION", null}, {"REGENERATION", null}, {"FIRE_RESISTANCE", null},
                {"JUMP_BOOST", "JUMP"}, {"RESISTANCE", "DAMAGE_RESISTANCE"}, {"ABSORPTION", null},
                {"WATER_BREATHING", null}, {"SLOW_FALLING", null}, {"DOLPHINS_GRACE", null},
                {"HASTE", "FAST_DIGGING"}, {"LUCK", null}}) {
            PotionEffectType type = VersionCompatibility.potionType(names[0], names[1]);
            if (type != null && player.getPotionEffect(type) != null) {
                player.removePotionEffect(type);
            }
        }
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyPassives(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.scheduler().runLater(1L, () -> {
            if (event.getPlayer().isOnline()) {
                applyPassives(event.getPlayer());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        climbBoost.remove(event.getPlayer().getUniqueId());
        cloudJump.remove(event.getPlayer().getUniqueId());
    }

    // ------------------------------------------------------------------
    // Bow damage bonus (event-based so it stacks cleanly with Minecraft)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)
                || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }
        TokenTier tier = activeTier(shooter);
        if (tier == null || tier.getBowDamageBonus() <= 0) {
            return;
        }
        event.setDamage(event.getDamage() * (1.0 + tier.getBowDamageBonus()));
    }

    // ------------------------------------------------------------------
    // Melee damage bonus (event-based, Piglin line)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        TokenTier tier = activeTier(attacker);
        if (tier == null || tier.getMeleeDamageBonus() <= 0) {
            return;
        }
        // Only direct melee strikes benefit - projectiles are skipped.
        if (event.getEntity() == null
                || event.getDamageSource().getDirectEntity() != attacker) {
            return;
        }
        event.setDamage(event.getDamage() * (1.0 + tier.getMeleeDamageBonus()));
    }

    // ------------------------------------------------------------------
    // Movement passives: wall climbing + cloud jumps
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TokenTier tier = activeTier(player);
        if (tier == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (tier.hasWallClimbing() && !player.isOnGround()
                && com.tokensmp.util.LocationUtil.isAgainstWall(player.getLocation())) {
            if (now - climbBoost.getOrDefault(player.getUniqueId(), 0L) >= 250L) {
                climbBoost.put(player.getUniqueId(), now);
                player.setVelocity(new org.bukkit.util.Vector(0, 0.42, 0));
            }
        }
        if (tier.hasCloudJumps() && player.getVelocity().getY() > 0.36
                && now - cloudJump.getOrDefault(player.getUniqueId(), 0L) >= 600L) {
            cloudJump.put(player.getUniqueId(), now);
            com.tokensmp.animation.ParticleManager.burst(player.getWorld(),
                    org.bukkit.Particle.CLOUD, player.getLocation(), 8, 0.2);
        }
    }
}
