package com.amantrivedi.tokensmp.data;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * All per-player token state (unlocked tiers, task progress, active token,
 * ability cooldowns) is stored on the player's PersistentDataContainer, which
 * Bukkit persists automatically inside the player's data file.
 */
public final class TokenDataManager {

    private final NamespacedKey activeKey;
    private final String namespace;

    public TokenDataManager(String namespace, NamespacedKey activeKey) {
        this.namespace = namespace;
        this.activeKey = activeKey;
    }

    private NamespacedKey tierKey(String tokenId) {
        return new NamespacedKey(namespace, "tier_" + tokenId);
    }

    private NamespacedKey progressKey(String tokenId) {
        return new NamespacedKey(namespace, "progress_" + tokenId);
    }

    private NamespacedKey cooldownKey(String tokenId) {
        return new NamespacedKey(namespace, "cd_" + tokenId);
    }

    // ------------------------------------------------------------------
    // Tier (0 = locked, 1-3 = unlocked tier)
    // ------------------------------------------------------------------

    public int getTier(Player player, String tokenId) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Integer tier = pdc.get(tierKey(tokenId), PersistentDataType.INTEGER);
        return tier == null ? 0 : tier;
    }

    public void setTier(Player player, String tokenId, int tier) {
        player.getPersistentDataContainer().set(tierKey(tokenId), PersistentDataType.INTEGER, tier);
    }

    public boolean hasUnlocked(Player player, String tokenId) {
        return getTier(player, tokenId) > 0;
    }

    public boolean isMaxed(Player player, String tokenId) {
        return getTier(player, tokenId) >= 3;
    }

    // ------------------------------------------------------------------
    // Task progress (progress toward the NEXT tier's unlock task)
    // ------------------------------------------------------------------

    public int getProgress(Player player, String tokenId) {
        Integer progress = player.getPersistentDataContainer().get(progressKey(tokenId), PersistentDataType.INTEGER);
        return progress == null ? 0 : progress;
    }

    public void setProgress(Player player, String tokenId, int value) {
        player.getPersistentDataContainer().set(progressKey(tokenId), PersistentDataType.INTEGER, Math.max(0, value));
    }

    public int addProgress(Player player, String tokenId, int amount) {
        int value = getProgress(player, tokenId) + amount;
        setProgress(player, tokenId, value);
        return value;
    }

    // ------------------------------------------------------------------
    // Active (selected) token
    // ------------------------------------------------------------------

    public String getActiveToken(Player player) {
        return player.getPersistentDataContainer().get(activeKey, PersistentDataType.STRING);
    }

    public void setActiveToken(Player player, String tokenId) {
        if (tokenId == null) {
            player.getPersistentDataContainer().remove(activeKey);
        } else {
            player.getPersistentDataContainer().set(activeKey, PersistentDataType.STRING, tokenId);
        }
    }

    // ------------------------------------------------------------------
    // Ability cooldowns (epoch-millisecond timestamps)
    // ------------------------------------------------------------------

    public long getCooldownEnd(Player player, String tokenId) {
        Long end = player.getPersistentDataContainer().get(cooldownKey(tokenId), PersistentDataType.LONG);
        return end == null ? 0L : end;
    }

    public long getCooldownRemaining(Player player, String tokenId) {
        return Math.max(0L, getCooldownEnd(player, tokenId) - System.currentTimeMillis());
    }

    public void setCooldown(Player player, String tokenId, int seconds) {
        player.getPersistentDataContainer().set(cooldownKey(tokenId), PersistentDataType.LONG,
                System.currentTimeMillis() + (seconds * 1000L));
    }

    /** Clears every token cooldown for the player. */
    public void clearAllCooldowns(Player player, Iterable<String> tokenIds) {
        for (String tokenId : tokenIds) {
            player.getPersistentDataContainer().remove(cooldownKey(tokenId));
        }
    }
}
