package com.amantrivedi.tokensmp;

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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the 6-row Token SMP inventory screen and intercepts custom item clicks.
 */
public final class TokenMenu implements Listener {

    public static final String DEFAULT_TITLE = "§8Token SMP - Menu";

    private final TokenSmpPlugin plugin;

    public TokenMenu(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** Opens the Token SMP menu for the given player. */
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(new TokenMenuHolder(), 54, getTitle());
        ItemStack filler = createFiller();

        // Border slots: 0-9, 17, 18, 26, 27, 35, 36, 44-53 (except 49).
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36,
                44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            inventory.setItem(slot, filler);
        }

        // Common tier tokens: slots 10, 11, 12.
        for (int slot : new int[]{10, 11, 12}) {
            inventory.setItem(slot, createTokenItem(TokenType.ZOMBIE));
        }
        // Rare tier tokens: slots 22, 23, 24.
        for (int slot : new int[]{22, 23, 24}) {
            inventory.setItem(slot, createTokenItem(TokenType.BLAZE));
        }
        // Legendary tier tokens: slots 32, 33, 34.
        for (int slot : new int[]{32, 33, 34}) {
            inventory.setItem(slot, createTokenItem(TokenType.WARDEN));
        }

        // Status anchor: slot 49 - player head of the viewer.
        inventory.setItem(49, createProfileItem(player));

        player.openInventory(inventory);
    }

    private String getTitle() {
        return plugin.getConfig().getString("gui.title", DEFAULT_TITLE);
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfig().getString("gui.filler-name", " "));
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack createTokenItem(TokenType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getItemName());
            List<String> lore = new ArrayList<>();
            switch (type) {
                case ZOMBIE -> {
                    lore.add("§7Tier: §f" + type.getTier());
                    lore.add("§7Passive: §f+2 Extra Hearts");
                    lore.add("§7Ability: §f" + type.getAbilityName());
                    lore.add("§8Shift + Left-Click in-game to activate");
                }
                case BLAZE -> {
                    lore.add("§7Tier: §f" + type.getTier());
                    lore.add("§7Passive: §fPermanent Fire Resistance");
                    lore.add("§7Ability: §f" + type.getAbilityName());
                    lore.add("§8Shift + Right-Click in-game to activate");
                }
                case WARDEN -> {
                    lore.add("§7Tier: §f" + type.getTier());
                    lore.add("§7Passive: §fResistance I + Night Vision");
                    lore.add("§7Ability: §f" + type.getAbilityName());
                    lore.add("§8Shift + Left-Click in-game to activate");
                }
            }
            lore.add(" ");
            lore.add("§eClick to equip this token!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createProfileItem(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§b§lYour Profile Status");
            PlayerData data = plugin.getData(player);
            TokenType active = TokenType.fromId(data.getActiveToken());
            List<String> lore = new ArrayList<>();
            lore.add("§7Extra Hearts Stacked: §c" + data.getExtraHearts());
            lore.add("§7Equipped Token: §a" + (active == null ? "None" : active.getDisplayName()));
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    // ------------------------------------------------------------------
    // Click interception
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!(view.getTopInventory().getHolder() instanceof TokenMenuHolder)) {
            return;
        }
        // Cancel the event completely - no pulling items out.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenMenuHolder)) {
            return;
        }

        TokenType clicked = tokenAt(event.getRawSlot());
        if (clicked != null) {
            plugin.setActiveToken(player, clicked);
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof TokenMenuHolder) {
            event.setCancelled(true);
        }
    }

    /** Maps a menu slot to the token it represents; null when the slot holds no token. */
    private TokenType tokenAt(int slot) {
        return switch (slot) {
            case 10, 11, 12 -> TokenType.ZOMBIE;
            case 22, 23, 24 -> TokenType.BLAZE;
            case 32, 33, 34 -> TokenType.WARDEN;
            default -> null;
        };
    }

    /** Marker holder so we can identify our menu inventories reliably. */
    public static final class TokenMenuHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
