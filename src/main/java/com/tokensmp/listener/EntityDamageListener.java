package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.ability.PassiveManager;
import com.tokensmp.token.TokenTier;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Damage passives for the ACTIVE token: fall immunity, explosion immunity,
 * ender pearl damage immunity, the Blaze burning aura (attackers ignite)
 * and cosmetic firework damage cancellation.
 */
public final class EntityDamageListener implements Listener {

    private final TokenSMP plugin;
    private final PassiveManager passives;

    public EntityDamageListener(TokenSMP plugin, PassiveManager passives) {
        this.plugin = plugin;
        this.passives = passives;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // Grace period: player-vs-player damage is disabled (configurable).
        if (plugin.grace().isActive()
                && plugin.config().getBoolean("grace-period.block-pvp", true)
                && event.getDamageSource().getCausingEntity() instanceof Player attacker
                && !attacker.equals(player)) {
            event.setCancelled(true);
            return;
        }
        TokenTier tier = passives.activeTier(player);
        if (tier == null) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();

        // Guardian water shield: projectiles are deflected entirely.
        if (plugin.abilities().isShielded(player)
                && event.getDamageSource().getDirectEntity() != null
                && !(event.getDamageSource().getDirectEntity() instanceof LivingEntity)) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_GUARDIAN_HURT, 1.0f, 1.4f);
            com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                    org.bukkit.Particle.BUBBLE_POP, player.getLocation().add(0, 1, 0), 20, 0.5);
            return;
        }
        // Enderman projectile dodge chance.
        if (tier.getProjectileDodge() > 0.0
                && event.getDamageSource().getDirectEntity() != null
                && !(event.getDamageSource().getDirectEntity() instanceof LivingEntity)
                && Math.random() < tier.getProjectileDodge()) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.6f);
            return;
        }

        // 100% fall damage immunity (Slime passive).
        if (tier.hasFallImmunity() && cause == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }
        // Ender pearl damage immunity (Enderman).
        if (tier.hasPearlDamageImmunity()
                && event.getDamageSource().getDirectEntity() instanceof org.bukkit.entity.EnderPearl) {
            event.setCancelled(true);
            return;
        }
        // Full fire + lava immunity (Blaze passive).
        if (tier.hasFireImmunity()
                && (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR
                || cause == EntityDamageEvent.DamageCause.MELTING
                || cause == EntityDamageEvent.DamageCause.CAMPFIRE)) {
            event.setCancelled(true);
            player.setFireTicks(0);
            return;
        }
        // Blast protection / explosion immunity (Creeper).
        if (tier.getExplosionImmunity() > 0
                && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            if (tier.getExplosionImmunity() >= 1.0) {
                event.setCancelled(true);
            } else {
                event.setDamage(event.getDamage() * (1.0 - tier.getExplosionImmunity()));
            }
        }
    }

    /** Guardian shield thorns + Wither Skeleton aura on melee attackers. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShieldThorns(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (plugin.abilities().isShielded(victim)) {
            PhysicalDamageEngine.dealDamage(attacker, plugin.abilities().shieldThorns(), victim);
            victim.getWorld().playSound(victim.getLocation(),
                    org.bukkit.Sound.ENCHANT_THORNS_HIT, 1.0f, 1.0f);
        }
        TokenTier tier = passives.activeTier(victim);
        if (tier != null && tier.hasWitherAura()) {
            attacker.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WITHER, 80, 1));
        }
    }

    /** Burning Aura: melee attackers of a Blaze token holder catch fire. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        TokenTier tier = passives.activeTier(victim);
        if (tier != null && tier.hasBurningAura()) {
            int burn = plugin.config().getInt("tokens.blaze.burning-aura-seconds", 4);
            attacker.setFireTicks(Math.max(attacker.getFireTicks(), burn * 20));
        }
    }

    /** Our cosmetic fireworks never damage anyone. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework
                && firework.hasMetadata("tokensmp_firework")) {
            event.setCancelled(true);
        }
    }
}
