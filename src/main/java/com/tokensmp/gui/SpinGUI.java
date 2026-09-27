package com.tokensmp.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Holder for the 9-slot crate spin inventory ("&5&lUnlocking Token...").
 * Purely a marker so other listeners ignore the spin window.
 */
public final class SpinGUI {

    private SpinGUI() {
    }

    public static final class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
