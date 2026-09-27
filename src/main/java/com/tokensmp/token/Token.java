package com.tokensmp.token;

import org.bukkit.Material;

import java.util.List;

/** A token definition: identity, rarity, icon and its three progressive tiers. */
public interface Token {

    /** Stable lowercase id used in commands, PDC and config (e.g. "zombie"). */
    String getId();

    String getDisplayName();

    TokenRarity getRarity();

    Material getIcon();

    boolean isAdminToken();

    /** The tier definition (1-3) or null when out of range. */
    TokenTier tier(int tier);

    List<TokenTier> getTiers();

    int getMaxTier();
}
