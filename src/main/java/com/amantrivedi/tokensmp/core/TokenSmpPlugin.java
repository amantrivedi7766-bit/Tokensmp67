package com.amantrivedi.tokensmp.core;

import com.amantrivedi.tokensmp.abilities.AbilityListener;
import com.amantrivedi.tokensmp.abilities.FreezeManager;
import com.amantrivedi.tokensmp.abilities.KillTaskListener;
import com.amantrivedi.tokensmp.abilities.PassiveManager;
import com.amantrivedi.tokensmp.abilities.TokenItemListener;
import com.amantrivedi.tokensmp.commands.TokenCommand;
import com.amantrivedi.tokensmp.commands.TokensAdminCommand;
import com.amantrivedi.tokensmp.commands.TokensCommand;
import com.amantrivedi.tokensmp.data.TokenDataManager;
import com.amantrivedi.tokensmp.ui.SelectionMenu;
import com.amantrivedi.tokensmp.ui.UpgradeMenu;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

/**
 * Core main class: handles setup, config load and wiring of all managers,
 * listeners and commands.
 */
public final class TokenSmpPlugin extends JavaPlugin {

    private TokenRegistry tokenRegistry;
    private TokenDataManager dataManager;
    private PassiveManager passiveManager;
    private FreezeManager freezeManager;
    private SelectionMenu selectionMenu;
    private UpgradeMenu upgradeMenu;
    private ChatNotifications notifications;

    private NamespacedKey tokenItemKey;
    private Attribute maxHealthAttribute;
    private PotionEffectType resistanceEffectType;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Version-safe max-health attribute (registry keys changed between 1.21.1 and 1.21.2+).
        maxHealthAttribute = resolveMaxHealthAttribute();
        if (maxHealthAttribute == null) {
            getLogger().severe("Could not resolve the max-health attribute on this server version - disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // The resistance effect constant was renamed between 1.21.1 and 1.21.3+.
        resistanceEffectType = resolveResistance();
        if (resistanceEffectType == null) {
            getLogger().warning("Could not resolve the resistance potion effect; resistance passives are disabled.");
        }

        tokenRegistry = new TokenRegistry(this);
        tokenRegistry.load();

        tokenItemKey = new NamespacedKey(this, "token_item");
        dataManager = new TokenDataManager(getName().toLowerCase(), new NamespacedKey(this, "active_token"));

        notifications = new ChatNotifications(this);
        freezeManager = new FreezeManager(this);
        passiveManager = new PassiveManager(this);
        selectionMenu = new SelectionMenu(this);
        upgradeMenu = new UpgradeMenu(this);

        // Listeners
        getServer().getPluginManager().registerEvents(passiveManager, this);
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new KillTaskListener(this), this);
        getServer().getPluginManager().registerEvents(new TokenItemListener(this), this);
        getServer().getPluginManager().registerEvents(selectionMenu, this);
        getServer().getPluginManager().registerEvents(upgradeMenu, this);

        // Commands (with strict permission checks inside the interpreters)
        registerCommand("tokens", new TokensCommand(this));
        registerCommand("token", new TokenCommand(this));
        registerCommand("tokensadmin", new TokensAdminCommand(this));

        // Periodic passive state re-verification for all online players.
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                passiveManager.applyPassives(player);
            }
        }, 100L, 100L);

        getLogger().info("TokenSMP v" + getDescription().getVersion()
                + " enabled. /tokens  |  /token balance  |  /token stats  |  /tokensadmin");
    }

    private void registerCommand(String name, Object handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' missing from plugin.yml!");
            return;
        }
        if (handler instanceof org.bukkit.command.CommandExecutor executor) {
            command.setExecutor(executor);
        }
        if (handler instanceof org.bukkit.command.TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    /** Atomically reloads config.yml and every token definition. */
    public void reloadPlugin() {
        reloadConfig();
        tokenRegistry.load();
        for (Player player : Bukkit.getOnlinePlayers()) {
            passiveManager.applyPassives(player);
        }
    }

    // ------------------------------------------------------------------
    // Version-safe lookups (1.21 - 1.21.4+)
    // ------------------------------------------------------------------

    private Attribute resolveMaxHealthAttribute() {
        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
        if (attribute == null) {
            attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
        }
        return attribute;
    }

    private PotionEffectType resolveResistance() {
        for (String fieldName : new String[]{"RESISTANCE", "DAMAGE_RESISTANCE"}) {
            try {
                return (PotionEffectType) PotionEffectType.class.getField(fieldName).get(null);
            } catch (ReflectiveOperationException ignored) {
                // try the next known constant name
            }
        }
        return null;
    }

    /** Resolves a potion effect by config name, across all 1.21.x constant names. */
    public PotionEffectType resolvePotion(String name) {
        return switch (name.toUpperCase()) {
            case "STRENGTH" -> PotionEffectType.STRENGTH;
            case "SPEED" -> PotionEffectType.SPEED;
            case "NIGHT_VISION" -> PotionEffectType.NIGHT_VISION;
            case "REGENERATION" -> PotionEffectType.REGENERATION;
            case "FIRE_RESISTANCE" -> PotionEffectType.FIRE_RESISTANCE;
            case "JUMP_BOOST", "JUMP" -> PotionEffectType.JUMP_BOOST;
            case "ABSORPTION" -> PotionEffectType.ABSORPTION;
            case "RESISTANCE", "DAMAGE_RESISTANCE" -> resistanceEffectType;
            default -> null;
        };
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public TokenRegistry getRegistry() { return tokenRegistry; }
    public TokenDataManager getData() { return dataManager; }
    public PassiveManager getPassiveManager() { return passiveManager; }
    public FreezeManager getFreezeManager() { return freezeManager; }
    public SelectionMenu getSelectionMenu() { return selectionMenu; }
    public UpgradeMenu getUpgradeMenu() { return upgradeMenu; }
    public ChatNotifications getNotifications() { return notifications; }
    public Attribute getMaxHealthAttribute() { return maxHealthAttribute; }
    public NamespacedKey getTokenItemKey() { return tokenItemKey; }

    /** Builds the configured progress bar string, e.g. "§a██████§7░░░░". */
    public String buildProgressBar(long current, long required) {
        int length = getConfig().getInt("settings.progress-bar.length", 10);
        String filled = getConfig().getString("settings.progress-bar.filled-char", "█");
        String empty = getConfig().getString("settings.progress-bar.empty-char", "░");
        String filledColor = getConfig().getString("settings.progress-bar.filled-color", "§a");
        String emptyColor = getConfig().getString("settings.progress-bar.empty-color", "§7");
        double ratio = required <= 0 ? 1.0 : Math.max(0.0, Math.min(1.0, (double) current / required));
        int filledCount = (int) Math.round(ratio * length);
        return filledColor + filled.repeat(Math.max(0, filledCount)) + emptyColor
                + empty.repeat(Math.max(0, length - filledCount));
    }

    /** Sends a prefixed chat message to a player. */
    public void sendMessage(org.bukkit.command.CommandSender sender, String message) {
        String prefix = getConfig().getString("settings.prefix", "§8§l[§6§lTokenSMP§8§l]§r ");
        sender.sendMessage(prefix + message);
    }
}
