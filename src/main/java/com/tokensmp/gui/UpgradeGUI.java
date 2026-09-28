package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ItemBuilder;
import com.tokensmp.util.PlayerUtil;
import com.tokensmp.util.ProgressBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-token detail view reached from the selection menu:
 * LEFT half shows the upgrade requirements (task + materials), RIGHT side the
 * live task parameters, with CLAIM / UNCLAIM / UPGRADE / BACK actions.
 * UNCLAIM always runs through a confirmation GUI ([CONFIRM] / [CANCEL]).
 */
public final class UpgradeGUI implements Listener {

    private static final int[] MATERIAL_SLOTS = {10, 11, 12, 13};
    private static final int TASK_SLOT = 15;
    private static final int TASK_MOB_SLOT = 16;
    private static final int CLAIM_SLOT = 31;
    private static final int BACK_SLOT = 45;
    private static final int UPGRADE_SLOT = 49;
    private static final int UNCLAIM_SLOT = 53;

    private static final int CONFIRM_YES = 11;
    private static final int CONFIRM_NO = 15;

    private final TokenSMP plugin;

    public UpgradeGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Token detail view
    // ------------------------------------------------------------------

    public void open(Player player, Token token) {
        TokenDataManager data = plugin.data();
        int tier = data.getTier(player, token.getId());
        boolean claimed = data.isClaimed(player, token.getId());

        Inventory inventory = Bukkit.createInventory(
                new TokenGUIHolder(TokenGUIHolder.Type.TOKEN_DETAIL, player.getUniqueId()), 54,
                plugin.config().getString("gui.upgrade.title", "&8Upgrade: {token} &7[Tier {tier}]")
                        .replace("{token}", token.getDisplayName())
                        .replace("{tier}", String.valueOf(tier)));
        holder_set(inventory, "token", token.getId());

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, plugin.selectionGUI().tokenIcon(player, token));

        TokenTier next = token.tier(tier + 1);
        if (next != null) {
            List<TokenTier.MaterialCost> cost = next.getCost();
            for (int i = 0; i < MATERIAL_SLOTS.length && i < cost.size(); i++) {
                TokenTier.MaterialCost entry = cost.get(i);
                int have = PlayerUtil.count(player, entry.material());
                inventory.setItem(MATERIAL_SLOTS[i], ItemBuilder.of(entry.material())
                        .name("&e" + com.tokensmp.util.TextUtil.pretty(entry.material()))
                        .addLore("&7Required: &f" + entry.amount() + "x",
                                "&7In inventory: " + (have >= entry.amount() ? "&a" : "&c") + have + "x",
                                "",
                                have >= entry.amount() ? "&a✔ Material requirement met!"
                                        : "&c✘ Not enough materials!").build());
            }

            TokenTier.TaskSpec task = next.getTask();
            int progress = data.getProgress(player, token.getId());
            inventory.setItem(TASK_SLOT, ItemBuilder.of(Material.WRITABLE_BOOK)
                    .name("&6&l🎯 UPGRADE TASK")
                    .addLore("&7- Task: &f" + task.description(),
                            "&7- Progress: &a[" + ProgressBar.bar(progress, task.count(), 10, "&a", "&8")
                                    + "&a] " + progress + "/" + task.count(),
                            "",
                            progress >= task.count() ? "&a✔ Task complete!"
                                    : "&c✘ Task not complete yet!").build());
            inventory.setItem(TASK_MOB_SLOT, ItemBuilder.of(Material.SPAWNER)
                    .name("&b&l⚡ TARGET")
                    .addLore("&7Target:", "&f" + PlayerGUI.taskTarget(task)).build());

            boolean ready = progress >= task.count() && materialsMet(player, next);
            inventory.setItem(UPGRADE_SLOT, ItemBuilder.of(ready ? Material.LIME_CONCRETE : Material.RED_CONCRETE)
                    .name(ready ? "&a&l✔ CONFIRM UPGRADE" : "&c&l✘ REQUIREMENTS NOT MET")
                    .addLore(ready ? "&7Click to consume materials and upgrade!"
                            : "&7Complete the task and gather materials first.").build());
        } else {
            inventory.setItem(UPGRADE_SLOT, ItemBuilder.of(Material.GOLDEN_APPLE)
                    .name("&5&l🌟 MAX TIER")
                    .addLore("&dThis token is fully maxed out!").build());
        }

