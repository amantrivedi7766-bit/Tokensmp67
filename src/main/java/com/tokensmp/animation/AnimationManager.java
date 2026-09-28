package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleManager;
import com.tokensmp.core.SchedulerManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Ongoing ability cinematics: looping particle profiles that play while a
 * timed ability is active. Every loop self-cancels when its duration ends
 * or the player disconnects, and registers with the SchedulerManager for
 * shutdown cleanup.
 */
public final class AnimationManager {

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;

    public AnimationManager(TokenSMP plugin, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    /** Undead Enrage trail: green sparkle spiral for the buff duration. */
    public void enrageTrail(Player player, int durationSeconds) {
        loop(player, durationSeconds, 4, () -> {
            Location center = player.getLocation().add(0, 1, 0);
            ParticleManager.spawn(player.getWorld(), Particle.HAPPY_VILLAGER, center, 5, 0.3, 0.5, 0.3, 0.1);
            ParticleManager.spawn(player.getWorld(), Particle.ENCHANTED_HIT, center, 5, 0.3, 0.5, 0.3, 0.1);
        });
    }

    /** Blazing Wrath aura: rising flames around the user for the burn window. */
    public void flameAura(Player player, int durationSeconds) {
        loop(player, durationSeconds, 5, () ->
                ParticleManager.column(player.getWorld(), Particle.FLAME, player.getLocation(), 2.0, 6));
    }

    /** Charged Overload charge-up: crackling sparks right before the blast. */
    public void chargeSparks(Player player, int chargeTicks) {
        loop(player, Math.max(1, chargeTicks / 5), 5, () ->
                ParticleManager.burst(player.getWorld(), Particle.ELECTRIC_SPARK,
                        player.getLocation().add(0, 1, 0), 6, 0.4));
    }

    /** Warp Strike implosion/explosion flourish at both ends of the jump. */
    public void warpBurst(Location location) {
        ParticleManager.burst(location.getWorld(), Particle.PORTAL, location, 40, 0.6);
        ParticleManager.burst(location.getWorld(), Particle.HEART, location, 20, 0.5);
    }

    /**
     * Sonic ray visual: a dense particle line along the travel path. The
     * direction must already be normalized.
     */
    public void sonicRay(Player player, Location start, Vector direction, double length) {
        Location point = start.clone();
        for (double travelled = 0; travelled < length; travelled += 1.0) {
            point = point.clone().add(direction);
            ParticleManager.point(player.getWorld(), Particle.SONIC_BOOM, point);
        }
    }

    /** Success heartbeat used when a freeze lands. */
    public static void freezeSound(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.6f);
    }

    // ------------------------------------------------------------------

    private void loop(Player player, int durationSeconds, int periodTicks, Runnable effect) {
        long totalTicks = durationSeconds * 20L;
        final long[] ticks = {0};
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || ticks[0] >= totalTicks) {
                    cancel();
                    return;
                }
                ticks[0] += periodTicks;
                effect.run();
            }
        };
        runnable.runTaskTimer(plugin, 0L, periodTicks);
        scheduler.register(runnable);
    }
}
