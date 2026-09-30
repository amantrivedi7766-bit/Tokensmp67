package com.tokensmp.gui;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin control panel (/tokensadmin, tokensmp.admin only):
 * Server Controls, Player Management, Token Management, Token Spinning,
 * Progress Management, Cooldown Management, Reload Configuration and
 * Statistics. Destructive actions always run through a confirmation menu,
 * and the Admin Token is only visible/approachable to authorized admins.
 */
public final class AdminGUI implements Listener {

    private static final int[] PLAYER_HEAD_SLOTS = {10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int[] TOKEN_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};
    private static final int BACK_SLOT = 45;
    private static final int CLOSE_SLOT = 53;

    private static final int CONFIRM_YES = 11;
    private static final int CONFIRM_NO = 15;

    private final TokenSMP plugin;

    public AdminGUI(TokenSMP plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Main menu
    // ------------------------------------------------------------------

    public void openMain(Player admin) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_MAIN, admin.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.admin.title", "&8TokenSMP &c&lADMIN"));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(11, ItemBuilder.of(Material.PLAYER_HEAD)
                .name("&c&l👥 Player Management")
                .addLore("&7Select a player to manage their", "&7tokens, progress and cooldowns.",
                        "", "&eClick to open the player list!").build());
        inventory.setItem(13, ItemBuilder.of(Material.COMPARATOR)
                .name("&6&l📊 Statistics")
                .addLore(statisticsLore()).build());
        inventory.setItem(15, ItemBuilder.of(Material.REPEATING_COMMAND_BLOCK)
                .name("&c&l⚙ Reload Configuration")
                .addLore("&7Atomically reload config.yml", "&7without a server reboot.",
                        "", "&cClick for confirmation!").build());
        inventory.setItem(CLOSE_SLOT, ItemBuilder.of(Material.BARRIER).name("&cClose").build());

