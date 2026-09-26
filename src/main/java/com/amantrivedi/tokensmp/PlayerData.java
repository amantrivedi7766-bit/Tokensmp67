package com.amantrivedi.tokensmp;

/**
 * Per-player runtime/data model.
 *
 * <ul>
 *   <li>{@code activeToken} - enum name of the equipped TokenType, or null when none is equipped.</li>
 *   <li>{@code extraHeartHp} - permanent max-health points stacked beyond the vanilla 20.0 base
 *       (1 heart = 2.0 HP). Persisted to data.yml so withdraw/claim cycles survive restarts.</li>
 * </ul>
 */
public final class PlayerData {

    private String activeToken;
    private double extraHeartHp;

    public String getActiveToken() {
        return activeToken;
    }

    public void setActiveToken(String activeToken) {
        this.activeToken = activeToken;
    }

    public double getExtraHeartHp() {
        return extraHeartHp;
    }

    public void setExtraHeartHp(double extraHeartHp) {
        this.extraHeartHp = Math.max(0.0, extraHeartHp);
    }

    /** Convenience: number of extra stacked hearts (1 heart = 2.0 HP). */
    public int getExtraHearts() {
        return (int) Math.floor(extraHeartHp / 2.0);
    }
}
