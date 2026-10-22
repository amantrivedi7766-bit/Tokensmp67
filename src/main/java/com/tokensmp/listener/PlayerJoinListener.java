package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.token.Token;
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
        plugin.lives().applyRevivedHealth(player);
        plugin.lives().applySacrifice(player);
        // An active token always keeps its item in the inventory (hotbar first),
        // so the player can hold it and use the ability.
        String active = plugin.data().getActiveToken(player);
        if (active != null) {
            Token token = plugin.registry().get(active);
            if (token != null) {
                plugin.tokenItems().ensureItem(player, token,
                        Math.max(1, plugin.data().getTier(player, active)));
            }
        }
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
