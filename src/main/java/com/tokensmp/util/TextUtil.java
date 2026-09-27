package com.tokensmp.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

/** Pretty naming helpers for materials, entities and enums shown to players. */
public final class TextUtil {

    private TextUtil() {
    }

    /** ROTTEN_FLESH -> "Rotten Flesh". */
    public static String pretty(Material material) {
        return pretty(material.name());
    }

    /** CAVE_SPIDER -> "Cave Spider". */
    public static String pretty(EntityType type) {
        return pretty(type.name());
    }

    /** WORD_WORD -> "Word Word". */
    public static String pretty(String raw) {
        String[] parts = raw.toLowerCase().replace('_', ' ').split(" ");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    /** Joins material amounts like "32x Diamond, 2x Netherite Block". */
    public static String materialsList(Map<Material, Integer> costs) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
            if (out.length() > 0) {
                out.append("§7, §f");
            }
            out.append(entry.getValue()).append("x ").append(pretty(entry.getKey()));
        }
        return out.length() == 0 ? "None" : out.toString();
    }

    /** Formats seconds as "1m 30s" / "45s". */
    public static String formatSeconds(long seconds) {
        if (seconds >= 60) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}
