package com.tokensmp.core;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * plugin.yml validation: commands, permissions, main class and the
 * Gradle-version placeholder must all be wired correctly.
 */
class PluginYmlTest {

    private static Map<String, Object> load() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/plugin.yml"), StandardCharsets.UTF_8);
        return new org.yaml.snakeyaml.Yaml().load(yaml);
    }

    @Test
    @SuppressWarnings("unchecked")
    void declaresPlayerAndAdminCommands() throws Exception {
        Map<String, Object> commands = (Map<String, Object>) load().get("commands");
        assertTrue(commands.containsKey("token"), "missing /token command");
        assertTrue(commands.containsKey("tokensadmin"), "missing /tokensadmin command");
        assertEquals("tokensmp.player", ((Map<String, Object>) commands.get("token")).get("permission"));
        assertEquals("tokensmp.admin", ((Map<String, Object>) commands.get("tokensadmin")).get("permission"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void declaresPermissionsWithCorrectDefaults() throws Exception {
        Map<String, Object> permissions = (Map<String, Object>) load().get("permissions");
        assertEquals("true", String.valueOf(((Map<String, Object>) permissions.get("tokensmp.player")).get("default")));
        assertEquals("op", ((Map<String, Object>) permissions.get("tokensmp.admin")).get("default"));
    }

    @Test
    void mainClassPointsAtCompositionRoot() throws Exception {
        assertEquals("com.tokensmp.TokenSMP", load().get("main"));
    }
}
