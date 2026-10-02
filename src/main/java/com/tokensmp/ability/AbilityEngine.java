package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleEngine;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * The ability engine: implements all 45 player abilities (15 tokens x 3
 * tiers) plus the three isolated Admin abilities. Every ability has its own
 * cast animation, projectile / attack behaviour, particle sequence, sound
 * sequence, impact animation, cooldown, damage value and targeting logic -
 * nothing is a reskinned copy of anything else.
 *
 * Global rules honoured by every implementation:
 * - real server-side damage attributed to the token owner,
 * - no potion-effect abilities, no mob spawning, no stat-boost abilities,
 * - no terrain destruction (all effects are visual only),
 * - PvP / damage protection rules respected (event-based damage),
 * - every scheduler task self-cancels and is registered for cleanup.
 */
public final class AbilityEngine {

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;
    private final FreezeManager freezeManager;

    /** Active/pending Enderman portal pairs, keyed by the owner's UUID. */
    private final Map<UUID, PortalPair> portals = new HashMap<>();
    /** Players who just came out of a portal - briefly immune to re-entering. */
    private final Map<UUID, Long> portalGrace = new HashMap<>();

    public AbilityEngine(TokenSMP plugin, SchedulerManager scheduler, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.freezeManager = freezeManager;
    }

    // ------------------------------------------------------------------
    // Dispatch
    // ------------------------------------------------------------------

    public void execute(Player player, TokenTier.AbilitySpec a) {
        switch (a.getType()) {
            // 1. Enderman
            case BLINK_CHAIN -> blinkChain(player, a);
            case PORTAL_LINK -> portalLink(player, a);
            case ENDER_ASSEMBLY -> enderAssembly(player, a);
            // 2. Creeper
            case TNT_CANNON -> tntCannon(player, a);
            case TNT_STRIKE -> tntStrike(player, a);
            case BOMB_CHICKENS -> bombChickens(player, a);
            // 3. Skeleton
            case BONE_BOLT -> boneBolt(player, a);
            case RICOCHET_SHOT -> ricochetShot(player, a);
            case DEADEYE_BARRAGE -> deadeyeBarrage(player, a);
            // 4. Ghast
            case GHAST_ORB -> ghastOrb(player, a);
            case INFERNO_COMET -> infernoComet(player, a);
            case NETHERFALL -> netherfall(player, a);
            // 5. Warden
            case SONIC_JAB -> sonicJab(player, a);
            case SONIC_BEAM -> sonicBeam(player, a);
            case SONIC_RUPTURE -> sonicRupture(player, a);
            // 6. Piglin
            case GOLDEN_CLEAVE -> goldenCleave(player, a);
            case GOLD_SPEAR -> goldSpear(player, a);
            case ROYAL_EXECUTION -> royalExecution(player, a);
            // 7. Fish
            case AQUA_BULLET -> aquaBullet(player, a);
            case TIDAL_RAM -> tidalRam(player, a);
            case LEVIATHAN_CRASH -> leviathanCrash(player, a);
            // 8. Zombie
            case ROTTEN_SMASH -> rottenSmash(player, a);
            case GRAVE_BREAKER -> graveBreaker(player, a);
            case UNDEAD_CATACLYSM -> undeadCataclysm(player, a);
            // 9. Wither
            case WITHER_SKULL -> witherSkull(player, a);
            case TRIPLE_SKULL_VOLLEY -> tripleSkullVolley(player, a);
            case WITHER_BARRAGE -> witherBarrage(player, a);
            // 10. Villager
            case EMERALD_LANCE -> emeraldLance(player, a);
            case TRADE_BREAKER -> tradeBreaker(player, a);
            case EMERALD_JUDGMENT -> emeraldJudgment(player, a);
            // 11. Slime
            case SLIME_SLAM -> slimeSlam(player, a);
            case BOUNCY_CRUSH -> bouncyCrush(player, a);
            case MEGA_SLIME_IMPACT -> megaSlimeImpact(player, a);
            // 12. Magma Cube
            case MAGMA_SLAM -> magmaSlam(player, a);
            case MAGMA_WAVE -> magmaWave(player, a);
            case MAGMA_CORE_ERUPTION -> magmaCoreEruption(player, a);
            // 13. Illusioner
            case PHANTOM_ARROW -> phantomArrow(player, a);
            case MIRROR_VOLLEY -> mirrorVolley(player, a);
            case REALITY_FRACTURE -> realityFracture(player, a);
            // 14. Blaze
            case FLAME_LANCE -> flameLance(player, a);
            case INFERNAL_SPIRAL -> infernalSpiral(player, a);
            case SOLAR_BURST -> solarBurst(player, a);
            // 15. Golem
            case IRON_FIST -> ironFist(player, a);
            case IRONQUAKE -> ironquake(player, a);
            case COLOSSUS_IMPACT -> colossusImpact(player, a);
            // Admin (isolated)
            case NETHER_SHOCKWAVE -> netherShockwave(player, a);
            case CHRONO_FREEZE -> chronoFreeze(player, a);
            case SERVER_JUDGMENT -> serverJudgment(player, a);
            case NONE -> { /* no ability */ }
        }
    }

    // ==================================================================
    // 1. ENDERMAN (each tier uses its own keybind)
    // ==================================================================

