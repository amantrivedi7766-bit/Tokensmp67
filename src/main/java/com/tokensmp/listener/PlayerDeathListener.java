package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Death handling: resets hard grinds (wither streaks) and hands the kill to
 * the transfer manager, which creates the PvP token-stealing claim
 * opportunity when the victim held an active token.
 */
public final class PlayerDeathListener implements Listener {

    private final TokenSMP plugin;

    public PlayerDeathListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Hard grinds restart on death.
        plugin.transferManager().resetStreaks(victim);

        // PvP token stealing: killer receives a claim opportunity.
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        plugin.transferManager().onPvpKill(killer, victim);
    }
}
