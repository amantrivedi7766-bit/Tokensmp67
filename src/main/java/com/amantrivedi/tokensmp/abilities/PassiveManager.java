package com.amantrivedi.tokensmp.abilities;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maintains the passive state of every player's ACTIVE token: potion effects,
 * extra-heart attribute modifiers, permanent survival flight, bow damage
 * bonuses, explosion/fall damage immunity, wall climbing and jump particles.
 */
public final class PassiveManager implements Listener {

    private final TokenSmpPlugin plugin;
    private final Map<UUID, Long> climbBoost = new HashMap<>();
    private final Map<UUID, Long> lastCloudJump = new HashMap<>();

    public PassiveManager(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** (Re-)applies the active token's passives; clears stale state when switching. */
    public void applyPassives(Player player) {
        String activeId = plugin.getData().getActiveToken(player);
        TokenDefinition active = activeId == null ? null : plugin.getRegistry().get(activeId);
        TierDefinition tier = active == null ? null : active.tier(plugin.getData().getTier(player, active.getId()));

        removeStaleState(player);

        if (tier == null) {
            return;
        }

        // Extra hearts via a per-token attribute modifier.
        if (tier.getExtraHearts() > 0) {
            AttributeInstance maxHealth = player.getAttribute(plugin.getMaxHealthAttribute());
            if (maxHealth != null) {
                NamespacedKey key = new NamespacedKey(plugin, "hearts_" + active.getId());
                if (maxHealth.getModifier(key) == null) {
                    maxHealth.addModifier(new AttributeModifier(key,
                            tier.getExtraHearts() * 2.0, AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }

        // Infinite potion passives.
        for (TierDefinition.PotionPassive passive : tier.getPotions()) {
            PotionEffect wanted = new PotionEffect(passive.type(), PotionEffect.INFINITE_DURATION,
                    passive.amplifier(), true, false);
            PotionEffect current = player.getPotionEffect(passive.type());
            if (current == null || !current.equals(wanted)) {
                player.addPotionEffect(wanted);
            }
        }

        // Permanent Survival Mode flight (admin God Mode).
        if (tier.hasFlight() && player.getGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(true);
        }
    }

    /** Clears every potion effect and flight permission granted by this plugin. */
    private void removeStaleState(Player player) {
        for (String name : new String[]{"STRENGTH", "SPEED", "NIGHT_VISION", "REGENERATION",
                "FIRE_RESISTANCE", "JUMP_BOOST", "RESISTANCE"}) {
            PotionEffectType type = plugin.resolvePotion(name);
            if (type != null && player.getPotionEffect(type) != null) {
                player.removePotionEffect(type);
            }
        }
        // Remove all heart modifiers granted by any of our tokens.
        AttributeInstance maxHealth = player.getAttribute(plugin.getMaxHealthAttribute());
        if (maxHealth != null) {
            for (TokenDefinition token : plugin.getRegistry().all()) {
                AttributeModifier modifier = maxHealth.getModifier(
                        new NamespacedKey(plugin, "hearts_" + token.getId()));
                if (modifier != null) {
                    maxHealth.removeModifier(modifier);
                }
            }
        }
        if (player.getGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(false);
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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                applyPassives(event.getPlayer());
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        climbBoost.remove(event.getPlayer().getUniqueId());
        lastCloudJump.remove(event.getPlayer().getUniqueId());
    }

    // ------------------------------------------------------------------
    // Damage passives
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        TierDefinition tier = activeTier(player);
        if (tier == null) {
            return;
        }
        // 100% fall damage immunity.
        if (tier.hasFallImmunity() && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }
        // Blast protection / explosion immunity.
        if (tier.getExplosionImmunity() > 0
                && (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            if (tier.getExplosionImmunity() >= 1.0) {
                event.setCancelled(true);
            } else {
                event.setDamage(event.getDamage() * (1.0 - tier.getExplosionImmunity()));
            }
        }
    }

    /** Bow damage bonus for arrows shot by players with an active bow-damage passive. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player player)) {
            return;
        }
        TierDefinition tier = activeTier(player);
        if (tier == null || tier.getBowDamageBonus() <= 0) {
            return;
        }
        event.setDamage(event.getDamage() * (1.0 + tier.getBowDamageBonus()));
    }

    // ------------------------------------------------------------------
    // Movement passives: wall climbing + cloud jump particles
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TierDefinition tier = activeTier(player);
        if (tier == null) {
            return;
        }

        // Wall climbing (Spider): boost upward while pushing against a solid wall mid-air.
        if (tier.hasWallClimbing() && !player.isOnGround() && isAgainstWall(player)) {
            long now = System.currentTimeMillis();
            if (now - climbBoost.getOrDefault(player.getUniqueId(), 0L) >= 250L) {
                climbBoost.put(player.getUniqueId(), now);
                player.setVelocity(new Vector(0, 0.42, 0));
            }
        }

        // Cloud particle bursts on jumps (Spider max tier).
        if (tier.hasCloudJumps()) {
            long now = System.currentTimeMillis();
            if (player.getVelocity().getY() > 0.36
                    && now - lastCloudJump.getOrDefault(player.getUniqueId(), 0L) >= 600L) {
                lastCloudJump.put(player.getUniqueId(), now);
                Location feet = player.getLocation();
                player.getWorld().spawnParticle(Particle.CLOUD, feet, 8, 0.2, 0.05, 0.2, 0.02);
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** The tier definition of the player's ACTIVE token (or null). */
    public TierDefinition activeTier(Player player) {
        String activeId = plugin.getData().getActiveToken(player);
        TokenDefinition active = activeId == null ? null : plugin.getRegistry().get(activeId);
        if (active == null) {
            return null;
        }
        return active.tier(plugin.getData().getTier(player, active.getId()));
    }

    /** True when the player's active token tier has the arrow trail passive. */
    public boolean hasArrowTrail(Player player) {
        TierDefinition tier = activeTier(player);
        return tier != null && tier.hasArrowTrail();
    }

    /** True when a solid block directly borders the player horizontally (wall climbing). */
    private boolean isAgainstWall(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return loc.getWorld() != null
                && (loc.getWorld().getBlockAt(x + 1, y, z).getType().isSolid()
                || loc.getWorld().getBlockAt(x - 1, y, z).getType().isSolid()
                || loc.getWorld().getBlockAt(x, y, z + 1).getType().isSolid()
                || loc.getWorld().getBlockAt(x, y, z - 1).getType().isSolid());
    }

    /** Spawns the arrow trail particles (white CRIT + SNOWFLAKE). */
    public void spawnArrowTrail(Location location, Player shooter) {
        shooter.getWorld().spawnParticle(Particle.CRIT, location, 4, 0.05, 0.05, 0.05, 0.01);
        shooter.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 2, 0.05, 0.05, 0.05, 0.0);
    }
}
