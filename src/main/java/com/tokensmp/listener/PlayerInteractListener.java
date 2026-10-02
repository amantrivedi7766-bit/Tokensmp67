package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.token.AbilityTrigger;
import com.tokensmp.token.Token;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Token item usage: right-clicking your own token item claims that token
 * (server-side ownership data decides - the item alone is never enough).
 * Items owned by another player are rejected with no ownership transfer.
 */
public final class PlayerInteractListener implements Listener {

    private final TokenSMP plugin;

    public PlayerInteractListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTokenItemUse(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> { /* continue */ }
            default -> {
                return;
            }
        }
        ItemStack item = event.getItem();
        String tokenId = plugin.tokenItems().tokenOf(item);
        if (tokenId == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        // Plain right click doubles as the ability keybind for tokens whose
        // active tier declares the RIGHT_CLICK trigger (e.g. Creeper tier 1).
        if (!player.isSneaking()
                && tokenId.equals(plugin.data().getActiveToken(player))
                && plugin.abilities().activeTrigger(player) == AbilityTrigger.RIGHT_CLICK) {
            plugin.abilities().activate(player, AbilityTrigger.RIGHT_CLICK);
            return;
        }

        if (!plugin.tokenItems().isClaimable(item)) {
            plugin.messages().send(player, "messages.item-not-claimable",
                    "&cThis token item cannot be claimed (missing claimable marker).");
            return;
        }
        Token token = plugin.registry().get(tokenId);

        // Ownership check: the item's PDC owner must be this player.
        String owner = plugin.tokenItems().ownerOf(item);
        if (owner != null && !owner.equals(player.getUniqueId().toString())
                && !player.hasPermission("tokensmp.admin")) {
            plugin.messages().send(player, "messages.item-not-owner",
                    "&cThis token item belongs to another player!");
            return;
        }

        if (token == null) {
            plugin.messages().send(player, "messages.item-unknown-token",
                    "&cThis token item references an unknown token.");
            return;
        }
        if (plugin.data().isClaimed(player, token.getId())) {
            plugin.messages().send(player, "messages.already-active",
                    "&eThis token is already your active token.");
            return;
        }
        if (!plugin.data().hasUnlocked(player, token.getId())) {
            // Right-clicking a token item unlocks it at tier 1 first.
            plugin.data().setTier(player, token.getId(), 1);
        }
        plugin.data().claim(player, token.getId());
        plugin.passiveManager().applyPassives(player);
        SoundEngine.levelUp(player);
        plugin.messages().send(player, "messages.token-claimed",
                "&aYou claimed the {color}&l{token} Token&a! It is now ACTIVE.",
                "{color}", token.getRarity().getColorCode(),
                "{token}", token.getDisplayName());
    }
}
