package com.amantrivedi.tokensmp;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Command handler: processes /token (and its alias /tokensmp) - opens the menu,
 * handles the withdraw sub-command, argument parsing and validation.
 */
public final class TokenSmpCommand implements CommandExecutor, TabCompleter {

    private final TokenSmpPlugin plugin;

    public TokenSmpCommand(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThe Token SMP menu can only be opened by players.");
            return true;
        }

        if (args.length == 0) {
            new TokenMenu(plugin).open(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("withdraw")) {
            handleWithdraw(player, args);
            return true;
        }

        player.sendMessage("§cUnknown sub-command. Usage: §f/" + label + " §7or §f/" + label + " withdraw <amount>");
        return true;
    }

    private void handleWithdraw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /token withdraw <amount>");
            return;
        }

        final int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage("§c'" + args[1] + "' is not a valid whole number.");
            return;
        }
        if (amount <= 0) {
            player.sendMessage("§cYou must withdraw at least 1 heart.");
            return;
        }

        PlayerData data = plugin.getData(player);
        int availableHearts = (int) Math.floor(data.getExtraHeartHp() / 2.0);
        if (amount > availableHearts) {
            player.sendMessage("§cYou only have §f" + availableHearts + "§c withdrawable heart(s) stacked.");
            return;
        }

        // Each heart is worth 2.0 max-health points beyond the unalterable 20.0 base.
        data.setExtraHeartHp(data.getExtraHeartHp() - (amount * 2.0));
        AttributeInstance maxHealth = player.getAttribute(plugin.getMaxHealthAttribute());
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0 + data.getExtraHeartHp());
        }

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(plugin.createHeartItem(amount));
        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        player.sendMessage("§aWithdrew §f" + amount + "§a heart(s). §7Right-click the Extra Heart item to re-deposit it.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("withdraw");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("withdraw")) {
            return List.of("1", "5", "10");
        }
        return List.of();
    }
}
