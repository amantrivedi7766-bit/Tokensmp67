package com.tokensmp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Color code translation and stripping tests. */
class ColorUtilTest {

    @Test
    void translatesAmpersandCodes() {
        assertEquals("§aGreen", ColorUtil.color("&aGreen"));
        assertEquals("§lBold§r", ColorUtil.color("&lBold&r"));
        assertEquals("§6§lTokenSMP§8", ColorUtil.color("&6&lTokenSMP&8"));
    }

    @Test
    void upperCaseCodesAreLowercased() {
        assertEquals("§cRed", ColorUtil.color("&CRed"));
    }

    @Test
    void unknownSequencesAreUntouched() {
        assertEquals("Fish & Chips", ColorUtil.color("Fish & Chips"));
        assertEquals("a&z", ColorUtil.color("a&z"));
    }

    @Test
    void nullSafe() {
        assertEquals("", ColorUtil.color((String) null));
        assertEquals("", ColorUtil.strip(null));
    }

    @Test
    void stripsBothCodeStyles() {
        assertTrue(ColorUtil.strip("§aGreen&cRed").matches("GreenRed"));
        assertEquals("Plain", ColorUtil.strip("§aPlain"));
    }

    @Test
    void colorsEveryLineOfAList() {
        var lines = ColorUtil.color(java.util.List.of("&aOne", "&bTwo"));
        assertEquals("§aOne", lines.get(0));
        assertEquals("§bTwo", lines.get(1));
    }
}
