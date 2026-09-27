package com.tokensmp.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Inventory counting / consuming helpers for upgrade material costs. */
public final class PlayerUtil {

    private PlayerUtil() {
    }

    /** Counts how many of a material the player carries (storage contents only). */
    public static int count(Player player, Material material) {
        int total = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /** True when the player carries at least the given amount of the material. */
    public static boolean has(Player player, Material material, int amount) {
        return count(player, material) >= amount;
    }

    /** Removes the given amount of a material (assumes availability was verified). */
    public static void remove(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (remaining <= 0) {
                return;
            }
            if (item != null && item.getType() == material) {
                int take = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - take);
                remaining -= take;
            }
        }
    }

    /** Aggregates a material->amount cost map and consumes it all at once. */
    public static void removeAll(Player player, Map<Material, Integer> costs) {
        Map<Material, Integer> aggregated = new HashMap<>();
        for (Map.Entry<Material, Integer> entry : costs.entrySet()) {
            aggregated.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        for (Map.Entry<Material, Integer> entry : aggregated.entrySet()) {
            remove(player, entry.getKey(), entry.getValue());
        }
    }
}
