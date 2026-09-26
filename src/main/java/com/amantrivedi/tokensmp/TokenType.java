package com.amantrivedi.tokensmp;

import org.bukkit.Material;

/**
 * Defines the three token tiers available in the Token SMP menu,
 * including their GUI representation and ability names.
 */
public enum TokenType {

    ZOMBIE("Zombie", "Common", Material.ZOMBIE_HEAD, "§a§lZombie Token §7(Common)", "Rotten Rush"),
    BLAZE("Blaze", "Rare", Material.BLAZE_POWDER, "§6§lBlaze Token §7(Rare)", "Fireball Barrage"),
    WARDEN("Warden", "Legendary", Material.ECHO_SHARD, "§3§lWarden Token §7(Legendary)", "Sonic Boom");

    private final String displayName;
    private final String tier;
    private final Material material;
    private final String itemName;
    private final String abilityName;

    TokenType(String displayName, String tier, Material material, String itemName, String abilityName) {
        this.displayName = displayName;
        this.tier = tier;
        this.material = material;
        this.itemName = itemName;
        this.abilityName = abilityName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTier() {
        return tier;
    }

    public Material getMaterial() {
        return material;
    }

    public String getItemName() {
        return itemName;
    }

    public String getAbilityName() {
        return abilityName;
    }

    /** Resolves a stored token id (enum name) back to a TokenType; null when unset/unknown. */
    public static TokenType fromId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            return valueOf(id);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
