package com.tokensmp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Progress bar rendering and percentage math tests. */
class ProgressBarTest {

    @Test
    void zeroProgressRendersEmptyBar() {
        assertEquals("§a" + "#".repeat(0) + "§c" + "-".repeat(20), ProgressBar.bar(0, 100));
    }

    @Test
    void fullProgressRendersFilledBar() {
        String bar = ProgressBar.bar(100, 100, 20, "§a", "§c");
        // Both colors are always emitted; only the #/- counts change.
        assertEquals("§a" + "#".repeat(20) + "§c", bar);
    }

    @Test
    void halfwayProgressSplitsTheBar() {
        String bar = ProgressBar.bar(50, 100, 20, "§a", "§c");
        assertEquals("§a" + "#".repeat(10) + "§c" + "-".repeat(10), bar);
    }

    @Test
    void overProgressIsClamped() {
        String bar = ProgressBar.bar(500, 100, 20, "§a", "§c");
        assertEquals("§a" + "#".repeat(20) + "§c", bar);
    }

    @Test
    void negativeProgressIsClamped() {
        String bar = ProgressBar.bar(-5, 100, 20, "§a", "§c");
        assertEquals("§a" + "§c" + "-".repeat(20), bar);
    }

    @Test
    void zeroRequiredDoesNotDivideByZero() {
        String bar = ProgressBar.bar(10, 0, 20, "§a", "§c");
        assertTrue(bar.startsWith("§a#"));
    }

    @Test
    void percentNeverExceedsHundred() {
        assertEquals("100%", ProgressBar.percent(120, 100));
        assertEquals("50%", ProgressBar.percent(1, 2));
        assertEquals("0%", ProgressBar.percent(0, 10));
    }
}
