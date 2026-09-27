package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleEngine;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.SchedulerManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Arrow trail engine (Skeleton max tier passive): every arrow launched by a
 * player whose ACTIVE token has the arrow-trail passive emits a CRIT +
 * SNOWFLAKE trail that follows the actual projectile, not a stationary
 * effect. Each trail self-cancels when the arrow lands/despawns and a
 * per-player cap prevents task floods.
 */
public final class ProjectileEngine {

    private static final int MAX_CONCURRENT_TRAILS = 24;

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;
    private final Map<UUID, Integer> activeTrails = new HashMap<>();

    public ProjectileEngine(TokenSMP plugin, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    /** Attaches a particle trail to an arrow if the shooter has the passive. */
    public void attachTrail(Arrow arrow, Player shooter, boolean enabled) {
        if (!enabled || shooter == null) {
            return;
        }
        UUID id = shooter.getUniqueId();
        int current = activeTrails.getOrDefault(id, 0);
        if (current >= MAX_CONCURRENT_TRAILS) {
            return;
        }
        activeTrails.put(id, current + 1);

        BukkitRunnable trail = new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isDead() || arrow.isOnGround()) {
                    activeTrails.merge(id, -1, Integer::sum);
                    if (activeTrails.getOrDefault(id, 0) <= 0) {
                        activeTrails.remove(id);
                    }
                    cancel();
                    return;
                }
                ParticleEngine.spawn(shooter.getWorld(), Particle.CRIT, arrow.getLocation(),
                        4, 0.05, 0.05, 0.05, 0.01);
                ParticleEngine.spawn(shooter.getWorld(), Particle.SNOWFLAKE, arrow.getLocation(),
                        2, 0.05, 0.05, 0.05, 0.0);
            }
        };
        trail.runTaskTimer(plugin, 1L, 1L);
        scheduler.register(trail);
    }

    /** Cleans the counter when a trailing player disconnects. */
    public void clear(Player player) {
        activeTrails.remove(player.getUniqueId());
    }

    /** Launch confirmation click used by abilities that spawn projectiles. */
    public static void shootSound(Player player) {
        SoundEngine.play(player, Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.5f);
    }
}
