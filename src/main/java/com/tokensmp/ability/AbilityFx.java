package com.tokensmp.ability;

import com.tokensmp.animation.ParticleEngine;
import com.tokensmp.animation.SoundEngine;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Shared cinematic helpers for the ability engine: cast bursts, impact
 * bursts, rings, lines, spirals, colored energy profiles and sound
 * sequences. Every effect is driven by the ability's configured particle /
 * sound lists, so each of the 45 abilities keeps its own visual identity.
 */
public final class AbilityFx {

    private AbilityFx() {
    }

    // ------------------------------------------------------------------
    // Cast / impact
    // ------------------------------------------------------------------

    /** Cast burst around the caster. */
    public static void cast(Player player, List<Particle> particles, int count) {
        Location center = player.getLocation().add(0, 1, 0);
        for (Particle particle : particles) {
            ParticleEngine.burst(player.getWorld(), particle, center, Math.max(4, count / 2), 0.6);
        }
    }

    /** Impact burst at a location. */
    public static void impact(Location location, List<Particle> particles, int count, double spread) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        for (Particle particle : particles) {
            ParticleEngine.burst(world, particle, location, Math.max(3, count), spread);
        }
    }

    // ------------------------------------------------------------------
    // Shapes
    // ------------------------------------------------------------------

    /** Horizontal ring of the configured particles. */
    public static void ring(Location center, double radius, List<Particle> particles, int points) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        for (Particle particle : particles) {
            ParticleEngine.ring(world, particle, center, radius, Math.max(8, points));
        }
    }

    /** A dense particle line from a start point along a direction. */
    public static void line(Location from, Vector direction, double length, List<Particle> particles) {
        World world = from.getWorld();
        if (world == null || direction.lengthSquared() == 0) {
            return;
        }
        Vector step = direction.clone().normalize().multiply(0.8);
        Location point = from.clone();
        for (double travelled = 0; travelled < length; travelled += 0.8) {
            point.add(step);
            for (Particle particle : particles) {
                ParticleEngine.point(world, particle, point);
            }
        }
    }

    /** A rising spiral around a center. */
    public static void spiral(Location center, double radius, double height,
                              List<Particle> particles, int steps) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        for (int i = 0; i <= steps; i++) {
            double angle = (Math.PI * 4 * i) / Math.max(1, steps);
            double y = (height * i) / Math.max(1, steps);
            Location point = com.tokensmp.util.LocationUtil.onCircle(center, radius, angle, y);
            for (Particle particle : particles) {
                ParticleEngine.point(world, particle, point);
            }
        }
    }

    /** A vertical particle column. */
    public static void column(Location base, double height, List<Particle> particles) {
        World world = base.getWorld();
        if (world == null) {
            return;
        }
        for (Particle particle : particles) {
            ParticleEngine.column(world, particle, base, height, 12);
        }
    }

    // ------------------------------------------------------------------
    // Energy profiles (colored dust)
    // ------------------------------------------------------------------

    public static final Color GOLD = Color.fromRGB(255, 215, 0);
    public static final Color EMERALD = Color.fromRGB(23, 214, 92);
    public static final Color AQUA = Color.fromRGB(60, 190, 255);
    public static final Color PURPLE = Color.fromRGB(150, 60, 220);
    public static final Color BLACK = Color.fromRGB(20, 15, 25);
    public static final Color WHITE = Color.fromRGB(240, 240, 240);

    public static void dust(Location location, Color color, int count, double spread) {
        if (location.getWorld() != null) {
            ParticleEngine.dust(location.getWorld(), location, color, 1.0f, count, spread);
        }
    }

    public static void crack(Location location, Material material, int count, double spread) {
        if (location.getWorld() != null) {
            ParticleEngine.blockCrack(location.getWorld(), location, material, count, spread);
        }
    }

    // ------------------------------------------------------------------
    // Sounds
    // ------------------------------------------------------------------

    /** Plays the ability's sound sequence at the caster. */
    public static void sounds(Player player, List<Sound> sounds) {
        for (Sound sound : sounds) {
            SoundEngine.play(player, sound, 1.0f, 1.0f);
        }
    }

    /** Plays the ability's sound sequence at a world location. */
    public static void soundsAt(Location location, List<Sound> sounds) {
        for (Sound sound : sounds) {
            SoundEngine.world(location, sound, 1.2f, 1.0f);
        }
    }
}
