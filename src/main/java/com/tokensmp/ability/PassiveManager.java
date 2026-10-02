package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
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
import org.bukkit.Sound;
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
    private final Map<UUID, org.bukkit.scheduler.BukkitRunnable> breathingLoops = new HashMap<>();

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
        applyWeightPassives(player, tier);
    }

    /** Heavy-body + unstoppable attribute passives (Ravager identity). */
    private void applyWeightPassives(Player player, TokenTier tier) {
        Attribute speed = VersionCompatibility.attribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        if (speed != null && tier.getMovementSpeedModifier() != 0.0) {
            AttributeInstance instance = player.getAttribute(speed);
            if (instance != null) {
                NamespacedKey key = new NamespacedKey(plugin, "weight_speed");
                if (instance.getModifier(key) == null) {
                    instance.addModifier(new AttributeModifier(key, tier.getMovementSpeedModifier(),
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                }
            }
        }
        Attribute knockback = VersionCompatibility.attribute("KNOCKBACK_RESISTANCE",
                "GENERIC_KNOCKBACK_RESISTANCE");
        if (knockback != null && tier.getKnockbackResistance() > 0.0) {
            AttributeInstance instance = player.getAttribute(knockback);
            if (instance != null) {
                NamespacedKey key = new NamespacedKey(plugin, "weight_knockback");
                if (instance.getModifier(key) == null) {
                    instance.addModifier(new AttributeModifier(key, tier.getKnockbackResistance(),
                            AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }
        if (tier.getAmbientSound() != null || tier.hasFireAura()) {
            startAmbientLoop(player);
        }
    }

    /**
     * Ambient passive loop (runs once per second): the token's periodic sound
     * and the fire aura that sets nearby enemies alight.
     */
    private void startAmbientLoop(Player player) {
        UUID id = player.getUniqueId();
        if (breathingLoops.containsKey(id)) {
            return;
        }
        org.bukkit.scheduler.BukkitRunnable loop = new org.bukkit.scheduler.BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                TokenTier active = activeTier(player);
                if (!player.isOnline() || active == null
                        || (active.getAmbientSound() == null && !active.hasFireAura())) {
                    breathingLoops.remove(id);
                    cancel();
                    return;
                }
                ticks += 20;
                if (active.getAmbientSound() != null
                        && ticks % Math.max(20, active.getAmbientIntervalTicks()) == 0) {
                    SoundEngine.play(player, active.getAmbientSound(), 0.6f, 0.8f);
                }
                if (active.hasFireAura()) {
                    double radius = plugin.config().getDouble("tokens.blaze.aura-radius", 3.0);
                    int fireTicks = plugin.config().getInt("tokens.blaze.aura-fire-ticks", 20);
                    for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                        if (entity instanceof org.bukkit.entity.LivingEntity living && !entity.equals(player)) {
                            living.setFireTicks(Math.max(living.getFireTicks(), fireTicks));
                        }
                    }
                }
            }
        };
        loop.runTaskTimer(plugin, 20L, 20L);
        plugin.scheduler().register(loop);
        breathingLoops.put(id, loop);
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
        Attribute speed = VersionCompatibility.attribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED");
        if (speed != null) {
            VersionCompatibility.removeModifier(player, speed, new NamespacedKey(plugin, "weight_speed"));
        }
        Attribute knockback = VersionCompatibility.attribute("KNOCKBACK_RESISTANCE",
                "GENERIC_KNOCKBACK_RESISTANCE");
        if (knockback != null) {
            VersionCompatibility.removeModifier(player, knockback,
                    new NamespacedKey(plugin, "weight_knockback"));
        }
        org.bukkit.scheduler.BukkitRunnable loop = breathingLoops.remove(player.getUniqueId());
        if (loop != null) {
            loop.cancel();
        }
    }

    private void clearPotions(Player player) {
        for (String[] names : new String[][]{{"STRENGTH", "INCREASE_DAMAGE"}, {"SPEED", null},
                {"NIGHT_VISION", null}, {"REGENERATION", null}, {"FIRE_RESISTANCE", null},
                {"JUMP_BOOST", "JUMP"}, {"RESISTANCE", "DAMAGE_RESISTANCE"}, {"ABSORPTION", null}}) {
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
        org.bukkit.scheduler.BukkitRunnable loop = breathingLoops.remove(event.getPlayer().getUniqueId());
        if (loop != null) {
            loop.cancel();
        }
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
            com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                    org.bukkit.Particle.CLOUD, player.getLocation(), 8, 0.2);
        }
    }
}