        admin.openInventory(inventory);
    }

    private String[] statisticsLore() {
        int online = Bukkit.getOnlinePlayers().size();
        int tokens = plugin.registry().all().size();
        int activeCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.data().getActiveToken(player) != null) {
                activeCount++;
            }
        }
        return new String[]{
                "&7Online players: &f" + online,
                "&7Registered tokens: &f" + tokens + " &7(8 player + 1 admin)",
                "&7Players with an active token: &f" + activeCount,
                "&7Spin pool size: &f" + plugin.registry().playerTokens().size(),
                "&7Token stealing: " + (plugin.config().getBoolean("token-stealing.enabled", true)
                        ? "&aenabled" : "&cdisabled")};
    }

    // ------------------------------------------------------------------
    // Player list
    // ------------------------------------------------------------------

    public void openPlayers(Player admin) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_PLAYERS, admin.getUniqueId());
        Map<Integer, String> slotMap = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.admin.players-title", "&8Admin » Select Player"));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }
        int index = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (index >= PLAYER_HEAD_SLOTS.length) {
                break;
            }
            slotMap.put(PLAYER_HEAD_SLOTS[index], online.getName());
            inventory.setItem(PLAYER_HEAD_SLOTS[index++], ItemBuilder.of(Material.PLAYER_HEAD)
                    .skullOwner(online)
                    .name("&e" + online.getName())
                    .addLore("&7Active: " + describeActive(online),
                            "&7Owned: &f" + plugin.data().getUnlocked(online, plugin.registry().playerTokens()).size()
                                    + "&7/&f8",
                            "", "&eClick to manage!").build());
        }
        if (index == 0) {
            inventory.setItem(22, ItemBuilder.of(Material.STRUCTURE_VOID)
                    .name("&7No players online").build());
        }
        holder.set("slotMap", slotMap);
        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("&7« Back to Admin Menu").build());
        admin.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Selected player management
    // ------------------------------------------------------------------

    public void openPlayerMenu(Player admin, Player target) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_PLAYER, admin.getUniqueId());
        holder.set("target", target.getName());
        Inventory inventory = Bukkit.createInventory(holder, 54,
                plugin.config().getString("gui.admin.player-title", "&8Admin » {player}")
                        .replace("{player}", target.getName()));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, ItemBuilder.of(Material.PLAYER_HEAD).skullOwner(target)
                .name("&e&l" + target.getName())
                .addLore(ownershipLore(target)).build());
        inventory.setItem(10, ItemBuilder.of(Material.ENDER_CHEST)
                .name("&5&l🎰 Force Token Spin")
                .addLore("&7Launch the cinematic token spin", "&7for this player (never Admin Token).",
                        "", "&eClick to spin!").build());
        inventory.setItem(11, ItemBuilder.of(Material.CHEST)
                .name("&e&l🎁 Give Token")
                .addLore("&7Give any token item, including", "&7the isolated &dAdmin Token&7.").build());
        inventory.setItem(12, ItemBuilder.of(Material.TNT)
                .name("&c&l✘ Remove Token")
                .addLore("&7Completely remove a token", "&7(tier, progress, claim state).",
                        "", "&cConfirmation required!").build());
        inventory.setItem(13, ItemBuilder.of(Material.ANVIL)
                .name("&6&l⬆ Force Upgrade")
                .addLore("&7Instantly upgrade a token to", "&7its next tier (task-free).").build());
        inventory.setItem(14, ItemBuilder.of(Material.CLOCK)
                .name("&b&l⏳ Reset Cooldowns")
                .addLore("&7Clear every token ability", "&7cooldown for this player.").build());
        inventory.setItem(15, ItemBuilder.of(Material.MAP)
                .name("&a&l📈 Set Progress")
                .addLore("&7Adjust task grind progress", "&7with +/- value buttons.").build());
        inventory.setItem(16, ItemBuilder.of(Material.LEVER)
                .name("&e&l⚖ Manage Claim State")
                .addLore("&7Toggle the claim/active state", "&7of any token.").build());
        inventory.setItem(19, ItemBuilder.of(Material.WRITTEN_BOOK)
                .name("&6&l🔍 Inspect Active Token")
                .addLore("&7View full details of the", "&7player's current active token.").build());

        inventory.setItem(BACK_SLOT, ItemBuilder.of(Material.ARROW)
                .name("&7« Back to Player List").build());
        admin.openInventory(inventory);
    }

    private String[] ownershipLore(Player target) {
        StringBuilder owned = new StringBuilder();
        TokenDataManager data = plugin.data();
        for (Token token : plugin.registry().playerTokens()) {
            int tier = data.getTier(target, token.getId());
            if (tier > 0) {
                if (owned.length() > 0) {
                    owned.append("&7, ");
                }
                owned.append(token.getRarity().getColorCode()).append(token.getDisplayName())
                        .append(" &7(T").append(tier)
                        .append(data.isClaimed(target, token.getId()) ? " ✔" : "").append(")");
            }
        }
        return new String[]{
                "&7Active: " + describeActive(target),
                "&7Owned tokens:",
                owned.length() == 0 ? "&cNone" : owned.toString()};
    }

    // ------------------------------------------------------------------
    // Token list for an action (give/remove/upgrade/progress/claim)
    // ------------------------------------------------------------------

    public void openTokenList(Player admin, Player target, String action) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_TOKENS, admin.getUniqueId());
        holder.set("target", target.getName());
        holder.set("action", action);
        Map<Integer, String> slotMap = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(holder, 27,
                plugin.config().getString("gui.admin.tokens-title", "&8Admin » {player} » {action}")
                        .replace("{player}", target.getName())
                        .replace("{action}", action));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 27; slot++) {
            inventory.setItem(slot, filler);
        }
        int index = 0;
        for (Token token : plugin.registry().all()) {
            if (index >= TOKEN_SLOTS.length) {
                break;
            }
            slotMap.put(TOKEN_SLOTS[index], token.getId());
            TokenTier current = token.tier(plugin.data().getTier(target, token.getId()));
            inventory.setItem(TOKEN_SLOTS[index++], ItemBuilder.of(token.getIcon())
                    .name(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token"
                            + (token.isAdminToken() ? " &4[ADMIN]" : ""))
                    .addLore("&7Rarity: " + token.getRarity().getColorCode() + token.getRarity().getDisplayName(),
                            "&7Tier: " + (current == null ? "&c0" : "&e" + current.getTier()),
                            "",
                            "&eClick to " + action + "!").build());
        }
        holder.set("slotMap", slotMap);
        inventory.setItem(22, ItemBuilder.of(Material.ARROW).name("&7« Back").build());
        admin.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Progress value menu (+100 / +1000 / -100 / reset)
    // ------------------------------------------------------------------

    public void openProgressMenu(Player admin, Player target, Token token) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_PLAYER, admin.getUniqueId());
        holder.set("target", target.getName());
        holder.set("token", token.getId());
        holder.set("mode", "progress");
        Inventory inventory = Bukkit.createInventory(holder, 27,
                plugin.config().getString("gui.admin.progress-title", "&8Progress » {player} » {token}")
                        .replace("{player}", target.getName())
                        .replace("{token}", token.getDisplayName()));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 27; slot++) {
            inventory.setItem(slot, filler);
        }
        TokenTier next = token.tier(plugin.data().getTier(target, token.getId()) + 1);
        int required = next == null ? 0 : next.getTask().count();
        int current = plugin.data().getProgress(target, token.getId());
        inventory.setItem(4, ItemBuilder.of(token.getIcon())
                .name("&6&l" + token.getDisplayName() + " progress")
                .addLore("&7Current: &f" + current + (required > 0 ? " &7/ &f" + required : ""),
                        "&7Target mob: &f" + (next == null ? "-"
                                : TokenSelectionGUI.taskTarget(next.getTask()))).build());
        inventory.setItem(10, ItemBuilder.of(Material.LIME_DYE).name("&a&l+100").build());
        inventory.setItem(11, ItemBuilder.of(Material.LIME_CONCRETE_POWDER).name("&a&l+1000").build());
        inventory.setItem(13, ItemBuilder.of(Material.SUNFLOWER).name("&e&lComplete task").build());
        inventory.setItem(15, ItemBuilder.of(Material.REDSTONE).name("&c&l-100").build());
        inventory.setItem(16, ItemBuilder.of(Material.TNT_MINECART).name("&c&lReset to 0").build());
        inventory.setItem(22, ItemBuilder.of(Material.ARROW).name("&7« Back").build());
        admin.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Confirmation menu (destructive actions)
    // ------------------------------------------------------------------

    public void openConfirm(Player admin, Player target, Token token, String action) {
        TokenGUIHolder holder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_CONFIRM, admin.getUniqueId());
        holder.set("target", target.getName());
        holder.set("token", token.getId());
        holder.set("action", action);
        Inventory inventory = Bukkit.createInventory(holder, 27,
                plugin.config().getString("gui.admin.confirm-title", "&8Confirm: {action} {token}?")
                        .replace("{action}", action).replace("{token}", token.getDisplayName()));

        ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
        for (int slot = 0; slot < 27; slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(4, ItemBuilder.of(token.getIcon())
                .name(token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token")
                .addLore("&7Player: &f" + target.getName(),
                        "&7Action: &c" + action).build());
        inventory.setItem(CONFIRM_YES, ItemBuilder.of(Material.LIME_CONCRETE)
                .name("&a&l[CONFIRM]").build());
        inventory.setItem(CONFIRM_NO, ItemBuilder.of(Material.RED_CONCRETE)
                .name("&c&l[CANCEL]").build());
        admin.openInventory(inventory);
    }

    // ------------------------------------------------------------------
    // Click routing
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TokenGUIHolder holder)) {
            return;
        }
        switch (holder.getType()) {
            case ADMIN_MAIN -> handleMainClick(event, holder);
            case ADMIN_PLAYERS -> handlePlayersClick(event, holder);
            case ADMIN_PLAYER -> handlePlayerMenuClick(event, holder);
            case ADMIN_TOKENS -> handleTokensClick(event, holder);
            case ADMIN_CONFIRM -> handleConfirmClick(event, holder);
            default -> { /* not ours */ }
        }
    }

    private void handleMainClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player admin)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 11) {
            SoundEngine.click(admin);
            openPlayers(admin);
        } else if (slot == 13) {
            SoundEngine.click(admin);
            openMain(admin); // statistics live in the menu item lore - refresh
        } else if (slot == 15) {
            SoundEngine.click(admin);
            // Reload also runs through a confirmation.
            TokenGUIHolder confirmHolder = new TokenGUIHolder(TokenGUIHolder.Type.ADMIN_CONFIRM,
                    admin.getUniqueId());
            confirmHolder.set("action", "reload");
            Inventory inventory = Bukkit.createInventory(confirmHolder, 27, "&8Confirm: reload configuration?");
            ItemStack filler = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).rawName(" ").build();
            for (int i = 0; i < 27; i++) {
                inventory.setItem(i, filler);
            }
            inventory.setItem(4, ItemBuilder.of(Material.REPEATING_COMMAND_BLOCK)
                    .name("&c&lReload configuration").addLore("&7Are you sure?").build());
            inventory.setItem(CONFIRM_YES, ItemBuilder.of(Material.LIME_CONCRETE).name("&a&l[CONFIRM]").build());
            inventory.setItem(CONFIRM_NO, ItemBuilder.of(Material.RED_CONCRETE).name("&c&l[CANCEL]").build());
            admin.openInventory(inventory);
        } else if (slot == CLOSE_SLOT) {
            admin.closeInventory();
        }
    }

    private void handlePlayersClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player admin)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        if (event.getRawSlot() == BACK_SLOT) {
            SoundEngine.click(admin);
            openMain(admin);
            return;
        }
        Map<Integer, String> slotMap = holder.get("slotMap");
        String name = slotMap == null ? null : slotMap.get(event.getRawSlot());
        Player target = name == null ? null : Bukkit.getPlayerExact(name);
        if (target != null) {
            SoundEngine.click(admin);
            openPlayerMenu(admin, target);
        }
    }

    private void handlePlayerMenuClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player admin)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        Player target = Bukkit.getPlayerExact(holder.getString("target"));
        if (target == null) {
            plugin.messages().send(admin, "messages.player-not-found", "&cPlayer is no longer online.");
            openPlayers(admin);
            return;
        }
        // Progress value mode.
        if ("progress".equals(holder.getString("mode"))) {
            handleProgressValueClick(event, holder, admin, target);
            return;
        }
        int slot = event.getRawSlot();
        switch (slot) {
            case BACK_SLOT -> {
                SoundEngine.click(admin);
                openPlayers(admin);
            }
            case 10 -> { // Force spin
                SoundEngine.click(admin);
                plugin.messages().send(admin, "messages.admin-force-spin",
                        "&eLaunched the token spin for &f{player}&e.", "{player}", target.getName());
                plugin.spinLauncher().launchFor(target);
            }
            case 11 -> openTokenList(admin, target, "give");
            case 12 -> openTokenList(admin, target, "remove");
            case 13 -> openTokenList(admin, target, "upgrade");
            case 14 -> { // Reset cooldowns
                SoundEngine.click(admin);
                plugin.cooldowns().clearAll(target);
                plugin.messages().send(admin, "messages.admin-cooldowns-reset",
                        "&aCleared all cooldowns for &f{player}&a.", "{player}", target.getName());
            }
            case 15 -> openTokenList(admin, target, "progress");
            case 16 -> openTokenList(admin, target, "claim");
            case 19 -> { // Inspect active
                String activeId = plugin.data().getActiveToken(target);
                Token active = activeId == null ? null : plugin.registry().get(activeId);
                if (active == null) {
                    plugin.messages().send(admin, "messages.admin-no-active",
                            "&c{player} has no active token.", "{player}", target.getName());
                } else {
                    TokenTier tier = active.tier(plugin.data().getTier(target, active.getId()));
                    plugin.messages().raw(admin, "&8[&6TokenSMP&8] &e" + target.getName() + " active token:");
                    plugin.messages().raw(admin, "&7- Token: " + active.getRarity().getColorCode()
                            + active.getDisplayName());
                    plugin.messages().raw(admin, "&7- Tier: &f" + (tier == null ? "?" : tier.getTier()));
                    plugin.messages().raw(admin, "&7- Passives: &f"
                            + (tier == null ? "?" : tier.getPassiveDescription()));
                }
            }
            default -> { /* filler */ }
        }
    }

    private void handleTokensClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player admin)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        if (event.getRawSlot() == 22) {
            SoundEngine.click(admin);
            Player back = Bukkit.getPlayerExact(holder.getString("target"));
            if (back != null) {
                openPlayerMenu(admin, back);
            } else {
                openPlayers(admin);
            }
            return;
        }
        Map<Integer, String> slotMap = holder.get("slotMap");
        String tokenId = slotMap == null ? null : slotMap.get(event.getRawSlot());
        Token token = plugin.registry().get(tokenId);
        if (token == null) {
            return;
        }
        Player target = Bukkit.getPlayerExact(holder.getString("target"));
        if (target == null) {
            plugin.messages().send(admin, "messages.player-not-found", "&cPlayer is no longer online.");
            openPlayers(admin);
            return;
        }
        String action = holder.getString("action");
        switch (action == null ? "" : action) {
            case "give" -> {
                SoundEngine.click(admin);
                plugin.tokenItems().give(target, token, Math.max(1, plugin.data().getTier(target, token.getId())));
                plugin.messages().send(admin, "messages.admin-gave",
                        "&aGave the {color}&l{token} Token &ato &f{player}&a.",
                        "{color}", token.getRarity().getColorCode(),
                        "{token}", token.getDisplayName(),
                        "{player}", target.getName());
                if (token.isAdminToken()) {
                    plugin.messages().alertAdmins("messages.admin-token-alert",
                            "&c[ALERT] &e{admin} &7gave the &cADMIN TOKEN &7to &e{player}&7!",
                            "{admin}", admin.getName(), "{player}", target.getName());
                }
            }
            case "remove" -> openConfirm(admin, target, token, "remove");
            case "upgrade" -> {
                SoundEngine.click(admin);
                forceUpgrade(admin, target, token);
            }
            case "progress" -> {
                SoundEngine.click(admin);
                openProgressMenu(admin, target, token);
            }
            case "claim" -> {
                SoundEngine.click(admin);
                TokenDataManager data = plugin.data();
                if (data.isClaimed(target, token.getId())) {
                    data.unclaim(target, token.getId());
                    plugin.passiveManager().clearPassives(target);
                    plugin.messages().send(admin, "messages.admin-unclaimed",
                            "&eUnclaimed the {token} Token &efor &f{player}&e.",
                            "{token}", token.getDisplayName(), "{player}", target.getName());
                } else {
                    if (data.getTier(target, token.getId()) <= 0) {
                        plugin.messages().send(admin, "messages.admin-not-unlocked",
                                "&c{player} has not unlocked that token.",
                                "{player}", target.getName());
                        return;
                    }
                    data.claim(target, token.getId());
                    plugin.passiveManager().applyPassives(target);
                    plugin.messages().send(admin, "messages.admin-claimed",
                            "&aClaimed the {token} Token &afor &f{player}&a.",
                            "{token}", token.getDisplayName(), "{player}", target.getName());
                }
                openPlayerMenu(admin, target);
            }
            default -> { /* unknown */ }
        }
    }

    private void handleProgressValueClick(InventoryClickEvent event, TokenGUIHolder holder,
                                          Player admin, Player target) {
        Token token = plugin.registry().get(holder.getString("token"));
        if (token == null) {
            openPlayers(admin);
            return;
        }
        TokenDataManager data = plugin.data();
        int current = data.getProgress(target, token.getId());
        TokenTier next = token.tier(data.getTier(target, token.getId()) + 1);
        switch (event.getRawSlot()) {
            case 10 -> data.setProgress(target, token.getId(), current + 100);
            case 11 -> data.setProgress(target, token.getId(), current + 1000);
            case 13 -> {
                if (next != null) {
                    data.setProgress(target, token.getId(), next.getTask().count());
                }
            }
            case 15 -> data.setProgress(target, token.getId(), Math.max(0, current - 100));
            case 16 -> data.setProgress(target, token.getId(), 0);
            case 22 -> {
                SoundEngine.click(admin);
                openTokenList(admin, target, "progress");
                return;
            }
            default -> {
                return;
            }
        }
        SoundEngine.click(admin);
        openProgressMenu(admin, target, token); // refresh values
    }

    private void handleConfirmClick(InventoryClickEvent event, TokenGUIHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player admin)
                || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof TokenGUIHolder)) {
            return;
        }
        int slot = event.getRawSlot();
        String action = holder.getString("action");
        if (slot == CONFIRM_NO) {
            SoundEngine.click(admin);
            admin.closeInventory();
            return;
        }
        if (slot != CONFIRM_YES) {
            return;
        }
        SoundEngine.click(admin);
        if ("reload".equals(action)) {
            plugin.reloadPlugin();
            plugin.messages().send(admin, "messages.config-reloaded",
                    "&aConfiguration reloaded atomically (no reboot required).");
            return;
        }
        Player target = Bukkit.getPlayerExact(holder.getString("target"));
        Token token = plugin.registry().get(holder.getString("token"));
        if (target == null || token == null) {
            admin.closeInventory();
            return;
        }
        if ("remove".equals(action)) {
            plugin.data().setTier(target, token.getId(), 0);
            plugin.data().setProgress(target, token.getId(), 0);
            plugin.data().unclaim(target, token.getId());
            plugin.passiveManager().clearPassives(target);
            plugin.messages().send(admin, "messages.admin-removed",
                    "&cRemoved the {token} Token &cfrom &f{player}&c.",
                    "{token}", token.getDisplayName(), "{player}", target.getName());
        }
        admin.closeInventory();
    }

    private void forceUpgrade(Player admin, Player target, Token token) {
        TokenDataManager data = plugin.data();
        int tier = data.getTier(target, token.getId());
        if (tier >= token.getMaxTier()) {
            plugin.messages().send(admin, "messages.already-maxed",
                    "&dThis token is already at MAX TIER!");
            return;
        }
        int newTier = tier + 1;
        data.setTier(target, token.getId(), newTier);
        data.setProgress(target, token.getId(), 0);
        data.claim(target, token.getId());
        plugin.passiveManager().applyPassives(target);
        plugin.messages().send(admin, "messages.admin-upgraded",
                "&aForced {token} Token &aof &f{player} &ato Tier &f{tier}&a.",
                "{token}", token.getDisplayName(),
                "{player}", target.getName(),
                "{tier}", String.valueOf(newTier));
    }

    private String describeActive(Player player) {
        String activeId = plugin.data().getActiveToken(player);
        Token active = activeId == null ? null : plugin.registry().get(activeId);
        return active == null ? "&cNone"
                : active.getRarity().getColorCode() + "&l" + active.getDisplayName();
    }
}