        if (tier <= 0) {
            inventory.setItem(CLAIM_SLOT, ItemBuilder.of(Material.OAK_BUTTON)
                    .name("&c&l🔒 LOCKED")
                    .addLore("&7Unlock this token via the spin", "&7or by claiming it from a",
                            "&7defeated player!").build());
        } else if (claimed) {
            inventory.setItem(CLAIM_SLOT, ItemBuilder.of(Material.LIME_DYE)
                    .name("&a&l✔ ACTIVE TOKEN")
                    .addLore("&7This is your active token.", "&7Abilities: Shift + Right Click").build());
            inventory.setItem(UNCLAIM_SLOT, ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&c&l✘ UNCLAIM TOKEN")
                    .addLore("&7Deactivate this token.", "&cRequires confirmation!").build());
        } else {
            inventory.setItem(CLAIM_SLOT, ItemBuilder.of(Material.EMERALD)
                    .name("&a&l✔ CLAIM TOKEN")
                    .addLore("&7Activate this token to use its", "&7tier abilities and passives!").build());
        }

        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("&7« Back to Token Selection").build());

        player.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Unclaim confirmation view ([CONFIRM] / [CANCEL])
    // ------------------------------------------------------------------

    public void openUnclaimConfirm(Player player, Token token) {
        Inventory inventory = Bukkit.createInventory(
                new TokenGUIHolder(TokenGUIHolder.Type.CONFIRM, player.getUniqueId()), 27,
                plugin.config().getString("gui.unclaim.title", "&8Unclaim {token}?")
                        .replace("{token}", token.getDisplayName()));
        holder_set(inventory, "token", token.getId());
        holder_set(inventory, "action", "unclaim");

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 27; slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(4, ItemBuilder.of(token.getIcon())
                .name(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token")
                .addLore("&7Are you sure you want to", "&7unclaim (deactivate) it?").build());
        inventory.setItem(CONFIRM_YES, ItemBuilder.of(Material.LIME_CONCRETE)
                .name("&a&l[CONFIRM]").addLore("&7Unclaim the token.").build());
        inventory.setItem(CONFIRM_NO, ItemBuilder.of(Material.RED_CONCRETE)
                .name("&c&l[CANCEL]").addLore("&7Keep the token active.").build());
        player.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Small holder helper
    // ------------------------------------------------------------------

    private static void holder_set(Inventory inventory, String key, Object value) {
        if (inventory.getHolder() instanceof TokenGUIHolder holder) {
            holder.set(key, value);
        }
    }

    // ------------------------------------------------------------------
    // Requirement helpers
    // ------------------------------------------------------------------

    private boolean materialsMet(Player player, TokenTier tier) {
        for (TokenTier.MaterialCost cost : tier.getCost()) {
            if (!PlayerUtil.has(player, cost.material(), cost.amount())) {
                return false;
            }
        }
        return true;
    }

    private void consume(Player player, TokenTier tier) {
        Map<org.bukkit.Material, Integer> aggregated = new HashMap<>();
        for (TokenTier.MaterialCost cost : tier.getCost()) {
            aggregated.merge(cost.material(), cost.amount(), Integer::sum);
        }
        PlayerUtil.removeAll(player, aggregated);
    }

    // ------------------------------------------------------------------
    // Click handling
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TokenGUIHolder holder)) {
            return;
        }
        if (holder.getType() == TokenGUIHolder.Type.CONFIRM) {
            handleConfirmClick(event, holder);
            return;
        }
        if (holder.getType() != TokenGUIHolder.Type.TOKEN_DETAIL) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }

        String tokenId = holder.getString("token");
        Token token = plugin.registry().get(tokenId);
        if (token == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot == BACK_SLOT) {
            plugin.selectionGUI().open(player);
            return;
        }
        if (slot == CLAIM_SLOT) {
            handleClaim(player, token);
            return;
        }
        if (slot == UNCLAIM_SLOT) {
            SoundManager.click(player);
            openUnclaimConfirm(player, token);
            return;
        }
        if (slot == UPGRADE_SLOT) {
            handleUpgrade(player, token);
        }
    }

    private void handleClaim(Player player, Token token) {
        TokenDataManager data = plugin.data();
        int tier = data.getTier(player, token.getId());
        if (tier <= 0) {
            // Not unlocked normally - but maybe this is a stolen-token claim.
            String pendingSteal = data.getPendingSteal(player);
            if (token.getId().equals(pendingSteal)) {
                handleStealClaim(player, token);
                return;
            }
            plugin.messages().send(player, "messages.token-locked",
                    "&cYou have not unlocked this token yet!");
            SoundManager.denied(player);
            return;
        }
        if (data.isClaimed(player, token.getId())) {
            return; // already claimed - nothing to do, no duplication
        }
        // Full validation chain: unlock state + ONE ACTIVE TOKEN rule.
        if (plugin.claimManager().claim(player, token)) {
            open(player, token);
        }
    }

    /**
     * Server-authoritative ownership transfer for the token-stealing system:
     * the killer claims the defeated victim's active token (same tier), the
     * victim loses the active state - never a duplicated token.
     */
    private void handleStealClaim(Player player, Token token) {
        TokenDataManager data = plugin.data();
        String victimName = data.getStealVictim(player);
        // Server-authoritative transfer through the claim manager: direct
        // claim (no spin), one-active-token rule enforced, never duplicated.
        if (plugin.claimManager().claimStolen(player, token, victimName)) {
            open(player, token);
        }
    }

    private void handleUpgrade(Player player, Token token) {
        TokenDataManager data = plugin.data();
        int tier = data.getTier(player, token.getId());
        TokenTier next = token.tier(tier + 1);
        if (next == null) {
            return;
        }
        TokenTier.TaskSpec task = next.getTask();
        if (data.getProgress(player, token.getId()) < task.count() || !materialsMet(player, next)) {
            SoundManager.denied(player);
            plugin.messages().send(player, "messages.upgrade-not-ready",
                    "&cRequirements not met: complete the task and gather the materials!");
            return;
        }
        consume(player, next);
        int newTier = tier + 1;
        data.setTier(player, token.getId(), newTier);
        data.setProgress(player, token.getId(), 0);
        player.closeInventory();

        SoundManager.levelUp(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        plugin.messages().title(player,
                plugin.config().getString("gui.upgrade.title-flash", "&6&lTIER {tier} REACHED!")
                        .replace("{tier}", String.valueOf(newTier)),
                token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token",
                10, 60, 10);
        if (data.isClaimed(player, token.getId())) {
            plugin.passiveManager().applyPassives(player);
        }
        if (newTier == 1) {
            plugin.messages().broadcast("messages.first-unlock",
                    "&e{player} &7just unlocked the {color}&l{token} Token &7(&fRarity: {rarity}&7)!",
                    "{player}", player.getName(),
                    "{color}", token.getRarity().getColorCode(),
                    "{token}", token.getDisplayName(),
                    "{rarity}", token.getRarity().getDisplayName());
        } else {
            plugin.messages().broadcast("messages.tier-upgrade",
                    "&e{player} &7upgraded the {color}&l{token} Token &7to &eTier {tier}&7!",
                    "{player}", player.getName(),
                    "{color}", token.getRarity().getColorCode(),
                    "{token}", token.getDisplayName(),
                    "{tier}", String.valueOf(newTier));
        }
    }

    private void handleConfirmClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        String tokenId = holder.getString("token");
        Token token = plugin.registry().get(tokenId);
        if (token == null) {
            player.closeInventory();
            return;
        }
        int slot = event.getRawSlot();
        if (slot == CONFIRM_NO) {
            SoundManager.click(player);
            open(player, token);
            return;
        }
        if (slot != CONFIRM_YES) {
            return;
        }
        if (!"unclaim".equals(holder.getString("action"))) {
            return;
        }
        SoundManager.click(player);
        // Immediate disable: ability, passives, animations, temporary state.
        plugin.claimManager().unclaim(player, token);
        open(player, token);
    }
}
