package com.amantrivedi.tokensmp.commands;

import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /tokens - opens the main token selection GUI.
 * Permission: tokensmp.player (default: everyone).
 */
public final class TokensCommand implements CommandExecutor, TabCompleter {

    private final TokenSmpPlugin plugin;

    public TokensCommand(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokensmp.player")) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission-player",
                    "§c[TokenSMP] You do not have permission to use this command."));
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.players-only",
                    "§cThis command can only be used by players."));
            return true;
        }
        plugin.getSelectionMenu().open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
