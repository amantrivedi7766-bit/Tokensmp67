package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/**
 * Arrow trail hook (Skeleton T3 passive): newly launched arrows from players
 * whose active token has the arrow-trail passive get a CRIT + SNOWFLAKE
 * trail that follows the projectile.
 */
public final class ProjectileListener implements Listener {

    private final TokenSMP plugin;

    public ProjectileListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player shooter)) {
            return;
        }
        com.tokensmp.token.TokenTier tier = plugin.passiveManager().activeTier(shooter);
        if (tier == null || !tier.hasArrowTrail()) {
            return;
        }
        plugin.projectileEngine().attachTrail(arrow, shooter, true);
    }
}
