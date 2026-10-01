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

    private final TokenSMP plugin;
    private volatile FileConfiguration config;
    private volatile FileConfiguration tokensConfig;

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

    public boolean getBoolean(String path, boolean def) {
        return cfg().getBoolean(path, def);
    }
}
