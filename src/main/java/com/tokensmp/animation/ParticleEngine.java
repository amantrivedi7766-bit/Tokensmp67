package com.tokensmp.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Version-safe particle helpers with a global master toggle and a per-effect
 * particle budget. Every particle name used here is verified against the
 * 1.21.x enum; the rest of the plugin spawns particles ONLY through this
 * class so a future rename needs a single-file fix.
 *
 * The engine is configured once at startup (and on every atomic reload)
 * from the particles.* and performance.* config sections.
 */
public final class ParticleEngine {

    private static volatile boolean enabled = true;
    private static volatile int maxPerEffect = 128;

    private ParticleEngine() {
    }

    /** Applies the particles.enabled / performance.max-particles-per-effect config. */
    public static void configure(boolean enabledConfig, int maxPerEffectConfig) {
        enabled = enabledConfig;
        maxPerEffect = Math.max(1, maxPerEffectConfig);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void spawn(World world, Particle particle, Location location,
                             int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!enabled || world == null || location == null) {
            return;
        }
        world.spawnParticle(particle, location, Math.min(Math.max(1, count), maxPerEffect),
                offsetX, offsetY, offsetZ, speed);
    }

    /** Spawns a particle that carries extra data (DUST, BLOCK, ITEM ...). */
    public static void spawn(World world, Particle particle, Location location, int count,
                             double offsetX, double offsetY, double offsetZ, double speed, Object data) {
        if (!enabled || world == null || location == null) {
            return;
        }
        world.spawnParticle(particle, location, Math.min(Math.max(1, count), maxPerEffect),
                offsetX, offsetY, offsetZ, speed, data);
    }

    /** Colored dust burst (gold, emerald and aqua energy profiles). */
    public static void dust(World world, Location location, org.bukkit.Color color, float size,
                            int count, double spread) {
        spawn(world, Particle.DUST, location, count, spread, spread, spread, 0.0,
                new Particle.DustOptions(color, size));
    }

    /** Block-crack burst (ground/fissure abilities). */
    public static void blockCrack(World world, Location location, org.bukkit.Material material,
                                  int count, double spread) {
        spawn(world, Particle.BLOCK, location, count, spread, spread, spread, 0.0,
                material.createBlockData());
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
