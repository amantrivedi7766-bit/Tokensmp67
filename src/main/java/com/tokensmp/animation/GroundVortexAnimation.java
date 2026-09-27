package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.SchedulerManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ground drop vortex for token items: when a PDC-tagged token item
 * lands, a visual-only lightning strike hits the spot and a rotating upward
 * vortex of TOTEM_OF_UNDYING + FLAME particles (radius 0.8, height 2, every
 * 4 ticks) follows the item until it despawns or is picked up. The vortex
 * task self-cancels - no leaks.
 */
public final class GroundVortexAnimation {

    private static final double RADIUS = 0.8;
    private static final double HEIGHT = 2.0;

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;

    public GroundVortexAnimation(TokenSMP plugin, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    /**
     * Watches a dropped item; when it lands, strikes cosmetic lightning
     * and starts the vortex loop. Safe to call for every item drop - it only
     * activates for items carrying the plugin's token-item PDC key.
     */
    public void track(Item dropped) {
        ItemStack stack = dropped.getItemStack();
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return;
        }
        String tokenId = stack.getItemMeta().getPersistentDataContainer()
                .get(plugin.tokenItemKey(), PersistentDataType.STRING);
        if (tokenId == null) {
            return;
        }
        dropped.setMetadata("tokensmp_token", new FixedMetadataValue(plugin, true));

        BukkitRunnable landingWatcher = new BukkitRunnable() {
            double waited = 0;

            @Override
            public void run() {
                if (!dropped.isValid()) {
                    cancel();
                    return;
                }
                if (!dropped.isOnGround()) {
                    waited += 2;
                    if (waited > 600) { // 30s safety cap
                        cancel();
                    }
                    return;
                }
                // Visual-only lightning (never damages anything).
                dropped.getWorld().strikeLightningEffect(dropped.getLocation());
                SoundEngine.world(dropped.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f);
                startVortex(dropped);
                cancel();
            }
        };
        landingWatcher.runTaskTimer(plugin, 2L, 2L);
        scheduler.register(landingWatcher);
    }

    private void startVortex(Item dropped) {
        BukkitRunnable vortex = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!dropped.isValid() || dropped.isDead()) {
                    cancel(); // item picked up / merged / despawned
                    return;
                }
                Location base = dropped.getLocation();
                angle += Math.PI / 8;
                double height = (angle / 3.0) % HEIGHT; // rises then resets at the bottom

                for (int i = 0; i < 3; i++) {
                    double theta = angle + (i * 2 * Math.PI / 3);
                    Location point = com.tokensmp.util.LocationUtil.onCircle(base, RADIUS, theta, height);
                    ParticleEngine.point(dropped.getWorld(), Particle.TOTEM_OF_UNDYING, point);
                    ParticleEngine.point(dropped.getWorld(), Particle.FLAME, point);
                }
            }
        };
        vortex.runTaskTimer(plugin, 0L, 4L);
        scheduler.register(vortex);
    }

    /** True when the item carries the plugin's token-item identity. */
    public static boolean isTokenItem(com.tokensmp.TokenSMP plugin, ItemStack stack) {
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer()
                .has(plugin.tokenItemKey(), PersistentDataType.STRING);
    }
}
