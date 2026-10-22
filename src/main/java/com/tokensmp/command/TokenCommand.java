package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.LifeManager;
import com.tokensmp.data.PlayerTokenData;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ProgressBar;
import com.tokensmp.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The player command hub.
 *
 * /token [menu|balance|list|stats|trust|untrust|revive]   - the hub
 * /tokens | /tokengui                                     - opens the GUI
 * /bal                                                    - balance shortcut
 */
public final class TokenCommand implements CommandExecutor {

    public enum Mode { HUB, GUI, BALANCE }

    private final TokenSMP plugin;
    private final Mode mode;

    public TokenCommand(TokenSMP plugin) {
        this(plugin, Mode.HUB);
    }

    public TokenCommand(TokenSMP plugin, Mode mode) {
        this.plugin = plugin;
        this.mode = mode;
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

        // /bal always shows the balance; /tokens + /tokengui open the GUI.
        if (mode == Mode.BALANCE) {
            showBalance(player);
            return true;
        }
        String sub = args.length == 0 ? (mode == Mode.GUI ? "menu" : "menu") : args[0].toLowerCase();
        switch (sub) {
            case "menu", "gui" -> plugin.selectionGUI().open(player);
            case "balance", "bal" -> showBalance(player);
            case "list" -> showList(player);
            case "stats" -> showStats(player);
            case "trust" -> trust(player, args, true);
            case "untrust" -> trust(player, args, false);
            case "revive" -> revive(player, args);
            default -> plugin.messages().send(player, "messages.token-usage",
                    "&eUsage: &f/token <menu|balance|list|stats|trust|untrust|revive>");
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Balance: tokens owned + progress
    // ------------------------------------------------------------------
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

    // ------------------------------------------------------------------
    // List: every token + unlocked status
    // ------------------------------------------------------------------
    private void showList(Player player) {
        plugin.messages().send(player, "messages.list-header", "&6&l📜 All Tokens:");
        for (Token token : plugin.registry().playerTokens()) {
            boolean unlocked = plugin.data().hasUnlocked(player, token.getId());
            plugin.messages().raw(player, "&8- " + token.getRarity().getColorCode() + "&l"
                    + token.getDisplayName() + " &7[" + token.getRarity().getDisplayName() + "&7] "
                    + (unlocked ? "&a✔ Unlocked" : "&c✘ Locked"));
        }
    }

    // ------------------------------------------------------------------
    // Stats: mob kills + collection detail
    // ------------------------------------------------------------------
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
            String bar = plugin.config().getBoolean("commands.stats-show-progress-bar", true)
                    ? " &7[" + ProgressBar.bar(progress, task.count(), 12, "&a", "&8") + "&r&7]"
                    : "";
            plugin.messages().raw(player, plugin.config().getString("messages.stats-task",
                            "    &7- Task: &f{task}{bar} &f{current}/{required}")
                    .replace("{task}", task.description())
                    .replace("{bar}", bar)
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

    // ------------------------------------------------------------------
    // Trust / untrust
    // ------------------------------------------------------------------
    private void trust(Player player, String[] args, boolean add) {
        if (args.length < 2) {
            plugin.messages().send(player, "messages.token-usage",
                    "&eUsage: /token " + (add ? "trust" : "untrust") + " <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.messages().send(player, "messages.player-not-found",
                    "&cPlayer &f{player} &cis not online.", "{player}", args[1]);
            return;
        }
        if (target.equals(player)) {
            plugin.messages().send(player, "messages.trust-self",
                    "&7You cannot trust yourself.");
            return;
        }
        if (add) {
            plugin.trusts().trust(player, target.getUniqueId());
            plugin.messages().send(player, "messages.trust-added",
                    "&aYou now trust &f{player}&a - your abilities will never hurt them.",
                    "{player}", target.getName());
        } else {
            plugin.trusts().untrust(player, target.getUniqueId());
            plugin.messages().send(player, "messages.trust-removed",
                    "&cYou no longer trust &f{player}&c.", "{player}", target.getName());
        }
    }

    // ------------------------------------------------------------------
    // Revive (command, no card needed)
    // ------------------------------------------------------------------
    private void revive(Player player, String[] args) {
        if (!plugin.lives().isEnabled()) {
            plugin.messages().send(player, "messages.lives-disabled",
                    "&7The life system is disabled on this server.");
            return;
        }
        if (args.length < 2) {
            plugin.messages().send(player, "messages.token-usage",
                    "&eUsage: /token revive <player>");
            return;
        }
        String name = args[1];
        LifeManager.Entry entry = null;
        for (LifeManager.Entry candidate : plugin.lives().eliminated()) {
            if (candidate.name().equalsIgnoreCase(name)) {
                entry = candidate;
                break;
            }
        }
        if (entry == null) {
            plugin.messages().send(player, "messages.revive-not-eliminated",
                    "&c{player} is not eliminated.", "{player}", name);
            return;
        }
        plugin.lives().revive(player, entry);
    }
}
