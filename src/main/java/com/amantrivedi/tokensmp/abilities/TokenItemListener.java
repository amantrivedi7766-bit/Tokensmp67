package com.amantrivedi.tokensmp.abilities;

import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Physical token items: right-click to unlock (admin token isolation),
 * ground drop vortex + fake lightning animation, and non-damaging
 * celebration fireworks.
 */
public final class TokenItemListener implements Listener {

    private final TokenSmpPlugin plugin;

    public TokenItemListener(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Token item factory + identification
    // ------------------------------------------------------------------

    /** Builds the physical Tier 1 token item (PDC-tagged, admin-give only). */
    public ItemStack createTokenItem(TokenDefinition token) {
        ItemStack item = new ItemStack(token.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(token.getRarityColor() + "§l" + token.getName() + " Token");
            meta.setLore(List.of(
                    "§7Tier: §f1 / 3",
                    "§7Rarity: " + token.getRarityColor() + token.getRarity(),
                    " ",
                    "§eRight-click to unlock this token!"));
            meta.getPersistentDataContainer().set(plugin.getTokenItemKey(), PersistentDataType.STRING, token.getId());
            item.setItemMeta(meta);
        }
        return item;
    }

    private TokenDefinition tokenFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta() == null) {
            return null;
        }
        String tokenId = item.getItemMeta().getPersistentDataContainer()
                .get(plugin.getTokenItemKey(), PersistentDataType.STRING);
        return tokenId == null ? null : plugin.getRegistry().get(tokenId);
    }

    // ------------------------------------------------------------------
    // Right-click: unlock the token (the ONLY path for the admin token)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onTokenItemUse(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> { /* continue */ }
            default -> {
                return;
            }
        }
        ItemStack item = event.getItem();
        TokenDefinition token = tokenFromItem(item);
        if (token == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (plugin.getData().hasUnlocked(player, token.getId())) {
            plugin.sendMessage(player, plugin.getConfig().getString("messages.already-unlocked",
                    "§cYou have already unlocked the {token} Token!")
                    .replace("{token}", token.getName()));
            return;
        }

        // Consume one item and unlock at Tier 1.
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) {
            player.getInventory().setItem(event.getHand(), null);
        }

        com.amantrivedi.tokensmp.ui.SpinAnimation spin = new com.amantrivedi.tokensmp.ui.SpinAnimation(plugin);
        plugin.getData().setTier(player, token.getId(), 1);
        plugin.getData().setActiveToken(player, token.getId());
        plugin.getPassiveManager().applyPassives(player);
        spin.play(player, token, 1);
        // The spin's finish() broadcasts the first-unlock announcement.
    }

    // ------------------------------------------------------------------
    // Ground drop & vortex animation
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        TokenDefinition token = tokenFromItem(event.getItemDrop().getItemStack());
        if (token == null) {
            return;
        }
        Item dropped = event.getItemDrop();
        // Unique metadata tag so we can spot our token items on the ground.
        dropped.setMetadata("tokensmp_token", new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            double ticksWaited = 0;

            @Override
            public void run() {
                if (!dropped.isValid()) {
                    cancel();
                    return;
                }
                // Wait until the item hits the ground.
                if (!dropped.isOnGround()) {
                    ticksWaited += 2;
                    if (ticksWaited > 600) { // 30s safety cap
                        cancel();
                    }
                    return;
                }
                // Visual (non-damaging) fake lightning strike at the landing point.
                dropped.getWorld().strikeLightningEffect(dropped.getLocation());
                dropped.getWorld().playSound(dropped.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f);
                startVortex(dropped);
                cancel();
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    /**
     * Upwards spiral vortex of golden TOTEM_OF_UNDYING and orange FLAME
     * particles: radius 0.8, spinning from the ground to 2 blocks height,
     * refreshed every 4 ticks while the item floats on the ground.
     */
    private void startVortex(Item dropped) {
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!dropped.isValid()) {
                    cancel();
                    return;
                }
                Location base = dropped.getLocation();
                angle += Math.PI / 8;
                double height = (angle / 3.0) % 2.0; // 0 -> 2 blocks, repeating

                for (int i = 0; i < 3; i++) {
                    double theta = angle + (i * 2 * Math.PI / 3);
                    double x = Math.cos(theta) * 0.8;
                    double z = Math.sin(theta) * 0.8;
                    Location point = base.clone().add(x, height, z);
                    dropped.getWorld().spawnParticle(Particle.TOTEM, point, 1, 0, 0, 0, 0);
                    dropped.getWorld().spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    // ------------------------------------------------------------------
    // Non-damaging celebration firework support
    // ------------------------------------------------------------------

    /** Cancels damage dealt by our tagged (cosmetic) fireworks. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework
                && firework.hasMetadata("tokensmp_firework")) {
            event.setCancelled(true);
        }
    }
}
