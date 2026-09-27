package com.amantrivedi.tokensmp.ui;

import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
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

import java.util.List;

/**
 * The main /tokens selection GUI: shows every token with its state-based hover
 * lore. Left-click an unlocked token to open the Upgrade menu, right-click to
 * set it as your ACTIVE token (its passives/ability apply).
 */
public final class SelectionMenu implements Listener {

    /** Marker holder so we can identify our menu inventories reliably. */
    public static final class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final TokenSmpPlugin plugin;
    private final LoreBuilder loreBuilder;

    public SelectionMenu(TokenSmpPlugin plugin) {
        this.plugin = plugin;
        this.loreBuilder = new LoreBuilder(plugin);
    }

    public void open(Player player) {
        int size = plugin.getConfig().getInt("gui.selection.size", 54);
        String title = plugin.getConfig().getString("gui.selection.title", "§8Token SMP - Select Your Token");
        Inventory inventory = Bukkit.createInventory(new Holder(), size, title);

        ItemStack filler = filler();
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler);
        }

        for (TokenDefinition token : plugin.getRegistry().all()) {
            int tier = plugin.getData().getTier(player, token.getId());
            // The admin token only appears once unlocked (admin isolation).
            if (token.isAdminOnly() && tier == 0) {
                continue;
            }
            int slot = token.getSlot();
            if (slot < 0 || slot >= size) {
                continue;
            }
            inventory.setItem(slot, tokenIcon(player, token, tier));
        }

        inventory.setItem(49, profileHead(player));
        player.openInventory(inventory);
    }

    private ItemStack filler() {
        Material material = Material.matchMaterial(
                plugin.getConfig().getString("gui.selection.filler-material", "GRAY_STAINED_GLASS_PANE"));
        ItemStack filler = new ItemStack(material == null ? Material.GRAY_STAINED_GLASS_PANE : material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getConfig().getString("gui.selection.filler-name", " "));
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack tokenIcon(Player player, TokenDefinition token, int tier) {
        ItemStack item = new ItemStack(token.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // Display name + lore depend on the token's state for this player.
        if (tier <= 0) {
            meta.setDisplayName(plugin.getConfig().getString("lore.locked-name", "§c§l[LOCKED] §7{token_name} Token")
                    .replace("{token_name}", token.getName()).replace("{rarity_color}", token.getRarityColor()));
            meta.setLore(loreBuilder.buildLocked(token, player));
        } else if (tier < 3) {
            meta.setDisplayName((plugin.getConfig().getString("lore.unlocked-name", "{rarity_color}§l{token_name} Token §e[Tier {current_tier}]"))
                    .replace("{token_name}", token.getName())
                    .replace("{rarity_color}", token.getRarityColor())
                    .replace("{current_tier}", String.valueOf(tier)));
            meta.setLore(loreBuilder.buildUnlocked(token, player, tier));
        } else {
            meta.setDisplayName((plugin.getConfig().getString("lore.maxed-name", "§d§k§l!§5§l MAXED §d§k§l! {rarity_color}§l{token_name} Token §b[MAX TIER]"))
                    .replace("{token_name}", token.getName())
                    .replace("{rarity_color}", token.getRarityColor()));
            meta.setLore(loreBuilder.buildMaxed(token, tier));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack profileHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(plugin.getConfig().getString("gui.selection.profile-name", "§b§lYour Token Profile"));
            String activeId = plugin.getData().getActiveToken(player);
            TokenDefinition active = activeId == null ? null : plugin.getRegistry().get(activeId);
            int unlocked = 0;
            int total = 0;
            for (TokenDefinition token : plugin.getRegistry().all()) {
                total++;
                if (plugin.getData().hasUnlocked(player, token.getId())) {
                    unlocked++;
                }
            }
            meta.setLore(List.of(
                    plugin.getConfig().getString("gui.selection.profile-lore-active", "§7Active Token: §a{token}")
                            .replace("{token}", active == null ? "None" : active.getName()),
                    plugin.getConfig().getString("gui.selection.profile-lore-count", "§7Tokens Unlocked: §f{count}§7/§f{total}")
                            .replace("{count}", String.valueOf(unlocked))
                            .replace("{total}", String.valueOf(total))));
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
        if (!(view.getTopInventory().getHolder() instanceof Holder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof Holder)) {
            return;
        }

        // Profile head slot click: refresh the menu with live data.
        if (event.getRawSlot() == 49) {
            open(player);
            return;
        }

        TokenDefinition token = tokenAt(event.getRawSlot());
        if (token == null) {
            return;
        }

        int tier = plugin.getData().getTier(player, token.getId());
        boolean rightClick = event.isRightClick();

        if (tier <= 0) {
            plugin.sendMessage(player, plugin.getConfig().getString("messages.locked-token",
                    "§cThis token is locked! Complete its unlock task first."));
            return;
        }

        if (rightClick) {
            // Right-click: set as ACTIVE token (passives + ability apply).
            plugin.getData().setActiveToken(player, token.getId());
            plugin.getPassiveManager().applyPassives(player);
            plugin.sendMessage(player, plugin.getConfig().getString("messages.token-activated",
                    "§aActivated the {color}§l{name} Token§a! §7(Shift + Right Click to use its ability)")
                    .replace("{color}", token.getRarityColor())
                    .replace("{name}", token.getName()));
            player.closeInventory();
            return;
        }

        if (tier >= 3) {
            plugin.sendMessage(player, plugin.getConfig().getString("messages.already-maxed",
                    "§dThis token is already at MAX TIER!"));
            return;
        }
        // Left-click: open the split upgrade menu.
        plugin.getUpgradeMenu().open(player, token.getId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            event.setCancelled(true);
        }
    }

    private TokenDefinition tokenAt(int slot) {
        for (TokenDefinition token : plugin.getRegistry().all()) {
            if (token.getSlot() == slot) {
                return token;
            }
        }
        return null;
    }
}
