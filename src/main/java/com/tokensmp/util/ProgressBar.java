package com.tokensmp.util;

/** Renders text progress bars for GUI lore, stats output and cooldown HUDs. */
public final class ProgressBar {

    private ProgressBar() {
    }

    /**
     * Builds a bar like "[########----------]".
     *
     * @param current  current progress value
     * @param required required total value
     * @param length   number of characters inside the bar
     */
    public static String bar(int current, int required, int length, String filledColor, String emptyColor) {
        int clampedRequired = Math.max(1, required);
        int clampedCurrent = Math.max(0, Math.min(clampedRequired, current));
        int filled = (int) Math.round((clampedCurrent / (double) clampedRequired) * length);
        if (filled > length) {
            filled = length;
        }
        return filledColor + "#".repeat(filled) + emptyColor + "-".repeat(length - filled);
    }

    /** Default green/red bar of length 20. */
    public static String bar(int current, int required) {
        return bar(current, required, 20, "§a", "§c");
    }

    /** Progress percentage text (never exceeds 100). */
    public static String percent(int current, int required) {
        int clampedRequired = Math.max(1, required);
        int clampedCurrent = Math.max(0, Math.min(clampedRequired, current));
        return Math.round((clampedCurrent * 100.0) / clampedRequired) + "%";
    }
}
