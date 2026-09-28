package com.tokensmp.data;

import com.tokensmp.TokenSMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Creates and secures the physical, HOLDABLE token items. The item is real:
 * the player can hold it, switch hotbar slots, inspect it, drop it and use
 * its ability. Identity lives in secure PDC data - token id, tier, unique
 * instance id, ownership UUID, claimed state, admin flag and item version -
 * never in the name, lore, material or slot.
 *
 * Also owns duplicate protection: reconcile() removes every unauthorized
 * copy of a token item so a player can never hold two active instances.
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
                com.tokensmp.util.ColorUtil.color("&7Owner: &f" + owner.getName()),
                com.tokensmp.util.ColorUtil.color("&7Hold it and use &fShift + Right Click"),
                com.tokensmp.util.ColorUtil.color("&7to activate the token ability.")));
        meta.getPersistentDataContainer().set(plugin.tokenItemKey(), PersistentDataType.STRING, token.getId());
        meta.getPersistentDataContainer().set(plugin.tierKey(), PersistentDataType.INTEGER, tier);
        meta.getPersistentDataContainer().set(plugin.instanceKey(), PersistentDataType.STRING,
                TokenDataManager.newInstanceId());
        meta.getPersistentDataContainer().set(plugin.ownerKey(), PersistentDataType.STRING,
                owner.getUniqueId().toString());
        meta.getPersistentDataContainer().set(plugin.adminFlagKey(), PersistentDataType.BYTE,
                token.isAdminToken() ? (byte) 1 : (byte) 0);
        meta.getPersistentDataContainer().set(plugin.claimedStateKey(), PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(plugin.itemVersionKey(), PersistentDataType.INTEGER, 2);
        item.setItemMeta(meta);
        return item;
    }

    /** Gives the item directly (drops overflow at the player's feet). */
    public void give(Player owner, com.tokensmp.token.Token token, int tier) {
        ItemStack item = createItem(owner, token, Math.max(1, tier));
        owner.getInventory().addItem(item).values().forEach(leftover ->
                owner.getWorld().dropItemNaturally(owner.getLocation(), leftover));
    }

    /**
     * Duplicate protection: removes every token item of this token from the
     * player, then gives exactly ONE fresh authorized instance. Called on
     * every claim, so inventory drift can never produce two active copies.
     */
    public void reconcile(Player owner, com.tokensmp.token.Token token, int tier) {
        removeFrom(owner, token);
        give(owner, token, tier);
    }

    /**
     * Validates that the item the player is holding is a genuine, owned
     * instance of the given token (held-token ability activation).
     */
    public boolean isValidHeldItem(Player player, ItemStack stack, String tokenId) {
        if (!isTokenOf(stack, tokenId)) {
            return false;
        }
        String owner = ownerOf(stack);
        return owner == null || owner.equals(player.getUniqueId().toString());
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
