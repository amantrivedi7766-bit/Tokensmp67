package com.tokensmp.animation;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** Version-safe sound helpers with consistent volume/pitch handling. */
public final class SoundEngine {

    /** Global master toggle, applied from the sounds.enabled config section. */
    private static volatile boolean enabled = true;

    private SoundEngine() {
    }

    public static void configure(boolean enabledConfig) {
        enabled = enabledConfig;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void play(Player player, Sound sound, float volume, float pitch) {
        if (enabled && player != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void world(Location location, Sound sound, float volume, float pitch) {
        if (enabled && location != null && location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    /** Standard denied/early-activation click: low-pitched bass note. */
    public static void denied(Player player) {
        play(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    /** Standard activation confirmation click. */
    public static void click(Player player) {
        play(player, Sound.UI_BUTTON_CLICK, 0.7f, 1.4f);
    }

    /** Standard tier-up fanfare. */
    public static void levelUp(Player player) {
        play(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
    }
}
