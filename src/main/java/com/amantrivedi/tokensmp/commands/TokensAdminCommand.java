package com.amantrivedi.tokensmp.commands;

import com.amantrivedi.tokensmp.abilities.TokenItemListener;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /tokensadmin - the strictly isolated administrative command registry with
 * context-aware tab completion and permission enforcement.
 */
public final class TokensAdminCommand implements CommandExecutor, TabCompleter {

    private final TokenSmpPlugin plugin;

    public TokensAdminCommand(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokensmp.admin")) {
            // Strict admin isolation error.
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission-admin",
                    "§c[TokenSMP] You do not have permission to execute this administrative command."));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> handleGive(sender, args);
            case "setprogress" -> handleSetProgress(sender, args);
            case "forceupgrade" -> handleForceUpgrade(sender, args);
            case "resetcooldown" -> handleResetCooldown(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // give <player> <token_id>
    // ------------------------------------------------------------------

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, "§eUsage: /tokensadmin give <player> <token_id>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.player-not-found",
                    "§cPlayer §f{player} §cis not online.").replace("{player}", args[1]));
            return;
        }
        TokenDefinition token = plugin.getRegistry().get(args[2]);
        if (token == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.unknown-token",
                    "§cUnknown token id: §f{token}").replace("{token}", args[2]));
            return;
        }

        ItemStack item = new TokenItemListener(plugin).createTokenItem(token);
        target.getInventory().addItem(item).values()
                .forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        plugin.sendMessage(sender, plugin.getConfig().getString("messages.token-given",
                "§aGave the {color}§l{name} Token §aitem to §f{player}§a.")
                .replace("{color}", token.getRarityColor())
                .replace("{name}", token.getName())
                .replace("{player}", target.getName()));
        plugin.sendMessage(target, plugin.getConfig().getString("messages.token-received",
                "§eYou received the {color}§l{name} Token §eitem! Right-click it to unlock.")
                .replace("{color}", token.getRarityColor())
                .replace("{name}", token.getName()));

        // Silent admin alert for admin-token generation (and every give).
        plugin.getNotifications().alertAdmins(sender, target.getName(), token);
    }

    // ------------------------------------------------------------------
    // setprogress <player> <task_id> <value>
    // ------------------------------------------------------------------

    private void handleSetProgress(CommandSender sender, String[] args) {
        if (args.length < 4) {
            plugin.sendMessage(sender, "§eUsage: /tokensadmin setprogress <player> <task_id> <value>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.player-not-found",
                    "§cPlayer §f{player} §cis not online.").replace("{player}", args[1]));
            return;
        }
        TokenDefinition token = plugin.getRegistry().get(args[2]);
        if (token == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.unknown-token",
                    "§cUnknown token id: §f{token}").replace("{token}", args[2]));
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            plugin.sendMessage(sender, "§c'" + args[3] + "' is not a valid whole number.");
            return;
        }
        plugin.getData().setProgress(target, token.getId(), value);
        plugin.sendMessage(sender, plugin.getConfig().getString("messages.progress-set",
                        "§aSet §f{token} §atask progress of §f{player} §ato §f{value}§a.")
                .replace("{token}", token.getName())
                .replace("{player}", target.getName())
                .replace("{value}", String.valueOf(value)));
    }

    // ------------------------------------------------------------------
    // forceupgrade <player> <token_id>
    // ------------------------------------------------------------------

    private void handleForceUpgrade(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.sendMessage(sender, "§eUsage: /tokensadmin forceupgrade <player> <token_id>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.player-not-found",
                    "§cPlayer §f{player} §cis not online.").replace("{player}", args[1]));
            return;
        }
        TokenDefinition token = plugin.getRegistry().get(args[2]);
        if (token == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.unknown-token",
                    "§cUnknown token id: §f{token}").replace("{token}", args[2]));
            return;
        }
        int tier = plugin.getData().getTier(target, token.getId());
        if (tier >= 3) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.already-maxed",
                    "§dThis token is already at MAX TIER!"));
            return;
        }
        int newTier = tier + 1;
        plugin.getData().setTier(target, token.getId(), newTier);
        plugin.getData().setProgress(target, token.getId(), 0);
        plugin.sendMessage(sender, plugin.getConfig().getString("messages.force-upgraded",
                        "§aForced §f{token} §aof §f{player} §ato Tier §f{tier}§a.")
                .replace("{token}", token.getName())
                .replace("{player}", target.getName())
                .replace("{tier}", String.valueOf(newTier)));
        if (newTier == 1) {
            plugin.getNotifications().broadcastFirstUnlock(target, token);
        } else {
            plugin.getNotifications().broadcastUpgrade(target, token, newTier);
        }
        plugin.getData().setActiveToken(target, token.getId());
        plugin.getPassiveManager().applyPassives(target);
    }

    // ------------------------------------------------------------------
    // resetcooldown <player>
    // ------------------------------------------------------------------

    private void handleResetCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, "§eUsage: /tokensadmin resetcooldown <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.player-not-found",
                    "§cPlayer §f{player} §cis not online.").replace("{player}", args[1]));
            return;
        }
        plugin.getData().clearAllCooldowns(target, plugin.getRegistry().getIds());
        plugin.sendMessage(sender, plugin.getConfig().getString("messages.cooldowns-reset",
                "§aCleared all token cooldowns for §f{player}§a.").replace("{player}", target.getName()));
        plugin.sendMessage(target, plugin.getConfig().getString("messages.cooldowns-reset-target",
                "§aAll your token ability cooldowns have been reset!"));
    }

    // ------------------------------------------------------------------
    // reload
    // ------------------------------------------------------------------

    private void handleReload(CommandSender sender) {
        plugin.reloadPlugin();
        plugin.sendMessage(sender, plugin.getConfig().getString("messages.config-reloaded",
                "§aConfiguration reloaded atomically (no reboot required)."));
    }

    // ------------------------------------------------------------------
    // Help + tab completion
    // ------------------------------------------------------------------

    private void sendHelp(CommandSender sender) {
        plugin.sendMessage(sender, "§6§lTokenSMP Admin §8» §7/tokensadmin");
        sender.sendMessage("§8- §f/tokensadmin give <player> <token_id> §7- spawn a Tier 1 token item");
        sender.sendMessage("§8- §f/tokensadmin setprogress <player> <task_id> <value> §7- force task progress");
        sender.sendMessage("§8- §f/tokensadmin forceupgrade <player> <token_id> §7- skip to the next tier");
        sender.sendMessage("§8- §f/tokensadmin resetcooldown <player> §7- clear all cooldowns");
        sender.sendMessage("§8- §f/tokensadmin reload §7- reload config.yml");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("tokensmp.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("give", "setprogress", "forceupgrade", "resetcooldown", "reload");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        boolean needsPlayer = sub.equals("give") || sub.equals("setprogress")
                || sub.equals("forceupgrade") || sub.equals("resetcooldown");
        if (args.length == 2 && needsPlayer) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    names.add(player.getName());
                }
            }
            return names;
        }
        if (args.length == 3 && (sub.equals("give") || sub.equals("setprogress") || sub.equals("forceupgrade"))) {
            List<String> ids = new ArrayList<>();
            for (String id : plugin.getRegistry().getIds()) {
                if (id.startsWith(args[2].toLowerCase(Locale.ROOT))) {
                    ids.add(id);
                }
            }
            return ids;
        }
        return List.of();
    }
}
