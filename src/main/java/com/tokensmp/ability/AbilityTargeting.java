package com.tokensmp.ability;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * The real targeting system behind every combat ability: area, ray/line,
 * cone and nearest-target acquisition, all with full validity validation
 * (same world, range, dead state, spectator status, PvP rules). No ability
 * ever damages through anything but this class + PhysicalDamageEngine.
 */
public final class AbilityTargeting {

    private AbilityTargeting() {
    }

    /** True when the entity is a valid combat target for the caster. */
    public static boolean validTarget(Player caster, Entity entity) {
        if (!(entity instanceof LivingEntity living)
                || entity.equals(caster)
                || living.isDead()
                || !living.isValid()) {
            return false;
        }
        if (living instanceof Player target) {
            if (target.getGameMode() == GameMode.SPECTATOR
                    || target.getGameMode() == GameMode.CREATIVE) {
                return false;
            }
            if (!caster.getWorld().equals(target.getWorld()) || !caster.getWorld().getPVP()) {
                return false; // respect world PvP rules
            }
        }
        return caster.getWorld().equals(living.getWorld());
    }

    /** Every valid living target in a radius around a center point. */
    public static List<LivingEntity> areaTargets(Player caster, Location center, double radius) {
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : caster.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (validTarget(caster, entity)) {
                targets.add((LivingEntity) entity);
            }
        }
        return targets;
    }

    /** Every valid target along a forward ray (direction must be normalized). */
    public static List<LivingEntity> rayTargets(Player caster, Location start, Vector direction,
                                                double length, double hitRadius) {
        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= length; distance += 1.0) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            for (Entity entity : caster.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                if (validTarget(caster, entity) && !victims.contains(entity)) {
                    victims.add((LivingEntity) entity);
                }
            }
        }
        return victims;
    }

    /** Every valid target inside a forward-facing cone from the caster's eyes. */
    public static List<LivingEntity> coneTargets(Player caster, double range, double halfAngleDegrees) {
        Location eye = caster.getEyeLocation();
        Vector forward = eye.getDirection().normalize();
        double cosLimit = Math.cos(Math.toRadians(halfAngleDegrees));
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : caster.getNearbyEntities(range, range, range)) {
            if (!validTarget(caster, entity)) {
                continue;
            }
            Vector toEntity = entity.getLocation().add(0, 1, 0).toVector()
                    .subtract(eye.toVector());
            if (toEntity.lengthSquared() > range * range) {
                continue;
            }
            if (toEntity.lengthSquared() < 0.01
                    || toEntity.normalize().dot(forward) >= cosLimit) {
                targets.add((LivingEntity) entity);
            }
        }
        return targets;
    }

    /** The closest valid target within range, or null. */
    public static LivingEntity nearestTarget(Player caster, double range) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity entity : caster.getNearbyEntities(range, range, range)) {
            if (!validTarget(caster, entity)) {
                continue;
            }
            double distance = entity.getLocation().distanceSquared(caster.getLocation());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) entity;
            }
        }
        return best;
    }
}
