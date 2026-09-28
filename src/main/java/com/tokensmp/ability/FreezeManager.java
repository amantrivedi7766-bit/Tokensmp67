package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleManager;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.SchedulerManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chrono Freeze support: fully freezes players in place (no move, jump, block
 * break, interact or attacks), runs an ice particle + action bar status loop
 * and automatically releases them on expiry. The loop only runs while at
 * least one player is frozen and every task is registered for shutdown
 * cleanup - no leaks.
 */
public final class FreezeManager implements Listener {

    private final TokenSMP plugin;
    private final MessageManager messages;
    private final SchedulerManager scheduler;
    private final Map<UUID, Long> frozenUntil = new ConcurrentHashMap<>();
    private volatile BukkitRunnable loop;

    public FreezeManager(TokenSMP plugin, MessageManager messages, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.messages = messages;
        this.scheduler = scheduler;
    }

    /** Freezes a player for the given duration (seconds). */
    public void freeze(Player player, double seconds) {
        long until = System.currentTimeMillis() + (long) (seconds * 1000L);
        frozenUntil.merge(player.getUniqueId(), until, Math::max);
        messages.actionBar(player, plugin.config().getString("freeze.frozen-message",
                "&b\u2744 You have been frozen for {seconds}s!")
                .replace("{seconds}", String.valueOf((long) seconds)));
        SoundManager.play(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.6f);
        ParticleManager.burst(player.getWorld(), Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0), 30, 0.5);
        ensureLoop();
    }

    public boolean isFrozen(Player player) {
        Long until = frozenUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void ensureLoop() {
        if (loop != null) {
            return;
        }
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                boolean any = false;
                Iterator<Map.Entry<UUID, Long>> it = frozenUntil.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Long> entry = it.next();
                    Player frozen = plugin.getServer().getPlayer(entry.getKey());
                    if (frozen == null || entry.getValue() <= System.currentTimeMillis()) {
                        if (frozen != null) {
                            messages.actionBar(frozen, plugin.config().getString(
                                    "freeze.unfrozen-message", "&a\u2744 You are free again!"));
                        }
                        it.remove();
                        continue;
                    }
                    any = true;
                    long secondsLeft = ((entry.getValue() - System.currentTimeMillis()) + 999L) / 1000L;
                    messages.actionBar(frozen, plugin.config().getString("freeze.status-message",
                                    "&b\u2744 Frozen! &f{seconds}s")
                            .replace("{seconds}", String.valueOf(secondsLeft)));
                    Location center = frozen.getLocation().add(0, 1, 0);
                    ParticleManager.spawn(frozen.getWorld(), Particle.SNOWFLAKE, center, 12, 0.4, 0.9, 0.4, 0.02);
                    ParticleManager.spawn(frozen.getWorld(), Particle.CLOUD, center, 3, 0.3, 0.9, 0.3, 0.01);
                }
                if (!any) {
                    loop = null;
                    cancel();
                }
            }
        };
        task.runTaskTimer(plugin, 4L, 4L);
        scheduler.register(task);
        loop = task;
    }

    // ------------------------------------------------------------------
    // Freeze enforcement
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (!isFrozen(event.getPlayer())) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
            event.setTo(from); // full movement block, head rotation stays free
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        frozenUntil.remove(event.getPlayer().getUniqueId());
    }
}
