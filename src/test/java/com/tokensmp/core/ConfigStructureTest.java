package com.tokensmp.core;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void tokenItemFlowIsConfigured() throws Exception {
        Map<String, Object> claim = (Map<String, Object>) load().get("claim-system");
        assertEquals(true, claim.get("unlock-gives-item"),
                "unlocking a token must hand the item to the player (hotbar)");
        assertEquals(true, claim.get("claim-gives-item"),
                "claiming must keep/ensure the token item");
        Map<String, Object> messages = (Map<String, Object>) load().get("messages");
        assertTrue(messages.containsKey("ability-need-item"),
                "the 'hold the token item' hint must be configurable");
    }

    @Test
    @SuppressWarnings("unchecked")
    void adminControlSurfaceIsConfigured() throws Exception {
        Map<String, Object> grace = (Map<String, Object>) load().get("grace-period");
        assertNotNull(grace, "grace-period section must exist");
        assertTrue(((Number) grace.get("default-minutes")).longValue() > 0,
                "grace period needs a default length");
        Map<String, Object> messages = (Map<String, Object>) load().get("messages");
        List.of("grace-period-start", "grace-period-end", "admin-spin-all", "admin-respin",
                        "admin-grace-started", "admin-grace-stopped", "admin-who-header",
                        "token-previous-deactivated")
                .forEach(key -> assertTrue(messages.containsKey(key), "missing message: " + key));
    }

    @Test
    @SuppressWarnings("unchecked")
    void livesAndRevivalAreConfigured() throws Exception {
        Map<String, Object> lives = (Map<String, Object>) load().get("lives");
        assertNotNull(lives, "lives section must exist");
        assertTrue(((Number) lives.get("starting-lives")).intValue() > 0, "starting lives must be > 0");
        assertNotNull(lives.get("revive-card"), "revive-card recipe config must exist");
        Map<String, Object> messages = (Map<String, Object>) load().get("messages");
        List.of("lives-lost", "eliminated-broadcast", "revive-cooldown", "revive-sacrifice",
                        "revive-success")
                .forEach(key -> assertTrue(messages.containsKey(key), "missing message: " + key));
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
        List.of("zombie", "skeleton", "spider", "creeper", "blaze", "enderman",
                        "warden", "ender_dragon", "guardian", "wither_skeleton", "ravager", "wolf",
                        "pig", "chicken", "snow_golem", "bee", "horse", "bat",
                        "squid", "witch", "slime", "drowned", "magma_cube", "golem",
                        "ghast", "piglin_brute", "evoker", "vex", "phantom", "shulker",
                        "piglin", "fish", "wither", "villager", "illusioner", "founder",
                        "admin") .forEach(id -> assertTrue(tokens.containsKey(id), "missing token: " + id));
        assertEquals(37, tokens.size(), "player tokens + the reserved Owner/Admin tokens");
    }

    @Test
    @SuppressWarnings("unchecked")
    void reservedTokensAreIsolated() throws Exception {
        Map<String, Object> tokens = (Map<String, Object>) load().get("tokens");
        for (String id : List.of("founder", "admin")) {
            assertTrue(tokens.containsKey(id), "config.yml is missing reserved token: " + id);
            Map<String, Object> token = (Map<String, Object>) tokens.get(id);
            for (int tier = 1; tier <= 3; tier++) {
                assertNotNull(token.get("tier" + tier), id + " must define tier " + tier);
            }
        }
        // Reserved tokens never appear in the player-facing tokens.yml roster.
        String tokensYml = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/resources/tokens.yml"),
                java.nio.charset.StandardCharsets.UTF_8);
        Map<String, Object> parsedTokensYml = new org.yaml.snakeyaml.Yaml().load(tokensYml);
        Map<String, Object> playerRoster = (Map<String, Object>) parsedTokensYml.get("tokens");
        assertTrue(!playerRoster.containsKey("founder") && !playerRoster.containsKey("admin"),
                "reserved tokens must never be in the player spin roster");
    }

    @Test
    @SuppressWarnings("unchecked")
    void announcementsUseExactSpecificationFormats() throws Exception {
        Map<String, Object> messages = (Map<String, Object>) load().get("messages");
        assertEquals("&8&l[&6&lTokenSMP&8&l] &fPlayer &b{player} &fhas just unlocked the {color}&l{token} Token &ffor the first time! 🎉",
                messages.get("first-unlock"));
        assertEquals("&8&l[&6&lTokenSMP&8&l] &d&lUPGRADE! &b{player} &fhas successfully upgraded their {color}&l{token} Token &fto &e&lTier {tier}! 🚀",
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
