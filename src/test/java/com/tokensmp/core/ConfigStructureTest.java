package com.tokensmp.core;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Configuration validation tests: the shipped config.yml must contain every
 * section required by the specification, the complete token roster with the
 * specified endgame values, and the exact announcement / cooldown formats.
 */
class ConfigStructureTest {

    private static Map<String, Object> load() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/config.yml"), StandardCharsets.UTF_8);
        return new org.yaml.snakeyaml.Yaml().load(yaml);
    }

    @Test
    @SuppressWarnings("unchecked")
    void containsEveryRequiredSection() throws Exception {
        Map<String, Object> config = load();
        List.of("plugin", "permissions", "commands", "colors", "spin", "first-join",
                        "cooldowns", "particles", "sounds", "token-stealing", "drop-animation",
                        "claim-system", "upgrade-system", "gui", "admin", "performance",
                        "tokens", "messages")
                .forEach(section -> assertTrue(config.containsKey(section),
                        "missing required config section: " + section));
    }

    @Test
    @SuppressWarnings("unchecked")
    void containsCompleteTokenRoster() throws Exception {
        Map<String, Object> tokens = (Map<String, Object>) load().get("tokens");
        List.of("zombie", "skeleton", "spider", "creeper", "enderman", "blaze", "warden", "admin")
                .forEach(id -> assertTrue(tokens.containsKey(id), "missing token: " + id));
        assertEquals(8, tokens.size(), "exactly the 8 specified tokens must be configured");
    }

    @Test
    @SuppressWarnings("unchecked")
    void endgameAdminTokenMatchesSpecification() throws Exception {
        Map<String, Object> admin = (Map<String, Object>) ((Map<String, Object>) load().get("tokens")).get("admin");
        // Tier 1: 15-block radius, 10 true damage, 45s cooldown, 4 netherite blocks.
        Map<String, Object> tier1 = (Map<String, Object>) admin.get("tier1");
        assertEquals(15, tier1.get("ability-radius"));
        assertEquals(10, tier1.get("ability-damage"));
        assertEquals(45, tier1.get("ability-cooldown"));
        assertEquals(4, tier1.get("cost-netherite-blocks"));
        // Tier 2: Chrono Freeze, 10-block radius, 7s, 60s cooldown, 8 diamond blocks + 1 dragon's breath.
        Map<String, Object> tier2 = (Map<String, Object>) admin.get("tier2");
        assertEquals(10, tier2.get("ability-radius"));
        assertEquals(7, tier2.get("ability-duration"));
        assertEquals(60, tier2.get("ability-cooldown"));
        assertEquals(8, tier2.get("cost-diamond-blocks"));
        assertEquals(1, tier2.get("cost-dragons-breath"));
        // Tier 3: God Mode, 35 true damage, 20-block knockback, 30s cooldown,
        // 1 dragon egg + 4 netherite blocks + 1 enchanted golden apple.
        Map<String, Object> tier3 = (Map<String, Object>) admin.get("tier3");
        assertEquals(35, tier3.get("ability-damage"));
        assertEquals(20, tier3.get("ability-knockback"));
        assertEquals(30, tier3.get("ability-cooldown"));
        assertEquals(1, tier3.get("cost-dragon-eggs"));
        assertEquals(4, tier3.get("cost-netherite-blocks"));
        assertEquals(1, tier3.get("cost-god-apples"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void announcementsUseExactSpecificationFormats() throws Exception {
        Map<String, Object> messages = (Map<String, Object>) load().get("messages");
        assertEquals("&8&l[&6&lTokenSMP&8&l] &fPlayer &b{player} &fhas just unlocked the {color}&l{token} Token &ffor the first time! \uD83C\uDF89",
                messages.get("first-unlock"));
        assertEquals("&8&l[&6&lTokenSMP&8&l] &d&lUPGRADE! &b{player} &fhas successfully upgraded their {color}&l{token} Token &fto &e&lTier {tier}! \uD83C\uDF80",
                messages.get("tier-upgrade"));
        assertEquals("&4&l[ADMIN ALERT] &cOperator &f{admin} &chas generated an &e&lOriginal Admin Token &cfor &f{target}&c.",
                messages.get("admin-token-alert"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void cooldownBarFormatHasPlaceholders() throws Exception {
        Map<String, Object> bar = (Map<String, Object>) ((Map<String, Object>) load().get("cooldowns")).get("bar");
        String format = (String) bar.get("format");
        assertTrue(format.contains("{bar}") && format.contains("{time}"),
                "cooldown bar format must expose {bar} and {time}");
        assertEquals(16, bar.get("length"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void tokenStealingDefaultsMatchSpecification() throws Exception {
        Map<String, Object> stealing = (Map<String, Object>) load().get("token-stealing");
        assertEquals(true, stealing.get("only-pvp"));
        assertEquals(false, stealing.get("require-killer-without-token"));
        assertEquals(true, stealing.get("steal-one-token-per-kill"));
        assertEquals(true, stealing.get("victim-keeps-progress"));
        assertEquals(true, stealing.get("victim-loses-active-state"));
        assertEquals(60, stealing.get("claim-timeout-seconds"));
        assertEquals(false, stealing.get("allow-admin-token"),
                "the Admin Token must never be stealable by default");
    }

    @Test
    @SuppressWarnings("unchecked")
    void dropVortexUsesSpecificationParameters() throws Exception {
        Map<String, Object> drop = (Map<String, Object>) load().get("drop-animation");
        assertEquals(4, drop.get("interval-ticks"));
        assertEquals(0.8, (Double) drop.get("radius"), 1e-9);
        assertEquals(2.0, (Double) drop.get("height"), 1e-9);
    }
}
