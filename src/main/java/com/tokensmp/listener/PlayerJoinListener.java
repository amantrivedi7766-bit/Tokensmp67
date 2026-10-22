package com.tokensmp.listener;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.token.Token;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * First-join detection: exactly once per player lifetime (persistent PDC
 * flag, so re-logging never re-triggers it) the cinematic token spin launches
 * one second after join - off the join tick so the client is ready.
 */
public final class PlayerJoinListener implements Listener {

    private final TokenSMP plugin;

    public PlayerJoinListener(TokenSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Always refresh passives on join (survives restarts cleanly).
        plugin.passiveManager().applyPassives(player);
        plugin.lives().applyRevivedHealth(player);
        plugin.lives().applySacrifice(player);
        // An active token always keeps its item in the inventory (hotbar first),
        // so the player can hold it and use the ability.
        String active = plugin.data().getActiveToken(player);
        if (active != null) {
            Token token = plugin.registry().get(active);
            if (token != null) {
                plugin.tokenItems().ensureItem(player, token,
                        Math.max(1, plugin.data().getTier(player, active)));
            }
        }
        if (!plugin.config().getBoolean("first-join.enabled", true)) {
            return;
        }
        // Only NEW players (no token yet) get the spin. A player who already
        // owns a token never spins again on join.
        if (plugin.data().hasAnyUnlocked(player)) {
            return;
        }
        if (!plugin.data().consumeFirstJoin(player)) {
            return;
        }
        int delay = Math.max(0, plugin.config().getInt("first-join.launch-delay-ticks", 20));
        plugin.scheduler().runLater(delay, () -> {
            if (player.isOnline()) {
                revealThenSpin(player);
            }
        });
    }

    /** The Choice of Destiny: freeze, glyph vortex, celestial chime, then the spin. */
    private void revealThenSpin(Player player) {
        Location at = player.getLocation();
        if (plugin.config().getBoolean("first-join.freeze", true)) {
            plugin.freezeManager().freeze(player, plugin.config()
                    .getDouble("first-join.freeze-seconds", 5.0));
        }
        SoundEngine.play(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        plugin.messages().title(player,
                plugin.config().getString("first-join.title", "&f&lCHOOSING YOUR TOKEN..."),
                plugin.config().getString("first-join.subtitle", "&7The dice of destiny are rolling"),
                5, 60, 10);
        // White/gold glyph vortex spiralling up the player.
        for (int i = 0; i < 34; i++) {
            final int step = i;
            plugin.scheduler().runLater(step, () -> {
                if (player.isOnline()) {
                    com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                            org.bukkit.Particle.ENCHANT, at.clone().add(0, step * 0.06, 0), 6, 0.4);
                    com.tokensmp.animation.ParticleEngine.burst(player.getWorld(),
                            org.bukkit.Particle.END_ROD, at.clone().add(0, step * 0.06, 0), 3, 0.3);
                }
            });
        }
        plugin.scheduler().runLater(60L, () -> {
            if (player.isOnline()) {
                SoundEngine.play(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
                plugin.spinLauncher().launchFor(player);
            }
        });
    }
}
