package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.GroundVortexAnimation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Ground drop handling: when a PDC-tagged token item is dropped, the ground
 * vortex animation tracks it (cosmetic lightning on landing + rotating
 * TOTEM_OF_UNDYING/FLAME vortex that follows the item).
 */
public final class PlayerDropListener implements Listener {

    private final TokenSMP plugin;
    private final GroundVortexAnimation vortex;

    public PlayerDropListener(TokenSMP plugin, GroundVortexAnimation vortex) {
        this.plugin = plugin;
        this.vortex = vortex;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        vortex.track(event.getItemDrop());
    }
}
