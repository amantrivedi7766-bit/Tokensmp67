package com.amantrivedi.tokensmp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core listener: absolute tracking over all keybind inputs, passive state
 * re-verification and heart claiming mechanics.
 */
public final class CoreListener implements Listener {

    private final TokenSmpPlugin plugin;

    public CoreListener(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Lifecycle: join / respawn / quit
    // ------------------------------------------------------------------

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getData(player);          // load into cache
        plugin.restoreBaseHealth(player);
        plugin.applyTokenPassives(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Potion effects are cleared on death - re-apply one tick after respawn.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = event.getPlayer();
            if (player.isOnline()) {
                plugin.applyTokenPassives(player);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        // Persist asynchronously so the main thread never blocks on disk I/O.
        plugin.persistPlayerData(playerId);
    }

    // ------------------------------------------------------------------
    // Heart item claiming (re-deposit listener)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onHeartClaim(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (!plugin.isHeartItem(item)) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        AttributeInstance maxHealth = player.getAttribute(plugin.getMaxHealthAttribute());
        if (maxHealth == null) {
            return;
        }
        // Hard cap: 40.0 total max HP (20 hearts).
        if (maxHealth.getValue() >= 40.0) {
            sendActionBar(player, "§cYou have already reached the 20 heart limit!");
            return;
        }

        PlayerData data = plugin.getData(player);
        data.setExtraHeartHp(data.getExtraHeartHp() + 2.0);
        maxHealth.setBaseValue(20.0 + data.getExtraHeartHp());

        // Consume exactly one heart item.
        if (item != null) {
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() <= 0) {
                player.getInventory().setItem(event.getHand(), null);
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
        sendActionBar(player, "§aHeart claimed! §7Max health is now §c" + (int) maxHealth.getValue() / 2 + "§7 hearts.");
    }

    // ------------------------------------------------------------------
    // Keybind interception: Shift + Left/Right Click
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onKeybind(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // only listen to the main hand
        }
        Action action = event.getAction();
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!leftClick && !rightClick) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        TokenType active = TokenType.fromId(plugin.getData(player).getActiveToken());
        if (active == null) {
            return;
        }
        // Zombie / Warden trigger on sneak + left click; Blaze on sneak + right click.
        if (active == TokenType.BLAZE ? !rightClick : !leftClick) {
            return;
        }

        event.setCancelled(true);

        long remainingMillis = plugin.getCooldownRemaining(player.getUniqueId(), active);
        if (remainingMillis > 0) {
            long seconds = (remainingMillis + 999L) / 1000L; // round up so 0.4s shows as 1s
            sendActionBar(player, "§c§l[!]§c Ability on cooldown! Wait " + seconds + "s");
            return;
        }

        plugin.startCooldown(player.getUniqueId(), active);
        activateAbility(player, active);
        sendActionBar(player, "§a§l[!] §2" + active.getAbilityName() + " Activated!");
    }

    // ------------------------------------------------------------------
    // Ability implementations
    // ------------------------------------------------------------------

    private void activateAbility(Player player, TokenType type) {
        switch (type) {
            case ZOMBIE -> activateRottenRush(player);
            case BLAZE -> activateFireballBarrage(player);
            case WARDEN -> activateSonicBoom(player);
        }
    }

    /** Rotten Rush: Strength II + Speed I for 8 seconds. */
    private void activateRottenRush(Player player) {
        int durationTicks = plugin.getConfig().getInt("tokens.zombie.effect-duration-ticks", 160);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, durationTicks, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, 0, false, true));
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.8f);
    }

    /** Fireball Barrage: fires a straight-line small fireball towards the crosshair. */
    private void activateFireballBarrage(Player player) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        SmallFireball fireball = player.getWorld().spawn(
                eye.clone().add(direction.clone().multiply(0.75)),
                SmallFireball.class,
                fb -> {
                    fb.setShooter(player);
                    fb.setVelocity(direction.clone().multiply(1.5));
                });
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }

    /** Sonic Boom: 15-block raycast, damaging and launching everything near the path. */
    private void activateSonicBoom(Player player) {
        double range = plugin.getConfig().getDouble("tokens.warden.sonic-boom-range", 15.0);
        double damage = plugin.getConfig().getDouble("tokens.warden.sonic-boom-damage", 12.0);

        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);

        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= range; distance += 2.0) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);

            for (Entity entity : player.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (entity instanceof LivingEntity living && !entity.getUniqueId().equals(player.getUniqueId())
                        && !victims.contains(living)) {
                    victims.add(living);
                }
            }
        }

        for (LivingEntity victim : victims) {
            // True damage: bypasses armor entirely.
            double newHealth = Math.max(0.0, victim.getHealth() - damage);
            victim.setHealth(newHealth);
            victim.setKiller(player);

            // Heavy knockback along the boom path with a 1.8 multiplier.
            Vector knockback = direction.clone().multiply(1.8);
            knockback.setY(Math.max(knockback.getY(), 0.4));
            victim.setVelocity(knockback);
        }
    }

    // ------------------------------------------------------------------
    // Action bar helper
    // ------------------------------------------------------------------

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
