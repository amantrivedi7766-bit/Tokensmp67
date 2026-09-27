package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central scheduler registry. Every task created by this plugin is
 * registered here so onDisable() can cancel all of them - guaranteeing that
 * spin animations, vortices, cooldown HUDs and freeze loops never leak.
 * Accepts both raw BukkitTasks and self-scheduling BukkitRunnables.
 */
public final class SchedulerManager {

    private final TokenSMP plugin;
    private final Set<BukkitTask> tasks = ConcurrentHashMap.newKeySet();
    private final Set<BukkitRunnable> runnables = ConcurrentHashMap.newKeySet();

    public SchedulerManager(TokenSMP plugin) {
        this.plugin = plugin;
    }

    /** Runs a task on a later tick. */
    public void runLater(long delayTicks, Runnable runnable) {
        register(plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    /** Runs a repeating task; returns the task so callers can cancel it themselves. */
    public BukkitTask runTimer(long delayTicks, long periodTicks, Runnable runnable) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        register(task);
        return task;
    }

    /** Registers a task created externally for cleanup tracking. */
    public void register(BukkitTask task) {
        tasks.add(task);
    }

    /**
     * Registers a self-scheduling BukkitRunnable (one that called
     * runTaskTimer/runTaskLater on itself) for shutdown cleanup.
     */
    public void register(BukkitRunnable runnable) {
        runnables.add(runnable);
    }

    /** Removes a finished task from the registry. */
    public void unregister(BukkitTask task) {
        tasks.remove(task);
    }

    /** Cancels every tracked task and runnable - called from onDisable(). */
    public void cancelAll() {
        for (BukkitTask task : tasks) {
            try {
                task.cancel();
            } catch (IllegalStateException ignored) {
                // already cancelled / plugin already disabled
            }
        }
        for (BukkitRunnable runnable : runnables) {
            try {
                runnable.cancel();
            } catch (IllegalStateException ignored) {
                // already cancelled / plugin already disabled
            }
        }
        tasks.clear();
        runnables.clear();
    }
}
