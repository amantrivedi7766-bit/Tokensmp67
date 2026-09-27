package com.amantrivedi.tokensmp.abilities;

import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chrono Freeze support: fully freezes players in place (no move, jump, block
 * break or attacks), runs an ice particle + action bar status loop and
 * automatically releases them when the freeze expires.
 */
public final class FreezeManager implements Listener {

    private final TokenSmpPlugin plugin;
    private final Map<UUID, Long> frozenUntil = new ConcurrentHashMap<>();
    private BukkitTask loopTask;

    public FreezeManager(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** Freezes a player for the given duration (seconds). */
    public void freeze(Player player, double seconds) {
        long until = System.currentTimeMillis() + (long) (seconds * 1000L);
        frozenUntil.put(player.getUniqueId(), Math.max(frozenUntil.getOrDefault(player.getUniqueId(), 0L), until));
        sendBar(player, plugin.getConfig().getString("freeze.frozen-message",
                "§b❄ You have been frozen for {seconds}s!").replace("{seconds}", String.valueOf((long) seconds)));
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.05);
        ensureLoop();
    }

    public boolean isFrozen(Player player) {
        Long until = frozenUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    /** Starts the shared freeze status loop (particles + auto unfreeze). */
    private void ensureLoop() {
        if (loopTask != null) {
            return;
        }
        loopTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean anyFrozen = false;
            Iterator<Map.Entry<UUID, Long>> iterator = frozenUntil.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player == null || entry.getValue() <= System.currentTimeMillis()) {
                    if (player != null) {
                        sendBar(player, plugin.getConfig().getString("freeze.unfrozen-message", "§a❄ You are free again!"));
                    }
                    iterator.remove();
                    continue;
                }
                anyFrozen = true;
                long remainingSeconds = ((entry.getValue() - System.currentTimeMillis()) + 999L) / 1000L;
                sendBar(player, plugin.getConfig().getString("freeze.status-message", "§b❄ Frozen! §f{seconds}s")
                        .replace("{seconds}", String.valueOf(remainingSeconds)));
                Location center = player.getLocation().add(0, 1, 0);
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, center, 12, 0.4, 0.9, 0.4, 0.02);
                player.getWorld().spawnParticle(Particle.CLOUD, center, 3, 0.3, 0.9, 0.3, 0.01);
            }
            if (!anyFrozen) {
                loopTask.cancel();
                loopTask = null;
            }
        }, 4L, 4L);
    }

    // ------------------------------------------------------------------
    // Freeze enforcement
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (!isFrozen(event.getPlayer())) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        // Block movement but allow free head rotation.
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private void sendBar(Player player, String message) {
        if (message != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
}
