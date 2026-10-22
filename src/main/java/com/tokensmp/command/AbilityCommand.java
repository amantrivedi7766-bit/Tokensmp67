package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import com.tokensmp.token.Token;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ability activate <token> - activates (claims) one of your unlocked tokens,
 * making its ability the one you can use.
 * /ability cooldowns        - shows every remaining ability cooldown.
 */
public final class AbilityCommand implements CommandExecutor {

    private final TokenSMP plugin;

    public AbilityCommand(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokensmp.player")) {
            sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                    plugin.config().getString("messages.no-permission-player",
                            "&c[TokenSMP] You do not have permission to use this command.")));
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "messages.players-only",
                    "&cThis command can only be used by players.");
            return true;
        }
        if (args.length == 0) {
            plugin.messages().send(player, "messages.ability-usage",
                    "&eUsage: /ability <activate <token>|cooldowns>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "activate" -> activate(player, args);
            case "cooldowns" -> cooldowns(player);
            default -> plugin.messages().send(player, "messages.ability-usage",
                    "&eUsage: /ability <activate <token>|cooldowns>");
        }
        return true;
    }

    private void activate(Player player, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(player, "messages.ability-usage",
                    "&eUsage: /ability activate <token>");
            return;
        }
        Token token = plugin.registry().get(args[1]);
        if (token == null || token.isAdminToken()) {
            plugin.messages().send(player, "messages.unknown-token",
                    "&cUnknown token id: &f{token}", "{token}", args[1]);
            return;
        }
        if (!plugin.data().hasUnlocked(player, token.getId())) {
            plugin.messages().send(player, "messages.token-not-unlocked",
                    "&cYou have not unlocked the {color}&l{token} Token &cyet!",
                    "{color}", token.getRarity().getColorCode(),
                    "{token}", token.getDisplayName());
            return;
        }
        // Activating claims the token (and deactivates any previous one) and
        // makes sure its item is in the inventory.
        plugin.data().claim(player, token.getId());
        plugin.tokenItems().ensureItem(player, token,
                Math.max(1, plugin.data().getTier(player, token.getId())));
        plugin.messages().send(player, "messages.ability-activated-token",
                "&aActivated the {color}&l{token} Token&a - hold its item and use your keybind!",
                "{color}", token.getRarity().getColorCode(),
                "{token}", token.getDisplayName());
    }

    private void cooldowns(Player player) {
        plugin.messages().send(player, "messages.cooldowns-header", "&6&l⏳ Ability Cooldowns:");
        boolean any = false;
        for (Token token : plugin.registry().playerTokens()) {
            if (!plugin.data().hasUnlocked(player, token.getId())) {
                continue;
            }
            long remaining = plugin.cooldowns().remainingMillis(player, token.getId());
            if (remaining <= 0L) {
                continue;
            }
            any = true;
            plugin.messages().raw(player, "&8- " + token.getRarity().getColorCode() + "&l"
                    + token.getDisplayName() + " Token &7- &c" + (remaining + 999L) / 1000L + "s");
        }
        if (!any) {
            plugin.messages().send(player, "messages.cooldowns-ready",
                    "&aAll your token abilities are ready to use!");
        }
    }
}
