package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import com.tokensmp.token.TokenTier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Death handling: the configurable PvP token-stealing system plus the wither
 * kill-streak reset for the admin token grind.
 */
public final class PlayerDeathListener implements Listener {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;

    public PlayerDeathListener(TokenSMP plugin, TokenRegistry registry, TokenDataManager data) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Reset wither kill streaks (hard grinds restart on death).
        for (Token token : registry.all()) {
            int tier = data.getTier(victim, token.getId());
            TokenTier next = token.tier(tier + 1);
            if (next != null && next.getTask().type() == TokenTier.TaskType.WITHER_STREAK) {
                data.setProgress(victim, token.getId(), 0);
            }
        }

        // ------------------------------------------------------------------
        // Token stealing: killer receives a claim opportunity.
        // ------------------------------------------------------------------
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        if (!plugin.config().getBoolean("token-stealing.enabled", true)) {
            return;
        }

        String activeId = data.getActiveToken(victim);
        if (activeId == null || !data.isClaimed(victim, activeId)) {
            return; // victim has no eligible active token
        }
        Token token = registry.get(activeId);
        if (token == null) {
            return;
        }
        // Admin tokens can never be stolen unless explicitly enabled.
        if (token.isAdminToken()
                && !plugin.config().getBoolean("token-stealing.allow-admin-token", false)) {
            return;
        }
        // Optional requirement: killer must have no active token themselves.
        if (plugin.config().getBoolean("token-stealing.require-killer-without-token", false)
                && data.getActiveToken(killer) != null) {
            return;
        }
        if (!plugin.config().getBoolean("token-stealing.steal-one-token-per-kill", true)
                && data.getPendingSteal(killer) != null) {
            return;
        }

        long timeout = plugin.config().getLong("token-stealing.claim-timeout-seconds", 60) * 1000L;
        data.setPendingSteal(killer, token.getId(), victim.getName(), timeout);

        // Cinematic claim notification.
        plugin.messages().send(killer, "messages.steal-opportunity",
                "&6⚔ You defeated &f{victim}&6!",
                "{victim}", victim.getName());
        plugin.messages().send(killer, "messages.steal-available",
                "&eTheir {color}&l{token} Token &ehas become claimable!",
                "{color}", token.getRarity().getColorCode(),
                "{token}", token.getDisplayName());
        plugin.messages().actionBar(killer, plugin.config().getString("messages.steal-actionbar",
                        "&6⚔ {token} claimable for {seconds}s - claim it via /tokens!")
                .replace("{token}", token.getDisplayName())
                .replace("{seconds}", String.valueOf(
                        plugin.config().getLong("token-stealing.claim-timeout-seconds", 60))));
    }
}
