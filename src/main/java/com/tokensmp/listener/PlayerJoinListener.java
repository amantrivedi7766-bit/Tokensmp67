package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * First-join detection: exactly once per player lifetime (persistent PDC
 * flag, so re-logging never re-triggers it) the cinematic token spin launches
 * one second after join - off the join tick so the client is ready.
 */
public final class PlayerJoinListener implements Listener {

    private final TokenSMP plugin;

    public PlayerJoinListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Always refresh passives on join (survives restarts cleanly).
        plugin.passiveManager().applyPassives(player);
        if (!plugin.config().getBoolean("first-join.enabled", true)) {
            return;
        }
        if (!plugin.data().consumeFirstJoin(player)) {
            return;
        }
        int delay = Math.max(0, plugin.config().getInt("first-join.launch-delay-ticks", 20));
        plugin.scheduler().runLater(delay, () -> {
            if (player.isOnline()) {
                plugin.spinLauncher().launchFor(player);
            }
        });
    }
}
