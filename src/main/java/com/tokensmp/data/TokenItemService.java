package com.tokensmp.data;

import com.tokensmp.TokenSMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Creates the physical token items handed out on claim / admin give.
 * The item is identity only - every gameplay decision re-validates through
 * the server-side player data; lore is never trusted.
 *
 * PDC stored on each item: token id, tier, unique instance id, ownership
 * UUID and the admin-token flag.
 */
public final class TokenItemService {

    private final TokenSMP plugin;
    private final TokenDataManager data;

    public TokenItemService(TokenSMP plugin, TokenDataManager data) {
        this.plugin = plugin;
        this.data = data;
    }

    /** Builds a PDC-tagged token item for the given tier. */
    public ItemStack createItem(Player owner, com.tokensmp.token.Token token, int tier) {
        ItemStack item = new ItemStack(token.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName(com.tokensmp.util.ColorUtil.color(
                token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token"));
        meta.setLore(List.of(
                com.tokensmp.util.ColorUtil.color("&7Tier: &f" + tier + " / 3"),
                com.tokensmp.util.ColorUtil.color("&7Rarity: " + token.getRarity().getColorCode()
                        + token.getRarity().getDisplayName()),
                com.tokensmp.util.ColorUtil.color(token.isAdminToken() ? "&4⚠ ADMIN TOKEN" : " "),
                com.tokensmp.util.ColorUtil.color("&7Owner: &f" + owner.getName())));
        meta.getPersistentDataContainer().set(plugin.tokenItemKey(), PersistentDataType.STRING, token.getId());
        meta.getPersistentDataContainer().set(plugin.tierKey(), PersistentDataType.INTEGER, tier);
        meta.getPersistentDataContainer().set(plugin.instanceKey(), PersistentDataType.STRING,
                TokenDataManager.newInstanceId());
        meta.getPersistentDataContainer().set(plugin.ownerKey(), PersistentDataType.STRING,
                owner.getUniqueId().toString());
        meta.getPersistentDataContainer().set(plugin.adminFlagKey(), PersistentDataType.BYTE,
                token.isAdminToken() ? (byte) 1 : (byte) 0);
        item.setItemMeta(meta);
        return item;
    }

    /** Gives the item directly (drops overflow at the player's feet). */
    public void give(Player owner, com.tokensmp.token.Token token, int tier) {
        ItemStack item = createItem(owner, token, Math.max(1, tier));
        owner.getInventory().addItem(item).values().forEach(leftover ->
                owner.getWorld().dropItemNaturally(owner.getLocation(), leftover));
    }

    /** Removes every matching token item from the player (unclaim cleanup). */
    public void removeFrom(Player owner, com.tokensmp.token.Token token) {
        String id = token.getId();
        ItemStack[] storage = owner.getInventory().getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            if (isTokenOf(storage[i], id)) {
                owner.getInventory().setItem(i, null);
            }
        }
    }

    /** Reads the token id stored on an item, or null when not a token item. */
    public String tokenOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer()
                .get(plugin.tokenItemKey(), PersistentDataType.STRING);
    }

    /** True when the stack is a token item of the given token id. */
    public boolean isTokenOf(ItemStack stack, String tokenId) {
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return false;
        }
        String id = stack.getItemMeta().getPersistentDataContainer()
                .get(plugin.tokenItemKey(), PersistentDataType.STRING);
        return id != null && id.equals(tokenId);
    }

    /** Reads the owner UUID stored on a token item, or null. */
    public String ownerOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer()
                .get(plugin.ownerKey(), PersistentDataType.STRING);
    }

    /** Reads the admin-token flag of an item. */
    public boolean isAdminItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer()
                .getOrDefault(plugin.adminFlagKey(), PersistentDataType.BYTE, (byte) 0) == (byte) 1;
    }
}
