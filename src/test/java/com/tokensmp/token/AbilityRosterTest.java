package com.tokensmp.token;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the configured token roster and its abilities: every token has
 * three tiers, the 20 tokens from the owner's list each carry their signature
 * ability (tier-wise scaled, never duplicated), and every configured ability
 * deals real damage and has a cooldown.
 */
class AbilityRosterTest {

    private static final List<String> REQUIRED_KEYS = List.of(
            "ability", "trigger", "damage", "cooldown", "range", "radius", "knockback",
            "count", "speed", "true-damage", "particle-count", "particles", "sounds");

    /** token id -> the ability enum it must carry. */
    private static final Map<String, String> SIGNATURES = Map.ofEntries(
            Map.entry("zombie", "UNDEAD_VITALITY"),
            Map.entry("skeleton", "ARCHERS_FOCUS"),
            Map.entry("spider", "WEB_TRAPPER"),
            Map.entry("creeper", "MEGA_DETONATOR"),
            Map.entry("blaze", "INFERNAL_AURA"),
            Map.entry("enderman", "VOID_SHIFT"),
            Map.entry("warden", "SONIC_SHRIEK"),
            Map.entry("ender_dragon", "DRAGONS_MIGHT"),
            Map.entry("guardian", "OCEANS_GRACE"),
            Map.entry("wither_skeleton", "DARK_PRESENCE"),
            Map.entry("ravager", "BERSERKER_RAGE"),
            Map.entry("wolf", "PACK_LEADER"),
            Map.entry("pig", "HOG_HEAL"),
            Map.entry("chicken", "FEATHER_FALL"),
            Map.entry("snow_golem", "FROZEN_HEART"),
            Map.entry("bee", "BUZZ_RUSH"),
            Map.entry("horse", "STALLION_SPRINT"),
            Map.entry("bat", "ECHOLOCATION"),
            Map.entry("squid", "INK_CLOUD"),
            Map.entry("witch", "BREW_MASTER"));

    private static Map<String, Object> load(String file) throws Exception {
        String text = Files.readString(Path.of("src/main/resources/" + file), StandardCharsets.UTF_8);
        return new org.yaml.snakeyaml.Yaml().load(text);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> tokensYml() throws Exception {
        return (Map<String, Object>) load("tokens.yml").get("tokens");
    }

    @Test
    void everyTokenHasThreeTiers() throws Exception {
        Map<String, Object> tokens = tokensYml();
        assertEquals(29, tokens.size(), "29 player tokens must be configured");
        for (Map.Entry<String, Object> entry : tokens.entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                assertNotNull(tiers.get("tier" + tier),
                        entry.getKey() + " must define tier " + tier);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void listedTokensCarryTheirSignatureAbility() throws Exception {
        Map<String, Object> tokens = tokensYml();
        for (Map.Entry<String, String> expected : SIGNATURES.entrySet()) {
            Object token = tokens.get(expected.getKey());
            assertNotNull(token, "missing token: " + expected.getKey());
            Map<String, Object> tiers = (Map<String, Object>) token;
            for (int tier = 1; tier <= 3; tier++) {
                Map<String, Object> values = (Map<String, Object>) tiers.get("tier" + tier);
                assertEquals(expected.getValue(), values.get("ability"),
                        expected.getKey() + " tier" + tier + " must use its signature ability");
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void abilitiesAreNeverDuplicatedAcrossTokens() throws Exception {
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            Object ability = ((Map<String, Object>) tiers.get("tier1")).get("ability");
            if ("NONE".equals(ability)) {
                continue;
            }
            assertTrue(seen.add(String.valueOf(ability)),
                    "ability repeated across tokens: " + ability);
        }
        assertEquals(SIGNATURES.size(), seen.size(), "every listed token needs a unique ability");
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyConfiguredAbilityDealsDamageAndHasACooldown() throws Exception {
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                Map<String, Object> values = (Map<String, Object>) tiers.get("tier" + tier);
                if ("NONE".equals(values.get("ability"))) {
                    continue; // cleared - awaiting the owner's spec
                }
                for (String key : REQUIRED_KEYS) {
                    assertTrue(values.containsKey(key),
                            entry.getKey() + " tier" + tier + " is missing key: " + key);
                }
                assertTrue(((Number) values.get("damage")).doubleValue() > 0,
                        entry.getKey() + " tier" + tier + " must deal real damage");
                assertTrue(((Number) values.get("cooldown")).doubleValue() > 0,
                        entry.getKey() + " tier" + tier + " must have a cooldown");
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void tierThreeIsTheStrongest() throws Exception {
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            Map<String, Object> t1 = (Map<String, Object>) tiers.get("tier1");
            if ("NONE".equals(t1.get("ability"))) {
                continue;
            }
            int d1 = (Integer) t1.get("damage");
            int d3 = (Integer) ((Map<String, Object>) tiers.get("tier3")).get("damage");
            assertTrue(d3 > d1, entry.getKey() + ": tier 3 must be stronger than tier 1");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void tokenRosterMatchesConfig() throws Exception {
        Map<String, Object> tokens = tokensYml();
        Map<String, Object> configured = (Map<String, Object>) load("config.yml").get("tokens");
        for (String id : tokens.keySet()) {
            assertTrue(configured.containsKey(id), "config.yml is missing token: " + id);
        }
        assertTrue(configured.containsKey("admin"), "the isolated Admin Token must stay configured");
        List<String> ids = new ArrayList<>(tokens.keySet());
        assertEquals(29, ids.size());
    }
}
