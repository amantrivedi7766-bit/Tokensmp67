package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import com.tokensmp.data.PlayerTokenData;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ProgressBar;
import com.tokensmp.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /token - the player command hub.
 * No arguments (or "menu") opens the selection GUI; "balance" prints every
 * unlocked token in chat; "stats" shows the detailed live task grinds.
 */
public final class TokenCommand implements CommandExecutor {

    private final TokenSMP plugin;

    public TokenCommand(TokenSMP plugin) {
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

        String sub = args.length == 0 ? "menu" : args[0].toLowerCase();
        switch (sub) {
            case "menu", "gui" -> plugin.selectionGUI().open(player);
            case "balance" -> showBalance(player);
            case "stats" -> showStats(player);
            default -> plugin.messages().send(player, "messages.token-usage",
                    "&eUsage: &f/token menu&7, &f/token balance &7or &f/token stats");
        }
        return true;
    }

    private void showBalance(Player player) {
        plugin.messages().send(player, "messages.balance-header", "&6&l🪙 Your Token Balance:");
        boolean any = false;
        for (PlayerTokenData entry : plugin.data().getUnlocked(player, plugin.registry().playerTokens())) {
            any = true;
            Token token = plugin.registry().get(entry.getTokenId());
            String tierText = entry.getTier() >= token.getMaxTier() ? "&5&lMAX" : "&eTier " + entry.getTier();
            String marker = entry.isClaimed() ? plugin.config().getString(
                    "messages.balance-active-marker", " &a✔ ACTIVE") : "";
            plugin.messages().raw(player, plugin.config().getString("messages.balance-entry",
                            "&8- {color}&l{name} Token &7({tier}){active}")
                    .replace("{color}", token.getRarity().getColorCode())
                    .replace("{name}", token.getDisplayName())
                    .replace("{tier}", tierText)
                    .replace("{active}", marker));
        }
        if (!any) {
            plugin.messages().send(player, "messages.balance-empty",
                    "&7You have not unlocked any tokens yet. Use &f/tokens &7to start grinding!");
        }
    }

    private void showStats(Player player) {
        plugin.messages().send(player, "messages.stats-header", "&6&l📊 Token Task Grinds:");
        for (Token token : plugin.registry().playerTokens()) {
            int tier = plugin.data().getTier(player, token.getId());
            TokenTier next = token.tier(tier + 1);
            plugin.messages().raw(player, "&8- " + token.getRarity().getColorCode() + "&l"
                    + token.getDisplayName() + " Token &7("
                    + (tier >= token.getMaxTier() ? "&5MAX" : tier == 0 ? "&cLocked" : "&eTier " + tier)
                    + "&7)");
            if (next == null) {
                plugin.messages().raw(player, plugin.config().getString("messages.stats-maxed",
                        "    &d🌟 This token is fully maxed out!"));
                continue;
            }
            TokenTier.TaskSpec task = next.getTask();
            int progress = plugin.data().getProgress(player, token.getId());
            plugin.messages().raw(player, plugin.config().getString("messages.stats-task",
                            "    &7- Task: &f{task} &7[{bar}&r&7] &f{current}/{required}")
                    .replace("{task}", task.description())
                    .replace("{bar}", ProgressBar.bar(progress, task.count(), 12, "&a", "&8"))
                    .replace("{current}", String.valueOf(progress))
                    .replace("{required}", String.valueOf(task.count())));
            if (!task.mobs().isEmpty()) {
                StringBuilder mobs = new StringBuilder();
                for (org.bukkit.entity.EntityType mob : task.mobs()) {
                    if (mobs.length() > 0) {
                        mobs.append("&7, &f");
                    }
                    mobs.append(TextUtil.pretty(mob));
                }
                plugin.messages().raw(player, "    &7- Targets: &f" + mobs);
            }
            if (!next.getCost().isEmpty()) {
                plugin.messages().raw(player, "    &7- Materials: &f"
                        + com.tokensmp.gui.TokenSelectionGUI.materialsList(next));
            }
        }
    }
}
