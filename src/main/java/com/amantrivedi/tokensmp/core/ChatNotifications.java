package com.amantrivedi.tokensmp.core;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * High-visibility chat announcements for milestone actions, all fully configurable.
 */
public final class ChatNotifications {

    private final TokenSmpPlugin plugin;

    public ChatNotifications(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** Broadcast on a player's first token unlock (crate spin stop). */
    public void broadcastFirstUnlock(Player player, TokenDefinition token) {
        String message = plugin.getConfig().getString("announcements.first-unlock",
                "§8§l[§6§lTokenSMP§8§l] §fPlayer §b{player} §fhas just unlocked the {color}§l{name} Token §ffor the first time! 🎉");
        if (message == null) {
            return;
        }
        Bukkit.broadcastMessage(message
                .replace("{player}", player.getName())
                .replace("{color}", token.getRarityColor())
                .replace("{name}", token.getName()));
    }

    /** Broadcast on an upgrade to Tier 2 or Tier 3. */
    public void broadcastUpgrade(Player player, TokenDefinition token, int newTier) {
        String message = plugin.getConfig().getString("announcements.upgrade",
                "§8§l[§6§lTokenSMP§8§l] §d§lUPGRADE! §b{player} §fhas successfully upgraded their {color}§l{name} Token §fto §e§lTier {tier}! 🚀");
        if (message == null) {
            return;
        }
        Bukkit.broadcastMessage(message
                .replace("{player}", player.getName())
                .replace("{color}", token.getRarityColor())
                .replace("{name}", token.getName())
                .replace("{tier}", String.valueOf(newTier)));
    }

    /** Silent alert to all online admins when an admin token is generated. */
    public void alertAdmins(CommandSender admin, String targetName, TokenDefinition token) {
        String message = plugin.getConfig().getString("announcements.admin-alert",
                "§4§l[ADMIN ALERT] §cOperator §f{admin} §chas generated an §e§lOriginal Admin Token §cfor §f{target}§c.");
        if (message == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("tokensmp.admin")) {
                online.sendMessage(message
                        .replace("{admin}", admin.getName())
                        .replace("{target}", targetName)
                        .replace("{name}", token.getName()));
            }
        }
    }
}
