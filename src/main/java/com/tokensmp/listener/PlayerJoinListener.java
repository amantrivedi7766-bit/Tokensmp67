package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * First-join system: a completely new player does NOT spin immediately -
 * the 60-second preparation sequence starts (server-side, persistent
 * pending/done flags). Reconnecting players resume/restart according to
 * configuration, and the spin can never trigger twice for one player.
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
        // Never trigger twice: once the spin completed, this player is done.
        if (plugin.data().isFirstJoinDone(player)) {
            return;
        }
        // A pending sequence (reconnect before the spin fired) restarts
        // according to the resume-after-reconnect configuration.
        if (plugin.data().isSpinPending(player)
                && !plugin.config().getBoolean("first-join.resume-after-reconnect", true)) {
            return;
        }
        // Small delay off the join tick so the client is fully ready.
        plugin.scheduler().runLater(20L, () -> {
            if (player.isOnline() && !plugin.data().isFirstJoinDone(player)) {
                plugin.spinLauncher().beginFirstJoinSequence(player);
            }
        });
    }
}
