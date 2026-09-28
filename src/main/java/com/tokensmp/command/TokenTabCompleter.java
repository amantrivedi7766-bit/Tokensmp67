package com.tokensmp.command;

import com.tokensmp.TokenSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Contextual tab completion with strict admin isolation: unauthorized
 * players never see admin command names, admin token ids or any admin
 * completion at all.
 */
public final class TokenTabCompleter implements TabCompleter {

    private static final List<String> PLAYER_SUBS = List.of("menu", "balance", "stats");
    private static final List<String> ADMIN_SUBS = List.of(
            "give", "spin", "setprogress", "forceupgrade", "resetcooldown",
            "claim", "unclaim", "remove", "inspect", "reload");

    private final TokenSMP plugin;

    public TokenTabCompleter(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (name.equals("token")) {
            if (args.length == 1) {
                return prefixMatch(args[0], PLAYER_SUBS);
            }
            return List.of();
        }
        if (name.equals("tokensadmin")) {
            // Absolute isolation: nothing leaks to unauthorized players.
            if (!sender.hasPermission("tokensmp.admin")) {
                return List.of();
            }
            if (args.length == 1) {
                return prefixMatch(args[0], ADMIN_SUBS);
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (args.length == 2) {
                if (sub.equals("spin") || sub.equals("resetcooldown") || sub.equals("forceupgrade")
                        || sub.equals("setprogress") || sub.equals("give")
                        || sub.equals("claim") || sub.equals("remove") || sub.equals("unclaim")
                        || sub.equals("inspect")) {
                    List<String> names = new ArrayList<>();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.getName().toLowerCase(Locale.ROOT)
                                .startsWith(args[1].toLowerCase(Locale.ROOT))) {
                            names.add(online.getName());
                        }
                    }
                    return names;
                }
                return List.of();
            }
            if (args.length == 3 && (sub.equals("give") || sub.equals("setprogress")
                    || sub.equals("forceupgrade") || sub.equals("claim") || sub.equals("remove"))) {
                // Full token list including the isolated admin token -
                // only visible to authorized admins.
                List<String> ids = new ArrayList<>();
                for (String id : plugin.registry().getIds()) {
                    if (id.startsWith(args[2].toLowerCase(Locale.ROOT))) {
                        ids.add(id);
                    }
                }
                return ids;
            }
            return List.of();
        }
        return List.of();
    }

    private List<String> prefixMatch(String input, List<String> options) {
        List<String> out = new ArrayList<>();
        String lowered = input.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.startsWith(lowered)) {
                out.add(option);
            }
        }
        return out;
    }
}
