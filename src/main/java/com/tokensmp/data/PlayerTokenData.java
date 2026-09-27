package com.tokensmp.data;

/**
 * Immutable snapshot of one player's state for one token: tier, claim state
 * and task grind progress. GUIs and commands read these snapshots; only
 * TokenDataManager writes them.
 */
public final class PlayerTokenData {

    private final String tokenId;
    private final int tier;
    private final boolean claimed;
    private final int progress;

    public PlayerTokenData(String tokenId, int tier, boolean claimed, int progress) {
        this.tokenId = tokenId;
        this.tier = tier;
        this.claimed = claimed;
        this.progress = progress;
    }

    public String getTokenId() {
        return tokenId;
    }

    /** 0 = locked, 1-3 = unlocked tier. */
    public int getTier() {
        return tier;
    }

    public boolean isUnlocked() {
        return tier >= 1;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public int getProgress() {
        return progress;
    }
}
