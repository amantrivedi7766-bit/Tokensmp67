package com.tokensmp.token;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rarity color system tests: enum defaults are stable, config overrides take
 * precedence and reset() restores the built-in colors.
 */
class TokenRarityTest {

    @AfterEach
    void cleanUp() {
        TokenRarity.reset();
    }

    @Test
    void everyRarityHasDisplayNameAndColor() {
        for (TokenRarity rarity : TokenRarity.values()) {
            assertTrue(!rarity.getDisplayName().isEmpty());
            assertTrue(rarity.getColorCode().startsWith("&"), rarity + " color must be a legacy & code");
        }
    }

    @Test
    void overrideChangesTheColorCode() {
        assertEquals("&9", TokenRarity.RARE.getColorCode());
        TokenRarity.override(TokenRarity.RARE, "&b");
        assertEquals("&b", TokenRarity.RARE.getColorCode());
        assertEquals("§b", TokenRarity.RARE.color());
    }

    @Test
    void overrideOnlyAffectsItsRarity() {
        TokenRarity.override(TokenRarity.RARE, "&b");
        assertEquals("&7", TokenRarity.COMMON.getColorCode());
    }

    @Test
    void resetRestoresDefaults() {
        TokenRarity.override(TokenRarity.COMMON, "&a");
        TokenRarity.reset();
        assertEquals("&7", TokenRarity.COMMON.getColorCode());
    }
}
