package com.amantrivedi.tokensmp.commands;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /token balance - prints all unlocked tokens directly in chat.
 * /token stats  - shows detailed live sub-task grinds and mob-kill counts.
 */
public final class TokenCommand implements CommandExecutor, TabCompleter {

    private final TokenSmpPlugin plugin;

    public TokenCommand(TokenSmpPlugin plugin) {
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

        String sub = args.length == 0 ? "" : args[0].toLowerCase();
        switch (sub) {
            case "balance" -> showBalance(player);
            case "stats" -> showStats(player);
            default -> plugin.sendMessage(player, plugin.getConfig().getString("messages.token-usage",
                    "§eUsage: §f/token balance §7or §f/token stats"));
        }
        return true;
    }

    /** Lists every unlocked token and its tier directly in chat. */
    private void showBalance(Player player) {
        String activeId = plugin.getData().getActiveToken(player);
        plugin.sendMessage(player, plugin.getConfig().getString("messages.balance-header",
                "§6§l🪙 Your Token Balance:"));
        for (TokenDefinition token : plugin.getRegistry().all()) {
            int tier = plugin.getData().getTier(player, token.getId());
            if (tier <= 0) {
                continue;
            }
            String activeMarker = token.getId().equals(activeId)
                    ? plugin.getConfig().getString("messages.balance-active-marker", " §a✔ ACTIVE") : "";
            String tierText = tier >= 3
                    ? plugin.getConfig().getString("messages.balance-max-marker", "§5§lMAX")
                    : "§eTier " + tier;
            player.sendMessage(plugin.getConfig().getString("messages.balance-entry",
                            "§8- {color}§l{name} Token §7({tier}){active}")
                    .replace("{color}", token.getRarityColor())
                    .replace("{name}", token.getName())
                    .replace("{tier}", tierText)
                    .replace("{active}", activeMarker));
        }
        if (plugin.getRegistry().all().stream().noneMatch(t -> plugin.getData().hasUnlocked(player, t.getId()))) {
            plugin.sendMessage(player, plugin.getConfig().getString("messages.balance-empty",
                    "§7You have not unlocked any tokens yet. Use §f/tokens §7to start grinding!"));
        }
    }

    /** Detailed live task grind overview for every token. */
    private void showStats(Player player) {
        plugin.sendMessage(player, plugin.getConfig().getString("messages.stats-header",
                "§6§l📊 Token Task Grinds:"));
        for (TokenDefinition token : plugin.getRegistry().all()) {
            int tier = plugin.getData().getTier(player, token.getId());
            TierDefinition next = token.tier(tier + 1);
            String head = token.getRarityColor() + "§l" + token.getName() + " Token §7("
                    + (tier >= 3 ? "§5MAX" : tier == 0 ? "§cLocked" : "§eTier " + tier) + "§7)";
            player.sendMessage("§8- " + head);
            if (next == null) {
                player.sendMessage(plugin.getConfig().getString("messages.stats-maxed",
                        "    §d🌟 This token is fully maxed out!"));
                continue;
            }
            TierDefinition.TaskSpec task = next.getTask();
            int progress = plugin.getData().getProgress(player, token.getId());
            player.sendMessage(plugin.getConfig().getString("messages.stats-task",
                            "    §7- Task: §f{task} §7[{bar}§r§7] §f{current}/{required}")
                    .replace("{task}", task.getDescription())
                    .replace("{bar}", plugin.buildProgressBar(progress, task.getCount()))
                    .replace("{current}", String.valueOf(progress))
                    .replace("{required}", String.valueOf(task.getCount())));
            List<String> mobs = new ArrayList<>();
            for (org.bukkit.entity.EntityType mob : task.getMobs()) {
                mobs.add(com.amantrivedi.tokensmp.ui.LoreBuilder.prettyName(mob.name()));
            }
            if (!mobs.isEmpty()) {
                player.sendMessage("    §7- Targets: §f" + String.join("§7, §f", mobs));
            }
            player.sendMessage("    §7- Materials: §f" + com.amantrivedi.tokensmp.ui.LoreBuilder.materialsList(next));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("balance", "stats");
        }
        return List.of();
    }
}
