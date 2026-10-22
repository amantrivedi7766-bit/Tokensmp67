package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.token.Token;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Cinematic Mode: when a big token's ability fires, the caster is put into
 * a short spectator camera shot (a moving armour-stand rig swooping around
 * them) while nearby players are slowed to a crawl. Only the caster sees the
 * camera move - everyone else just sees the world FX.
 *
 * Pure Bukkit API: no NMS, no client mod. The ability itself fires at the
 * configured peak of the shot.
 */
public final class CinematicManager {

    private final TokenSMP plugin;
    /** Casters currently inside a shot (prevents stacking). */
    private final Set<UUID> active = new HashSet<>();
    /** Offered (not yet clicked) cinematic prompts. */
    private final Map<UUID, PendingShot> pending = new HashMap<>();

    /** A prompt that is waiting to be clicked. */
    private static final class PendingShot {
        private final long until;
        private final String tokenId;
        private final String ability;

        private PendingShot(long until, String tokenId, String ability) {
            this.until = until;
            this.tokenId = tokenId;
            this.ability = ability;
        }
    }

    public CinematicManager(TokenSMP plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.config().getBoolean("cinematic.enabled", true);
    }

    /** True when this token is configured to play a cinematic shot. */
    public boolean shouldPlay(String tokenId) {
        if (!isEnabled() || tokenId == null) {
            return false;
        }
        return plugin.config().getStringList("cinematic.tokens").stream()
                .anyMatch(id -> id.equalsIgnoreCase(tokenId));
    }

    public boolean isOptional() {
        return plugin.config().getBoolean("cinematic.optional", true);
    }

    /**
     * Called when a big-token ability fires. The ability itself has already
     * run (so the damage has landed); this only offers the optional camera
     * shot - either as a clickable prompt or immediately, per config.
     */
    public void offer(Player caster, Token token, String abilityName) {
        if (!shouldPlay(token.getId()) || !caster.isOnline()) {
            return;
        }
        if (!isOptional()) {
            play(caster, token, abilityName, null);
            return;
        }
        pending.put(caster.getUniqueId(), new PendingShot(System.currentTimeMillis()
                + plugin.config().getLong("cinematic.prompt-timeout-seconds", 8L) * 1000L,
                token.getId(), abilityName));
        plugin.messages().sendClickable(caster,
                plugin.config().getString("cinematic.prompt",
                        "&6&l✦ &e&l[ CLICK HERE TO WATCH THE CINEMATIC SHOT ] &6&l✦"),
                "/tokencinematic",
                plugin.config().getString("cinematic.prompt-hover",
                                "&7Play the camera shot for &f{ability}")
                        .replace("{ability}", abilityName));
    }

    /** The clickable prompt handler - plays the shot if it is still valid. */
    public void requestShot(Player caster) {
        PendingShot shot = pending.remove(caster.getUniqueId());
        if (shot == null || System.currentTimeMillis() > shot.until) {
            plugin.messages().send(caster, "messages.cinematic-expired",
                    "&7That cinematic prompt has expired - use the ability again.");
            return;
        }
        Token token = plugin.registry().get(shot.tokenId);
        if (token == null) {
            return;
        }
        play(caster, token, shot.ability, null);
    }

