package com.tokensmp.core;

import com.tokensmp.TokenSMP;

/**
 * Server-wide grace period. While it is active, PvP damage and token stealing
 * can be blocked (configurable) and admins can see the remaining time.
 */
public final class GraceManager {

    private final TokenSMP plugin;
    private volatile long until = 0L;

    public GraceManager(TokenSMP plugin) {
        this.plugin = plugin;
    }

    /** True while the grace period is running. */
    public boolean isActive() {
        return System.currentTimeMillis() < until;
    }

    /** Remaining seconds (0 when inactive). */
    public long remainingSeconds() {
        return Math.max(0L, (until - System.currentTimeMillis() + 999L) / 1000L);
    }

    /** Starts (or extends) the grace period. */
    public void start(long seconds) {
        long safe = Math.max(1L, seconds);
        until = System.currentTimeMillis() + safe * 1000L;
        plugin.messages().broadcastLine(plugin.messages().msg("messages.grace-period-start",
                "&a&lGRACE PERIOD &7started for &f{minutes} minute(s)&7 - PvP and token stealing are disabled!",
                "{minutes}", String.valueOf(Math.max(1L, safe / 60L))));
    }

    /** Ends the grace period immediately. */
    public void stop() {
        boolean wasActive = isActive();
        until = 0L;
        if (wasActive) {
            plugin.messages().broadcastLine(plugin.messages().msg("messages.grace-period-end",
                    "&c&lGRACE PERIOD &7has ended - PvP and token stealing are back on!"));
        }
    }
}
