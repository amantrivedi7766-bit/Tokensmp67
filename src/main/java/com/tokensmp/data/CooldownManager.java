package com.tokensmp.data;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side timestamp cooldowns stored in the player PDC (they survive
 * reconnects and GUI changes) plus the live action-bar cooldown HUD that
 * updates every 2 ticks. HUD tasks self-cancel when the cooldown expires
 * or the player disconnects, so nothing leaks.
 */
public final class CooldownManager {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final SchedulerManager scheduler;
    private final MessageManager messages;
    private final Map<UUID, Long> runningHud = new HashMap<>();

    public CooldownManager(TokenSMP plugin, TokenRegistry registry,
                           SchedulerManager scheduler, MessageManager messages) {
        this.plugin = plugin;
        this.registry = registry;
        this.scheduler = scheduler;
        this.messages = messages;
    }

    // ------------------------------------------------------------------
    // Timestamps
    // ------------------------------------------------------------------

    /** Starts a cooldown of the given length (seconds). */
    public void start(Player player, String tokenId, int seconds) {
        pdc(player).set(key(tokenId), PersistentDataType.LONG, System.currentTimeMillis() + seconds * 1000L);
    }

    /** Remaining milliseconds (0 when ready). */
    public long remainingMillis(Player player, String tokenId) {
        Long until = pdc(player).get(key(tokenId), PersistentDataType.LONG);
        if (until == null) {
            return 0L;
        }
        return Math.max(0L, until - System.currentTimeMillis());
    }

    public boolean isReady(Player player, String tokenId) {
        return remainingMillis(player, tokenId) <= 0L;
    }

    public void clear(Player player, String tokenId) {
        pdc(player).remove(key(tokenId));
    }

    /** Clears cooldowns for every registered token. */
    public void clearAll(Player player) {
        for (Token token : registry.all()) {
            clear(player, token.getId());
        }
    }

    // ------------------------------------------------------------------
    // Live action bar HUD (every 2 ticks, self-cancelling)
    // ------------------------------------------------------------------

    /** Shows the cooldown bar every 2 ticks until the ability is ready. */
    public void showHud(Player player, String tokenId, int totalSeconds) {
        UUID id = player.getUniqueId();
        long totalMs = Math.max(1, totalSeconds) * 1000L;
        if (runningHud.containsKey(id)) {
            return; // an HUD for a newer cooldown already runs
        }
        long startTime = System.currentTimeMillis();
        runningHud.put(id, startTime);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || runningHud.get(id) != startTime) {
                    runningHud.remove(id);
                    cancel();
                    return;
                }
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = totalMs - elapsed;
                if (remaining <= 0L) {
                    runningHud.remove(id);
                    cancel();
                    messages.actionBar(player, plugin.config().getString(
                            "messages.ability-ready", "&a&l[!] &aAbility ready!"));
                    if (plugin.config().getBoolean("cooldowns.ready-sound", true)) {
                        org.bukkit.Sound ready = org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                        player.playSound(player.getLocation(), ready, 0.8f, 1.6f);
                    }
                    return;
                }
                int length = plugin.config().getInt("cooldowns.bar.length", 16);
                String filled = plugin.config().getString("cooldowns.bar.filled-char", "█");
                String empty = plugin.config().getString("cooldowns.bar.empty-char", "░");
                String color = plugin.config().getString("cooldowns.bar.filled-color", "&c");
                int filledCount = (int) Math.ceil((remaining / (double) totalMs) * length);
                String bar = color + filled.repeat(Math.max(0, Math.min(length, filledCount)))
                        + empty.repeat(Math.max(0, length - filledCount));
                String text = plugin.config().getString("cooldowns.bar.format",
                                "&cAbility Cooldown: [&c{bar}&r] &f{time}s")
                        .replace("{bar}", bar)
                        .replace("{time}", String.format("%.1f", remaining / 1000.0));
                messages.actionBar(player, text);
            }
        };
        long hudInterval = Math.max(1L, plugin.config().getLong("performance.cooldown-hud-interval-ticks", 2L));
        runnable.runTaskTimer(plugin, hudInterval, hudInterval);
        scheduler.register(runnable);
    }

    // ------------------------------------------------------------------

    private PersistentDataContainer pdc(Player player) {
        return player.getPersistentDataContainer();
    }

    private org.bukkit.NamespacedKey key(String tokenId) {
        return new org.bukkit.NamespacedKey(plugin, "cooldown_" + tokenId);
    }
}
