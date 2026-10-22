package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.LifeManager;
import com.tokensmp.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Revival Altar: right-clicking a Revive Card opens this menu, which lists
 * every eliminated player. Selecting one performs the ritual (lightning,
 * particles, sounds) and brings that player back to life.
 */
public final class RevivalGUI implements Listener {

    private static final int HEAD_SLOTS = 45;
    private static final int CLOSE_SLOT = 49;

    /** Marker holder carrying the slot -> eliminated player mapping. */
    public static final class Holder implements InventoryHolder {

        private final UUID viewer;
        private final Map<Integer, LifeManager.Entry> entries = new HashMap<>();

        public Holder(UUID viewer) {
            this.viewer = viewer;
        }

        public UUID getViewer() {
            return viewer;
        }

        public void put(int slot, LifeManager.Entry entry) {
            entries.put(slot, entry);
        }

        public LifeManager.Entry get(int slot) {
            return entries.get(slot);
        }

        @Override
        public Inventory getInventory() {
            return null; // marker holder
        }
    }

    private final TokenSMP plugin;

    public RevivalGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        List<LifeManager.Entry> eliminated = plugin.lives().eliminated();
        Holder holder = new Holder(viewer.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.revival.title", "&d&lRevival Altar"));

        ItemStack filler = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        int index = 0;
        for (LifeManager.Entry entry : eliminated) {
            if (index >= HEAD_SLOTS) {
                break;
            }
            holder.put(index, entry);
            inventory.setItem(index++, ItemBuilder.of(Material.PLAYER_HEAD)
                    .skullOwner(Bukkit.getOfflinePlayer(entry.id()))
                    .name("&d&l" + entry.name())
                    .addLore("&7Revive level: &f" + entry.level(),
                            "&7Cost: &f" + Math.max(0.0, (entry.level() - 1)
                                    * plugin.config().getDouble("lives.heart-sacrifice-per-level", 1.0))
                                    + " &7heart(s)",
                            "", "&dClick to perform the revival ritual!")
                    .build());
        }
        if (index == 0) {
            inventory.setItem(22, ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&7No eliminated players")
                    .addLore("&7Nobody has run out of lives yet.")
                    .build());
        }

        inventory.setItem(CLOSE_SLOT, ItemBuilder.of(Material.BARRIER).name("&cClose").build());
        viewer.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player viewer)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof Holder holder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }
        if (event.getSlot() == CLOSE_SLOT) {
            viewer.closeInventory();
            return;
        }
        LifeManager.Entry entry = holder.get(event.getSlot());
        if (entry == null) {
            return;
        }
        viewer.closeInventory();
        plugin.lives().revive(viewer, entry);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }
}
