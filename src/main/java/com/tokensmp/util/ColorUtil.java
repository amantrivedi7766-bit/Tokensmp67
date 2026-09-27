package com.tokensmp.util;

import java.util.ArrayList;
import java.util.List;

/** Legacy chat color translation helper used across all messages and lore. */
public final class ColorUtil {

    private ColorUtil() {
    }

    /** Translates '&' color codes into legacy section codes. */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789abcdefklmnorxABCDEFKLMNORX".indexOf(chars[i + 1]) > -1) {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    /** Strips all color codes from the input. */
    public static String strip(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("§[0-9a-fk-orxA-FK-OR]", "").replaceAll("&[0-9a-fk-orxA-FK-OR]", "");
    }

    /** Colors every line of a list. */
    public static List<String> color(List<String> lines) {
        List<String> out = new ArrayList<>(lines.size());
        for (String line : lines) {
            out.add(color(line));
        }
        return out;
    }
}
