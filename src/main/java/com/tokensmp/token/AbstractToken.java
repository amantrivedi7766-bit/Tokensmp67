package com.tokensmp.token;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

    /** Config override with code default (whole numbers). */
    protected int num(String path, int def) {
        return config.getInt("tokens." + id + "." + path, def);
    }

    /** Config override with code default (floating point values). */
    protected double dnum(String path, double def) {
        return config.getDouble("tokens." + id + "." + path, def);
    }

    // ------------------------------------------------------------------
    // tokens.yml - ability value overrides (tier-scoped)
    // ------------------------------------------------------------------

    /** Integer ability value from tokens.yml (tokens.<id>.tierN.<key>). */
    protected int anum(int tier, String key, int def) {
        return config.tokens().getInt("tokens." + id + ".tier" + tier + "." + key, def);
    }

    /** Floating point ability value from tokens.yml. */
    protected double adnum(int tier, String key, double def) {
        return config.tokens().getDouble("tokens." + id + ".tier" + tier + "." + key, def);
    }

    /** Boolean ability value from tokens.yml. */
    protected boolean abool(int tier, String key, boolean def) {
        return config.tokens().getBoolean("tokens." + id + ".tier" + tier + "." + key, def);
    }

    /** Configurable ability trigger from tokens.yml (defaults to the code value). */
    protected AbilityTrigger atrigger(int tier, AbilityTrigger def) {
        String raw = config.tokens().getString("tokens." + id + ".tier" + tier + ".trigger");
        if (raw == null || raw.isBlank()) {
            return def;
        }
        try {
            return AbilityTrigger.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    /** Configurable particle list (names) with code defaults. */
    protected List<Particle> aparticles(int tier, Particle... def) {
        List<String> names = config.tokens().getStringList("tokens." + id + ".tier" + tier + ".particles");
        if (names.isEmpty()) {
            return List.of(def);
        }
        List<Particle> out = new ArrayList<>();
        for (String name : names) {
            try {
                out.add(Particle.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // unknown particle name in config - skipped safely
            }
        }
        return out.isEmpty() ? List.of(def) : out;
    }

    /** Configurable sound list (names) with code defaults. */
    protected List<Sound> asounds(int tier, Sound... def) {
        List<String> names = config.tokens().getStringList("tokens." + id + ".tier" + tier + ".sounds");
        if (names.isEmpty()) {
            return List.of(def);
        }
        List<Sound> out = new ArrayList<>();
        for (String name : names) {
            try {
                out.add(Sound.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // unknown sound name in config - skipped safely
            }
        }
        return out.isEmpty() ? List.of(def) : out;
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

    protected PotionEffectType waterBreathing() {
        return potion("WATER_BREATHING", null);
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
