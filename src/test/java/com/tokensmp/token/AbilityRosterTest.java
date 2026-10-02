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
 * Validates the 45-ability model: 15 tokens x 3 tiers, every tier a distinct
 * ability, tier damage strictly increasing, and every ability value present
 * and configurable in tokens.yml.
 */
class AbilityRosterTest {

    @SuppressWarnings("unchecked")
    private static Map<String, Object> tokensYml() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/tokens.yml"), StandardCharsets.UTF_8);
        return (Map<String, Object>) ((Map<String, Object>) new org.yaml.snakeyaml.Yaml().load(yaml)).get("tokens");
    }

    private static final List<String> REQUIRED_KEYS = List.of(
            "ability", "damage", "cooldown", "range", "radius", "knockback",
            "projectiles", "speed", "true-damage", "particle-count", "particles", "sounds");

    @Test
    void fifteenTokensWithThreeTiersEach() throws Exception {
        Map<String, Object> tokens = tokensYml();
        assertEquals(15, tokens.size(), "exactly 15 player tokens must be configured");
        for (Map.Entry<String, Object> entry : tokens.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                assertTrue(tiers.containsKey("tier" + tier),
                        entry.getKey() + " is missing tier" + tier);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyAbilityValueIsConfigurable() throws Exception {
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                Map<String, Object> values = (Map<String, Object>) tiers.get("tier" + tier);
                for (String key : REQUIRED_KEYS) {
                    assertNotNull(values.get(key),
                            entry.getKey() + " tier" + tier + " is missing '" + key + "'");
                }
                assertTrue(values.get("particles") instanceof List && !((List<?>) values.get("particles")).isEmpty(),
                        entry.getKey() + " tier" + tier + " must list particles");
                assertTrue(values.get("sounds") instanceof List && !((List<?>) values.get("sounds")).isEmpty(),
                        entry.getKey() + " tier" + tier + " must list sounds");
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void allFortyFiveAbilitiesAreDistinct() throws Exception {
        Set<String> abilities = new HashSet<>();
        List<String> ordered = new ArrayList<>();
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                String ability = String.valueOf(((Map<String, Object>) tiers.get("tier" + tier)).get("ability"));
                assertTrue(abilities.add(ability), "duplicate ability implementation: " + ability);
                ordered.add(ability);
            }
        }
        assertEquals(45, ordered.size(), "15 tokens x 3 tiers = 45 unique abilities");
    }

    @Test
    @SuppressWarnings("unchecked")
    void higherTiersAreStronger() throws Exception {
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            int t1 = (Integer) ((Map<String, Object>) tiers.get("tier1")).get("damage");
            int t2 = (Integer) ((Map<String, Object>) tiers.get("tier2")).get("damage");
            int t3 = (Integer) ((Map<String, Object>) tiers.get("tier3")).get("damage");
            assertTrue(t2 > t1, entry.getKey() + ": tier 2 must be stronger than tier 1");
            assertTrue(t3 > t2, entry.getKey() + ": tier 3 must be stronger than tier 2");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyAbilityDealsRealDamageAndHasCooldown() throws Exception {
        for (Map.Entry<String, Object> entry : tokensYml().entrySet()) {
            Map<String, Object> tiers = (Map<String, Object>) entry.getValue();
            for (int tier = 1; tier <= 3; tier++) {
                Map<String, Object> values = (Map<String, Object>) tiers.get("tier" + tier);
                assertTrue(((Number) values.get("damage")).doubleValue() > 0,
                        entry.getKey() + " tier" + tier + " must deal real damage");
                assertTrue(((Number) values.get("cooldown")).doubleValue() > 0,
                        entry.getKey() + " tier" + tier + " must have a cooldown");
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void creeperTiersUseTheirOwnKeybinds() throws Exception {
        Map<String, Object> creeper = (Map<String, Object>) tokensYml().get("creeper");
        assertEquals("TNT_CANNON", ((Map<String, Object>) creeper.get("tier1")).get("ability"));
        assertEquals("TNT_STRIKE", ((Map<String, Object>) creeper.get("tier2")).get("ability"));
        assertEquals("BOMB_CHICKENS", ((Map<String, Object>) creeper.get("tier3")).get("ability"));
        assertEquals("RIGHT_CLICK", ((Map<String, Object>) creeper.get("tier1")).get("trigger"));
        assertEquals("SHIFT_LEFT_CLICK", ((Map<String, Object>) creeper.get("tier2")).get("trigger"));
        assertEquals("SHIFT_RIGHT_CLICK", ((Map<String, Object>) creeper.get("tier3")).get("trigger"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void endermanTiersUseTheirOwnKeybinds() throws Exception {
        Map<String, Object> enderman = (Map<String, Object>) tokensYml().get("enderman");
        assertEquals("BLINK_CHAIN", ((Map<String, Object>) enderman.get("tier1")).get("ability"));
        assertEquals("PORTAL_LINK", ((Map<String, Object>) enderman.get("tier2")).get("ability"));
        assertEquals("ENDER_ASSEMBLY", ((Map<String, Object>) enderman.get("tier3")).get("ability"));
        assertEquals("RIGHT_CLICK", ((Map<String, Object>) enderman.get("tier1")).get("trigger"));
        assertEquals("SHIFT_LEFT_CLICK", ((Map<String, Object>) enderman.get("tier2")).get("trigger"));
        assertEquals("SHIFT_RIGHT_CLICK", ((Map<String, Object>) enderman.get("tier3")).get("trigger"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void tokenRosterMatchesConfig() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/config.yml"), StandardCharsets.UTF_8);
        Map<String, Object> config = (Map<String, Object>) new org.yaml.snakeyaml.Yaml().load(yaml);
        Map<String, Object> configTokens = (Map<String, Object>) config.get("tokens");
        java.util.Set<String> playerIds = new java.util.HashSet<>(configTokens.keySet());
        assertTrue(playerIds.remove("admin"), "the isolated Admin Token must stay configured");
        assertEquals(tokensYml().keySet(), playerIds,
                "config.yml and tokens.yml must describe the same player token roster");
    }
}
