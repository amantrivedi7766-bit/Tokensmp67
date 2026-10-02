package com.tokensmp.ability;

import com.tokensmp.token.AbilityTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Listens for the shift-based ability keybinds and forwards them to the
 * AbilityManager, which owns the full validation chain (including whether the
 * active ability actually uses that trigger).
 *
 * - sneak + right click -> SHIFT_RIGHT_CLICK
 * - sneak + left click  -> SHIFT_LEFT_CLICK
 *
 * The player's held item is irrelevant here - server-side ownership data
 * decides. Plain right click is handled by the token item listener so it can
 * coexist with claiming.
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
        AbilityTrigger trigger = switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> AbilityTrigger.SHIFT_RIGHT_CLICK;
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> AbilityTrigger.SHIFT_LEFT_CLICK;
            default -> null;
        };
        if (trigger == null) {
            return;
        }
        event.setCancelled(true);
        abilityManager.activate(player, trigger);
    }
}
