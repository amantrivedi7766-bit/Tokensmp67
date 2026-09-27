package com.tokensmp.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Version-safe particle helpers. Every particle name used here is verified
 * against the 1.21.x enum; the rest of the plugin spawns particles ONLY
 * through this class so a future rename needs a single-file fix.
 */
public final class ParticleEngine {

    private ParticleEngine() {
    }

    public static void spawn(World world, Particle particle, Location location,
                             int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (world != null && location != null) {
            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    public static void point(World world, Particle particle, Location location) {
        spawn(world, particle, location, 1, 0, 0, 0, 0);
    }

    public static void burst(World world, Particle particle, Location location, int count, double spread) {
        spawn(world, particle, location, count, spread, spread, spread, 0.05);
    }

    public static void column(World world, Particle particle, Location base, double height, int points) {
        for (double y = 0; y <= height; y += height / Math.max(1, points)) {
            point(world, particle, base.clone().add(0, y, 0));
        }
    }

    /** Ring of particles around a center at the given height offset. */
    public static void ring(World world, Particle particle, Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            Location point = com.tokensmp.util.LocationUtil.onCircle(center, radius, angle, 0.1);
            point(world, particle, point);
        }
    }
}
