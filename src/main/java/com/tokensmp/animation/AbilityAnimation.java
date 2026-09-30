package com.tokensmp.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Reusable ability activation cinematics: rings, spirals and bursts that any
 * ability can layer over its mechanical effect. Purely visual - the actual
 * damage/heal is applied by the ability engines.
 */
public final class AbilityAnimation {

    private AbilityAnimation() {
    }

    /** Expanding double ring around the player (activation flourish). */
    public static void activationRings(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        ParticleEngine.ring(player.getWorld(), Particle.HAPPY_VILLAGER, center, 1.0, 16);
        ParticleEngine.ring(player.getWorld(), Particle.END_ROD, center, 2.0, 24);
    }

    /** Rising spiral around the player for buff-type abilities. */
    public static void spiral(Player player, Particle particle, double radius, double height, int steps) {
        Location base = player.getLocation();
        for (int i = 0; i <= steps; i++) {
            double angle = (Math.PI * 4 * i) / steps;
            double y = (height * i) / steps;
            Location point = com.tokensmp.util.LocationUtil.onCircle(base, radius, angle, y);
            ParticleEngine.point(player.getWorld(), particle, point);
        }
    }

    /** Simple directional burst between two points (traveling abilities). */
    public static void burstBetween(Location from, Location to, Particle particle, int steps) {
        org.bukkit.World world = from.getWorld();
        if (world == null) {
            return;
        }
        org.bukkit.util.Vector delta = to.toVector().subtract(from.toVector());
        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(delta.clone().multiply((double) i / steps));
            ParticleEngine.point(world, particle, point);
        }
    }

    /** Shockwave visual: several expanding rings. */
    public static void shockwave(Player player, double radius) {
        for (int i = 1; i <= 3; i++) {
            final double r = radius * i / 3.0;
            ParticleEngine.ring(player.getWorld(), Particle.EXPLOSION, player.getLocation(), r, 24);
        }
    }
}
