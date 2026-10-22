package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Central config access wrapper: typed getters with defaults, atomic reload
 * (values are re-read on /tokensadmin reload without a server reboot).
 *
 * Two configuration files are managed:
 * - config.yml  - plugin behaviour, messages, GUIs, tasks and costs.
 * - tokens.yml  - every ability value (damage, cooldown, range, radius,
 *   knockback, projectile counts, particle counts, particle lists and sound
 *   lists) for all 45 token abilities.
 */
public final class ConfigManager {

    /** Bump whenever the default config layout changes. */
    public static final int CONFIG_VERSION = 6;
    public static final int TOKENS_VERSION = 6;

    private final TokenSMP plugin;
    private volatile FileConfiguration config;
    private volatile FileConfiguration tokensConfig;

    /**
     * Replaces a stale config.yml / tokens.yml (from an older plugin build)
     * with the bundled defaults, backing the old file up first. This makes an
     * upgrade safe even if the old config folder was left in place.
     */
    public static void migrate(TokenSMP plugin) {
        migrateFile(plugin, "config.yml", CONFIG_VERSION);
        migrateFile(plugin, "tokens.yml", TOKENS_VERSION);
    }

    private static void migrateFile(TokenSMP plugin, String name, int expected) {
        java.io.File file = new java.io.File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
            return;
        }
        int version;
        try {
            version = YamlConfiguration.loadConfiguration(file).getInt("config-version", 0);
        } catch (Throwable error) {
            version = 0;
        }
        if (version >= expected) {
            return;
        }
        java.io.File backup = new java.io.File(plugin.getDataFolder(),
                name + ".backup-" + System.currentTimeMillis());
        try {
            java.nio.file.Files.copy(file.toPath(), backup.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
            // a failed backup must not stop the migration
        }
        plugin.saveResource(name, true);
        plugin.getLogger().info("Migrated " + name + " (v" + version + " -> v" + expected
                + "); the old file was backed up as " + backup.getName());
    }

    public ConfigManager(TokenSMP plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadTokens();
    }

    /** Reloads both config.yml and tokens.yml atomically. */
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadTokens();
    }

    private void loadTokens() {
        File file = new File(plugin.getDataFolder(), "tokens.yml");
        if (!file.exists()) {
            plugin.saveResource("tokens.yml", false);
        }
        this.tokensConfig = YamlConfiguration.loadConfiguration(file);
    }

    private FileConfiguration cfg() {
        return config;
    }

    /** The tokens.yml configuration (ability values). */
    public FileConfiguration tokens() {
        return tokensConfig;
    }

    public String getString(String path, String def) {
        return com.tokensmp.util.ColorUtil.color(cfg().getString(path, def));
    }

    public String rawString(String path, String def) {
        return cfg().getString(path, def);
    }

    public int getInt(String path, int def) {
        return cfg().getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return cfg().getDouble(path, def);
    }

    public long getLong(String path, long def) {
        return cfg().getLong(path, def);
    }

    /** A string list from config (empty when missing). */
    public java.util.List<String> getStringList(String path) {
        java.util.List<String> list = config.getStringList(path);
        return list == null ? new java.util.ArrayList<>() : list;
    }

    public boolean getBoolean(String path, boolean def) {
        return cfg().getBoolean(path, def);
    }
}
