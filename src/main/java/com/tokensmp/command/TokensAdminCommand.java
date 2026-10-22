package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tokensadmin - the strictly isolated administrative command registry.
 * No arguments opens the Admin GUI (players only). The exact unauthorized
 * error never leaks admin command names or admin token information.
 */
public final class TokensAdminCommand implements CommandExecutor {

    private final TokenSMP plugin;

    public TokensAdminCommand(TokenSMP plugin) {
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
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.adminGUI().openMain(player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "spin" -> handleSpin(sender, args);
            case "spinall" -> handleSpinAll(sender);
            case "respin" -> handleRespin(sender, args);
            case "grace" -> handleGrace(sender, args);
            case "who" -> handleWho(sender, args);
            case "setprogress" -> handleSetProgress(sender, args);
            case "forceupgrade" -> handleForceUpgrade(sender, args);
            case "resetcooldown" -> handleResetCooldown(sender, args);
            case "reload" -> {
                plugin.reloadPlugin();
                plugin.messages().send(sender, "messages.config-reloaded",
                        "&aConfiguration reloaded atomically (no reboot required).");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin give <player> <token_id>");
        if (target == null) {
            return;
        }
        com.tokensmp.token.Token token = requireToken(sender, args, 2, "/tokensadmin give <player> <token_id>");
        if (token == null) {
            return;
        }
        int tier = Math.max(1, plugin.data().getTier(target, token.getId()));
        plugin.tokenItems().ensureItem(target, token, tier);
        logAction(sender.getName() + " gave the " + token.getDisplayName() + " Token to " + target.getName());
        plugin.messages().send(sender, "messages.admin-gave",
                "&aGave the {color}&l{token} Token &ato &f{player}&a.",
                "{color}", token.getRarity().getColorCode(),
                "{token}", token.getDisplayName(),
                "{player}", target.getName());
        plugin.messages().send(target, "messages.token-received",
                "&eYou received the {color}&l{token} Token &eitem! Right-click it to claim.",
                "{color}", token.getRarity().getColorCode(),
                "{token}", token.getDisplayName());
        if (token.isAdminToken()) {
            String alert = plugin.messages().msg("messages.admin-token-alert",
                    "&4&l[ADMIN ALERT] &cOperator &f{admin} &chas generated an &e&lOriginal Admin Token &cfor &f{target}&c.",
                    "{admin}", sender.getName(), "{target}", target.getName());
            if (plugin.config().getBoolean("admin.give-alert-broadcast", true)) {
                plugin.messages().broadcastLine(alert);
            } else {
                plugin.messages().alertAdmins("messages.admin-token-alert",
                        "&4&l[ADMIN ALERT] &cOperator &f{admin} &chas generated an &e&lOriginal Admin Token &cfor &f{target}&c.",
                        "{admin}", sender.getName(), "{target}", target.getName());
            }
        }
    }

    /** Mirrors an administrative action to the console when admin.log-actions is on. */
    private void logAction(String message) {
        if (plugin.config().getBoolean("admin.log-actions", true)) {
            plugin.getLogger().info("[admin] " + com.tokensmp.util.ColorUtil.strip(message));
        }
    }

    private void handleSpin(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin spin <player>");
        if (target == null) {
            return;
        }
        plugin.spinLauncher().launchFor(target);
        logAction(sender.getName() + " launched the token spin for " + target.getName());
        plugin.messages().send(sender, "messages.admin-force-spin",
                "&eLaunched the token spin for &f{player}&e.", "{player}", target.getName());
    }

    private void handleSetProgress(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin setprogress <player> <task_id> <value>");
        if (target == null) {
            return;
        }
        com.tokensmp.token.Token token = requireToken(sender, args, 2,
                "/tokensadmin setprogress <player> <task_id> <value>");
        if (token == null) {
            return;
        }
        if (args.length < 4) {
            plugin.messages().send(sender, "messages.admin-usage",
                    "&eUsage: /tokensadmin setprogress <player> <task_id> <value>");
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            plugin.messages().send(sender, "messages.admin-invalid-number",
                    "&c'{value}' is not a valid whole number.", "{value}", args[3]);
            return;
        }
        plugin.data().setProgress(target, token.getId(), value);
        plugin.messages().send(sender, "messages.progress-set",
                "&aSet &f{token} &atask progress of &f{player} &ato &f{value}&a.",
                "{token}", token.getDisplayName(),
                "{player}", target.getName(),
                "{value}", String.valueOf(value));
    }

    private void handleForceUpgrade(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin forceupgrade <player> <token_id>");
        if (target == null) {
            return;
        }
        com.tokensmp.token.Token token = requireToken(sender, args, 2,
                "/tokensadmin forceupgrade <player> <token_id>");
        if (token == null) {
            return;
        }
        int tier = plugin.data().getTier(target, token.getId());
        if (tier >= token.getMaxTier()) {
            plugin.messages().send(sender, "messages.already-maxed",
                    "&dThis token is already at MAX TIER!");
            return;
        }
        int newTier = tier + 1;
        plugin.data().setTier(target, token.getId(), newTier);
        plugin.data().setProgress(target, token.getId(), 0);
        plugin.data().claim(target, token.getId());
        plugin.passiveManager().applyPassives(target);
        plugin.messages().send(sender, "messages.admin-upgraded",
                "&aForced {token} Token &aof &f{player} &ato Tier &f{tier}&a.",
                "{token}", token.getDisplayName(),
                "{player}", target.getName(),
                "{tier}", String.valueOf(newTier));
    }

    private void handleResetCooldown(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin resetcooldown <player>");
        if (target == null) {
            return;
        }
        plugin.cooldowns().clearAll(target);
        plugin.messages().send(sender, "messages.admin-cooldowns-reset",
                "&aCleared all cooldowns for &f{player}&a.", "{player}", target.getName());
        plugin.messages().send(target, "messages.cooldowns-reset-target",
                "&aAll your token ability cooldowns have been reset!");
    }

    /** Spins a token for every online player. */
    private void handleSpinAll(CommandSender sender) {
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.spinLauncher().launchFor(online);
            count++;
        }
        plugin.messages().send(sender, "messages.admin-spin-all",
                "&aStarted a token spin for &f{count} &aonline player(s).",
                "{count}", String.valueOf(count));
        logAction(sender.getName() + " started a token spin for all players (" + count + ")");
    }

    /** Restarts (re-runs) the token spin for one player. */
    private void handleRespin(CommandSender sender, String[] args) {
        Player target = requirePlayer(sender, args, "/tokensadmin respin <player>");
        if (target == null) {
            return;
        }
        plugin.data().resetFirstJoin(target);
        plugin.spinLauncher().launchFor(target);
        plugin.messages().send(sender, "messages.admin-respin",
                "&aRestarted the token spin for &f{player}&a.", "{player}", target.getName());
        logAction(sender.getName() + " restarted the token spin for " + target.getName());
    }

    /** /tokensadmin grace <start [minutes]|stop|status> */
    private void handleGrace(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.messages().send(sender, "messages.admin-usage",
                    "&eUsage: /tokensadmin grace <start [minutes]|stop|status>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "start" -> {
                long minutes = plugin.config().getLong("grace-period.default-minutes", 30L);
                if (args.length >= 3) {
                    try {
                        minutes = Math.max(1L, Long.parseLong(args[2]));
                    } catch (NumberFormatException ignored) {
                        // keep the configured default
                    }
                }
                plugin.grace().start(minutes * 60L);
                plugin.messages().send(sender, "messages.admin-grace-started",
                        "&aGrace period started for &f{minutes} &aminute(s).",
                        "{minutes}", String.valueOf(minutes));
                logAction(sender.getName() + " started a grace period for " + minutes + " minute(s)");
            }
            case "stop" -> {
                plugin.grace().stop();
                plugin.messages().send(sender, "messages.admin-grace-stopped",
                        "&aGrace period ended.");
                logAction(sender.getName() + " ended the grace period");
            }
            case "status" -> {
                if (plugin.grace().isActive()) {
                    plugin.messages().send(sender, "messages.admin-grace-status-active",
                            "&aGrace period is ACTIVE - &f{seconds}s &aremaining.",
                            "{seconds}", String.valueOf(plugin.grace().remainingSeconds()));
                } else {
                    plugin.messages().send(sender, "messages.admin-grace-status-inactive",
                            "&7No grace period is running.");
                }
            }
            default -> plugin.messages().send(sender, "messages.admin-usage",
                    "&eUsage: /tokensadmin grace <start [minutes]|stop|status>");
        }
    }

