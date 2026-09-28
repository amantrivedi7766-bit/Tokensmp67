package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.data.PlayerTokenData;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ItemBuilder;
import com.tokensmp.util.ProgressBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "My Tokens" view (button in the main menu): the player's single ACTIVE
 * token at the top, then a paginated grid covering every unlocked token
 * (available + claimed states) with live grind progress - one polished
 * statistics overview for all 20 tokens.
 */
public final class PlayerTokenGUI implements Listener {

    private static final int[] GRID_SLOTS = {10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 37, 38, 39, 40, 41, 42};
    private static final int BACK_SLOT = 49;
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;

    private final TokenSMP plugin;

    public PlayerTokenGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        List<Token> tokens = plugin.registry().playerTokens();
        int totalPages = Math.max(1, (int) Math.ceil(tokens.size() / (double) GRID_SLOTS.length));
        int current = Math.max(0, Math.min(page, totalPages - 1));

        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.PLAYER, player.getUniqueId());
        holder.set("page", current);
        Map<Integer, String> slotMap = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.player.title", "&8My Tokens &7(My Collection)"));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        int owned = 0;
        for (Token token : tokens) {
            if (plugin.data().hasUnlocked(player, token.getId())) {
                owned++;
            }
        }

        inventory.setItem(4, ItemBuilder.of(Material.PLAYER_HEAD).skullOwner(player)
                .name("&6&l" + player.getName() + "'s Tokens")
                .addLore("&7Active: " + describeActive(player),
                        "&7Owned: &f" + owned + "&7/" + tokens.size()).build());

        int start = current * GRID_SLOTS.length;
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            int index = start + i;
            if (index >= tokens.size()) {
                break;
            }
            Token token = tokens.get(index);
            slotMap.put(GRID_SLOTS[i], token.getId());
            inventory.setItem(GRID_SLOTS[i], ownedIcon(player, token));
        }

        if (current > 0) {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("&7« Previous Page").build());
        }
        if (current < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("&7Next Page »").build());
        }
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("&7« Back to Token Collection").build());

        holder.set("slotMap", slotMap);
        player.openInventory(inventory);
    }

    private String describeActive(Player player) {
        String activeId = plugin.data().getActiveToken(player);
        Token active = activeId == null ? null : plugin.registry().get(activeId);
        return active == null ? "&cNone"
                : active.getRarity().getColorCode() + "&l" + active.getDisplayName();
    }

    private ItemStack ownedIcon(Player player, Token token) {
        PlayerTokenData data = plugin.data().getData(player, token.getId());
        boolean active = plugin.data().isClaimed(player, token.getId());
        List<String> lore = new ArrayList<>();
        if (!data.isUnlocked()) {
            lore.add("&c❌ Locked");
        } else {
            lore.add("&7Tier: " + (data.getTier() >= token.getMaxTier() ? "&5MAX" : "&e" + data.getTier() + " / 3"));
            lore.add("&7Status: " + (active ? "&a✔ ACTIVE (your one claimed token)" : "&eAvailable (unclaimed)"));
            lore.add("&7Passives: &f" + passiveSummary(token, data.getTier()));
            TokenTier next = token.tier(data.getTier() + 1);
            if (next == null) {
                lore.add("&d🌟 Fully maxed out!");
            } else {
                TokenTier.TaskSpec task = next.getTask();
                lore.add("&7Next task: &f" + task.description());
                lore.add("&7Progress: &a[" + ProgressBar.bar(data.getProgress(), task.count(), 12, "&a", "&8")
                        + "&a] &f" + data.getProgress() + "/" + task.count());
            }
            lore.add("");
            lore.add("&eClick to manage this token!");
        }
        return ItemBuilder.of(token.getIcon())
                .name(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token"
                        + (active ? " &a✔" : ""))
                .addLore(lore).build();
    }

    private String passiveSummary(Token token, int tier) {
        TokenTier def = token.tier(tier);
        return def == null ? "-" : def.getPassiveDescription();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TokenGUIHolder holder)
                || holder.getType() != TokenGUIHolder.Type.PLAYER) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        int page = holder.<Integer>get("page") == null ? 0 : holder.<Integer>get("page");
        int slot = event.getRawSlot();
        if (slot == BACK_SLOT) {
            plugin.selectionGUI().open(player);
            return;
        }
        if (slot == PREV_PAGE_SLOT) {
            SoundManager.click(player);
            open(player, page - 1);
            return;
        }
        if (slot == NEXT_PAGE_SLOT) {
            SoundManager.click(player);
            open(player, page + 1);
            return;
        }
        // Any token icon opens its detail view.
        Map<Integer, String> slotMap = holder.get("slotMap");
        if (slotMap == null) {
            return;
        }
        String tokenId = slotMap.get(slot);
        Token token = plugin.registry().get(tokenId);
        if (token != null) {
            SoundManager.click(player);
            plugin.tokenUpgradeGUI().open(player, token);
        }
    }
}
