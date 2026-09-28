package com.tokensmp.claim;

import com.tokensmp.TokenSMP;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import org.bukkit.entity.Player;

/**
 * PvP token stealing: when a player kills the holder of an active token,
 * this creates the server-side claim opportunity (with timeout). The killer
 * receives a claim interaction - the token is NEVER granted automatically
 * unless configured - and claiming it goes through TokenClaimManager so the
 * one-active-token rule still applies.
 */
public final class TokenTransferManager {

    private final TokenSMP plugin;
    private final TokenDataManager data;

    public TokenTransferManager(TokenSMP plugin, TokenDataManager data) {
        this.plugin = plugin;
        this.data = data;
    }

    /**
     * Handles a PvP kill: if the victim had an active token and stealing is
     * enabled, the killer receives a timed claim opportunity.
     */
    public void onPvpKill(Player killer, Player victim) {
        if (!plugin.config().getBoolean("token-stealing.enabled", true)) {
            return;
        }
        String activeId = data.getActiveToken(victim);
        if (activeId == null || !data.isClaimed(victim, activeId)) {
            return; // victim has no eligible active token
        }
        Token token = plugin.registry().get(activeId);
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
                "&6You defeated &f{victim}&6!",
                "{victim}", victim.getName());
        plugin.messages().send(killer, "messages.steal-available",
                "&fTheir {token} Token is now claimable.",
                "{token}", token.getDisplayName());
        plugin.messages().actionBar(killer, plugin.config().getString("messages.steal-actionbar",
                        "&6\u2694 {token} claimable for {seconds}s - claim it via /tokens!")
                .replace("{token}", token.getDisplayName())
                .replace("{seconds}", String.valueOf(
                        plugin.config().getLong("token-stealing.claim-timeout-seconds", 60))));
    }

    /** Resets hard-streak grinds (wither streaks) on death. */
    public void resetStreaks(Player victim) {
        for (Token token : plugin.registry().all()) {
            int tier = data.getTier(victim, token.getId());
            TokenTier next = token.tier(tier + 1);
            if (next != null && next.getTask().type() == TokenTier.TaskType.WITHER_STREAK) {
                data.setProgress(victim, token.getId(), 0);
            }
        }
    }
}
