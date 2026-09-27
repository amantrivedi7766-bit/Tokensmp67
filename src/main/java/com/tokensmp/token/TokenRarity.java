package com.tokensmp.token;

/** Token rarity tiers with display colors. */
public enum TokenRarity {

    COMMON("Common", "&7"),
    RARE("Rare", "&9"),
    EPIC("Epic", "&5"),
    LEGENDARY("Legendary", "&6"),
    MYTHIC("Mythic", "&d");

    private final String displayName;
    private final String colorCode;

    TokenRarity(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Legacy color code, e.g. "&9". */
    public String getColorCode() {
        return colorCode;
    }

    /** The color code already translated to a section sign. */
    public String color() {
        return com.tokensmp.util.ColorUtil.color(colorCode);
    }
}
