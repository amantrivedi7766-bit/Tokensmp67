package com.tokensmp.data;

import com.tokensmp.TokenSMP;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Server-authoritative player data: token ownership (tier + claim state),
 * task progress, active token, steal opportunities and the first-join flag.
 *
 * All state lives in the player's PersistentDataContainer, so it survives
 * reconnects, restarts and /tokensadmin reload. Item lore is never trusted.
 */
public final class TokenDataManager {

    private final TokenSMP plugin;

    public TokenDataManager(TokenSMP plugin) {
        this.plugin = plugin;
    }

    private PersistentDataContainer pdc(Player player) {
        return player.getPersistentDataContainer();
    }

    // ------------------------------------------------------------------
    // Tier / unlock state
    // ------------------------------------------------------------------

    /** 0 = LOCKED, 1-3 = unlocked tier. */
    public int getTier(Player player, String tokenId) {
        return pdc(player).getOrDefault(tierKey(tokenId), PersistentDataType.INTEGER, 0);
    }

    public void setTier(Player player, String tokenId, int tier) {
        pdc(player).set(tierKey(tokenId), PersistentDataType.INTEGER, tier);
    }

    public boolean hasUnlocked(Player player, String tokenId) {
        return getTier(player, tokenId) >= 1;
    }

    // ------------------------------------------------------------------
    // Claim state (LOCKED -> SPIN -> UNLOCKED -> CLAIMED/ACTIVE -> UNCLAIMED)
    // ------------------------------------------------------------------

    public boolean isClaimed(Player player, String tokenId) {
        return pdc(player).getOrDefault(claimKey(tokenId), PersistentDataType.BYTE, (byte) 0) == (byte) 1;
    }

    public void setClaimed(Player player, String tokenId, boolean claimed) {
        pdc(player).set(claimKey(tokenId), PersistentDataType.BYTE, claimed ? (byte) 1 : (byte) 0);
    }

    /** Marks the token claimed and makes it the player's single ACTIVE token. */
    public void claim(Player player, String tokenId) {
        setClaimed(player, tokenId, true);
        setActiveToken(player, tokenId);
    }

    /** Deactivates the token (state returns to UNCLAIMED). */
    public void unclaim(Player player, String tokenId) {
        setClaimed(player, tokenId, false);
        if (tokenId.equals(getActiveToken(player))) {
            setActiveToken(player, null);
        }
    }

    // ------------------------------------------------------------------
    // Active token
    // ------------------------------------------------------------------

    /** The active token id, or null when no token is active. */
    public String getActiveToken(Player player) {
        return pdc(player).get(activeKey(), PersistentDataType.STRING);
    }

    public void setActiveToken(Player player, String tokenId) {
        if (tokenId == null) {
            pdc(player).remove(activeKey());
        } else {
            pdc(player).set(activeKey(), PersistentDataType.STRING, tokenId);
        }
    }

    // ------------------------------------------------------------------
    // Task progress
    // ------------------------------------------------------------------

    public int getProgress(Player player, String tokenId) {
        return pdc(player).getOrDefault(progressKey(tokenId), PersistentDataType.INTEGER, 0);
    }

    public void setProgress(Player player, String tokenId, int value) {
        pdc(player).set(progressKey(tokenId), PersistentDataType.INTEGER, Math.max(0, value));
    }

    /** Increments progress and returns the new value. */
    public int addProgress(Player player, String tokenId, int amount) {
        int updated = getProgress(player, tokenId) + amount;
        setProgress(player, tokenId, updated);
        return updated;
    }

    // ------------------------------------------------------------------
    // Snapshots
    // ------------------------------------------------------------------

    public PlayerTokenData getData(Player player, String tokenId) {
        return new PlayerTokenData(tokenId, getTier(player, tokenId),
                isClaimed(player, tokenId), getProgress(player, tokenId));
    }

    /** All tokens the player has unlocked (any tier). */
    public List<PlayerTokenData> getUnlocked(Player player, Iterable<com.tokensmp.token.Token> registry) {
        List<PlayerTokenData> out = new ArrayList<>();
        for (com.tokensmp.token.Token token : registry) {
            if (hasUnlocked(player, token.getId())) {
                out.add(getData(player, token.getId()));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Token stealing (death claim opportunities)
    // ------------------------------------------------------------------

    /** The token id the player may currently claim from a defeated victim, or null. */
    public String getPendingSteal(Player player) {
        String token = pdc(player).get(stealTokenKey(), PersistentDataType.STRING);
        if (token == null) {
            return null;
        }
        Long expiry = pdc(player).get(stealExpiryKey(), PersistentDataType.LONG);
        if (expiry == null || expiry <= System.currentTimeMillis()) {
            clearPendingSteal(player);
            return null;
        }
        return token;
    }

    /** Grants a claim opportunity (server-authoritative, one at a time). */
    public void setPendingSteal(Player player, String tokenId, String victimName, long timeoutMillis) {
        pdc(player).set(stealTokenKey(), PersistentDataType.STRING, tokenId);
        pdc(player).set(stealVictimKey(), PersistentDataType.STRING, victimName);
        pdc(player).set(stealExpiryKey(), PersistentDataType.LONG,
                System.currentTimeMillis() + timeoutMillis);
    }

    /** The victim whose token the player may claim, or null. */
    public String getStealVictim(Player player) {
        return getPendingSteal(player) == null ? null
                : pdc(player).get(stealVictimKey(), PersistentDataType.STRING);
    }

    public void clearPendingSteal(Player player) {
        pdc(player).remove(stealTokenKey());
        pdc(player).remove(stealExpiryKey());
        pdc(player).remove(stealVictimKey());
    }

    // ------------------------------------------------------------------
    // First-join flag
    // ------------------------------------------------------------------

    /** True exactly once in the player's lifetime (spin trigger). */
    public boolean consumeFirstJoin(Player player) {
        if (pdc(player).has(firstJoinKey(), PersistentDataType.BYTE)) {
            return false;
        }
        pdc(player).set(firstJoinKey(), PersistentDataType.BYTE, (byte) 1);
        return true;
    }

    // ------------------------------------------------------------------
    // Keys
    // ------------------------------------------------------------------

    private org.bukkit.NamespacedKey tierKey(String tokenId) {
        return new org.bukkit.NamespacedKey(plugin, "tier_" + tokenId);
    }

    private org.bukkit.NamespacedKey claimKey(String tokenId) {
        return new org.bukkit.NamespacedKey(plugin, "claimed_" + tokenId);
    }

    private org.bukkit.NamespacedKey progressKey(String tokenId) {
        return new org.bukkit.NamespacedKey(plugin, "progress_" + tokenId);
    }

    private org.bukkit.NamespacedKey activeKey() {
        return new org.bukkit.NamespacedKey(plugin, "active_token");
    }

    private org.bukkit.NamespacedKey stealTokenKey() {
        return new org.bukkit.NamespacedKey(plugin, "steal_token");
    }

    private org.bukkit.NamespacedKey stealExpiryKey() {
        return new org.bukkit.NamespacedKey(plugin, "steal_expiry");
    }

    private org.bukkit.NamespacedKey stealVictimKey() {
        return new org.bukkit.NamespacedKey(plugin, "steal_victim");
    }

    private org.bukkit.NamespacedKey firstJoinKey() {
        return new org.bukkit.NamespacedKey(plugin, "first_join_done");
    }

    /** Utility: a fresh random token instance id for claimed token items. */
    public static String newInstanceId() {
        return UUID.randomUUID().toString();
    }
}
