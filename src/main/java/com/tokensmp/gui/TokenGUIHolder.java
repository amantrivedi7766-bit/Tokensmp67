package com.tokensmp.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Marker holder for every inventory this plugin opens. The InventoryListener
 * uses it to block all item movement exploits, and each GUI stores its state
 * (target token, selected admin player, pending action) in the data map.
 */
public final class TokenGUIHolder implements InventoryHolder {

    public enum Type {
        SELECTION, UPGRADE, TOKEN_DETAIL, PLAYER, ADMIN_MAIN, ADMIN_PLAYERS,
        ADMIN_PLAYER, ADMIN_TOKENS, ADMIN_CONFIRM, CONFIRM, SPIN
    }

    private final Type type;
    private final UUID viewer;
    private final Map<String, Object> data = new HashMap<>();

    public TokenGUIHolder(Type type, UUID viewer) {
        this.type = type;
        this.viewer = viewer;
    }

    public Type getType() {
        return type;
    }

    public UUID getViewer() {
        return viewer;
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public Inventory getInventory() {
        return null; // marker holder; the live inventory is owned by the view
    }
}
