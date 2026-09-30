package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
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
 * Main player menu (/tokens): every eligible player token in one polished
 * view with the exact three lore states (LOCKED / UNLOCKED / MAX TIER).
 * Left-click opens the token detail (claim / upgrade / unclaim) menu.
 */
public final class TokenSelectionGUI implements Listener {

    private static final int[] TOKEN_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19};
    private static final int MY_TOKENS_SLOT = 45;
    private static final int PROFILE_SLOT = 49;
    private static final int CLOSE_SLOT = 53;

    private final TokenSMP plugin;

    public TokenSelectionGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(
                new TokenGUIHolder(TokenGUIHolder.Type.SELECTION, player.getUniqueId()), 54,
                plugin.config().getString("gui.selection.title", "&8Token Collection"));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        int index = 0;
        for (Token token : plugin.registry().playerTokens()) {
            if (index >= TOKEN_SLOTS.length) {
                break;
            }
            inventory.setItem(TOKEN_SLOTS[index++], tokenIcon(player, token));
        }

        inventory.setItem(MY_TOKENS_SLOT, ItemBuilder.of(Material.BOOKSHELF)
                .name("&6&l📖 My Tokens")
                .addLore("&7View your claimed tokens,", "&7available tokens and progress.")
                .addLore("", "&eClick to open!").build());

        int owned = countOwned(player);
        inventory.setItem(PROFILE_SLOT, ItemBuilder.of(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name("&6&l🪙 Your Balance: &f" + owned + "&7/&f8 Tokens")
                .addLore("&7Claimed tokens act as your", "&7single ACTIVE token.", "",
                        "&7Active: " + describeActive(player))
                .build());

        inventory.setItem(CLOSE_SLOT, ItemBuilder.of(Material.BARRIER)
                .name("&cClose").build());

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

    private String describeActive(Player player) {
        String activeId = plugin.data().getActiveToken(player);
        if (activeId == null) {
            return "&cNone";
        }
        Token active = plugin.registry().get(activeId);
        return active == null ? "&cNone"
                : active.getRarity().getColorCode() + "&l" + active.getDisplayName();
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
            lore.add("&a✅ Active Tier: &f" + tier + " / 3");
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
        if (slot == MY_TOKENS_SLOT) {
            plugin.playerTokenGUI().open(player);
            return;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        if (slot == PROFILE_SLOT) {
            return;
        }

        for (int i = 0; i < TOKEN_SLOTS.length; i++) {
            if (slot == TOKEN_SLOTS[i]) {
                List<Token> tokens = plugin.registry().playerTokens();
                if (i < tokens.size()) {
                    SoundEngine.click(player);
                    plugin.tokenUpgradeGUI().open(player, tokens.get(i));
                }
                return;
            }
        }
    }
}
