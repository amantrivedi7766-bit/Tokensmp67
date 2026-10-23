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

    /** An attribute by its modern or legacy field name (null when unavailable). */
    public static Attribute attribute(String modernName, String legacyName) {
        try {
            return Attribute.valueOf(modernName);
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        if (legacyName != null) {
            try {
                return Attribute.valueOf(legacyName);
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        try {
            Class<?> registryClass = Class.forName("org.bukkit.Registry");
            Field attributeField = registryClass.getField("ATTRIBUTE");
            Object registry = attributeField.get(null);
            String wanted = modernName.toLowerCase(java.util.Locale.ROOT);
            for (Object entry : (Iterable<?>) registry) {
                Object key = entry.getClass().getMethod("getKey").invoke(entry);
                if (String.valueOf(key).toLowerCase(java.util.Locale.ROOT).endsWith(wanted)) {
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

    // ------------------------------------------------------------------
    // Crash-proof enum lookups. A Sound/Particle/Material/EntityType name
    // that does not exist on the running server version must NEVER throw -
    // it simply resolves to null (and the caller skips it) or to a fallback.
    // ------------------------------------------------------------------

    /** Resolves a Sound by name, or null when this server version has none. */
    public static org.bukkit.Sound sound(String name) {
        if (name == null) {
            return null;
        }
        try {
            return org.bukkit.Sound.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Resolves a Particle by name, or null when this server version has none. */
    public static org.bukkit.Particle particle(String name) {
        if (name == null) {
            return null;
        }
        try {
            return org.bukkit.Particle.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** A list of resolved particles (unknown names are skipped). */
    public static java.util.List<org.bukkit.Particle> particles(String... names) {
        java.util.List<org.bukkit.Particle> out = new java.util.ArrayList<>();
        for (String name : names) {
            org.bukkit.Particle particle = particle(name);
            if (particle != null) {
                out.add(particle);
            }
        }
        return out;
    }

    /** A list of resolved sounds (unknown names are skipped). */
    public static java.util.List<org.bukkit.Sound> sounds(String... names) {
        java.util.List<org.bukkit.Sound> out = new java.util.ArrayList<>();
        for (String name : names) {
            org.bukkit.Sound sound = sound(name);
            if (sound != null) {
                out.add(sound);
            }
        }
        return out;
    }

    /** Resolves a Material by name, falling back when the version lacks it. */
    public static org.bukkit.Material material(String name, org.bukkit.Material fallback) {
        if (name != null) {
            try {
                org.bukkit.Material material = org.bukkit.Material.matchMaterial(name.trim());
                if (material != null) {
                    return material;
                }
            } catch (Throwable ignored) {
                // fall through to the fallback
            }
        }
        return fallback == null ? org.bukkit.Material.STONE : fallback;
    }

    /** Resolves EntityType names into a list (unknown names are skipped). */
    public static java.util.List<org.bukkit.entity.EntityType> entityTypes(String... names) {
        java.util.List<org.bukkit.entity.EntityType> out = new java.util.ArrayList<>();
        for (String name : names) {
            try {
                out.add(org.bukkit.entity.EntityType.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (Throwable ignored) {
                // skip unknown entity type
            }
        }
        return out;
    }
}
