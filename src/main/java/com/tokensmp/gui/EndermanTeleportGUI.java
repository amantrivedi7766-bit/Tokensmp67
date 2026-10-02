package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Ender Assembly menu (Enderman tier 3): every online player's head with their
 * skin, in two steps.
 *
 * Step 1 - pick the player who should be teleported.
 * Step 2 - pick the destination player; the first player is then teleported
 *          to the second one (a player can also teleport themselves).
 */
public final class EndermanTeleportGUI implements Listener {

    private static final int HEAD_SLOTS = 45;
    private static final int CLOSE_SLOT = 49;

    /** Marker holder so the click listener can recognise its own menus. */
    public static final class Holder implements InventoryHolder {

        private final UUID viewer;
        private final int step;
        private final UUID source;

        public Holder(UUID viewer, int step, UUID source) {
            this.viewer = viewer;
            this.step = step;
            this.source = source;
        }

        public UUID getViewer() {
            return viewer;
        }

        public int getStep() {
            return step;
        }

        public UUID getSource() {
            return source;
        }

        @Override
        public Inventory getInventory() {
            return null; // marker holder; the live inventory is owned by the view
        }
    }

    private final TokenSMP plugin;

    public EndermanTeleportGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    /** Step 1: choose the player who will be teleported. */
    public void openSource(Player viewer) {
        open(viewer, 1, null);
    }

    /** Step 2: choose where the selected player should be teleported. */
    public void openDestination(Player viewer, UUID source) {
        open(viewer, 2, source);
    }

    private void open(Player viewer, int step, UUID source) {
        String title = step == 1
                ? plugin.config().getString("gui.ender-assembly.title-source",
                        "&5Ender Assembly &8» &7Who to teleport?")
                : plugin.config().getString("gui.ender-assembly.title-destination",
                        "&5Ender Assembly &8» &7Pick the destination");
        Inventory inventory = Bukkit.createInventory(
                new Holder(viewer.getUniqueId(), step, source), 54, title);

        ItemStack filler = ItemBuilder.of(Material.PURPLE_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        int index = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (index >= HEAD_SLOTS) {
                break;
            }
            ItemStack head = ItemBuilder.of(Material.PLAYER_HEAD)
                    .skullOwner(online)
                    .name("&d&l" + online.getName())
                    .addLore(step == 1
                            ? "&7Click to choose this player."
                            : "&7Click to teleport the chosen player here.")
                    .addLore("", "&8» &7Health: &f" + Math.round(online.getHealth())
                            + "&7/&f" + Math.round(online.getMaxHealth()))
                    .build();
            inventory.setItem(index++, head);
        }

        if (index == 0) {
            inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                    .name("&cNo players online")
                    .addLore("&7There is nobody to assemble right now.")
                    .build());
        }

        inventory.setItem(CLOSE_SLOT, ItemBuilder.of(Material.BARRIER)
                .name("&cClose").build());

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
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD
                || !(clicked.getItemMeta() instanceof SkullMeta skull)) {
            return;
        }
        OfflinePlayer owner = skull.getOwningPlayer();
        if (owner == null || owner.getName() == null) {
            return;
        }
        Player selected = Bukkit.getPlayerExact(owner.getName());
        if (selected == null || !selected.isOnline()) {
            plugin.messages().send(viewer, "messages.ender-assembly-offline",
                    "&cThat player is no longer online.");
            return;
        }
        if (holder.getStep() == 1) {
            openDestination(viewer, selected.getUniqueId());
            return;
        }
        Player source = holder.getSource() == null ? null : Bukkit.getPlayer(holder.getSource());
        if (source == null || !source.isOnline()) {
            plugin.messages().send(viewer, "messages.ender-assembly-offline",
                    "&cThat player is no longer online.");
            return;
        }
        viewer.closeInventory();
        plugin.abilities().completeEnderTeleport(viewer, source, selected);
    }

    /** No dragging inside the menu. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }
}