    /** Plays the shot for the caster; {@code peak} fires the ability itself. */
    public void play(Player caster, Token token, String abilityName, Runnable peak) {
        if (!shouldPlay(token.getId()) || active.contains(caster.getUniqueId())
                || !caster.isOnline()
                || (caster.getGameMode() != GameMode.SURVIVAL
                && caster.getGameMode() != GameMode.ADVENTURE)) {
            if (peak != null) {
                peak.run();
            }
            return;
        }
        active.add(caster.getUniqueId());
        int duration = Math.max(20, plugin.config().getInt("cinematic.duration-ticks", 50));
        int peakTick = Math.max(1, (int) (duration
                * plugin.config().getDouble("cinematic.peak-fraction", 0.4)));
        GameMode original = caster.getGameMode();

        // Camera rig: an invisible, gravity-free armour stand.
        Location base = caster.getLocation().clone();
        Vector facing = base.getDirection().setY(0);
        if (facing.lengthSquared() < 0.001) {
            facing = new Vector(0, 0, 1);
        }
        facing = facing.normalize();
        Vector right = new Vector(facing.getZ(), 0, -facing.getX()).normalize();
        final Location start = base.clone().add(facing.clone().multiply(-4.0)).add(0, 2.6, 0);
        final Location end = base.clone().add(facing.clone().multiply(3.2))
                .add(right.clone().multiply(2.2)).add(0, 1.6, 0);
        final Vector lookAt = base.clone().add(0, 1.2, 0).toVector();

        ArmorStand rig = caster.getWorld().spawn(start, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);
            stand.setSilent(true);
            stand.setPersistent(false);
        });

        caster.setGameMode(GameMode.SPECTATOR);
        caster.setSpectatorTarget(rig);

        // Slow-motion on nearby players (configurable radius).
        Map<UUID, Float> oldSpeeds = applySlowMotion(caster);

        // Title + boss bar + charge sound.
        plugin.messages().title(caster,
                plugin.config().getString("cinematic.title", "&6&l✦ CINEMATIC ✦"),
                plugin.config().getString("cinematic.subtitle", "&7{token} &8» &f{ability}")
                        .replace("{token}", token.getDisplayName())
                        .replace("{ability}", abilityName),
                5, duration, 10);
        BossBar bar = Bukkit.createBossBar(com.tokensmp.util.ColorUtil.color(
                        plugin.config().getString("cinematic.bossbar",
                                        "&6&l✦ {token} &7- {ability}")
                                .replace("{token}", token.getDisplayName())
                                .replace("{ability}", abilityName)),
                BarColor.YELLOW, BarStyle.SEGMENTED_20);
        bar.addPlayer(caster);
        SoundEngine.play(caster, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        SoundEngine.play(caster, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.2f);

        final boolean[] fired = {false};
        BukkitRunnable shot = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick > duration || !caster.isOnline()) {
                    cancel();
                    cleanup(caster, rig, original, oldSpeeds, bar);
                    return;
                }
                double t = (double) tick / duration;
                double eased = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
                Location pos = start.clone().multiply(1 - eased).add(end.clone().multiply(eased));
                pos.setY(pos.getY() + Math.sin(Math.PI * t) * 1.4);
                face(pos, lookAt);
                rig.teleport(pos);
                // Camera sparkle trail.
                ParticleEngine.burst(caster.getWorld(), Particle.END_ROD, pos, 2, 0.05);
                bar.setProgress(Math.max(0.0, Math.min(1.0, 1.0 - t)));
                if (!fired[0] && tick >= peakTick) {
                    fired[0] = true;
                    SoundEngine.play(caster, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
                    if (peak != null) {
                        peak.run();
                    } else {
                        // Cosmetic shot: the ability already ran, so just a burst.
                        ParticleEngine.burst(caster.getWorld(), Particle.EXPLOSION_EMITTER,
                                caster.getLocation().add(0, 1, 0), 30, 1.2);
                    }
                }
                tick++;
            }
        };
        shot.runTaskTimer(plugin, 0L, 1L);
        plugin.scheduler().register(shot);
    }

    /** Points a location at a target (yaw + pitch). */
    private void face(Location at, Vector target) {
        Vector dir = target.clone().subtract(at.toVector());
        if (dir.lengthSquared() < 0.0001) {
            return;
        }
        Location flat = at.clone().setDirection(dir);
        at.setYaw(flat.getYaw());
        at.setPitch(flat.getPitch());
    }

    /** Slows every nearby player to a crawl; returns their original speeds. */
    private Map<UUID, Float> applySlowMotion(Player caster) {
        Map<UUID, Float> old = new HashMap<>();
        if (!plugin.config().getBoolean("cinematic.slow-motion.enabled", true)) {
            return old;
        }
        double radius = plugin.config().getDouble("cinematic.slow-motion.radius", 20.0);
        float speed = (float) plugin.config().getDouble("cinematic.slow-motion.walk-speed", 0.05);
        int amplifier = plugin.config().getInt("cinematic.slow-motion.slowness-amplifier", 3);
        PotionEffectType slowness = com.tokensmp.core.VersionCompatibility
                .potionType("SLOWNESS", "SLOW");
        List<Player> nearby = new ArrayList<>(caster.getWorld()
                .getNearbyPlayers(caster.getLocation(), radius));
        for (Player other : nearby) {
            old.put(other.getUniqueId(), other.getWalkSpeed());
            other.setWalkSpeed(speed);
            if (slowness != null) {
                other.addPotionEffect(new PotionEffect(slowness, 60, amplifier, false, false));
            }
        }
        return old;
    }

    /** Restores the camera, game mode and every slowed player. */
    private void cleanup(Player caster, ArmorStand rig, GameMode original,
                         Map<UUID, Float> oldSpeeds, BossBar bar) {
        active.remove(caster.getUniqueId());
        if (rig != null && rig.isValid()) {
            ParticleEngine.burst(rig.getWorld(), Particle.CLOUD, rig.getLocation(), 20, 0.4);
            rig.remove();
        }
        if (caster.isOnline()) {
            caster.setSpectatorTarget(null);
            caster.setGameMode(original);
            SoundEngine.play(caster, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.2f);
        }
        for (Map.Entry<UUID, Float> entry : oldSpeeds.entrySet()) {
            Player other = Bukkit.getPlayer(entry.getKey());
            if (other != null && other.isOnline()) {
                other.setWalkSpeed(entry.getValue());
            }
        }
        bar.removeAll();
    }
}
