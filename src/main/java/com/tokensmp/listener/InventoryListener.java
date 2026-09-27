package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.gui.SpinGUI;
import com.tokensmp.gui.TokenGUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Global GUI security layer: every inventory opened by this plugin is
 * untouchable - clicks, shift-clicks, number-key swaps, drag-splitting,
 * double-click collect-to-cursor, hotbar movement and Q-drops are all
 * cancelled. This closes the classic inventory duplication exploits.
 */
public final class InventoryListener implements Listener {

    private final TokenSMP plugin;

    public InventoryListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    private boolean isOurGui(Inventory top) {
        return top != null
                && (top.getHolder() instanceof TokenGUIHolder || top.getHolder() instanceof SpinGUI.Holder);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!isOurGui(top)) {
            return;
        }
        // Any interaction that would move items around is dead: both clicking
        // inside the GUI and shift/hotbar/double-click transfers from the
        // player inventory while our GUI is open.
        if (event.getClickedInventory() != null
                && (event.getClickedInventory().equals(top)
                || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR
                || event.getHotbarButton() >= 0)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!isOurGui(top)) {
            return;
        }
        // Any drag that touches a slot of our GUI is cancelled.
        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /** Blocks Q-dropping items while our GUI is focused (cursor inside it). */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropKey(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!isOurGui(top)) {
            return;
        }
        if (event.getClick() == org.bukkit.event.inventory.ClickType.DROP
                || event.getClick() == org.bukkit.event.inventory.ClickType.CONTROL_DROP) {
            event.setCancelled(true);
        }
    }
}
