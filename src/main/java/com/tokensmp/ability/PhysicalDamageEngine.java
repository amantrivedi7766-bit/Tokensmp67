package com.tokensmp.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * The real combat engine behind every damaging ability.
 *
 * Two damage modes:
 * - dealDamage(): a genuine Bukkit damage event attributed to the attacker
 *   (armour, shields and PvP rules apply).
 * - dealTrueDamage(): server-authoritative true damage that bypasses armour
 *   and shields entirely.
 *
 * Self-damage and invalid targets are never hit; world PvP rules are
 * respected for event-based damage.
 */
public final class PhysicalDamageEngine {

    private PhysicalDamageEngine() {
    }

    /**
     * Deals real event-based damage to a target (respects armour, shields,
     * PvP rules and protection plugins).
     */
    public static void dealDamage(LivingEntity target, double damage, Player attacker) {
        if (target == null || target.equals(attacker) || target.isDead()) {
            return;
        }
        if (target instanceof Player && !target.getWorld().getPVP()) {
            return; // respect world PvP rules
        }
        target.damage(damage, attacker);
    }

    /**
     * Deals TRUE damage: bypasses armour and shields entirely, but is still
     * attributed to the attacker (kills credit the ability user).
     */
    public static void dealTrueDamage(LivingEntity target, double damage, Player attacker) {
        if (target == null || target.equals(attacker) || target.isDead()) {
            return;
        }
        target.setHealth(Math.max(0.0, target.getHealth() - damage));
        target.setKiller(attacker);
    }

    /**
     * AoE damage with knockback around a center. Every living entity in range
     * except the attacker is hit; victims are knocked away from the center.
     */
    public static int areaDamage(Player attacker, Location center, double radius,
                                 double damage, double knockback, boolean trueDamage) {
        int hit = 0;
        for (Entity entity : attacker.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(attacker)) {
                if (trueDamage) {
                    dealTrueDamage(target, damage, attacker);
                } else {
                    dealDamage(target, damage, attacker);
                }
                if (knockback > 0 && entity.isValid()) {
                    Vector away = entity.getLocation().toVector()
                            .subtract(center.toVector());
                    if (away.lengthSquared() < 0.001) {
                        away = new Vector(0, 1, 0);
                    }
                    Vector push = away.normalize().multiply(knockback);
                    push.setY(Math.max(push.getY(), 0.4));
                    entity.setVelocity(push);
                }
                hit++;
            }
        }
        return hit;
    }

    /**
     * True when a Bukkit damage event was caused by the player's arrow
     * (used for the bow damage passives).
     */
    public static boolean arrowFrom(EntityDamageByEntityEvent event, Player shooter) {
        return event.getDamager() instanceof org.bukkit.entity.Arrow arrow
                && arrow.getShooter() instanceof Player player
                && player.equals(shooter);
    }
}
