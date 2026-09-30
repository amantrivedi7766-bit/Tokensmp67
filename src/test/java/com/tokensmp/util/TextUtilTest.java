package com.tokensmp.util;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pretty-printing helpers used in lore, stats and material lists. */
class TextUtilTest {

    @Test
    void prettyNamesSplitAndCapitalize() {
        assertEquals("Rotten Flesh", TextUtil.pretty(Material.ROTTEN_FLESH));
        assertEquals("Cave Spider", TextUtil.pretty("CAVE_SPIDER"));
        assertEquals("Netherite Block", TextUtil.pretty("NETHERITE_BLOCK"));
    }

    @Test
    void prettyHandlesSingleWords() {
        assertEquals("Zombie", TextUtil.pretty("ZOMBIE"));
    }

    @Test
    void materialListsJoinAmounts() {
        Map<Material, Integer> costs = new LinkedHashMap<>();
        costs.put(Material.DIAMOND, 32);
        costs.put(Material.NETHERITE_BLOCK, 2);
        assertEquals("32x Diamond§7, §f2x Netherite Block", TextUtil.materialsList(costs));
    }

    @Test
    void emptyMaterialListSaysNone() {
        assertEquals("None", TextUtil.materialsList(Map.of()));
    }

    @Test
    void secondsAreFormattedForDisplay() {
        assertEquals("45s", TextUtil.formatSeconds(45));
        assertEquals("1m 30s", TextUtil.formatSeconds(90));
        assertEquals("5m 0s", TextUtil.formatSeconds(300));
    }
}
