package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Central message service: prefix, configurable message templates with
 * {placeholders}, action bar, titles, and global broadcasts.
 */
public final class MessageManager {

    private final TokenSMP plugin;
    private final ConfigManager config;

    public MessageManager(TokenSMP plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public String prefix() {
        return config.getString("messages.prefix", "&8[&6TokenSMP&8] &r");
    }

    /** Reads a message template and applies {key} placeholder pairs. */
    public String msg(String key, String def, String... placeholders) {
        String message = config.rawString(key, def);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return com.tokensmp.util.ColorUtil.color(message);
    }

    /** Prefixed chat message to any sender. */
    public void send(CommandSender sender, String key, String def, String... placeholders) {
        if (sender != null) {
            sender.sendMessage(prefix() + msg(key, def, placeholders));
        }
    }

    /** Prefixed chat message to a player (null-safe). */
    public void send(Player player, String key, String def, String... placeholders) {
        if (player != null) {
            player.sendMessage(prefix() + msg(key, def, placeholders));
        }
    }

    /** Unprefixed raw line to any sender. */
    public void raw(CommandSender sender, String text) {
        if (sender != null) {
            sender.sendMessage(com.tokensmp.util.ColorUtil.color(text));
        }
    }

    /** Action bar message. */
    public void actionBar(Player player, String text) {
        if (player != null && text != null && !text.isEmpty()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(com.tokensmp.util.ColorUtil.color(text)));
        }
    }

    /** Title + subtitle. */
    public void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {
            player.sendTitle(com.tokensmp.util.ColorUtil.color(title),
                    com.tokensmp.util.ColorUtil.color(subtitle), fadeIn, stay, fadeOut);
        }
    }

    /** Global broadcast of a message template. */
    public void broadcast(String key, String def, String... placeholders) {
        String message = prefix() + msg(key, def, placeholders);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    /** Global broadcast of an already-built colored line. */
    public void broadcastLine(String line) {
        String message = com.tokensmp.util.ColorUtil.color(line);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    /** Debug log line - only printed when plugin.debug is true. */
    public void debug(String message) {
        if (config.getBoolean("plugin.debug", false)) {
            plugin.getLogger().info("[debug] " + message);
        }
    }

    /** Silent admin alert (console + every tokensmp.admin holder). */
    public void alertAdmins(String key, String def, String... placeholders) {
        String message = prefix() + msg(key, def, placeholders);
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tokensmp.admin")) {
                player.sendMessage(message);
            }
        }
    }
}
