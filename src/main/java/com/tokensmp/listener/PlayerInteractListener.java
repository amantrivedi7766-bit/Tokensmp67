package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.token.Token;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Token item usage: right-clicking (without sneaking) your own token item
 * claims that token through the claim manager - which enforces the
 * one-active-token rule server-side. Items owned by another player are
 * rejected with no ownership transfer. Shift + Right Click is reserved for
 * the ability activation and handled by the AbilityListener.
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
        if (event.getPlayer().isSneaking()) {
            return; // Shift + Right Click = ability activation, not claiming
        }
        ItemStack item = event.getItem();
        String tokenId = plugin.tokenItems().tokenOf(item);
        if (tokenId == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
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
        // Full claim validation (unlock state + ONE ACTIVE TOKEN rule).
        plugin.claimManager().claim(player, token);
    }
}
