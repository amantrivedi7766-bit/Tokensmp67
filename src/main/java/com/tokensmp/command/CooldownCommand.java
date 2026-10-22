package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /cooldown <player> - admin toggle: enables or disables every ability
 * cooldown for that player (same as /tokensadmin togglecooldown).
 */
public final class CooldownCommand implements CommandExecutor {

    private final TokenSMP plugin;

    public CooldownCommand(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean admin = sender.hasPermission("tokensmp.admin")
                || (plugin.config().getBoolean("permissions.op-implies-admin", true) && sender.isOp());
        if (!admin) {
            sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                    plugin.config().getString("messages.no-permission-admin",
                            "&c[TokenSMP] You do not have permission to execute this administrative command.")));
            return true;
        }
        if (args.length < 1) {
            plugin.messages().send(sender, "messages.admin-usage",
                    "&eUsage: /cooldown <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            plugin.messages().send(sender, "messages.player-not-found",
                    "&cPlayer &f{player} &cis not online.", "{player}", args[0]);
            return true;
        }
        boolean nowDisabled = !plugin.cooldowns().isDisabled(target);
        plugin.cooldowns().setDisabled(target, nowDisabled);
        plugin.messages().send(sender, "messages.admin-cooldown-toggled",
                "&aCooldowns for &f{player} &aare now &f{state}&a.",
                "{player}", target.getName(),
                "{state}", nowDisabled ? "DISABLED" : "ENABLED");
        plugin.messages().send(target, "messages.cooldown-toggled-target",
                "&7An admin set your ability cooldowns to &f{state}&7.",
                "{state}", nowDisabled ? "DISABLED" : "ENABLED");
        return true;
    }
}
