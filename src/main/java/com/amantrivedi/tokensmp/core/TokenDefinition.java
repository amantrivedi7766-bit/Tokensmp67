package com.amantrivedi.tokensmp.core;

import org.bukkit.Material;

/**
 * A full token definition (id, name, rarity, icon) plus its three progressive tiers.
 */
public final class TokenDefinition {

    private final String id;
    private final String name;
    private final String rarity;
    private final String rarityColor;
    private final Material material;
    private final int slot;
    private final boolean adminOnly;
    private final TierDefinition[] tiers = new TierDefinition[3];

    public TokenDefinition(String id, String name, String rarity, String rarityColor,
                           Material material, int slot, boolean adminOnly) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.material = material;
        this.slot = slot;
        this.adminOnly = adminOnly;
    }

    public void setTier(int tier, TierDefinition definition) {
        if (tier >= 1 && tier <= 3) {
            tiers[tier - 1] = definition;
        }
    }

    public TierDefinition tier(int tier) {
        if (tier < 1 || tier > 3) {
            return null;
        }
        return tiers[tier - 1];
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public boolean isAdminOnly() { return adminOnly; }
    public TierDefinition[] getTiers() { return tiers; }
}
