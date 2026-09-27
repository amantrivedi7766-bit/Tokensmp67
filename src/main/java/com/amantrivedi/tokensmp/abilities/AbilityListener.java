package com.amantrivedi.tokensmp.abilities;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the Shift + Right Click ability triggers with strict cooldown
 * enforcement, the live action-bar cooldown HUD, arrow trails and every
 * active ability implementation.
 */
public final class AbilityListener implements Listener {

    private final TokenSmpPlugin plugin;
    private final Map<UUID, Long> runningHud = new HashMap<>();
    private final Map<UUID, Integer> arrowsTrailed = new HashMap<>();

    public AbilityListener(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    // Keybind trigger: Shift + Right Click
    // ------------------------------------------------------------------

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

        TokenDefinition token = plugin.getRegistry().get(plugin.getData().getActiveToken(player));
        if (token == null) {
            return;
        }
        int tier = plugin.getData().getTier(player, token.getId());
        TierDefinition tierDef = token.tier(tier);
        if (tierDef == null || tierDef.getAbility() == null) {
            return;
        }
        TierDefinition.AbilitySpec ability = tierDef.getAbility();
        if ("NONE".equals(ability.getType())) {
            return;
        }

        event.setCancelled(true);

        long remaining = plugin.getData().getCooldownRemaining(player, token.getId());
        if (remaining > 0) {
            // Denied: low-pitched error click + hotbar alert.
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            sendBar(player, plugin.getConfig().getString("messages.ability-denied",
                    "§c§l[!]§c Ability on cooldown! Wait {seconds}s")
                    .replace("{seconds}", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }

        // Success: start cooldown, fire the ability, flash the action bar.
        plugin.getData().setCooldown(player, token.getId(), ability.getCooldown());
        dispatch(player, ability);
        sendBar(player, plugin.getConfig().getString("messages.ability-activated",
                "§a§l[!] §2{ability} Activated!").replace("{ability}", ability.getName()));
        startHudLoop(player, token.getId(), ability.getCooldown());
    }

    // ------------------------------------------------------------------
    // Live action-bar cooldown HUD (updates every 2 ticks)
    // ------------------------------------------------------------------

    private void startHudLoop(Player player, String tokenId, int totalSeconds) {
        UUID id = player.getUniqueId();
        long start = System.currentTimeMillis();
        long totalMs = totalSeconds * 1000L;
        if (runningHud.containsKey(id)) {
            return; // a HUD is already running for a newer cooldown
        }
        runningHud.put(id, start);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !runningHud.containsKey(id)
                        || runningHud.get(id) != start) {
                    runningHud.remove(id);
                    cancel();
                    return;
                }
                long elapsed = System.currentTimeMillis() - start;
                long remainingMs = totalMs - elapsed;
                if (remainingMs <= 0) {
                    runningHud.remove(id);
                    cancel();
                    sendBar(player, plugin.getConfig().getString("messages.ability-ready",
                            "§a§l[!] §aAbility ready!"));
                    return;
                }
                int barLength = plugin.getConfig().getInt("settings.cooldown-bar.length", 16);
                String filledChar = plugin.getConfig().getString("settings.cooldown-bar.filled-char", "█");
                String emptyChar = plugin.getConfig().getString("settings.cooldown-bar.empty-char", "░");
                String filledColor = plugin.getConfig().getString("settings.cooldown-bar.filled-color", "§c");
                int filled = (int) Math.ceil((remainingMs / (double) totalMs) * barLength);
                String bar = filledColor + filledChar.repeat(Math.max(0, Math.min(barLength, filled)))
                        + emptyChar.repeat(Math.max(0, barLength - filled));
                sendBar(player, plugin.getConfig().getString("settings.cooldown-bar.format",
                                "§cAbility Cooldown: [§c{bar}§r] §f{time}s")
                        .replace("{bar}", bar)
                        .replace("{time}", String.format("%.1f", remainingMs / 1000.0)));
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    // ------------------------------------------------------------------
    // Ability dispatch
    // ------------------------------------------------------------------

    private void dispatch(Player player, TierDefinition.AbilitySpec ability) {
        switch (ability.getType()) {
            case "ABSORPTION" -> doAbsorption(player, ability);
            case "SHOCKWAVE", "EXPLOSION_NOVA" -> doShockwave(player, ability);
            case "CHRONO_FREEZE" -> doChronoFreeze(player, ability);
            case "SERVER_JUDGMENT" -> doServerJudgment(player, ability);
            case "SONIC_BOOM" -> doSonicBoom(player, ability);
            case "FIRE_NOVA" -> doFireNova(player, ability);
            case "WARP_STRIKE" -> doWarpStrike(player, ability);
            case "HEALING_MIST" -> doHealingMist(player, ability);
            default -> plugin.getLogger().warning("Unknown ability type: " + ability.getType());
        }
    }

    /** "Undead Enrage": 15s of Health Absorption IV + green particle trail. */
    private void doAbsorption(Player player, TierDefinition.AbilitySpec ability) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                (int) (ability.getDuration() * 20), ability.getAmplifier(), false, true));
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.8f);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= ability.getDuration() * 20) {
                    cancel();
                    return;
                }
                ticks += 4;
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0),
                        5, 0.3, 0.5, 0.3, 0.1);
                player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation().add(0, 1, 0),
                        5, 0.3, 0.5, 0.3, 0.1);
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    /** Shockwave / explosion nova: true damage + knockback in a radius. */
    private void doShockwave(Player player, TierDefinition.AbilitySpec ability) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 3, 0.5, 0.5, 0.5, 0.0);
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof LivingEntity target && entity != player) {
                dealTrueDamage(target, ability.getDamage(), player);
                Vector knockback = entity.getLocation().toVector().subtract(player.getLocation().toVector())
                        .normalize().multiply(ability.getKnockback());
                knockback.setY(Math.max(knockback.getY(), 0.4));
                entity.setVelocity(knockback);
            }
        }
    }

    /** "Chrono Freeze": freezes all players within the radius. */
    private void doChronoFreeze(Player player, TierDefinition.AbilitySpec ability) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.6f);
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0),
                60, ability.getRadius() / 2, 1, ability.getRadius() / 2, 0.05);
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof Player target) {
                plugin.getFreezeManager().freeze(target, ability.getDuration());
            }
        }
    }

    /** "Server Judgment": sonic shockwave projectile - 35 true damage, 20-block knockback. */
    private void doServerJudgment(Player player, TierDefinition.AbilitySpec ability) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);

        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= 30.0; distance += 2.0) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);
            player.getWorld().spawnParticle(Particle.EXPLOSION, point, 1, 0.1, 0.1, 0.1, 0.0);
            for (Entity entity : player.getWorld().getNearbyEntities(point, 3.0, 3.0, 3.0)) {
                if (entity instanceof LivingEntity living && entity != player && !victims.contains(living)) {
                    victims.add(living);
                }
            }
        }
        for (LivingEntity victim : victims) {
            dealTrueDamage(victim, ability.getDamage(), player);
            Vector knockback = direction.clone().multiply(ability.getKnockback() / 8.0);
            knockback.setY(1.2);
            victim.setVelocity(victim.getVelocity().add(knockback));
        }
    }

    /** "Sonic Boom" (Warden): raycast forward, true damage + heavy knockback. */
    private void doSonicBoom(Player player, TierDefinition.AbilitySpec ability) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);

        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= 25.0; distance += 1.5) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);
            for (Entity entity : player.getWorld().getNearbyEntities(point, 1.8, 1.8, 1.8)) {
                if (entity instanceof LivingEntity living && entity != player && !victims.contains(living)) {
                    victims.add(living);
                }
            }
        }
        for (LivingEntity victim : victims) {
            dealTrueDamage(victim, ability.getDamage(), player);
            Vector knockback = direction.clone().multiply(ability.getKnockback());
            knockback.setY(Math.max(knockback.getY(), 0.5));
            victim.setVelocity(knockback);
        }
    }

    /** "Blazing Wrath": ignites every enemy within the radius. */
    private void doFireNova(Player player, TierDefinition.AbilitySpec ability) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.7f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0),
                80, ability.getRadius() / 2, 1, ability.getRadius() / 2, 0.1);
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof LivingEntity target) {
                target.setFireTicks((int) (ability.getDuration() * 20));
                dealTrueDamage(target, ability.getDamage(), player);
            }
        }
    }

    /** "Warp Strike": teleport forward + true damage at the landing point. */
    private void doWarpStrike(Player player, TierDefinition.AbilitySpec ability) {
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        Location landing = start.clone().add(direction.clone().multiply(ability.getRadius()));
        landing.setDirection(direction);
        landing.setY(player.getLocation().getY());
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 40, 0.3, 1, 0.3, 0.2);
        player.teleport(landing);
        player.getWorld().spawnParticle(Particle.PORTAL, landing, 40, 0.3, 1, 0.3, 0.2);
        player.getWorld().spawnParticle(Particle.HEART, landing, 20, 0.5, 0.5, 0.5, 0.1);

        for (Entity entity : player.getWorld().getNearbyEntities(landing, 3.0, 3.0, 3.0)) {
            if (entity instanceof LivingEntity target && entity != player) {
                dealTrueDamage(target, ability.getDamage(), player);
            }
        }
    }

    /** "Healing Mist": AoE heal for self + nearby players. */
    private void doHealingMist(Player player, TierDefinition.AbilitySpec ability) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0),
                60, ability.getRadius() / 2, 1, ability.getRadius() / 2, 0.1);
        heal(player, ability.getDamage() * 2.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                (int) (ability.getDuration() * 20), 1, false, true));
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof Player ally) {
                heal(ally, ability.getDamage() * 2.0);
                ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                        (int) (ability.getDuration() * 20), 1, false, true));
            }
        }
    }

    // ------------------------------------------------------------------
    // Arrow trails (Skeleton max tier passive)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player player)) {
            return;
        }
        if (!plugin.getPassiveManager().hasArrowTrail(player)) {
            return;
        }
        int maxTrailsPerPlayer = 24;
        int current = arrowsTrailed.getOrDefault(player.getUniqueId(), 0);
        if (current >= maxTrailsPerPlayer) {
            return;
        }
        arrowsTrailed.put(player.getUniqueId(), current + 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isOnGround() || arrow.isDead()) {
                    arrowsTrailed.merge(player.getUniqueId(), -1, Integer::sum);
                    cancel();
                    return;
                }
                plugin.getPassiveManager().spawnArrowTrail(arrow.getLocation(), player);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** True damage: bypasses armor and shields entirely. */
    private void dealTrueDamage(LivingEntity target, double damage, Player source) {
        target.setHealth(Math.max(0.0, target.getHealth() - damage));
        target.setKiller(source);
    }

    private void heal(Player player, double amount) {
        player.setHealth(Math.min(player.getHealth() + amount,
                player.getAttribute(plugin.getMaxHealthAttribute()) == null ? 20.0
                        : player.getAttribute(plugin.getMaxHealthAttribute()).getValue()));
    }

    private void sendBar(Player player, String message) {
        if (message != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
}