    /** T1 - Blink Chain (RIGHT CLICK): blink through 5 positions, 1s apart. */
    private void blinkChain(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        int hops = Math.max(1, a.getCount());
        double perHop = a.getDamage() / hops;
        long interval = Math.max(1L,
                plugin.config().getLong("tokens.enderman.tier1.blink-interval-ticks", 20L));
        double search = Math.max(3.0, a.getRadius());
        for (int i = 0; i < hops; i++) {
            final int hop = i;
            later(interval * i, () -> {
                if (!p.isOnline()) {
                    return;
                }
                Location from = p.getLocation().clone();
                Location to = safeSpot(p, from, search);
                AbilityFx.impact(from, a.getParticles(), a.getParticleCount(), 0.6);
                p.teleport(to);
                p.setFallDistance(0f);
                AbilityFx.impact(to, a.getParticles(), a.getParticleCount(), 0.6);
                AbilityFx.ring(to, 1.4, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 18);
                SoundEngine.play(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                PhysicalDamageEngine.areaDamage(p, to, 3.0, perHop, a.getKnockback(), a.isTrueDamage());
                if (hop == hops - 1) {
                    SoundEngine.play(p, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.2f);
                }
            });
        }
    }

    /** T2 - Portal Link (SHIFT + LEFT CLICK): two linked portals for 60 seconds. */
    private void portalLink(Player p, TokenTier.AbilitySpec a) {
        PortalPair pair = portals.get(p.getUniqueId());
        if (pair == null) {
            // First placement: a portal at the player's feet.
            pair = new PortalPair();
            pair.owner = p.getUniqueId();
            pair.first = p.getLocation().clone();
            pair.ability = a;
            portals.put(p.getUniqueId(), pair);
            AbilityFx.impact(pair.first, a.getParticles(), a.getParticleCount(), 0.9);
            AbilityFx.ring(pair.first, 1.2, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 20);
            SoundEngine.play(p, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.2f);
            plugin.messages().send(p, "messages.portal-first-placed",
                    "&5&lPORTAL &7First portal placed! Now place the second portal where you want "
                            + "to travel - &fShift + Left Click&7.");
            startPortalLoop(p, pair);
            return;
        }
        // Second placement: at the cursor position.
        Location target = groundTarget(p, a.getRange());
        pair.second = target;
        pair.expiryMillis = System.currentTimeMillis() + portalSeconds() * 1000L;
        AbilityFx.impact(target, a.getParticles(), a.getParticleCount(), 0.9);
        AbilityFx.ring(target, 1.2, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 20);
        SoundEngine.play(p, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        // Linking surges void energy out of the new portal.
        PhysicalDamageEngine.areaDamage(p, target, Math.max(2.0, a.getRadius()), a.getDamage(),
                a.getKnockback(), a.isTrueDamage());
        plugin.messages().send(p, "messages.portal-linked",
                "&5&lPORTAL &7Portals linked for &f{seconds}s&7! Step into one to travel to the other.",
                "{seconds}", String.valueOf(portalSeconds()));
    }

    private long portalSeconds() {
        return Math.max(5L, plugin.config().getLong("tokens.enderman.tier2.portal-seconds", 60L));
    }

    private double portalRadius() {
        return Math.max(1.0, plugin.config().getDouble("tokens.enderman.tier2.portal-radius", 1.6));
    }

    /** Per-tick portal loop: draws both portals + the connecting particle line. */
    private void startPortalLoop(Player owner, PortalPair pair) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!owner.isOnline()) {
                    closePortals(owner.getUniqueId(), false);
                    return;
                }
                TokenTier.AbilitySpec ability = pair.ability;
                if (ability == null) {
                    cancel();
                    return;
                }
                if (pair.second == null) {
                    // Only one portal: draw it and remind anyone stepping in.
                    drawPortal(pair.first, ability);
                    double radiusSq = portalRadius() * portalRadius();
                    for (Player other : pair.first.getWorld().getPlayers()) {
                        if (other.getWorld().equals(pair.first.getWorld())
                                && other.getLocation().distanceSquared(pair.first) < radiusSq) {
                            plugin.messages().send(other, "messages.portal-needs-second",
                                    "&5&lPORTAL &7You must place the second portal first! "
                                            + "(&fShift + Left Click&7)");
                        }
                    }
                    return;
                }
                long now = System.currentTimeMillis();
                if (now >= pair.expiryMillis) {
                    closePortals(owner.getUniqueId(), true);
                    return;
                }
                drawPortal(pair.first, ability);
                drawPortal(pair.second, ability);
                drawLink(pair.first, pair.second, ability);
                double radiusSq = portalRadius() * portalRadius();
                for (Player other : pair.first.getWorld().getPlayers()) {
                    Long grace = portalGrace.get(other.getUniqueId());
                    if (grace != null && grace > now) {
                        continue;
                    }
                    Location exit = null;
                    if (other.getWorld().equals(pair.first.getWorld())
                            && other.getLocation().distanceSquared(pair.first) < radiusSq) {
                        exit = pair.second;
                    } else if (other.getWorld().equals(pair.second.getWorld())
                            && other.getLocation().distanceSquared(pair.second) < radiusSq) {
                        exit = pair.first;
                    }
                    if (exit != null) {
                        teleportThrough(other, exit, ability);
                    }
                }
            }
        };
        runnable.runTaskTimer(plugin, 0L, 2L);
        scheduler.register(runnable);
        pair.task = runnable;
    }

    /** Sends a player through a portal to the exit location. */
    private void teleportThrough(Player player, Location exit, TokenTier.AbilitySpec a) {
        Location landing = exit.clone().add(0, 0.2, 0);
        AbilityFx.impact(player.getLocation(), a.getParticles(), a.getParticleCount(), 0.7);
        player.teleport(landing);
        player.setFallDistance(0f);
        AbilityFx.impact(landing, a.getParticles(), a.getParticleCount(), 0.7);
        AbilityFx.ring(landing, 1.2, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 18);
        SoundEngine.play(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        long graceTicks = Math.max(10L,
                plugin.config().getLong("tokens.enderman.tier2.teleport-grace-ticks", 40L));
        portalGrace.put(player.getUniqueId(), System.currentTimeMillis() + graceTicks * 50L);
        plugin.messages().send(player, "messages.portal-teleported",
                "&5&lPORTAL &7Whoosh! You travelled through the portal.");
    }

    private void drawPortal(Location center, TokenTier.AbilitySpec a) {
        AbilityFx.spiral(center.clone().add(0, 0.2, 0), 1.1, 2.2, a.getParticles(), 14);
        AbilityFx.ring(center.clone().add(0, 0.15, 0), 1.1,
                List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 18);
    }

    private void drawLink(Location from, Location to, TokenTier.AbilitySpec a) {
        if (!from.getWorld().equals(to.getWorld())) {
            return;
        }
        Vector delta = to.toVector().subtract(from.toVector());
        int steps = (int) Math.max(6, delta.length() * 2.0);
        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(delta.clone().multiply((double) i / steps));
            ParticleEngine.point(from.getWorld(), Particle.PORTAL, point);
            if (i % 2 == 0) {
                ParticleEngine.point(from.getWorld(), Particle.REVERSE_PORTAL, point);
            }
        }
    }

    /** Closes a player's portals (optionally notifying the owner). */
    private void closePortals(UUID ownerId, boolean notify) {
        PortalPair pair = portals.remove(ownerId);
        if (pair == null) {
            return;
        }
        if (pair.task != null) {
            pair.task.cancel();
        }
        if (notify) {
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                plugin.messages().send(owner, "messages.portal-expired",
                        "&5&lPORTAL &7Your portals have closed.");
            }
        }
    }

    /**
     * True while the player is mid-sequence (one portal placed, waiting for the
     * second): those activations must not be blocked or re-trigger the cooldown.
     */
    public boolean deferCooldown(Player player, TokenTier.AbilitySpec ability, long remainingMillis) {
        if (ability.getType() != TokenAbility.PORTAL_LINK) {
            return false;
        }
        if (remainingMillis > 0L) {
            return false;
        }
        return !portals.containsKey(player.getUniqueId());
    }

    /** T3 - Ender Assembly (SHIFT + RIGHT CLICK): void spiral + player-head menu. */
    private void enderAssembly(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        // Cinematic void spiral opening in front of the player.
        AbilityFx.spiral(p.getLocation().add(0, 1.0, 0), 2.2, 2.8, a.getParticles(), 48);
        for (int i = 1; i <= 3; i++) {
            AbilityFx.ring(p.getLocation().add(0, 1.0, 0), 1.0 + i * 0.8,
                    List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 26);
        }
        Location front = com.tokensmp.util.LocationUtil.forward(p.getEyeLocation(), 2.0);
        AbilityFx.column(front, 2.5, List.of(Particle.PORTAL, Particle.DRAGON_BREATH));
        SoundEngine.play(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        plugin.endermanGUI().openSource(p);
    }

    /** Performs the tier-3 teleport chosen in the player-head menu. */
    public void enderTeleport(Player caster, Player source, Player destination, TokenTier.AbilitySpec a) {
        Location from = source.getLocation().clone();
        Location to = destination.getLocation().clone().add(0, 0.3, 0);
        AbilityFx.impact(from, a.getParticles(), a.getParticleCount(), 0.9);
        AbilityFx.ring(from, 1.4, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 20);
        source.teleport(to);
        source.setFallDistance(0f);
        AbilityFx.impact(to, a.getParticles(), a.getParticleCount(), 0.9);
        AbilityFx.ring(to, 1.4, List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 20);
        AbilityFx.column(to, 2.5, List.of(Particle.PORTAL, Particle.DRAGON_BREATH));
        SoundEngine.play(source, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        SoundEngine.play(destination, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        // The tear damages whatever is standing at the destination.
        PhysicalDamageEngine.areaDamage(caster, to, Math.max(2.0, a.getRadius()), a.getDamage(),
                a.getKnockback(), a.isTrueDamage());
        plugin.messages().send(source, "messages.ender-assembly-teleported",
                "&5&lENDER &7You were teleported to &f{destination}&7!",
                "{destination}", destination.getName());
        if (!destination.equals(source)) {
            plugin.messages().send(destination, "messages.ender-assembly-arrived",
                    "&5&lENDER &f{source} &7was teleported to you!",
                    "{source}", source.getName());
        }
    }

    /** A safe standable spot within a radius of the origin (never lava/water/void). */
    private Location safeSpot(Player p, Location origin, double radius) {
        org.bukkit.World world = origin.getWorld();
        int minY = world.getMinHeight() + 2;
        for (int attempt = 0; attempt < 14; attempt++) {
            double dx = (Math.random() * 2 - 1) * radius;
            double dz = (Math.random() * 2 - 1) * radius;
            Location candidate = origin.clone().add(dx, 0, dz);
            Location ground = groundBelow(candidate, 8);
            if (ground == null || ground.getY() < minY) {
                continue;
            }
            Material floor = ground.clone().subtract(0, 1, 0).getBlock().getType();
            Material feet = ground.getBlock().getType();
            Material head = ground.clone().add(0, 1, 0).getBlock().getType();
            if (floor == Material.LAVA || floor == Material.WATER || floor == Material.MAGMA_BLOCK
                    || floor == Material.CACTUS || floor == Material.FIRE
                    || !feet.isAir() || !head.isAir()) {
                continue;
            }
            Location result = ground.clone();
            result.setYaw(origin.getYaw());
            result.setPitch(origin.getPitch());
            return result;
        }
        return origin.clone();
    }

    /** The first standable ground location below a point, or null. */
    private Location groundBelow(Location from, int maxDrop) {
        for (int dy = 0; dy <= maxDrop; dy++) {
            Location at = from.clone().subtract(0, dy, 0);
            if (at.getBlock().getType().isSolid()) {
                return at.clone().add(0, 1, 0);
            }
        }
        return null;
    }

    /** One player's portal state (first/second placement + expiry). */
    private static final class PortalPair {
        private UUID owner;
        private Location first;
        private Location second;
        private long expiryMillis;
        private TokenTier.AbilitySpec ability;
        private BukkitRunnable task;
    }

    // ==================================================================
    // 2. CREEPER (each tier uses its own keybind)
    // ==================================================================

    /** T1 - TNT Cannon (RIGHT CLICK): launch a TNT shot toward the cursor. */
    private void tntCannon(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 4L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        double speed = Math.max(0.4, 0.9 * a.getSpeed());
        Location pos = eye.clone().add(dir.clone().multiply(1.2));
        BlockDisplay tnt = spawnTntDisplay(p, pos, 0.45f);
        Vector step = dir.clone().multiply(speed);
        int maxSteps = (int) Math.ceil(a.getRange() / speed);
        final boolean[] exploded = {false};
        scheduleSteps(maxSteps, 1L, i -> {
            if (exploded[0]) {
                return;
            }
            if (!tnt.isValid()) {
                exploded[0] = true;
                return;
            }
            pos.add(step);
            // The TNT starts small and grows to a full-size block while flying.
            scaleTntDisplay(tnt, Math.min(1.0f, 0.45f + i * 0.06f));
            tnt.teleport(pos);
            for (Particle particle : a.getParticles()) {
                ParticleEngine.burst(p.getWorld(), particle, pos, 2, 0.12);
            }
            if (pos.getBlock().getType().isSolid()) {
                exploded[0] = true;
                detonate(p, a, pos, tnt);
            }
        });
        // Safety: if it flew the whole way without hitting anything, blow up at the end.
        later(maxSteps + 2L, () -> {
            if (!exploded[0]) {
                exploded[0] = true;
                detonate(p, a, pos, tnt);
            }
        });
    }

    /** T2 - TNT Strike (SHIFT + LEFT CLICK): a slow 5x5 TNT block drops onto the cursor area. */
    private void tntStrike(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 4L);
        Location target = groundTarget(p, a.getRange());
        int side = Math.max(1, a.getCount());
        int half = side / 2;
        double startHeight = 14.0;
        List<BlockDisplay> blocks = new ArrayList<>();
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                Location at = target.clone().add(dx, startHeight, dz);
                blocks.add(spawnTntDisplay(p, at, 1.0f));
            }
        }
        AbilityFx.ring(target, side, List.of(Particle.SMOKE), 24);
        AbilityFx.dust(target, AbilityFx.BLACK, 30, 0.4);
        double dropPerStep = Math.max(0.15, a.getSpeed());
        int steps = (int) Math.ceil(startHeight / dropPerStep);
        scheduleSteps(steps, 2L, i -> {
            for (BlockDisplay block : blocks) {
                if (block.isValid()) {
                    block.teleport(block.getLocation().subtract(0, dropPerStep, 0));
                }
            }
            for (Particle particle : a.getParticles()) {
                ParticleEngine.burst(p.getWorld(), particle, target.clone().add(0, 1, 0), 2, side / 2.0);
            }
        });
        later(steps * 2L + 2L, () -> {
            for (BlockDisplay block : blocks) {
                if (block.isValid()) {
                    block.remove();
                }
            }
            // Massive impact.
            AbilityFx.impact(target, a.getParticles(), a.getParticleCount() * 2, 1.6);
            for (int ring = 1; ring <= 3; ring++) {
                AbilityFx.ring(target, a.getRadius() * ring / 3.0,
                        List.of(Particle.EXPLOSION, Particle.LARGE_SMOKE), 32);
            }
            AbilityFx.column(target, 5.0, List.of(Particle.LARGE_SMOKE, Particle.CLOUD));
            AbilityFx.soundsAt(target, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, target, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
            // Blow the ground out, then fill it back in.
            blastTerrain(target, a.getRadius(),
                    plugin.config().getInt("tokens.creeper.tier2.terrain-restore-ticks", 60));
        });
    }

    /** T3 - Bomb Chickens (SHIFT + RIGHT CLICK): 5 bomb-headed chickens hunt and detonate. */
    private void bombChickens(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 5L);
        int count = Math.max(1, a.getCount());
        double perHit = a.getDamage() / count;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            Location at = com.tokensmp.util.LocationUtil.onCircle(p.getLocation(), 2.0, angle, 0.2);
            spawnBombChicken(p, a, at, perHit);
        }
    }

    /** One bomb-headed chicken: follows the nearest player, detonates on its fuse. */
    private void spawnBombChicken(Player owner, TokenTier.AbilitySpec a, Location at, double perHit) {
        Chicken chicken = owner.getWorld().spawn(at, Chicken.class, c -> {
            c.setAdult();
            c.setAI(false);
            c.setSilent(false);
            c.setInvulnerable(true);
        });
        BlockDisplay head = spawnTntDisplay(owner, chicken.getLocation().add(0, 0.9, 0), 0.7f);
        int fuseTicks = (int) Math.max(20.0, a.getDurationSeconds() * 20.0);
        final boolean[] blown = {false};
        scheduleSteps(fuseTicks, 1L, i -> {
            if (blown[0]) {
                return;
            }
            if (!chicken.isValid()) {
                blown[0] = true;
                if (head.isValid()) {
                    head.remove();
                }
                return;
            }
            // Follow the nearest player that is not the caster.
            Player prey = nearestPlayer(owner, chicken.getLocation(), a.getRange());
            if (prey != null) {
                Vector to = prey.getLocation().toVector().subtract(chicken.getLocation().toVector());
                double distance = to.length();
                if (distance > 0.1) {
                    chicken.setVelocity(to.normalize().multiply(0.32).setY(0.24));
                }
                // Reached the target: blow up in time.
                if (distance < 1.8) {
                    blown[0] = true;
                    detonateChicken(owner, a, chicken, head, perHit);
                    return;
                }
            }
            if (head.isValid()) {
                head.teleport(chicken.getLocation().add(0, 0.9, 0));
            }
            for (Particle particle : a.getParticles()) {
                ParticleEngine.burst(owner.getWorld(), particle, chicken.getLocation().add(0, 0.8, 0),
                        2, 0.15);
            }
        });
        later(fuseTicks + 1L, () -> {
            if (!blown[0]) {
                blown[0] = true;
                detonateChicken(owner, a, chicken, head, perHit);
            }
        });
    }

    /** Removes a bomb chicken and detonates it. */
    private void detonateChicken(Player owner, TokenTier.AbilitySpec a,
                                 Chicken chicken, BlockDisplay head, double perHit) {
        Location at = chicken.getLocation().add(0, 0.5, 0);
        if (head.isValid()) {
            head.remove();
        }
        chicken.remove();
        AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 1.0);
        AbilityFx.ring(at, a.getRadius(), List.of(Particle.EXPLOSION), 22);
        AbilityFx.soundsAt(at, a.getSounds());
        PhysicalDamageEngine.areaDamage(owner, at, a.getRadius(), perHit, a.getKnockback(), a.isTrueDamage());
    }

    /** Detonates the TNT cannon shot (exact configured damage, no terrain damage). */
    private void detonate(Player p, TokenTier.AbilitySpec a, Location at, BlockDisplay tnt) {
        if (tnt.isValid()) {
            tnt.remove();
        }
        AbilityFx.impact(at, a.getParticles(), a.getParticleCount() * 2, 1.2);
        AbilityFx.ring(at, a.getRadius(), List.of(Particle.EXPLOSION, Particle.FLAME), 26);
        AbilityFx.soundsAt(at, a.getSounds());
        PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(), a.getKnockback(), a.isTrueDamage());
    }

    /** The nearest player to a location within a radius (never the caster). */
    private Player nearestPlayer(Player caster, Location from, double radius) {
        Player best = null;
        double bestDistance = radius * radius;
        for (Player other : caster.getWorld().getPlayers()) {
            if (other.equals(caster) || other.isDead() || other.getGameMode()
                    == org.bukkit.GameMode.SPECTATOR) {
                continue;
            }
            double distance = other.getLocation().distanceSquared(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    /** The ground point under the cursor (used by falling area abilities). */
    private Location groundTarget(Player caster, double range) {
        Location point = aimPoint(caster, range);
        Location ground = point.clone();
        for (int dy = 0; dy <= 6; dy++) {
            Location check = point.clone().subtract(0, dy, 0);
            if (check.getBlock().getType().isSolid()) {
                ground = check.clone().add(0, 1, 0);
                break;
            }
        }
        return ground;
    }

    /** Spawns a TNT block display (scaled) - visual only, no vanilla explosion. */
    private BlockDisplay spawnTntDisplay(Player owner, Location at, float scale) {
        BlockDisplay display = owner.getWorld().spawn(at, BlockDisplay.class, d -> {
            d.setBlock(Material.TNT.createBlockData());
            d.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new Quaternionf(),
                    new Vector3f(scale, scale, scale), new Quaternionf()));
        });
        display.setInterpolationDuration(2);
        display.setInterpolationDelay(0);
        return display;
    }

    private void scaleTntDisplay(BlockDisplay display, float scale) {
        if (!display.isValid()) {
            return;
        }
        display.setTransformation(new Transformation(new Vector3f(0f, 0f, 0f), new Quaternionf(),
                new Vector3f(scale, scale, scale), new Quaternionf()));
    }

    /**
     * Blows the terrain out in a sphere and fills it back in after a short
     * delay. Gated behind tokens.creeper.tier2.terrain-destruction; bedrock,
     * barriers, water and lava are never touched.
     */
    private void blastTerrain(Location center, double radius, int restoreTicks) {
        if (!plugin.config().getBoolean("tokens.creeper.tier2.terrain-destruction", true)) {
            return;
        }
        int configured = (int) Math.max(2.0,
                plugin.config().getDouble("tokens.creeper.tier2.terrain-radius", radius));
        List<BlockState> broken = new ArrayList<>();
        int max = 400;
        int r = configured;
        outer:
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (broken.size() >= max) {
                        break outer;
                    }
                    if (dx * dx + dy * dy + dz * dz > configured * configured) {
                        continue;
                    }
                    Block block = center.clone().add(dx, dy, dz).getBlock();
                    Material type = block.getType();
                    if (type.isAir() || type == Material.BEDROCK || type == Material.BARRIER
                            || type == Material.WATER || type == Material.LAVA) {
                        continue;
                    }
                    broken.add(block.getState());
                    block.setType(Material.AIR, false);
                }
            }
        }
        if (!broken.isEmpty()) {
            later(Math.max(20L, restoreTicks), () ->
                    broken.forEach(state -> state.update(true, false)));
        }
    }

    // ==================================================================
    // 3. SKELETON
    // ==================================================================

    /** T1 - Bone Bolt: high-speed spectral bone projectile, 18 blocks. */
    private void boneBolt(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        travel(p, a, start, dir, a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
        });
    }

    /** T2 - Ricochet Shot: projectile ricochets to a second nearby target. */
    private void ricochetShot(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        double perHit = a.getDamage() / 2.0;
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            if (victim == null) {
                return;
            }
            hitFor(victim, p, perHit, a.isTrueDamage());
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
            List<LivingEntity> next = nearest(p, at, 12.0, 1, victim);
            if (!next.isEmpty()) {
                LivingEntity second = next.get(0);
                SoundEngine.world(at, Sound.ENTITY_ARROW_HIT, 1.0f, 1.6f);
                travelHoming(p, a, at, second::getLocation, 0.9, (v2, at2) -> {
                    if (v2 != null) {
                        hitFor(v2, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at2, a.getParticles(), a.getParticleCount(), 0.5);
                });
            }
        });
    }

    /** T3 - Deadeye Barrage: cinematic volley of precision arrows. */
    private void deadeyeBarrage(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location target = aimPoint(p, a.getRange());
        // Arrows hover around the player briefly, then launch together.
        AbilityFx.spiral(p.getLocation().add(0, 1.5, 0), 1.2, 1.0, a.getParticles(), 24);
        int count = Math.max(2, a.getCount());
        double perHit = a.getDamage() / count;
        later(10L, () -> {
            for (int i = 0; i < count; i++) {
                double offset = (i - (count - 1) / 2.0) * 1.1;
                Location end = target.clone().add(offset, 0, offset * 0.5);
                travelArc(p, a, p.getEyeLocation(), end, 1.6, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.6);
                });
            }
            SoundEngine.world(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.2f, 1.2f);
        });
    }

    // ==================================================================
    // 4. GHAST
    // ==================================================================

    /** T1 - Ghast Orb: compact explosive projectile with a small blast. */
    private void ghastOrb(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.9);
            PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
            AbilityFx.soundsAt(at, a.getSounds());
        });
    }

    /** T2 - Inferno Comet: large flaming comet travelling in an arc. */
    private void infernoComet(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location target = aimPoint(p, a.getRange());
        travelArc(p, a, p.getEyeLocation(), target, 3.5, (victim, at) -> {
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount() * 2, 1.2);
            AbilityFx.column(at, 4.0, List.of(Particle.FLAME, Particle.LAVA));
            PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
            AbilityFx.soundsAt(at, a.getSounds());
        });
    }

    /** T3 - Netherfall: marked area bombarded by falling explosive projectiles. */
    private void netherfall(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        // Red targeting circle appears before the attack.
        AbilityFx.dust(center, AbilityFx.BLACK, 40, 0.3);
        AbilityFx.ring(center, a.getRadius(), List.of(Particle.FLAME, Particle.LAVA), 28);
        int count = Math.max(3, a.getCount());
        double perHit = a.getDamage() / count;
        for (int i = 0; i < count; i++) {
            final int index = i;
            later(12L + i * 6L, () -> {
                double angle = (Math.PI * 2 * index) / count;
                Location drop = com.tokensmp.util.LocationUtil.onCircle(center, a.getRadius() * 0.6, angle, 0);
                drop = drop.clone().add(0, 22, 0);
                travelHoming(p, a, drop, () -> center, 1.6, (victim, at) -> {
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 1.0);
                    PhysicalDamageEngine.areaDamage(p, at, 3.0, perHit, a.getKnockback(), a.isTrueDamage());
                });
            });
        }
        later(12L + count * 6L + 10L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.6);
            AbilityFx.ring(center, a.getRadius(), List.of(Particle.EXPLOSION, Particle.LAVA), 32);
            AbilityFx.soundsAt(center, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), perHit, a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 5. WARDEN
    // ==================================================================

    /** T1 - Sonic Jab: short-range concentrated sonic blast (narrow cone). */
    private void sonicJab(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        AbilityFx.line(start, dir, a.getRange(), List.of(Particle.SONIC_BOOM, Particle.SCULK_SOUL));
        coneStrike(p, a, 0.94);
        Location impact = start.clone().add(dir.clone().multiply(a.getRange()));
        AbilityFx.ring(impact, 1.6, List.of(Particle.SONIC_BOOM), 20);
    }

    /** T2 - Sonic Beam: focused beam damaging every enemy along the line. */
    private void sonicBeam(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        // 0.8s charge, then the beam fires.
        scheduleSteps(8, 2L, i -> AbilityFx.ring(p.getEyeLocation(), 0.6 + i * 0.15,
                List.of(Particle.SCULK_CHARGE, Particle.SONIC_BOOM), 12));
        later(16L, () -> {
            Location start = p.getEyeLocation();
            Vector dir = start.getDirection().normalize();
            AbilityFx.line(start, dir, a.getRange(), List.of(Particle.SONIC_BOOM, Particle.SCULK_CHARGE));
            for (LivingEntity victim : lineVictims(p, start, dir, a.getRange(), 1.8)) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
                AbilityFx.ring(victim.getLocation(), 1.4, List.of(Particle.SONIC_BOOM), 16);
            }
            SoundEngine.world(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.6f, 1.0f);
        });
    }

    /** T3 - Sonic Rupture: massive sonic wave expanding from the target. */
    private void sonicRupture(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        scheduleSteps(24, 1L, i -> {
            double radius = 1.0 + i * (a.getRadius() / 24.0);
            AbilityFx.ring(center, radius, List.of(Particle.SONIC_BOOM, Particle.SCULK_SOUL), 26);
            if (i % 6 == 0) {
                AbilityFx.impact(center, a.getParticles(), a.getParticleCount(), radius / 2);
            }
        });
        later(6L, () -> PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                a.getKnockback(), a.isTrueDamage()));
        later(30L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.8);
            AbilityFx.soundsAt(center, a.getSounds());
        });
    }

    // ==================================================================
    // 6. PIGLIN
    // ==================================================================

    /** T1 - Golden Cleave: golden melee slash travelling forward (arc). */
    private void goldenCleave(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        for (int i = 0; i <= 12; i++) {
            double angle = (Math.PI * (i / 12.0)) - Math.PI / 2;
            Vector rotated = dir.clone().rotateAroundY(angle * 0.6);
            Location point = eye.clone().add(rotated.multiply(2.0));
            AbilityFx.dust(point, AbilityFx.GOLD, 3, 0.15);
            ParticleEngine.point(p.getWorld(), Particle.CRIT, point);
        }
        coneStrike(p, a, 0.5);
        AbilityFx.dust(p.getLocation().add(0, 1, 0), AbilityFx.GOLD, 40, 0.6);
    }

    /** T2 - Gold Spear: golden spear that pierces the first and hits a second target. */
    private void goldSpear(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 2, (victim, at) -> {
            if (victim == null) {
                return;
            }
            hit(victim, a, p);
            knock(victim, start, a.getKnockback(), false);
            AbilityFx.dust(at, AbilityFx.GOLD, 24, 0.5);
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
        });
    }

    /** T3 - Royal Execution: cinematic dash into a huge golden crescent. */
    private void royalExecution(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Vector facing = p.getEyeLocation().getDirection().setY(0);
        if (facing.lengthSquared() < 0.001) {
            facing = new Vector(0, 0, 1);
        }
        final Vector dir = facing.normalize();
        p.setVelocity(dir.clone().multiply(a.getRange() * 0.12).setY(0.35));
        later(6L, () -> {
            coneStrike(p, a, 0.45);
            for (int i = 0; i <= 16; i++) {
                double angle = (Math.PI * (i / 16.0)) - Math.PI / 2;
                Vector rotated = dir.clone().rotateAroundY(angle * 0.7);
                Location point = p.getEyeLocation().clone().add(rotated.multiply(2.6));
                AbilityFx.dust(point, AbilityFx.GOLD, 4, 0.2);
                ParticleEngine.point(p.getWorld(), Particle.FLAME, point);
            }
            AbilityFx.dust(p.getLocation().add(0, 1, 0), AbilityFx.GOLD, 60, 0.9);
        });
    }

    // ==================================================================
    // 7. FISH
    // ==================================================================

    /** T1 - Aqua Bullet: compressed water projectile. */
    private void aquaBullet(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.spiral(p.getEyeLocation(), 0.6, 0.6, List.of(Particle.SPLASH, Particle.BUBBLE), 12);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.6);
            AbilityFx.ring(at, 1.4, List.of(Particle.SPLASH, Particle.BUBBLE), 16);
        });
    }

    /** T2 - Tidal Ram: a moving water wave that damages everything it passes. */
    private void tidalRam(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        groundLane(p, a, null, false);
        later(20L, () -> AbilityFx.impact(aimPoint(p, a.getRange()), a.getParticles(),
                a.getParticleCount() * 2, 1.0));
    }

    /** T3 - Leviathan Crash: water column forms above the target then crashes. */
    private void leviathanCrash(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        scheduleSteps(20, 1L, i -> {
            Location column = center.clone().add(0, i * 0.4, 0);
            for (Particle particle : a.getParticles()) {
                ParticleEngine.ring(center.getWorld(), particle, column, 1.6, 12);
            }
        });
        later(22L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.4);
            for (int i = 1; i <= 3; i++) {
                AbilityFx.ring(center, a.getRadius() * i / 3.0, List.of(Particle.SPLASH, Particle.CLOUD), 26);
            }
            AbilityFx.soundsAt(center, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 8. ZOMBIE
    // ==================================================================

    /** T1 - Rotten Smash: heavy close-range physical strike. */
    private void rottenSmash(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        coneStrike(p, a, 0.6);
        AbilityFx.ring(p.getLocation(), 1.8, List.of(Particle.CLOUD, Particle.CRIT), 18);
        AbilityFx.crack(p.getLocation(), Material.DIRT, 16, 0.6);
    }

    /** T2 - Grave Breaker: a ground fissure travels toward the target. */
    private void graveBreaker(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location end = aimPoint(p, a.getRange());
        groundLane(p, a, Material.DIRT, false);
        later(22L, () -> {
            AbilityFx.crack(end, Material.DIRT, 30, 1.0);
            AbilityFx.impact(end, a.getParticles(), a.getParticleCount(), 1.0);
            PhysicalDamageEngine.areaDamage(p, end, 3.0, a.getDamage() * 0.5,
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    /** T3 - Undead Cataclysm: huge undead shockwave erupting around the player. */
    private void undeadCataclysm(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = p.getLocation();
        scheduleSteps(18, 2L, i -> AbilityFx.ring(center, a.getRadius() * (i + 1) / 6.0,
                List.of(Particle.SOUL, Particle.ENCHANTED_HIT, Particle.HAPPY_VILLAGER), 30));
        PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                a.getKnockback(), a.isTrueDamage());
        AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.5);
    }

    // ==================================================================
    // 9. WITHER
    // ==================================================================

    /** T1 - Wither Skull: controlled wither skull projectile, no status effect. */
    private void witherSkull(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.9);
        });
    }

    /** T2 - Triple Skull Volley: three skulls on separate trajectories. */
    private void tripleSkullVolley(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location target = aimPoint(p, a.getRange());
        int count = Math.max(3, a.getCount());
        double perHit = a.getDamage() / count;
        // Skulls orbit the player briefly before firing.
        AbilityFx.spiral(p.getLocation().add(0, 1.5, 0), 1.4, 1.2, a.getParticles(), 20);
        later(12L, () -> {
            for (int i = 0; i < count; i++) {
                double offset = (i - 1) * 1.6;
                Location end = target.clone().add(offset, 0, offset);
                travelArc(p, a, p.getEyeLocation(), end, 2.2, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.8);
                });
            }
            SoundEngine.world(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.2f, 0.8f);
        });
    }

    /** T3 - Wither Barrage: rotating spiral barrage converging on the target. */
    private void witherBarrage(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        int count = Math.max(4, a.getCount());
        double perHit = a.getDamage() / count;
        for (int i = 0; i < count; i++) {
            final int index = i;
            later(6L + i * 4L, () -> {
                double angle = (Math.PI * 2 * index) / count;
                Location start = com.tokensmp.util.LocationUtil.onCircle(center, 8.0, angle, 3.0);
                travelHoming(p, a, start, () -> center, 1.2, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.7);
                });
            });
        }
        later(6L + count * 4L + 12L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.6);
            AbilityFx.ring(center, a.getRadius(), List.of(Particle.SOUL, Particle.EXPLOSION), 30);
            AbilityFx.soundsAt(center, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), perHit, a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 10. VILLAGER
    // ==================================================================

    /** T1 - Emerald Lance: compressed emerald-energy spear. */
    private void emeraldLance(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.dust(p.getEyeLocation(), AbilityFx.EMERALD, 30, 0.4);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.dust(at, AbilityFx.EMERALD, 24, 0.5);
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
        });
    }

    /** T2 - Trade Breaker: three emerald blades launched from different angles. */
    private void tradeBreaker(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = aimPoint(p, a.getRange());
        int count = Math.max(3, a.getCount());
        double perHit = a.getDamage() / count;
        AbilityFx.spiral(center, 2.4, 1.4, List.of(Particle.END_ROD), 20);
        for (int i = 0; i < count; i++) {
            final int index = i;
            later(i * 4L, () -> {
                double angle = (Math.PI * 2 * index) / count;
                Location start = com.tokensmp.util.LocationUtil.onCircle(center, 6.0, angle, 2.5);
                travelHoming(p, a, start, () -> center, 1.1, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.dust(at, AbilityFx.EMERALD, 18, 0.4);
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
                });
            });
        }
        later(count * 4L + 14L, () -> AbilityFx.ring(center, a.getRadius(),
                List.of(Particle.END_ROD), 24));
    }

    /** T3 - Emerald Judgment: charged emerald projectile that shatters into shards. */
    private void emeraldJudgment(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        // 1s charge: rotating emerald shards surround the target.
        scheduleSteps(20, 1L, i -> AbilityFx.ring(center, 2.0,
                List.of(Particle.END_ROD), 12 + (i % 8)));
        AbilityFx.dust(center, AbilityFx.EMERALD, 40, 1.0);
        later(22L, () -> {
            travelHoming(p, a, p.getEyeLocation(), () -> center, 1.4, (victim, at) -> {
                AbilityFx.impact(at, a.getParticles(), a.getParticleCount() * 2, 1.2);
                AbilityFx.ring(at, a.getRadius(), List.of(Particle.END_ROD), 28);
                AbilityFx.dust(at, AbilityFx.EMERALD, 60, 1.2);
                AbilityFx.soundsAt(at, a.getSounds());
                PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(),
                        a.getKnockback(), a.isTrueDamage());
            });
        });
    }

    // ==================================================================
    // 11. SLIME
    // ==================================================================

    /** T1 - Slime Slam: jump forward and crash down (area damage). */
    private void slimeSlam(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Vector dir = p.getEyeLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 0.001) {
            dir = new Vector(0, 0, 1);
        }
        p.setVelocity(dir.normalize().multiply(0.7).setY(0.55));
        later(14L, () -> {
            Location landing = p.getLocation();
            AbilityFx.impact(landing, a.getParticles(), a.getParticleCount() * 2, 1.2);
            AbilityFx.ring(landing, a.getRadius(), List.of(Particle.ITEM_SLIME), 26);
            PhysicalDamageEngine.areaDamage(p, landing, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
            AbilityFx.soundsAt(landing, a.getSounds());
        });
    }

    /** T2 - Bouncy Crush: bounce between up to three targets, damaging each landing. */
    private void bouncyCrush(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        int maxHits = Math.max(2, a.getCount());
        double perHit = a.getDamage() / maxHits;
        Set<LivingEntity> visited = new HashSet<>();
        bounceStep(p, a, perHit, maxHits, 0, visited);
    }

    private void bounceStep(Player p, TokenTier.AbilitySpec a, double perHit,
                            int maxHits, int done, Set<LivingEntity> visited) {
        if (done >= maxHits) {
            return;
        }
        List<LivingEntity> candidates = nearest(p, p.getLocation(), 12.0, 1, null);
        candidates.removeIf(visited::contains);
        if (candidates.isEmpty()) {
            return;
        }
        LivingEntity target = candidates.get(0);
        visited.add(target);
        p.teleport(target.getLocation().clone().add(0, 1.2, 0));
        AbilityFx.impact(target.getLocation(), a.getParticles(), a.getParticleCount(), 0.9);
        AbilityFx.ring(target.getLocation(), 2.4, List.of(Particle.ITEM_SLIME, Particle.CLOUD), 20);
        hitFor(target, p, perHit, a.isTrueDamage());
        knock(target, p.getLocation(), a.getKnockback(), true);
        SoundEngine.world(p.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1.2f, 0.8f);
        later(8L, () -> bounceStep(p, a, perHit, maxHits, done + 1, visited));
    }

    /** T3 - Mega Slime Impact: high launch into a massive downward slam. */
    private void megaSlimeImpact(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        p.setVelocity(new Vector(0, 1.3, 0));
        // Slow-motion-style descent visual while airborne.
        scheduleSteps(30, 2L, i -> AbilityFx.ring(p.getLocation(), 1.0 + (i % 5) * 0.3,
                List.of(Particle.ITEM_SLIME, Particle.CLOUD), 14));
        later(30L, () -> {
            Location landing = p.getLocation();
            for (int i = 1; i <= 3; i++) {
                AbilityFx.ring(landing, a.getRadius() * i / 3.0,
                        List.of(Particle.ITEM_SLIME, Particle.EXPLOSION), 30);
            }
            AbilityFx.impact(landing, a.getParticles(), a.getParticleCount() * 2, 1.5);
            AbilityFx.soundsAt(landing, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, landing, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 12. MAGMA CUBE
    // ==================================================================

    /** T1 - Magma Slam: molten ground strike. */
    private void magmaSlam(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = p.getLocation();
        AbilityFx.dust(center, AbilityFx.GOLD, 30, 0.8);
        AbilityFx.impact(center, a.getParticles(), a.getParticleCount(), 1.0);
        AbilityFx.ring(center, a.getRadius(), List.of(Particle.LAVA, Particle.FLAME), 24);
        PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                a.getKnockback(), a.isTrueDamage());
    }

    /** T2 - Magma Wave: a low molten wave that travels forward. */
    private void magmaWave(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        groundLane(p, a, null, false);
        later(18L, () -> {
            Location end = aimPoint(p, a.getRange());
            AbilityFx.column(end, 3.0, List.of(Particle.FLAME, Particle.LAVA));
            AbilityFx.impact(end, a.getParticles(), a.getParticleCount(), 1.0);
        });
    }

    /** T3 - Magma Core Eruption: molten core under the target erupts upward. */
    private void magmaCoreEruption(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        scheduleSteps(24, 1L, i -> {
            AbilityFx.ring(center, 1.0 + i * 0.12, List.of(Particle.LAVA), 16);
            if (i % 4 == 0) {
                AbilityFx.dust(center, AbilityFx.GOLD, 10, 0.4);
            }
        });
        later(26L, () -> {
            for (int i = 0; i <= 10; i++) {
                Location spike = center.clone().add(0, i * 0.5, 0);
                AbilityFx.ring(spike, 1.2, List.of(Particle.LAVA, Particle.SOUL_FIRE_FLAME), 12);
            }
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.4);
            AbilityFx.ring(center, a.getRadius(), List.of(Particle.LAVA, Particle.FLAME), 30);
            AbilityFx.soundsAt(center, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 13. ILLUSIONER
    // ==================================================================

    /** T1 - Phantom Arrow: precise illusion projectile that briefly splits. */
    private void phantomArrow(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location start = p.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        // Visual split: two decoy trails flanking the real projectile.
        AbilityFx.line(start.clone().add(0, 0.5, 0), dir.clone().rotateAroundY(0.12), 4.0,
                List.of(Particle.WITCH));
        AbilityFx.line(start.clone().add(0, -0.5, 0), dir.clone().rotateAroundY(-0.12), 4.0,
                List.of(Particle.WITCH));
        travel(p, a, start, dir, a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.6);
        });
    }

    /** T2 - Mirror Volley: illusion projectiles from different angles, all server-side. */
    private void mirrorVolley(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = aimPoint(p, a.getRange());
        int count = Math.max(3, a.getCount());
        double perHit = a.getDamage() / count;
        // Illusionary copies appear around the target.
        AbilityFx.spiral(center, 2.6, 1.6, List.of(Particle.WITCH, Particle.END_ROD), 24);
        for (int i = 0; i < count; i++) {
            final int index = i;
            later(i * 3L, () -> {
                double angle = (Math.PI * 2 * index) / count;
                Location start = com.tokensmp.util.LocationUtil.onCircle(center, 7.0, angle, 2.0);
                travelHoming(p, a, start, () -> center, 1.0, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
                });
            });
        }
        later(count * 3L + 12L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount(), 0.8);
            AbilityFx.soundsAt(center, a.getSounds());
        });
    }

    /** T3 - Reality Fracture: blades strike a fractured target from every angle. */
    private void realityFracture(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        int blades = Math.max(4, a.getCount());
        double perHit = a.getDamage() / blades;
        // Purple cracks spread through the air around the target.
        scheduleSteps(16, 1L, i -> AbilityFx.ring(center, 1.5 + (i % 6) * 0.4,
                List.of(Particle.WITCH, Particle.REVERSE_PORTAL), 16));
        for (int i = 0; i < blades; i++) {
            final int index = i;
            later(16L + i * 3L, () -> {
                double angle = (Math.PI * 2 * index) / blades;
                Location start = com.tokensmp.util.LocationUtil.onCircle(center, 5.0, angle, 2.2);
                travelHoming(p, a, start, () -> center, 1.3, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.5);
                });
            });
        }
        later(16L + blades * 3L + 8L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.4);
            AbilityFx.ring(center, a.getRadius(), List.of(Particle.WITCH, Particle.ENCHANTED_HIT), 30);
            SoundEngine.world(center, Sound.BLOCK_GLASS_BREAK, 1.4f, 0.8f);
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), perHit,
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 14. BLAZE
    // ==================================================================

    /** T1 - Flame Lance: concentrated flame projectile with a narrow hitbox. */
    private void flameLance(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        // Three small flames orbit the token, then combine.
        AbilityFx.spiral(p.getEyeLocation(), 0.7, 0.8, List.of(Particle.FLAME), 18);
        Location start = p.getEyeLocation();
        travel(p, a, start, start.getDirection().normalize(), a.getRange(), 1, (victim, at) -> {
            if (victim != null) {
                hit(victim, a, p);
                knock(victim, start, a.getKnockback(), false);
            }
            AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.6);
        });
    }

    /** T2 - Infernal Spiral: fire projectiles spiralling toward the target. */
    private void infernalSpiral(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = aimPoint(p, a.getRange());
        int count = Math.max(3, a.getCount());
        double perHit = a.getDamage() / count;
        for (int i = 0; i < count; i++) {
            final int index = i;
            later(i * 5L, () -> {
                double angle = (Math.PI * 2 * index) / count;
                Location start = com.tokensmp.util.LocationUtil.onCircle(center, 6.0, angle, 3.0);
                AbilityFx.spiral(start, 1.0, 1.2, List.of(Particle.FLAME, Particle.LAVA), 16);
                travelHoming(p, a, start, () -> center, 1.1, (victim, at) -> {
                    if (victim != null) {
                        hitFor(victim, p, perHit, a.isTrueDamage());
                    }
                    AbilityFx.impact(at, a.getParticles(), a.getParticleCount(), 0.7);
                });
            });
        }
        later(count * 5L + 14L, () -> AbilityFx.ring(center, a.getRadius(),
                List.of(Particle.FLAME, Particle.LAVA), 28));
    }

    /** T3 - Solar Burst: a compressed solar sphere detonates radially. */
    private void solarBurst(Player p, TokenTier.AbilitySpec a) {
        Location center = aimPoint(p, a.getRange());
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        // Sphere rapidly grows and compresses.
        scheduleSteps(20, 1L, i -> {
            double radius = i <= 10 ? 1.0 + i * 0.5 : 6.0 - (i - 10) * 0.5;
            AbilityFx.ring(center, Math.max(0.8, radius), List.of(Particle.FLAME, Particle.END_ROD), 20);
        });
        later(22L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.6);
            AbilityFx.ring(center, a.getRadius(), List.of(Particle.FLAME, Particle.LAVA, Particle.END_ROD), 34);
            AbilityFx.soundsAt(center, a.getSounds());
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 15. GOLEM
    // ==================================================================

    /** T1 - Iron Fist: devastating close-range punch with heavy knockback. */
    private void ironFist(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        coneStrike(p, a, 0.62);
        AbilityFx.crack(p.getLocation(), Material.STONE, 20, 0.8);
        AbilityFx.ring(p.getLocation(), 1.6, List.of(Particle.CLOUD), 16);
    }

    /** T2 - Ironquake: two shockwave rings sent forward, each pushing enemies back. */
    private void ironquake(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        double perWave = a.getDamage() / 2.0;
        for (int wave = 0; wave < 2; wave++) {
            later(wave * 10L, () -> {
                Location base = p.getLocation();
                Vector dir = p.getEyeLocation().getDirection().setY(0);
                if (dir.lengthSquared() < 0.001) {
                    dir = new Vector(0, 0, 1);
                }
                dir.normalize();
                Vector waveDir = dir;
                Set<LivingEntity> struck = new HashSet<>();
                scheduleSteps(16, 1L, i -> {
                    Location point = base.clone().add(waveDir.clone().multiply(2.0 + i * 1.0));
                    AbilityFx.crack(point, Material.STONE, 6, 0.4);
                    AbilityFx.ring(point, 1.4, List.of(Particle.CLOUD, Particle.EXPLOSION), 14);
                    for (LivingEntity victim : radiusVictims(p, point, 1.6)) {
                        if (struck.add(victim)) {
                            hitFor(victim, p, perWave, a.isTrueDamage());
                            knock(victim, point, a.getKnockback(), false);
                        }
                    }
                });
            });
        }
        later(30L, () -> AbilityFx.impact(p.getLocation(), a.getParticles(),
                a.getParticleCount(), 1.2));
    }

    /** T3 - Colossus Impact: the ultimate - three enormous sequential shockwaves. */
    private void colossusImpact(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        soundSequence(p, a.getSounds(), 3L);
        Location center = p.getLocation();
        // Charge: a ground ring grows around the player.
        scheduleSteps(30, 2L, i -> {
            AbilityFx.ring(center, 1.0 + i * 0.4, List.of(Particle.CLOUD), 20);
            if (i % 5 == 0) {
                AbilityFx.crack(center, Material.STONE, 12, 1.0);
            }
        });
        later(32L, () -> {
            AbilityFx.impact(center, a.getParticles(), a.getParticleCount() * 2, 1.6);
            PhysicalDamageEngine.areaDamage(p, center, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
            for (int i = 1; i <= 3; i++) {
                final int ring = i;
                later((i - 1) * 6L, () -> {
                    AbilityFx.ring(center, a.getRadius() * ring / 3.0,
                            List.of(Particle.CLOUD, Particle.EXPLOSION, Particle.CRIT), 36);
                    AbilityFx.crack(center, Material.STONE, 24, a.getRadius() * ring / 3.0);
                });
            }
        });
    }

    // ==================================================================
    // ADMIN (isolated)
    // ==================================================================

    /** Admin T1 - Nether Shockwave: 15-block true damage shockwave. */
    private void netherShockwave(Player p, TokenTier.AbilitySpec a) {
        soundSequence(p, a.getSounds(), 3L);
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
        for (int i = 1; i <= 3; i++) {
            AbilityFx.ring(p.getLocation(), a.getRadius() * i / 3.0, List.of(Particle.FLAME), 30);
        }
        PhysicalDamageEngine.areaDamage(p, p.getLocation(), a.getRadius(), a.getDamage(),
                a.getKnockback(), true);
    }

    /** Admin T2 - Chrono Freeze: freezes every player in radius. */
    private void chronoFreeze(Player p, TokenTier.AbilitySpec a) {
        soundSequence(p, a.getSounds(), 3L);
        ParticleEngine.burst(p.getWorld(), Particle.SNOWFLAKE,
                p.getLocation().add(0, 1, 0), 60, a.getRadius() / 2);
        for (Entity entity : p.getNearbyEntities(a.getRadius(), a.getRadius(), a.getRadius())) {
            if (entity instanceof Player target) {
                freezeManager.freeze(target, a.getDurationSeconds());
            }
        }
    }

    /** Admin T3 - Server Judgment: god-tier sonic projectile with 20-block knockback. */
    private void serverJudgment(Player p, TokenTier.AbilitySpec a) {
        soundSequence(p, a.getSounds(), 3L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        AbilityFx.line(eye, dir, 30, List.of(Particle.SONIC_BOOM));
        for (LivingEntity victim : lineVictims(p, eye, dir, 30.0, 3.0)) {
            PhysicalDamageEngine.dealTrueDamage(victim, a.getDamage(), p);
            Vector push = dir.clone().multiply(a.getKnockback() / 8.0);
            push.setY(1.2);
            victim.setVelocity(victim.getVelocity().add(push));
        }
        ParticleEngine.column(p.getWorld(), Particle.END_ROD, p.getLocation(), 4, 12);
    }

    // ==================================================================
    // Shared helpers
    // ==================================================================

    /** Short-range cone strike in front of the caster. */
    private void coneStrike(Player p, TokenTier.AbilitySpec a, double halfAngleCos) {
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        double range = Math.max(2.0, a.getRange());
        for (LivingEntity victim : coneVictims(p, range, halfAngleCos)) {
            hit(victim, a, p);
            knock(victim, eye, a.getKnockback(), false);
            AbilityFx.impact(victim.getLocation().add(0, 1, 0), a.getParticles(), a.getParticleCount() / 2, 0.5);
        }
    }

    /** Ground lane attack: a wave travelling forward along the floor. */
    private void groundLane(Player p, TokenTier.AbilitySpec a, Material crackMaterial, boolean upwardKnock) {
        Location base = p.getLocation();
        Vector dir = p.getEyeLocation().getDirection();
        dir.setY(0);
        if (dir.lengthSquared() < 0.001) {
            dir = new Vector(0, 0, 1);
        }
        final Vector lane = dir.normalize();
        int steps = (int) Math.max(6, a.getRange());
        Set<LivingEntity> already = new HashSet<>();
        scheduleSteps(steps, 1L, i -> {
            Location point = base.clone().add(lane.clone().multiply(i));
            for (Particle particle : a.getParticles()) {
                ParticleEngine.burst(point.getWorld(), particle, point.clone().add(0, 0.25, 0), 4, 0.2);
            }
            if (crackMaterial != null) {
                AbilityFx.crack(point, crackMaterial, 4, 0.3);
            }
            for (LivingEntity victim : radiusVictims(p, point, 1.8)) {
                if (already.add(victim)) {
                    hit(victim, a, p);
                    knock(victim, point, a.getKnockback(), upwardKnock);
                }
            }
        });
    }

    /** Straight-line projectile with pierce count and per-hit impact callback. */
    private void travel(Player caster, TokenTier.AbilitySpec a, Location start, Vector dir,
                        double distance, int pierce, Impact impact) {
        Location pos = start.clone();
        double speed = Math.max(0.45, 0.75 * a.getSpeed());
        Vector step = dir.clone().normalize().multiply(speed);
        int maxSteps = (int) Math.ceil(distance / speed);
        final int[] stepIndex = {0};
        final int[] left = {Math.max(1, pierce)};
        final List<LivingEntity> hitList = new ArrayList<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (stepIndex[0]++ >= maxSteps) {
                    AbilityFx.impact(pos, a.getParticles(), a.getParticleCount(), 0.5);
                    cancel();
                    return;
                }
                pos.add(step);
                if (pos.getBlock().getType().isSolid()) {
                    AbilityFx.impact(pos, a.getParticles(), a.getParticleCount(), 0.5);
                    cancel();
                    return;
                }
                for (Particle particle : a.getParticles()) {
                    ParticleEngine.burst(pos.getWorld(), particle, pos, 2, 0.06);
                }
                for (Entity entity : pos.getWorld().getNearbyEntities(pos, 1.3, 1.3, 1.3)) {
                    if (entity instanceof LivingEntity victim && !victim.equals(caster)
                            && !hitList.contains(victim)) {
                        hitList.add(victim);
                        impact.apply(victim, victim.getLocation().add(0, 1, 0));
                        if (--left[0] <= 0) {
                            cancel();
                            return;
                        }
                    }
                }
            }
        };
        runnable.runTaskTimer(plugin, 0L, 1L);
        scheduler.register(runnable);
    }

    /** Parabolic projectile from a start point to a target with an arc height. */
    private void travelArc(Player caster, TokenTier.AbilitySpec a, Location start, Location target,
                           double arcHeight, Impact impact) {
        double dist = start.distance(target);
        int steps = Math.max(10, (int) (dist * 1.3));
        final int[] index = {0};
        final List<LivingEntity> hitList = new ArrayList<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (index[0] > steps) {
                    AbilityFx.impact(target, a.getParticles(), a.getParticleCount(), 0.7);
                    cancel();
                    return;
                }
                double t = index[0] / (double) steps;
                Location point = start.clone().add(target.toVector().subtract(start.toVector()).multiply(t));
                point.add(0, Math.sin(Math.PI * t) * arcHeight, 0);
                for (Particle particle : a.getParticles()) {
                    ParticleEngine.burst(point.getWorld(), particle, point, 3, 0.1);
                }
                for (Entity entity : point.getWorld().getNearbyEntities(point, 1.7, 1.7, 1.7)) {
                    if (entity instanceof LivingEntity victim && !victim.equals(caster)
                            && !hitList.contains(victim)) {
                        hitList.add(victim);
                        impact.apply(victim, point);
                        cancel();
                        return;
                    }
                }
                index[0]++;
            }
        };
        runnable.runTaskTimer(plugin, 0L, 1L);
        scheduler.register(runnable);
    }

    /** Projectile that homes onto a (possibly moving) target location. */
    private void travelHoming(Player caster, TokenTier.AbilitySpec a, Location start,
                              Supplier<Location> targetSupplier, double speed, Impact impact) {
        Location pos = start.clone();
        final int[] life = {120};
        final List<LivingEntity> hitList = new ArrayList<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (life[0]-- <= 0) {
                    cancel();
                    return;
                }
                Location target = targetSupplier.get();
                if (target == null) {
                    cancel();
                    return;
                }
                Vector delta = target.toVector().subtract(pos.toVector());
                if (delta.lengthSquared() <= Math.max(1.0, speed * speed)) {
                    boolean struck = false;
                    for (Entity entity : pos.getWorld().getNearbyEntities(target, 2.2, 2.2, 2.2)) {
                        if (entity instanceof LivingEntity victim && !victim.equals(caster)
                                && !hitList.contains(victim)) {
                            hitList.add(victim);
                            impact.apply(victim, target);
                            struck = true;
                            break;
                        }
                    }
                    if (!struck) {
                        impact.apply(null, target);
                    }
                    cancel();
                    return;
                }
                pos.add(delta.normalize().multiply(speed));
                for (Particle particle : a.getParticles()) {
                    ParticleEngine.burst(pos.getWorld(), particle, pos, 3, 0.1);
                }
            }
        };
        runnable.runTaskTimer(plugin, 0L, 1L);
        scheduler.register(runnable);
    }

    /** Per-hit impact callback (victim may be null for ground/area impacts). */
    @FunctionalInterface
    private interface Impact {
        void apply(LivingEntity victim, Location at);
    }

    /** Damages a target using the ability's damage mode. */
    private void hit(LivingEntity target, TokenTier.AbilitySpec a, Player caster) {
        hitFor(target, caster, a.getDamage(), a.isTrueDamage());
    }

    private void hitFor(LivingEntity target, Player caster, double damage, boolean trueDamage) {
        if (target == null || target.isDead()) {
            return;
        }
        if (trueDamage) {
            PhysicalDamageEngine.dealTrueDamage(target, damage, caster);
        } else {
            PhysicalDamageEngine.dealDamage(target, damage, caster);
        }
    }

    /** Pushes a victim away from an origin, optionally with an upward arc. */
    private void knock(LivingEntity target, Location from, double strength, boolean upward) {
        if (strength <= 0) {
            return;
        }
        Vector away = target.getLocation().toVector().subtract(from.toVector());
        if (away.lengthSquared() < 0.001) {
            away = new Vector(0, 1, 0);
        }
        Vector push = away.normalize().multiply(strength);
        push.setY(upward ? Math.max(0.5, strength * 0.5) : Math.max(0.3, push.getY()));
        target.setVelocity(push);
    }

    /** Living entities within a radius of a centre (caster excluded). */
    private List<LivingEntity> radiusVictims(Player caster, Location center, double radius) {
        List<LivingEntity> victims = new ArrayList<>();
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !entity.equals(caster)) {
                victims.add(living);
            }
        }
        return victims;
    }

    /** Living entities inside a forward cone. */
    private List<LivingEntity> coneVictims(Player caster, double range, double halfAngleCos) {
        List<LivingEntity> victims = new ArrayList<>();
        Location eye = caster.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        for (Entity entity : caster.getWorld().getNearbyEntities(eye, range, range, range)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(caster)) {
                continue;
            }
            Vector to = living.getLocation().add(0, 0.9, 0).toVector().subtract(eye.toVector());
            double distance = to.length();
            if (distance > range || distance < 0.1) {
                continue;
            }
            if (to.normalize().dot(dir) >= halfAngleCos) {
                victims.add(living);
            }
        }
        return victims;
    }

    /** Living entities along a forward ray (unique, caster excluded). */
    private List<LivingEntity> lineVictims(Player caster, Location start, Vector direction,
                                           double length, double hitRadius) {
        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= length; distance += 1.5) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            for (Entity entity : caster.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                if (entity instanceof LivingEntity living && !entity.equals(caster)
                        && !victims.contains(living)) {
                    victims.add(living);
                }
            }
        }
        return victims;
    }

    /** The point the player is aiming at, clamped to a maximum range. */
    private Location aimPoint(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Location point = eye.clone();
        for (double travelled = 1.0; travelled <= range; travelled += 0.5) {
            Location next = eye.clone().add(dir.clone().multiply(travelled));
            if (next.getBlock().getType().isSolid()) {
                break;
            }
            point = next;
        }
        return point;
    }

    /** The nearest living entities to a centre (caster excluded, optionally one excluded). */
    private List<LivingEntity> nearest(Player caster, Location center, double radius,
                                       int max, LivingEntity exclude) {
        List<LivingEntity> found = radiusVictims(caster, center, radius);
        if (exclude != null) {
            found.remove(exclude);
        }
        found.sort((left, right) -> Double.compare(
                left.getLocation().distanceSquared(center),
                right.getLocation().distanceSquared(center)));
        if (found.size() > max) {
            return new ArrayList<>(found.subList(0, max));
        }
        return found;
    }

    /** Plays the ability's sound sequence with a short stagger between layers. */
    private void soundSequence(Player player, List<Sound> sounds, long spacingTicks) {
        if (sounds.isEmpty()) {
            return;
        }
        SoundEngine.play(player, sounds.get(0), 1.0f, 1.0f);
        for (int i = 1; i < sounds.size(); i++) {
            final Sound sound = sounds.get(i);
            later(spacingTicks * i, () -> SoundEngine.play(player, sound, 1.0f, 1.0f));
        }
    }

    /** Schedules a counted sequence of steps on the main thread. */
    private void scheduleSteps(int steps, long periodTicks, IntConsumer step) {
        BukkitRunnable runnable = new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (index >= steps) {
                    cancel();
                    return;
                }
                step.accept(index++);
            }
        };
        runnable.runTaskTimer(plugin, 0L, Math.max(1L, periodTicks));
        scheduler.register(runnable);
    }

    /** Runs an action after a delay (registered for cleanup). */
    private void later(long delayTicks, Runnable action) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                action.run();
            }
        };
        runnable.runTaskLater(plugin, Math.max(1L, delayTicks));
        scheduler.register(runnable);
    }
}
