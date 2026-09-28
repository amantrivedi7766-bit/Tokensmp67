package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ItemBuilder;
import com.tokensmp.util.ProgressBar;
import com.tokensmp.util.TextUtil;
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
 * Main player menu (/tokens): all 20 player tokens in one polished,
 * paginated view with the exact three lore states (LOCKED / UNLOCKED /
 * MAX TIER), the player's single ACTIVE token at the top and clickable
 * navigation (pages, My Tokens, close).
 */
public final class PlayerGUI implements Listener {

    /** 18 token slots per page (rows 1-3 interior). */
    private static final int[] TOKEN_SLOTS = {10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31};
    private static final int ACTIVE_SLOT = 4;
    private static final int PREV_PAGE_SLOT = 45;
    private static final int MY_TOKENS_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 53;

    private final TokenSMP plugin;

    public PlayerGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        List<Token> tokens = plugin.registry().playerTokens();
        int totalPages = Math.max(1, (int) Math.ceil(tokens.size() / (double) TOKEN_SLOTS.length));
        int current = Math.max(0, Math.min(page, totalPages - 1));

        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.SELECTION, player.getUniqueId());
        holder.set("page", current);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.selection.title", "&8Token Collection")
                        + (totalPages > 1 ? " &7(" + (current + 1) + "/" + totalPages + ")" : ""));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        // The player's ONE active token, front and center.
        String activeId = plugin.data().getActiveToken(player);
        if (activeId != null) {
            Token active = plugin.registry().get(activeId);
            if (active != null) {
                inventory.setItem(ACTIVE_SLOT, ItemBuilder.of(active.getIcon())
                        .name("&a&l✔ ACTIVE TOKEN")
                        .addLore(active.getRarity().getColorCode() + "&l" + active.getDisplayName()
                                + " Token &7(Tier " + plugin.data().getTier(player, activeId) + ")",
                                "",
                                "&7Claimed tokens act as your single",
                                "&7active token. Unclaim it to swap.",
                                "",
                                "&eClick to manage!").build());
            }
        } else {
            inventory.setItem(ACTIVE_SLOT, ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&7No active token")
                    .addLore("&7Claim an unlocked token",
                            "&7to activate its abilities.").build());
        }

        int start = current * TOKEN_SLOTS.length;
        for (int i = 0; i < TOKEN_SLOTS.length; i++) {
            int index = start + i;
            if (index >= tokens.size()) {
                break;
            }
            inventory.setItem(TOKEN_SLOTS[i], tokenIcon(player, tokens.get(index)));
        }

        if (current > 0) {
            inventory.setItem(PREV_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("&7« Previous Page").build());
        }
        if (current < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, ItemBuilder.of(Material.ARROW)
                    .name("&7Next Page »").build());
        }

        int owned = countOwned(player);
        inventory.setItem(MY_TOKENS_SLOT, ItemBuilder.of(Material.BOOKSHELF)
                .name("&6&l📖 My Tokens")
                .addLore("&7View your active token, available",
                        "&7tokens and live progress.",
                        "",
                        "&7Unlocked: &f" + owned + "&7/" + plugin.registry().playerTokens().size(),
                        "&eClick to open!").build());

        player.openInventory(inventory);
    }

    private int countOwned(Player player) {
        int owned = 0;
        for (Token token : plugin.registry().playerTokens()) {
            if (plugin.data().hasUnlocked(player, token.getId())) {
                owned++;
            }
        }
        return owned;
    }

    // ------------------------------------------------------------------
    // Token icon + exact lore states (LOCKED / UNLOCKED / MAX TIER)
    // ------------------------------------------------------------------

    public ItemStack tokenIcon(Player player, Token token) {
        TokenDataManager data = plugin.data();
        int tier = data.getTier(player, token.getId());
        String separator = "&8-----------------------------";

        List<String> lore = new ArrayList<>();
        if (tier <= 0) {
            TokenTier next = token.tier(1);
            TokenTier.TaskSpec task = next.getTask();
            int progress = data.getProgress(player, token.getId());
            TokenTier.AbilitySpec ability = next.getAbility();
            lore.add("&c&l[LOCKED] &7" + token.getDisplayName() + " Token");
            lore.add(separator);
            lore.add("&7Rarity: " + token.getRarity().getColorCode() + token.getRarity().getDisplayName());
            lore.add(separator);
            lore.add("&e&l🔮 CURRENT STATUS:");
            lore.add("&c❌ You have not unlocked this token yet!");
            lore.add(separator);
            lore.add("&6&l🎯 UNLOCK TASK:");
            lore.add("&7- Progress: &c[" + ProgressBar.bar(progress, task.count(), 10, "&c", "&8")
                    + "&c] " + progress + "/" + task.count() + " Kills");
            lore.add("&7- Target Mob: &f" + taskTarget(task));
            lore.add(separator);
            lore.add("&b&l⚡ FUTURE TIER 1 ABILITY:");
            if (ability != null) {
                lore.add("&e✨ Type: &fActive");
                lore.add("&e📝 Description: &7" + ability.getDescription());
                lore.add("&e⏳ Cooldown: &f" + ability.getCooldownSeconds() + "s");
            } else {
                lore.add("&e✨ Type: &fPassive");
                lore.add("&e📝 Description: &7" + next.getPassiveDescription());
                lore.add("&e⏳ Cooldown: &fNone (passive)");
            }
            lore.add(separator);
            lore.add("&7- Tier 1 Cost: &f" + materialsList(next));
            lore.add(separator);
        } else if (tier >= token.getMaxTier()) {
            TokenTier max = token.tier(token.getMaxTier());
            TokenTier.AbilitySpec ability = max.getAbility();
            lore.add("&d&k&l!&5&l MAXED &d&k&l! " + token.getRarity().getColorCode() + "&l"
                    + token.getDisplayName() + " Token &b[MAX TIER]");
            lore.add(separator);
            lore.add("&7Rarity: " + token.getRarity().getColorCode() + token.getRarity().getDisplayName());
            lore.add(separator);
            lore.add("&e&l🔮 CURRENT STATUS:");
            lore.add("&a👑 Active Tier: &fMAX TIER (God Mode)");
            lore.add("&e✨ Max Passives: &7" + max.getPassiveDescription());
            if (ability != null) {
                lore.add("&b⚡ Max Ability: &f" + ability.getName() + " &7(Shift + Right Click)");
                lore.add("&e📝 Ultimate Action: &7" + ability.getDescription());
                lore.add("&e⏳ Cooldown: &f" + ability.getCooldownSeconds() + "s");
            }
            lore.add(separator);
            lore.add("&d🌟 THIS TOKEN IS FULLY MAXED OUT! 🌟");
            lore.add(separator);
            lore.add("&7Claimed: " + (plugin.data().isClaimed(player, token.getId()) ? "&aYes" : "&cNo"));
        } else {
            TokenTier current = token.tier(tier);
            TokenTier next = token.tier(tier + 1);
            TokenTier.TaskSpec task = next.getTask();
            TokenTier.AbilitySpec ability = current.getAbility();
            int progress = data.getProgress(player, token.getId());
            lore.add(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token &e[Tier " + tier + "]");
            lore.add(separator);
            lore.add("&7Rarity: " + token.getRarity().getColorCode() + token.getRarity().getDisplayName());
            lore.add(separator);
            lore.add("&e&l🔮 CURRENT STATUS:");
            lore.add("&a✔ Active Tier: &f" + tier + " / 3");
            lore.add("&e✨ Active Effects: &7" + current.getPassiveDescription());
            if (ability != null) {
                lore.add("&b⚡ Active Ability: &f" + ability.getName() + " &7(Shift + Right Click)");
                lore.add("&e📝 Ability Action: &7" + ability.getDescription());
                lore.add("&e⏳ Cooldown: &f" + ability.getCooldownSeconds() + "s");
            }
            lore.add(separator);
            lore.add("&6&l🚀 NEXT TIER UPGRADE TASK:");
            lore.add("&7- Progress: &a[" + ProgressBar.bar(progress, task.count(), 10, "&a", "&8")
                    + "&a] " + progress + "/" + task.count() + " Kills");
            lore.add("&7- Required Items: &f" + materialsList(next));
            lore.add(separator);
            lore.add("&4&l🎁 NEXT TIER BENEFITS:");
            lore.add("&7- Upgraded Ability: &f" + nextAbilityDescription(next));
            lore.add("&7- Enhanced Passives: &f" + next.getPassiveDescription());
            lore.add(separator);
            lore.add("&e⚡ Click to open Upgrade Menu!");
        }

        return ItemBuilder.of(token.getIcon()).rawName(" ").addLore(lore).build();
    }

    public static String taskTarget(TokenTier.TaskSpec task) {
        if (task.mobs().isEmpty()) {
            return task.description();
        }
        List<String> names = new ArrayList<>();
        for (org.bukkit.entity.EntityType mob : task.mobs()) {
            names.add(TextUtil.pretty(mob));
        }
        return String.join(", ", names);
    }

    public static String materialsList(TokenTier tier) {
        StringBuilder out = new StringBuilder();
        for (TokenTier.MaterialCost cost : tier.getCost()) {
            if (out.length() > 0) {
                out.append("&7, &f");
            }
            out.append(cost.amount()).append("x ").append(TextUtil.pretty(cost.material()));
        }
        return out.length() == 0 ? "None" : out.toString();
    }

    private String nextAbilityDescription(TokenTier next) {
        TokenTier.AbilitySpec ability = next.getAbility();
        return ability != null ? ability.getName() + " - " + ability.getDescription()
                : next.getPassiveDescription() + " (passive)";
    }

    // ------------------------------------------------------------------
    // Click handling
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TokenGUIHolder holder)
                || holder.getType() != TokenGUIHolder.Type.SELECTION) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }

        int slot = event.getRawSlot();
        int page = holder.<Integer>get("page") == null ? 0 : holder.<Integer>get("page");

        if (slot == MY_TOKENS_SLOT) {
            plugin.playerTokenGUI().open(player);
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

        if (slot == ACTIVE_SLOT) {
            String activeId = plugin.data().getActiveToken(player);
            if (activeId != null) {
                Token active = plugin.registry().get(activeId);
                if (active != null) {
                    SoundManager.click(player);
                    plugin.tokenUpgradeGUI().open(player, active);
                }
            }
            return;
        }

        for (int i = 0; i < TOKEN_SLOTS.length; i++) {
            if (slot == TOKEN_SLOTS[i]) {
                List<Token> tokens = plugin.registry().playerTokens();
                int index = page * TOKEN_SLOTS.length + i;
                if (index < tokens.size()) {
                    SoundManager.click(player);
                    plugin.tokenUpgradeGUI().open(player, tokens.get(index));
                }
                return;
            }
        }
    }
}
