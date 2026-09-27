package com.tokensmp.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Listens for the global ability keybind (SHIFT + RIGHT CLICK) and forwards
 * to the AbilityManager, which owns the full validation chain. The player's
 * held item is irrelevant - server-side ownership data decides.
 */
public final class AbilityListener implements Listener {

    private final AbilityManager abilityManager;

    public AbilityListener(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKeybind(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> { /* continue */ }
            default -> {
                return;
            }
        }
        event.setCancelled(true);
        abilityManager.activate(player);
    }
}
