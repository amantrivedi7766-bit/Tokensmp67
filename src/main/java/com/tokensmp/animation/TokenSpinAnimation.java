package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.gui.SpinGUI;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.Bukkit;
import com.tokensmp.animation.SoundEngine;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

/**
 * Cinematic first-unlock spin: a 9-slot inventory filled with random eligible
 * player tokens that shift while slowing down over ~5 seconds
 * (2 -> 5 -> 10 -> 15 tick phases). Slot 4 holds the winner. Runs entirely on
 * the main scheduler and self-cancels when the player disconnects.
 * The Admin Token can never appear here (caller passes the player pool only).
 */
public final class TokenSpinAnimation {

    private static final Random RANDOM = new Random();

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final MessageManager messages;
    private final SchedulerManager scheduler;

    public TokenSpinAnimation(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                               MessageManager messages, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.messages = messages;
        this.scheduler = scheduler;
    }

    /**
     * Runs the full spin for the player and unlocks the given token at tier 1.
     * The token is UNLOCKED (available) - it only becomes ACTIVE once claimed.
     */
    public void play(Player player, Token token) {
        String title = plugin.config().getString("spin.title", "&5&lUnlocking Token...");
        Inventory inventory = Bukkit.createInventory(new SpinGUI.Holder(), 9, title);
        List<Token> pool = registry.playerTokens();

        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, randomTokenItem(pool));
        }
        player.openInventory(inventory);

        schedulePhase(player, inventory, pool, token,
                plugin.config().getInt("spin.shifts-fast", 10), 2L);
    }

    private void schedulePhase(Player player, Inventory inventory, List<Token> pool,
                               Token winner, int shiftsLeft, long period) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    finish(player, winner);
                    return;
                }
                shift(inventory, pool);
                SoundEngine.play(player, com.tokensmp.core.VersionCompatibility.sound("ENTITY_EXPERIENCE_ORB_PICKUP"), 0.5f, 1.8f);

                int remaining = shiftsLeft - 1;
                if (remaining > 0) {
                    schedulePhase(player, inventory, pool, winner, remaining, period);
                    return;
                }
                if (period == 2L) {
                    schedulePhase(player, inventory, pool, winner,
                            plugin.config().getInt("spin.shifts-medium", 5), 5L);
                } else if (period == 5L) {
                    schedulePhase(player, inventory, pool, winner,
                            plugin.config().getInt("spin.shifts-slow", 3), 10L);
                } else if (period == 10L) {
                    schedulePhase(player, inventory, pool, winner,
                            plugin.config().getInt("spin.shifts-final", 2), 15L);
                } else {
                    inventory.setItem(4, tokenItem(winner));
                    finish(player, winner);
                }
            }
        };
        runnable.runTaskLater(plugin, period);
        scheduler.register(runnable);
    }

    private void shift(Inventory inventory, List<Token> pool) {
        for (int slot = 0; slot < 8; slot++) {
            inventory.setItem(slot, inventory.getItem(slot + 1));
        }
        inventory.setItem(8, randomTokenItem(pool));
    }

    private void finish(Player player, Token token) {
        if (player.isOnline()) {
            player.closeInventory();
            messages.title(player,
                    plugin.config().getString("spin.title-allocated", "&6&lTOKEN ALLOCATED: &f{token} &6★")
                            .replace("{token}", token.getDisplayName()),
                    plugin.config().getString("spin.subtitle-unlocked", "&7Check your /tokens menu!"),
                    10, 70, 20);
            dropEvent(player, token);
        }
        // Server-authoritative unlock + global announcement.
        data.setTier(player, token.getId(), 1);
        // The token item is handed straight to the player so it sits in their
        // hotbar: holding it is how the ability is used (no commands needed).
        if (player.isOnline()
                && plugin.config().getBoolean("claim-system.unlock-gives-item", true)) {
            plugin.tokenItems().ensureItem(player, token, 1);
        }
        messages.broadcastLine(messages.msg("messages.first-unlock",
                "&8&l[&6&lTokenSMP&8&l] &fPlayer &b{player} &fhas just unlocked the {color}&l{token} Token &ffor the first time! 🎉",
                "{player}", player.getName(),
                "{token}", token.getDisplayName(),
                "{color}", token.getRarity().getColorCode()));
    }

    /** The Token Drop Event: lightning, a rarity firework and a spiral beam. */
    private void dropEvent(Player player, Token token) {
        Location at = player.getLocation();
        player.getWorld().strikeLightningEffect(at);
        SoundEngine.world(at, com.tokensmp.core.VersionCompatibility.sound("ENTITY_LIGHTNING_BOLT_THUNDER"), 1.2f, 1.2f);
        spawnFirework(player);
        // Rarity-coloured spiral beacon beam rising from the player.
        Color rarity = rarityColor(token);
        for (int i = 0; i < 60; i++) {
            final int step = i;
            plugin.scheduler().runLater(step, () -> {
                if (player.isOnline()) {
                    Location point = at.clone().add(0, step * 0.35, 0);
                    com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                            com.tokensmp.core.VersionCompatibility.particle("END_ROD"), point, 2, 0.05);
                    com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                            com.tokensmp.core.VersionCompatibility.particle("FIREWORK"), point, 3, 0.1);
                }
            });
        }
        plugin.scheduler().runLater(60L, () -> {
            if (player.isOnline()) {
                com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                        com.tokensmp.core.VersionCompatibility.particle("FIREWORK"), at.clone().add(0, 4, 0), 60, 1.5);
            }
        });
    }

    /** The rarity's firework colour. */
    private Color rarityColor(Token token) {
        return switch (token.getRarity()) {
            case COMMON -> Color.SILVER;
            case RARE -> Color.AQUA;
            case EPIC -> Color.FUCHSIA;
            case LEGENDARY -> Color.ORANGE;
            case MYTHIC -> Color.PURPLE;
        };
    }

    /** Colorful cosmetic firework - damage cancelled by the plugin listener. */
    private void spawnFirework(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.AQUA, Color.LIME, Color.ORANGE, Color.FUCHSIA)
                    .trail(true)
                    .flicker(true)
                    .build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
            fw.setMetadata("tokensmp_firework", new FixedMetadataValue(plugin, true));
        });
        BukkitRunnable detonate = new BukkitRunnable() {
            @Override
            public void run() {
                if (firework.isValid()) {
                    firework.detonate();
                }
            }
        };
        detonate.runTaskLater(plugin, 25L);
        scheduler.register(detonate);
    }

    private ItemStack randomTokenItem(List<Token> pool) {
        if (pool.isEmpty()) {
            return new ItemStack(Material.PAPER);
        }
        return tokenItem(pool.get(RANDOM.nextInt(pool.size())));
    }

    private ItemStack tokenItem(Token token) {
        ItemStack item = new ItemStack(token.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(com.tokensmp.util.ColorUtil.color(
                    token.getRarity().getColorCode() + "&l" + token.getDisplayName() + " Token"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