    /** Shows who holds which token (one player, or every online player). */
    private void handleWho(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.messages().send(sender, "messages.player-not-found",
                        "&cPlayer &f{player} &cis not online.", "{player}", args[1]);
                return;
            }
            plugin.messages().send(sender, "messages.admin-who-header",
                    "&6&lTokens &8» &f{player}", "{player}", target.getName());
            describeTokens(sender, target);
            return;
        }
        plugin.messages().send(sender, "messages.admin-who-header", "&6&lToken Ownership &8» &7online players");
        for (Player online : Bukkit.getOnlinePlayers()) {
            describeTokens(sender, online);
        }
    }

    private void describeTokens(CommandSender sender, Player target) {
        String active = plugin.data().getActiveToken(target);
        int owned = 0;
        StringBuilder unlocked = new StringBuilder();
        for (com.tokensmp.token.Token token : plugin.registry().playerTokens()) {
            if (!plugin.data().hasUnlocked(target, token.getId())) {
                continue;
            }
            owned++;
            boolean isActive = token.getId().equals(active);
            unlocked.append(isActive ? " &a" : " &7").append(token.getDisplayName())
                    .append(isActive ? " (T" + plugin.data().getTier(target, token.getId()) + ", ACTIVE)" : "");
        }
        sender.sendMessage(com.tokensmp.util.ColorUtil.color("&8» &f" + target.getName()
                + " &7- active: " + (active == null ? "&cnone"
                : "&e" + active + " &7(T" + plugin.data().getTier(target, active) + ")")
                + " &7| owned: &f" + owned + "/" + plugin.registry().playerTokens().size()));
        if (owned > 0) {
            sender.sendMessage(com.tokensmp.util.ColorUtil.color("   " + unlocked));
        }
    }

    private Player requirePlayer(CommandSender sender, String[] args, String usage) {
        if (args.length < 2) {
            plugin.messages().send(sender, "messages.admin-usage", "&eUsage: " + usage);
            return null;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(sender, "messages.player-not-found",
                    "&cPlayer &f{player} &cis not online.", "{player}", args[1]);
        }
        return target;
    }

    private com.tokensmp.token.Token requireToken(CommandSender sender, String[] args, int index, String usage) {
        if (args.length <= index) {
            plugin.messages().send(sender, "messages.admin-usage", "&eUsage: " + usage);
            return null;
        }
        com.tokensmp.token.Token token = plugin.registry().get(args[index]);
        if (token == null) {
            plugin.messages().send(sender, "messages.unknown-token",
                    "&cUnknown token id: &f{token}", "{token}", args[index]);
        }
        return token;
    }

    private void sendHelp(CommandSender sender) {
        plugin.messages().send(sender, "messages.admin-help-header", "&6&lTokenSMP Admin &8» &7/tokensadmin");
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin &7- open the admin GUI"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin give <player> <token_id> &7- spawn a token item"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin spin <player> &7- launch the token spin"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin spinall &7- spin a token for every online player"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin respin <player> &7- restart the spin for a player"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin who [player] &7- see who holds which token"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin grace <start [min]|stop|status> &7- grace period"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin setprogress <player> <task_id> <value> &7- force task progress"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin forceupgrade <player> <token_id> &7- skip to the next tier"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin resetcooldown <player> &7- clear all cooldowns"));
        sender.sendMessage(com.tokensmp.util.ColorUtil.color(
                "&8- &f/tokensadmin reload &7- reload config.yml"));
    }
}
