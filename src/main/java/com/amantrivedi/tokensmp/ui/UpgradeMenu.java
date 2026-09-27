package com.amantrivedi.tokensmp.ui;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Split upgrade layout: material costs on the LEFT side, live task parameters
 * on the RIGHT side, and a central confirm button. Handles the actual
 * task-complete + material consumption + tier upgrade flow.
 */
public final class UpgradeMenu implements Listener {

    /** Marker holder carrying the token id of the displayed upgrade. */
    public static final class Holder implements InventoryHolder {
        private final String tokenId;

        public Holder(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getTokenId() {
            return tokenId;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static final int[] MATERIAL_SLOTS = {10, 11, 12, 13};   // left side
    public static final int TASK_SLOT = 15;                        // right side
    public static final int TASK_MOB_SLOT = 16;                    // right side
    public static final int UPGRADE_SLOT = 49;
    public static final int BACK_SLOT = 45;

    private final TokenSmpPlugin plugin;

    public UpgradeMenu(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String tokenId) {
        TokenDefinition token = plugin.getRegistry().get(tokenId);
        if (token == null) {
            return;
        }
        int tier = plugin.getData().getTier(player, token.getId());
        TierDefinition next = token.tier(tier + 1);
        if (next == null) {
            return;
        }

        String title = plugin.getConfig().getString("gui.upgrade.title", "§8Upgrade: {token_name} §7[Tier {tier}]")
                .replace("{token_name}", token.getName())
                .replace("{tier}", String.valueOf(tier + 1));
        Inventory inventory = Bukkit.createInventory(new Holder(token.getId()), 54, title);

        ItemStack filler = filler();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        // LEFT side: required materials with have/need status.
        List<TierDefinition.MaterialCost> cost = next.getCost();
        for (int i = 0; i < MATERIAL_SLOTS.length && i < cost.size(); i++) {
            inventory.setItem(MATERIAL_SLOTS[i], materialItem(cost.get(i), player));
        }

        // RIGHT side: live task parameters.
        TierDefinition.TaskSpec task = next.getTask();
        inventory.setItem(TASK_SLOT, taskItem(task, player, token.getId()));
        inventory.setItem(TASK_MOB_SLOT, taskMobItem(task));

        inventory.setItem(BACK_SLOT, backItem());
        inventory.setItem(UPGRADE_SLOT, confirmItem(taskComplete(player, token.getId(), next)
                && materialsComplete(player, next)));

        player.openInventory(inventory);
    }

    private ItemStack filler() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack materialItem(TierDefinition.MaterialCost cost, Player player) {
        ItemStack item = new ItemStack(cost.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int have = countItem(player, cost.material());
            meta.setDisplayName("§e" + LoreBuilder.prettyName(cost.material().name()));
            List<String> lore = new ArrayList<>();
            lore.add("§7Required: §f" + cost.amount() + "x");
            lore.add("§7In inventory: " + (have >= cost.amount() ? "§a" : "§c") + have + "x");
            lore.add(" ");
            lore.add(have >= cost.amount() ? "§a✔ Material requirement met!" : "§c✘ Not enough materials!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack taskItem(TierDefinition.TaskSpec task, Player player, String tokenId) {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int progress = plugin.getData().getProgress(player, tokenId);
            int required = task.getCount();
            meta.setDisplayName("§6§l🎯 UNLOCK/UPGRADE TASK");
            List<String> lore = new ArrayList<>();
            lore.add("§7- Task: §f" + task.getDescription());
            lore.add("§7- Progress: " + (progress >= required ? "§a" : "§e")
                    + "[" + plugin.buildProgressBar(progress, required) + "§r" + (progress >= required ? "§a" : "§e") + "] "
                    + progress + "/" + required);
            lore.add(" ");
            lore.add(progress >= required ? "§a✔ Task complete!" : "§c✘ Task not complete yet!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack taskMobItem(TierDefinition.TaskSpec task) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§l⚡ TARGET");
            List<String> lore = new ArrayList<>();
            if (!task.getMobs().isEmpty()) {
                lore.add("§7Target mobs:");
                for (org.bukkit.entity.EntityType mob : task.getMobs()) {
                    lore.add("§8- §f" + LoreBuilder.prettyName(mob.name()));
                }
            } else {
                lore.add("§7" + task.getDescription());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack backItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7« Back to Token Selection");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack confirmItem(boolean ready) {
        ItemStack item = new ItemStack(ready ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ready ? "§a§l✔ CONFIRM UPGRADE" : "§c§l✘ REQUIREMENTS NOT MET");
            meta.setLore(List.of(
                    ready ? "§7Click to consume materials and upgrade!"
                          : "§7Complete the task and gather the materials first."));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ------------------------------------------------------------------
    // Requirement helpers
    // ------------------------------------------------------------------

    public boolean taskComplete(Player player, String tokenId, TierDefinition tier) {
        return plugin.getData().getProgress(player, tokenId) >= tier.getTask().getCount();
    }

    public boolean materialsComplete(Player player, TierDefinition tier) {
        for (TierDefinition.MaterialCost cost : tier.getCost()) {
            if (countItem(player, cost.material()) < cost.amount()) {
                return false;
            }
        }
        return true;
    }

    private int countItem(Player player, Material material) {
        int total = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /** Removes the given materials from the player's inventory (assumes verified). */
    private void consumeMaterials(Player player, List<TierDefinition.MaterialCost> cost) {
        Map<Material, Integer> toRemove = new HashMap<>();
        for (TierDefinition.MaterialCost entry : cost) {
            toRemove.merge(entry.material(), entry.amount(), Integer::sum);
        }
        for (Map.Entry<Material, Integer> entry : toRemove.entrySet()) {
            int remaining = entry.getValue();
            for (ItemStack item : player.getInventory().getStorageContents()) {
                if (remaining <= 0) {
                    break;
                }
                if (item != null && item.getType() == entry.getKey()) {
                    int take = Math.min(remaining, item.getAmount());
                    item.setAmount(item.getAmount() - take);
                    remaining -= take;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Click handling
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!(view.getTopInventory().getHolder() instanceof Holder holder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof Holder)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot == BACK_SLOT) {
            plugin.getSelectionMenu().open(player);
            return;
        }
        if (slot != UPGRADE_SLOT) {
            return;
        }

        TokenDefinition token = plugin.getRegistry().get(holder.getTokenId());
        if (token == null) {
            return;
        }
        int tier = plugin.getData().getTier(player, token.getId());
        TierDefinition next = token.tier(tier + 1);
        if (next == null) {
            return;
        }

        if (!taskComplete(player, token.getId(), next) || !materialsComplete(player, next)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
            plugin.sendMessage(player, plugin.getConfig().getString("messages.upgrade-not-ready",
                    "§cRequirements not met: complete the task and gather the materials!"));
            return;
        }

        consumeMaterials(player, next.getCost());
        int newTier = tier + 1;
        plugin.getData().setTier(player, token.getId(), newTier);
        plugin.getData().setProgress(player, token.getId(), 0);

        if (tier == 0) {
            // FIRST unlock: crate wheel spin animation -> unlock + announcements.
            player.closeInventory();
            new SpinAnimation(plugin).play(player, token, newTier);
        } else {
            // Tier 2/3 upgrade: instant + broadcast.
            player.closeInventory();
            finishUpgrade(player, token, newTier);
            player.sendTitle(
                    plugin.getConfig().getString("gui.upgrade.title-flash", "§6§lTIER {tier} REACHED!")
                            .replace("{tier}", String.valueOf(newTier)),
                    plugin.getConfig().getString("gui.upgrade.subtitle-flash", "§7{color}§l{name} Token")
                            .replace("{color}", token.getRarityColor())
                            .replace("{name}", token.getName()), 10, 60, 10);
        }
    }

    /** Applies the unlocked tier: broadcasts + auto-activates the token. */
    private void finishUpgrade(Player player, TokenDefinition token, int newTier) {
        plugin.getData().setActiveToken(player, token.getId());
        plugin.getPassiveManager().applyPassives(player);
        if (newTier == 1) {
            plugin.getNotifications().broadcastFirstUnlock(player, token);
        } else {
            plugin.getNotifications().broadcastUpgrade(player, token, newTier);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }
}
