package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
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
import java.util.List;

/**
 * "My Tokens" view (button in the selection menu): a compact overview split
 * into MY TOKENS (claimed), AVAILABLE TOKENS (unlocked, unclaimed) and a
 * PROGRESS section listing every token's live grind status.
 */
public final class PlayerTokenGUI implements Listener {

    private static final int BACK_SLOT = 49;
    private static final int[] MY_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] AVAILABLE_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final int[] PROGRESS_SLOTS = {19, 20, 21, 22, 23, 24, 25, 37};

    private final TokenSMP plugin;

    public PlayerTokenGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.PLAYER, player.getUniqueId());
        java.util.Map<Integer, String> slotMap = new java.util.HashMap<>();
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.player.title", "&8My Tokens &7(My Collection)"));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, ItemBuilder.of(Material.PLAYER_HEAD).skullOwner(player)
                .name("&6&l" + player.getName() + "'s Tokens")
                .addLore("&7Active: " + describeActive(player),
                        "&7Owned: &f" + plugin.data().getUnlocked(player, plugin.registry().playerTokens()).size()
                                + "&7/&f8").build());

        // MY TOKENS: claimed.
        inventory.setItem(9, header(Material.LIME_DYE, "&a&lMY TOKENS (ACTIVE/CLAIMED)"));
        int myIndex = 0;
        for (Token token : plugin.registry().playerTokens()) {
            if (myIndex >= MY_SLOTS.length) {
                break;
            }
            if (plugin.data().isClaimed(player, token.getId())) {
                slotMap.put(MY_SLOTS[myIndex], token.getId());
                inventory.setItem(MY_SLOTS[myIndex++], ownedIcon(player, token, true));
            }
        }
        if (myIndex == 0) {
            inventory.setItem(MY_SLOTS[0], ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&7No claimed tokens yet").build());
        }

        // AVAILABLE TOKENS: unlocked but not claimed.
        inventory.setItem(27, header(Material.EMERALD, "&e&lAVAILABLE TOKENS (UNCLAIMED)"));
        int availIndex = 0;
        for (Token token : plugin.registry().playerTokens()) {
            if (availIndex >= AVAILABLE_SLOTS.length) {
                break;
            }
            if (plugin.data().hasUnlocked(player, token.getId())
                    && !plugin.data().isClaimed(player, token.getId())) {
                slotMap.put(AVAILABLE_SLOTS[availIndex], token.getId());
                inventory.setItem(AVAILABLE_SLOTS[availIndex++], ownedIcon(player, token, false));
            }
        }
        if (availIndex == 0) {
            inventory.setItem(AVAILABLE_SLOTS[0], ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&7No available tokens - claim one!").build());
        }

        // PROGRESS: live grind status for every token.
        int progIndex = 0;
        for (Token token : plugin.registry().playerTokens()) {
            if (progIndex >= PROGRESS_SLOTS.length) {
                break;
            }
            slotMap.put(PROGRESS_SLOTS[progIndex], token.getId());
            inventory.setItem(PROGRESS_SLOTS[progIndex++], progressIcon(player, token));
        }

        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("&7« Back to Token Selection").build());

        holder.set("slotMap", slotMap);
        player.openInventory(inventory);
    }

    private ItemStack header(Material material, String title) {
        return ItemBuilder.of(material).name(title).build();
    }

    private String describeActive(Player player) {
        String activeId = plugin.data().getActiveToken(player);
        Token active = activeId == null ? null : plugin.registry().get(activeId);
        return active == null ? "&cNone"
                : active.getRarity().getColorCode() + "&l" + active.getDisplayName();
    }

    private ItemStack ownedIcon(Player player, Token token, boolean claimed) {
        PlayerTokenData data = plugin.data().getData(player, token.getId());
        List<String> lore = new ArrayList<>();
        lore.add("&7Tier: " + (data.getTier() >= token.getMaxTier() ? "&5MAX" : "&e" + data.getTier() + " / 3"));
        lore.add("&7Status: " + (claimed ? "&aClaimed (active)" : "&eAvailable (unclaimed)"));
        lore.add("&7Passives: &f" + passiveSummary(token, data.getTier()));
        lore.add("");
        lore.add("&eClick to manage this token!");
        return ItemBuilder.of(token.getIcon())
                .name(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token")
                .addLore(lore).build();
    }

    private ItemStack progressIcon(Player player, Token token) {
        PlayerTokenData data = plugin.data().getData(player, token.getId());
        TokenTier next = token.tier(data.getTier() + 1);
        List<String> lore = new ArrayList<>();
        if (next == null) {
            lore.add("&d🌟 Fully maxed out!");
        } else {
            TokenTier.TaskSpec task = next.getTask();
            lore.add("&7Next: &fTier " + next.getTier());
            lore.add("&7Task: &f" + task.description());
            lore.add("&7Progress: &a[" + ProgressBar.bar(data.getProgress(), task.count(), 12, "&a", "&8")
                    + "&a] &f" + data.getProgress() + "/" + task.count());
        }
        return ItemBuilder.of(token.getIcon())
                .name("&6📊 " + token.getDisplayName() + " Progress")
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
        if (event.getRawSlot() == BACK_SLOT) {
            plugin.selectionGUI().open(player);
            return;
        }
        // Any token icon (owned, available or progress) opens its detail view.
        java.util.Map<Integer, String> slotMap = holder.get("slotMap");
        if (slotMap == null) {
            return;
        }
        String tokenId = slotMap.get(event.getRawSlot());
        Token token = plugin.registry().get(tokenId);
        if (token != null) {
            SoundEngine.click(player);
            plugin.tokenUpgradeGUI().open(player, token);
        }
    }
}
