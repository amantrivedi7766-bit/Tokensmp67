package com.tokensmp.token;

import java.util.EnumMap;
import java.util.Map;

/**
 * Token rarity tiers with display colors. Color codes can be overridden at
 * runtime from the colors.* config section (applied atomically on enable
 * and on /tokensadmin reload); without an override the enum default is used.
 */
public enum TokenRarity {

    COMMON("Common", "&7"),
    RARE("Rare", "&9"),
    EPIC("Epic", "&5"),
    LEGENDARY("Legendary", "&6"),
    MYTHIC("Mythic", "&d");

    private static final Map<TokenRarity, String> OVERRIDES = new EnumMap<>(TokenRarity.class);

    private final String displayName;
    private final String colorCode;

    TokenRarity(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Legacy color code, e.g. "&9" (config override wins). */
    public String getColorCode() {
        return OVERRIDES.getOrDefault(this, colorCode);
    }

    /** The color code already translated to a section sign. */
    public String color() {
        return com.tokensmp.util.ColorUtil.color(getColorCode());
    }

    /** Installs a config color override for this rarity. */
    public static void override(TokenRarity rarity, String colorCode) {
        OVERRIDES.put(rarity, colorCode);
    }

    /** Clears every override (called before re-reading the config). */
    public static void reset() {
        OVERRIDES.clear();
    }
}
