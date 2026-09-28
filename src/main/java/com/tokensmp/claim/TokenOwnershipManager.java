package com.tokensmp.claim;

import com.tokensmp.TokenSMP;
import com.tokensmp.data.PlayerTokenData;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Read-side ownership facade over the server-authoritative player data:
 * who owns what, at which tier, and - the CORE RULE of the system - whether
 * a player already holds their ONE allowed active token. Every claim path
 * (GUI, command, death claim, admin action) checks through here.
 */
public final class TokenOwnershipManager {

    private final TokenSMP plugin;
    private final TokenDataManager data;

    public TokenOwnershipManager(TokenSMP plugin, TokenDataManager data) {
        this.plugin = plugin;
        this.data = data;
    }

    /** The player's single ACTIVE token id, or null. */
    public String getActiveToken(Player player) {
        return data.getActiveToken(player);
    }

    /** True when the player already has ONE claimed/active token. */
    public boolean hasActiveToken(Player player) {
        return data.getActiveToken(player) != null;
    }

    /** The active token definition, or null. */
    public Token getActiveTokenDef(Player player) {
        String id = data.getActiveToken(player);
        return id == null ? null : plugin.registry().get(id);
    }

    /** True when the player owns (has unlocked) the token. */
    public boolean owns(Player player, Token token) {
        return data.hasUnlocked(player, token.getId());
    }

    /** Every unlocked token of the player. */
    public List<PlayerTokenData> getUnlocked(Player player) {
        return data.getUnlocked(player, plugin.registry().playerTokens());
    }

    /**
     * Claim gate: the multi-claim exploit blocker. True when the player may
     * claim another token (claim-system.one-active-token-only).
     */
    public boolean canClaimAnotherToken(Player player) {
        if (!plugin.config().getBoolean("claim-system.one-active-token-only", true)) {
            return true;
        }
        return !hasActiveToken(player);
    }
}
