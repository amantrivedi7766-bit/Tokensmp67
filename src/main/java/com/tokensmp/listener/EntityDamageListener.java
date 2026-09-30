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
        TokenTier tier = passives.activeTier(player);
        if (tier == null) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();

        // 100% fall damage immunity (Spider T3).
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
