package com.tokensmp.core;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

/**
 * Cross-version compatibility layer. All version-sensitive lookups live here -
 * the rest of the plugin never performs version checks.
 *
 * Resolves renamed constants (RESISTANCE vs DAMAGE_RESISTANCE, MAX_HEALTH vs
 * GENERIC_MAX_HEALTH) through the 1.20.5+ registries with reflective field
 * fallbacks for older 1.21 builds. When a future version renames something,
 * these helpers degrade gracefully (return null / no-op) instead of crashing.
 */
public final class VersionCompatibility {

    private VersionCompatibility() {
    }

    /** The max-health attribute, whatever the current server names it. */
    public static Attribute maxHealthAttribute() {
        try {
            return Attribute.valueOf("MAX_HEALTH");
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        try {
            return Attribute.valueOf("GENERIC_MAX_HEALTH");
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        // Registry-based (1.20.5+): find the attribute whose key ends in max_health
        try {
            Class<?> registryClass = Class.forName("org.bukkit.Registry");
            Field attributeField = registryClass.getField("ATTRIBUTE");
            Object registry = attributeField.get(null);
            for (Object entry : (Iterable<?>) registry) {
                Object key = entry.getClass().getMethod("getKey").invoke(entry);
                if (String.valueOf(key).contains("max_health")) {
                    return (Attribute) entry;
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // fall through
        }
        return null;
    }

    /** A potion effect type by its modern or legacy field name. */
    public static PotionEffectType potionType(String modernName, String legacyName) {
        try {
            Field field = PotionEffectType.class.getField(modernName);
            return (PotionEffectType) field.get(null);
        } catch (ReflectiveOperationException ignored) {
            // fall through to legacy
        }
        if (legacyName != null) {
            try {
                Field field = PotionEffectType.class.getField(legacyName);
                return (PotionEffectType) field.get(null);
            } catch (ReflectiveOperationException ignored) {
                // fall through
            }
        }
        // Registry fallback (1.20.5+): match by NamespacedKey value
        try {
            Class<?> registryClass = Class.forName("org.bukkit.Registry");
            Field effectField = registryClass.getField("EFFECT");
            Object registry = effectField.get(null);
            String wanted = modernName.toLowerCase(java.util.Locale.ROOT);
            for (Object entry : (Iterable<?>) registry) {
                Object key = entry.getClass().getMethod("getKey").invoke(entry);
                if (String.valueOf(key).toLowerCase(java.util.Locale.ROOT).endsWith(wanted)) {
                    return (PotionEffectType) entry;
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // fall through
        }
        return null;
    }

    /** Safely removes a heart modifier by key from the player's max health. */
    public static void removeModifier(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier modifier = instance.getModifier(key);
        if (modifier != null) {
            instance.removeModifier(modifier);
        }
    }
}
