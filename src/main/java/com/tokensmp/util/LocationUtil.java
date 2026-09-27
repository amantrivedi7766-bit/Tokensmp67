package com.tokensmp.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/** Location math helpers: wall detection, circles, forward offsets. */
public final class LocationUtil {

    private LocationUtil() {
    }

    /** True when a solid block borders the player horizontally (wall climbing). */
    public static boolean isAgainstWall(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return world.getBlockAt(x + 1, y, z).getType().isSolid()
                || world.getBlockAt(x - 1, y, z).getType().isSolid()
                || world.getBlockAt(x, y, z + 1).getType().isSolid()
                || world.getBlockAt(x, y, z - 1).getType().isSolid();
    }

    /** A point on a horizontal circle of the given radius around the center. */
    public static Location onCircle(Location center, double radius, double angleRadians, double yOffset) {
        return center.clone().add(Math.cos(angleRadians) * radius, yOffset, Math.sin(angleRadians) * radius);
    }

    /** The point n blocks in the direction the location faces. */
    public static Location forward(Location origin, double distance) {
        Vector direction = origin.getDirection();
        if (direction.lengthSquared() == 0) {
            return origin.clone();
        }
        return origin.clone().add(direction.normalize().multiply(distance));
    }

    /** Horizontal distance between two locations (ignores Y). */
    public static double horizontalDistance(Location a, Location b) {
        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            return Double.MAX_VALUE;
        }
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
