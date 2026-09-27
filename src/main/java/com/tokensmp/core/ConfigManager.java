package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Central config access wrapper: typed getters with defaults, atomic reload
 * (values are re-read on /tokensadmin reload without a server reboot).
 */
public final class ConfigManager {

    private final TokenSMP plugin;
    private volatile FileConfiguration config;

    public ConfigManager(TokenSMP plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /** Reloads config.yml from disk atomically. */
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    private FileConfiguration cfg() {
        return config;
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
