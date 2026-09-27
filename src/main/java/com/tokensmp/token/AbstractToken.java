package com.tokensmp.token;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.core.VersionCompatibility;

/**
 * Shared plumbing for the token implementations: identity fields, the tier
 * map and config-backed numeric overrides so every gameplay value can be
 * tuned from config.yml without touching code.
 */
public abstract class AbstractToken implements Token {

    private final String id;
    private final String displayName;
    private final TokenRarity rarity;
    private final Material icon;
    private final boolean adminToken;
    private final Map<Integer, TokenTier> tiers = new HashMap<>();
    private final ConfigManager config;

    protected AbstractToken(String id, String displayName, TokenRarity rarity,
                            Material icon, boolean adminToken, ConfigManager config) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.icon = icon;
        this.adminToken = adminToken;
        this.config = config;
    }

    /** Registers a built tier. */
    protected void addTier(TokenTier tier) {
        tiers.put(tier.getTier(), tier);
    }

    /** Config override with code default. */
    protected int num(String path, int def) {
        return config.getInt("tokens." + id + "." + path, def);
    }

    /** Standard potion names resolved through the compatibility layer. */
    protected PotionEffectType potion(String modernName, String legacyName) {
        return VersionCompatibility.potionType(modernName, legacyName);
    }

    protected PotionEffectType strength() {
        return potion("STRENGTH", "INCREASE_DAMAGE");
    }

    protected PotionEffectType speed() {
        return potion("SPEED", null);
    }

    protected PotionEffectType nightVision() {
        return potion("NIGHT_VISION", null);
    }

    protected PotionEffectType regeneration() {
        return potion("REGENERATION", null);
    }

    protected PotionEffectType resistance() {
        return potion("RESISTANCE", "DAMAGE_RESISTANCE");
    }

    protected PotionEffectType fireResistance() {
        return potion("FIRE_RESISTANCE", null);
    }

    protected PotionEffectType jumpBoost() {
        return potion("JUMP_BOOST", "JUMP");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public TokenRarity getRarity() {
        return rarity;
    }

    @Override
    public Material getIcon() {
        return icon;
    }

    @Override
    public boolean isAdminToken() {
        return adminToken;
    }

    @Override
    public TokenTier tier(int tier) {
        return tiers.get(tier);
    }

    @Override
    public List<TokenTier> getTiers() {
        List<TokenTier> ordered = new ArrayList<>(3);
        for (int t = 1; t <= 3; t++) {
            TokenTier tier = tiers.get(t);
            if (tier != null) {
                ordered.add(tier);
            }
        }
        return ordered;
    }

    @Override
    public int getMaxTier() {
        return 3;
    }
}
