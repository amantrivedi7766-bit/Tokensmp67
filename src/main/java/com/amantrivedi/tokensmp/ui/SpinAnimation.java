package com.amantrivedi.tokensmp.ui;

import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

/**
 * Crate wheel spin animation played on a first token unlock:
 * a 9-slot inventory with random token items that spin left-to-right,
 * slowing down over ~5 seconds, with click sounds on every shift.
 * Slot 4 holds the winner at the end, followed by a title, a firework
 * and the first-unlock broadcast.
 */
public final class SpinAnimation {

    /** Marker holder so other listeners ignore this inventory. */
    public static final class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static final Random RANDOM = new Random();

    private final TokenSmpPlugin plugin;

    public SpinAnimation(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the 5-second spin animation and then unlocks the token at the given
     * tier (announcements + effects included).
     */
    public void play(Player player, TokenDefinition token, int newTier) {
        String title = plugin.getConfig().getString("gui.spin.title", "§5§lUnlocking Token...");
        Inventory inventory = Bukkit.createInventory(new Holder(), 9, title);

        List<TokenDefinition> all = plugin.getRegistry().all();
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, randomTokenItem(all));
        }
        player.openInventory(inventory);

        // Shift periods: fast (2 ticks), then 5, 10 and 15 - ~5 seconds total.
        int fastShifts = plugin.getConfig().getInt("gui.spin.shifts-fast", 10);
        int mediumShifts = plugin.getConfig().getInt("gui.spin.shifts-medium", 5);
        int slowShifts = plugin.getConfig().getInt("gui.spin.shifts-slow", 3);
        int finalShifts = plugin.getConfig().getInt("gui.spin.shifts-final", 2);

        scheduleNextShift(player, inventory, all, token, newTier, fastShifts, 2L);
    }

    private void scheduleNextShift(Player player, Inventory inventory, List<TokenDefinition> all,
                                   TokenDefinition token, int newTier, int shiftsLeft, long period) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    finish(player, token, newTier);
                    return;
                }
                shiftRight(inventory, all);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                        0.5f, 1.8f);

                int remaining = shiftsLeft - 1;
                if (remaining > 0) {
                    scheduleNextShift(player, inventory, all, token, newTier, remaining, period);
                    return;
                }
                // Move to the next (slower) phase.
                if (period == 2L) {
                    scheduleNextShift(player, inventory, all, token, newTier,
                            plugin.getConfig().getInt("gui.spin.shifts-medium", 5), 5L);
                } else if (period == 5L) {
                    scheduleNextShift(player, inventory, all, token, newTier,
                            plugin.getConfig().getInt("gui.spin.shifts-slow", 3), 10L);
                } else if (period == 10L) {
                    scheduleNextShift(player, inventory, all, token, newTier,
                            plugin.getConfig().getInt("gui.spin.shifts-final", 2), 15L);
                } else {
                    // Spin finished: slot 4 holds the winner.
                    inventory.setItem(4, tokenItem(token));
                    finish(player, token, newTier);
                }
            }
        }.runTaskLater(plugin, period);
    }

    /** Shifts every item one slot to the right; a new random item enters slot 0. */
    private void shiftRight(Inventory inventory, List<TokenDefinition> all) {
        for (int slot = 8; slot > 0; slot--) {
            inventory.setItem(slot, inventory.getItem(slot - 1));
        }
        inventory.setItem(0, randomTokenItem(all));
    }

    private void finish(Player player, TokenDefinition token, int newTier) {
        if (player.isOnline()) {
            player.closeInventory();
            player.sendTitle(
                    plugin.getConfig().getString("gui.spin.title-unlocked", "§6§lTOKEN UNLOCKED"),
                    plugin.getConfig().getString("gui.spin.subtitle-unlocked", "§7Check your /tokens menu!"),
                    10, 70, 20);
            spawnNonDamagingFirework(player, player.getLocation());
        }
        // Unlock bookkeeping + announcements + auto-activate.
        plugin.getData().setActiveToken(player, token.getId());
        plugin.getPassiveManager().applyPassives(player);
        plugin.getNotifications().broadcastFirstUnlock(player, token);
    }

    private ItemStack randomTokenItem(List<TokenDefinition> all) {
        if (all.isEmpty()) {
            return new ItemStack(Material.PAPER);
        }
        return tokenItem(all.get(RANDOM.nextInt(all.size())));
    }

    private ItemStack tokenItem(TokenDefinition token) {
        ItemStack item = new ItemStack(token.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(token.getRarityColor() + "§l" + token.getName() + " Token");
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Colorful, non-damaging firework rocket at the player's position. */
    private void spawnNonDamagingFirework(Player player, Location location) {
        Firework firework = player.getWorld().spawn(location, Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.AQUA, Color.LIME, Color.ORANGE, Color.FUCHSIA)
                    .trail(true)
                    .flicker(true)
                    .build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
            // Tag the rocket so TokenItemListener cancels any damage it deals.
            fw.setMetadata("tokensmp_firework", new FixedMetadataValue(plugin, true));
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                if (firework.isValid()) {
                    firework.detonate();
                }
            }
        }.runTaskLater(plugin, 25L);
    }
}
