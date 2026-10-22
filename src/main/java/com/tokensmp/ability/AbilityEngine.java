package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleEngine;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.core.VersionCompatibility;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
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
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * The ability engine: one distinct implementation per token ability, executed
 * for the tier that is currently active. Every ability is self-contained,
 * server-authoritative, cleans up after itself and never griefs the world
 * permanently (temporary blocks are always restored).
 */
public final class AbilityEngine {

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;
    private final FreezeManager freezeManager;

    /** Guardian water-shield expiry timestamps. */
    private final Map<UUID, Long> shieldUntil = new HashMap<>();

    public AbilityEngine(TokenSMP plugin, SchedulerManager scheduler, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.freezeManager = freezeManager;
    }

    /** Dispatches one ability activation. */
    public void execute(Player player, TokenTier.AbilitySpec a) {
        switch (a.getType()) {
            // 1. Zombie
            case UNDEAD_VITALITY -> undeadVitality(player, a);
            // 2. Skeleton
            case ARCHERS_FOCUS -> archersFocus(player, a);
            // 3. Spider
            case WEB_TRAPPER -> webTrapper(player, a);
            // 4. Creeper
            case MEGA_DETONATOR -> megaDetonator(player, a);
            // 5. Blaze
            case INFERNAL_AURA -> infernalAura(player, a);
            // 6. Enderman
            case VOID_SHIFT -> voidShift(player, a);
            // 7. Warden
            case SONIC_SHRIEK -> sonicShriek(player, a);
            // 8. Ender Dragon
            case DRAGONS_MIGHT -> dragonsMight(player, a);
            // 9. Guardian
            case OCEANS_GRACE -> oceansGrace(player, a);
            // 10. Wither Skeleton
            case DARK_PRESENCE -> darkPresence(player, a);
            // 11. Ravager
            case BERSERKER_RAGE -> berserkerRage(player, a);
            // 12. Wolf
            case PACK_LEADER -> packLeader(player, a);
            // 13. Pig
            case HOG_HEAL -> hogHeal(player, a);
            // 14. Chicken
            case FEATHER_FALL -> featherFall(player, a);
            // 15. Snow Golem
            case FROZEN_HEART -> frozenHeart(player, a);
            // 16. Bee
            case BUZZ_RUSH -> buzzRush(player, a);
            // 17. Horse
            case STALLION_SPRINT -> stallionSprint(player, a);
            // 18. Bat
            case ECHOLOCATION -> echolocation(player, a);
            // 19. Squid
            case INK_CLOUD -> inkCloud(player, a);
            // 20. Witch
            case BREW_MASTER -> brewMaster(player, a);
            // Admin (isolated)
            case NETHER_SHOCKWAVE -> netherShockwave(player, a);
            case CHRONO_FREEZE -> chronoFreeze(player, a);
            case SERVER_JUDGMENT -> serverJudgment(player, a);
            default -> {
                // NONE - nothing to run
            }
        }
    }

    // ==================================================================
    // 1. ZOMBIE - Undead Vitality: heal + poison nearby enemies
    // ==================================================================
    private void undeadVitality(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        // Instant heal (6 hearts by default, tier-scaled).
        double hearts = Math.max(1.0, a.getRadius());
        double max = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null ? 20.0
                : p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        p.setHealth(Math.min(max, p.getHealth() + hearts * 2.0));
        AbilityFx.ring(p.getLocation(), 1.6, List.of(Particle.HAPPY_VILLAGER, Particle.HEART), 26);
        scheduleSteps(6, 3L, i -> AbilityFx.ring(p.getLocation(), 1.0 + i * 0.4,
                List.of(Particle.HAPPY_VILLAGER), 22));
        // Poison everything around.
        int ticks = (int) (Math.max(2.0, a.getDurationSeconds()) * 20.0);
        for (LivingEntity victim : radiusVictims(p, p.getLocation(), a.getRange())) {
            potion(victim, "POISON", "POISON", ticks, Math.max(0, (int) a.getCount() - 1));
            hit(victim, a, p);
            AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                    List.of(Particle.HAPPY_VILLAGER, Particle.SMOKE), 18, 0.5);
        }
    }

    // ==================================================================
    // 2. SKELETON - Archer's Focus: 360 degree arrow rain
    // ==================================================================
    private void archersFocus(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        int arrows = Math.max(4, a.getCount());
        double perHit = a.getDamage() / arrows;
        for (int i = 0; i < arrows; i++) {
            double angle = (Math.PI * 2 * i) / arrows;
            Vector dir = new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize();
            Location start = p.getEyeLocation();
            AbilityFx.line(start, dir, 3.0, List.of(Particle.CRIT, Particle.SNOWFLAKE));
            travel(p, a, start, dir, a.getRange(), 1, (victim, at) -> {
                hitFor(victim, p, perHit, a.isTrueDamage());
                AbilityFx.impact(at, List.of(Particle.CRIT, Particle.SNOWFLAKE), 16, 0.5);
                SoundEngine.world(at, Sound.ENTITY_ARROW_HIT, 1.0f, 1.4f);
            });
        }
    }

    // ==================================================================
    // 3. SPIDER - Web Trapper: cobwebs immobilise enemies
    // ==================================================================
    private void webTrapper(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        int restore = plugin.config().getInt("tokens.spider.web-restore-ticks", 160);
        List<LivingEntity> victims = radiusVictims(p, p.getLocation(), a.getRange());
        for (LivingEntity victim : victims) {
            // Trap the victim's feet in cobwebs (restored automatically).
            Location feet = victim.getLocation();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    placeTemp(feet.clone().add(dx, 0, dz), Material.COBWEB, restore);
                }
            }
            hit(victim, a, p);
            potion(victim, "SLOWNESS", "SLOW", (int) (a.getDurationSeconds() * 20.0), 2);
            AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                    List.of(Particle.ITEM_COBWEB, Particle.WITCH), 26, 0.7);
        }
        AbilityFx.ring(p.getLocation(), a.getRange(),
                List.of(Particle.ITEM_COBWEB, Particle.WITCH), 40);
    }

    // ==================================================================
    // 4. CREEPER - Mega Detonator: huge safe explosion
    // ==================================================================
    private void megaDetonator(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location at = p.getLocation();
        // Charge-up fuse with the classic creeper hiss.
        SoundEngine.world(at, Sound.ENTITY_CREEPER_PRIMED, 1.6f, 0.8f);
        scheduleSteps(10, 2L, i -> AbilityFx.ring(at, 0.6 + i * 0.5,
                List.of(Particle.SMOKE, Particle.EXPLOSION), 24));
        later(20L, () -> {
            SoundEngine.world(at, Sound.ENTITY_GENERIC_EXPLODE, 1.8f, 0.7f);
            AbilityFx.impact(at, List.of(Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE),
                    70, 1.8);
            for (int ring = 1; ring <= 4; ring++) {
                AbilityFx.ring(at, a.getRadius() * ring / 4.0,
                        List.of(Particle.EXPLOSION, Particle.FLAME, Particle.CLOUD), 36);
            }
            AbilityFx.crack(at, Material.DIRT, 50, a.getRadius() / 2.0);
            // The owner is never hurt by their own detonation.
            PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(),
                    a.getKnockback(), a.isTrueDamage());
        });
    }

    // ==================================================================
    // 5. BLAZE - Infernal Aura: three explosive fireballs
    // ==================================================================
    private void infernalAura(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        int shots = Math.max(1, a.getCount());
        double perHit = a.getDamage() / shots;
        for (int i = 0; i < shots; i++) {
            final int index = i;
            later(index * 8L, () -> {
                if (!p.isOnline()) {
                    return;
                }
                Vector aim = p.getEyeLocation().getDirection().normalize();
                SoundEngine.world(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.2f, 1.0f);
                AbilityFx.line(p.getEyeLocation(), aim, 2.0, List.of(Particle.FLAME));
                travel(p, a, p.getEyeLocation(), aim, a.getRange(), 1, (victim, at) -> {
                    AbilityFx.impact(at, List.of(Particle.EXPLOSION, Particle.FLAME, Particle.LAVA),
                            30, 1.0);
                    AbilityFx.ring(at, a.getRadius(), List.of(Particle.FLAME, Particle.LAVA), 24);
                    for (LivingEntity target : radiusVictims(p, at, a.getRadius())) {
                        hitFor(target, p, perHit, a.isTrueDamage());
                        target.setFireTicks(Math.max(target.getFireTicks(),
                                (int) (a.getDurationSeconds() * 20.0)));
                        knock(target, at, a.getKnockback(), true);
                    }
                    SoundEngine.world(at, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.2f);
                });
            });
        }
    }

    // ==================================================================
    // 6. ENDERMAN - Void Shift: line-of-sight teleport
    // ==================================================================
    private void voidShift(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location from = p.getLocation().clone();
        Location target = aimPoint(p, a.getRange());
        double distance = from.distance(target);
        if (distance < Math.max(3.0, a.getRadius())) {
            target = from.clone().add(from.getDirection().normalize()
                    .multiply(Math.max(3.0, a.getRadius())));
        }
        AbilityFx.spiral(from.clone().add(0, 1, 0), 1.4, 2.2,
                List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 26);
        AbilityFx.impact(from, a.getParticles(), a.getParticleCount(), 0.8);
        p.teleport(target);
        p.setFallDistance(0f);
        AbilityFx.impact(target, a.getParticles(), a.getParticleCount(), 0.8);
        AbilityFx.spiral(target.clone().add(0, 1, 0), 1.4, 2.2,
                List.of(Particle.PORTAL, Particle.REVERSE_PORTAL), 26);
        SoundEngine.play(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        SoundEngine.world(target, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.9f);
        // Void shock at the arrival point.
        for (LivingEntity victim : radiusVictims(p, target, a.getRadius())) {
            hit(victim, a, p);
            knock(victim, target, a.getKnockback(), true);
        }
    }

    // ==================================================================
    // 7. WARDEN - Sonic Shriek: penetrating beam
    // ==================================================================
    private void sonicShriek(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        SoundEngine.world(eye, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 0.9f);
        scheduleSteps(10, 1L, i -> {
            AbilityFx.line(p.getEyeLocation(), dir, 2.0 + i * 1.2,
                    List.of(Particle.SONIC_BOOM, Particle.SCULK_SOUL));
            AbilityFx.ring(p.getEyeLocation().add(dir.clone().multiply(2.0 + i * 1.2)), 0.8,
                    List.of(Particle.SONIC_BOOM), 14);
        });
        later(10L, () -> {
            SoundEngine.world(p.getEyeLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.6f, 1.0f);
            AbilityFx.line(eye, dir, a.getRange(),
                    List.of(Particle.SONIC_BOOM, Particle.SCULK_SOUL, Particle.SONIC_BOOM));
            for (LivingEntity victim : lineVictims(p, eye, dir, a.getRange(), 1.4)) {
                hit(victim, a, p);
                knock(victim, p.getLocation(), a.getKnockback(), true);
                AbilityFx.ring(victim.getLocation().add(0, 1, 0), 1.4,
                        List.of(Particle.SONIC_BOOM, Particle.SCULK_SOUL), 20);
                SoundEngine.world(victim.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.2f);
            }
        });
    }

    // ==================================================================
    // 8. ENDER DRAGON - Dragon's Might: fireball + breath cloud
    // ==================================================================
    private void dragonsMight(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 4L);
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        SoundEngine.world(eye, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.4f, 1.0f);
        travel(p, a, eye, dir, a.getRange(), 1, (victim, at) -> {
            hit(victim, a, p);
            breathCloud(p, at, a);
        });
        // If it flies the whole way, leave the cloud where it lands.
        later(60L, () -> breathCloud(p, aimPoint(p, a.getRange()), a));
    }

    /** A lingering dragon-breath cloud that damages anything inside. */
    private void breathCloud(Player owner, Location at, TokenTier.AbilitySpec a) {
        SoundEngine.world(at, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 1.0f);
        AbilityFx.impact(at, List.of(Particle.DRAGON_BREATH, Particle.PORTAL), 40, 1.0);
        AreaEffectCloud cloud = at.getWorld().spawn(at, AreaEffectCloud.class, c -> {
            c.setParticle(Particle.DRAGON_BREATH);
            c.setRadius((float) Math.max(2.0, a.getRadius()));
            c.setRadiusPerTick(0.01f);
            c.setDuration((int) Math.max(40.0, a.getDurationSeconds() * 20.0));
            c.setRadiusOnUse(0f);
            c.setWaitTime(0);
        });
        PotionEffectType instant = VersionCompatibility.potionType("INSTANT_DAMAGE", "HARM");
        if (instant != null) {
            cloud.addCustomEffect(new PotionEffect(instant, 1,
                    Math.max(0, (int) (a.getDamage() / 8.0) - 1)), true);
        }
    }

    // ==================================================================
    // 9. GUARDIAN - Ocean's Grace: water shield (deflect + thorns)
    // ==================================================================
    private void oceansGrace(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        long until = System.currentTimeMillis()
                + (long) Math.max(3.0, a.getDurationSeconds()) * 1000L;
        shieldUntil.put(p.getUniqueId(), until);
        SoundEngine.play(p, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.4f);
        scheduleSteps((int) (Math.max(3.0, a.getDurationSeconds()) * 20.0), 2L, i -> {
            if (!p.isOnline() || System.currentTimeMillis() > until) {
                return;
            }
            for (Particle particle : List.of(Particle.BUBBLE, Particle.BUBBLE_POP,
                    Particle.NAUTILUS, Particle.SPLASH)) {
                AbilityFx.ring(p.getLocation().add(0, 0.4, 0), a.getRadius(),
                        List.of(particle), 26);
            }
        });
        later((long) (Math.max(3.0, a.getDurationSeconds()) * 20.0) + 2L, () -> {
            shieldUntil.remove(p.getUniqueId());
            SoundEngine.play(p, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
            AbilityFx.impact(p.getLocation().add(0, 1, 0), List.of(Particle.SPLASH), 30, 1.0);
        });
    }

    /** True while the Guardian water shield is up. */
    public boolean isShielded(Player player) {
        Long until = shieldUntil.get(player.getUniqueId());
        return until != null && System.currentTimeMillis() <= until;
    }

    /** The thorns damage a shielded player reflects on melee attackers. */
    public double shieldThorns() {
        return plugin.config().getDouble("tokens.guardian.thorns-damage", 8.0);
    }

    // ==================================================================
    // 10. WITHER SKELETON - Dark Presence: wither pulse + drain
    // ==================================================================
    private void darkPresence(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        Location at = p.getLocation();
        SoundEngine.world(at, Sound.ENTITY_WITHER_SHOOT, 1.4f, 0.8f);
        AbilityFx.impact(at, List.of(Particle.SOUL, Particle.SMOKE, Particle.ASH), 50, 1.4);
        for (int ring = 1; ring <= 3; ring++) {
            AbilityFx.ring(at, a.getRadius() * ring / 3.0,
                    List.of(Particle.SOUL, Particle.SOUL_FIRE_FLAME), 32);
        }
        int ticks = (int) (Math.max(2.0, a.getDurationSeconds()) * 20.0);
        double drained = 0.0;
        for (LivingEntity victim : radiusVictims(p, at, a.getRadius())) {
            hit(victim, a, p);
            potion(victim, "WITHER", "WITHER", ticks, 1);
            drained += a.getDamage() * 0.25;
            AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                    List.of(Particle.SOUL, Particle.ASH), 20, 0.5);
        }
        // Drain: the owner is healed for part of the damage dealt.
        if (drained > 0.0) {
            double max = p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null ? 20.0
                    : p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            p.setHealth(Math.min(max, p.getHealth() + drained));
            AbilityFx.ring(p.getLocation(), 1.2, List.of(Particle.HEART), 14);
        }
    }

    // ==================================================================
    // 11. RAVAGER - Berserker Rage: forward charge + shield break
    // ==================================================================
    private void berserkerRage(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        Vector facing = p.getEyeLocation().getDirection().setY(0);
        if (facing.lengthSquared() < 0.001) {
            facing = new Vector(0, 0, 1);
        }
        final Vector charge = facing.normalize();
        int ticks = (int) Math.max(10, a.getDurationSeconds() * 20.0);
        Set<LivingEntity> struck = new HashSet<>();
        scheduleSteps(ticks, 1L, i -> {
            if (!p.isOnline()) {
                return;
            }
            p.setVelocity(charge.clone().multiply(0.55).setY(0.08));
            p.setFallDistance(0f);
            for (Particle particle : a.getParticles()) {
                ParticleEngine.burst(p.getWorld(), particle, p.getLocation(), 4, 0.3);
            }
            AbilityFx.crack(p.getLocation(), Material.DIRT, 6, 0.6);
            for (LivingEntity victim : radiusVictims(p, p.getLocation(), 2.2)) {
                if (struck.add(victim)) {
                    hit(victim, a, p);
                    knock(victim, p.getLocation(), a.getKnockback(), true);
                    // Shield disable.
                    if (victim instanceof Player target) {
                        target.setCooldown(Material.SHIELD,
                                plugin.config().getInt("tokens.ravager.shield-disable-ticks", 100));
                        plugin.messages().send(target, "messages.shield-disabled",
                                "&cYour shield was shattered by a Ravager charge!");
                    }
                    AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                            List.of(Particle.CRIT, Particle.CLOUD), 20, 0.6);
                    SoundEngine.world(victim.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.2f, 1.0f);
                }
            }
        });
        later(ticks + 1L, () -> {
            SoundEngine.world(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.4f, 0.9f);
            AbilityFx.ring(p.getLocation(), a.getRadius(), List.of(Particle.CLOUD), 30);
            AbilityFx.crack(p.getLocation(), Material.DIRT, 20, 1.0);
        });
    }

    // ==================================================================
    // 12. WOLF - Pack Leader: summon three ghostly wolves
    // ==================================================================
    private void packLeader(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        SoundEngine.world(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.2f, 1.0f);
        int count = Math.max(1, a.getCount());
        int lifeTicks = (int) Math.max(100.0, a.getDurationSeconds() * 20.0);
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            Location spawn = com.tokensmp.util.LocationUtil.onCircle(p.getLocation(), 1.8, angle, 0);
            Wolf wolf = p.getWorld().spawn(spawn, Wolf.class, w -> {
                w.setOwner(p);
                w.setTamed(true);
                w.setAdult();
                w.setInvisible(true);
                w.setGlowing(true);
                w.setCustomName("\u00a77Spectral Wolf");
                w.setCustomNameVisible(false);
                w.setPersistent(false);
            });
            AbilityFx.impact(spawn, List.of(Particle.SOUL, Particle.HEART), 20, 0.6);
            later(lifeTicks, () -> {
                if (wolf.isValid()) {
                    AbilityFx.impact(wolf.getLocation().add(0, 0.5, 0),
                            List.of(Particle.SOUL, Particle.SMOKE), 24, 0.6);
                    SoundEngine.world(wolf.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
                    wolf.remove();
                }
            });
        }
    }

    // ==================================================================
    // 13. PIG - Hog Heal: saturation burst + burrow
    // ==================================================================
    private void hogHeal(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        // Saturation burst.
        potion(p, "SATURATION", "SATURATION", (int) Math.max(40.0, a.getDurationSeconds() * 20.0), 1);
        AbilityFx.ring(p.getLocation(), 1.4, List.of(Particle.HAPPY_VILLAGER, Particle.COMPOSTER), 24);
        SoundEngine.play(p, Sound.ENTITY_PIG_AMBIENT, 1.0f, 1.0f);
        // Underground burrow: 5x5x5 soft-block excavation.
        int side = Math.max(1, a.getCount());
        int restore = plugin.config().getInt("tokens.pig.burrow-restore-ticks", 200);
        Location base = p.getLocation().clone().subtract(0, 1, 0);
        List<Material> soft = List.of(Material.DIRT, Material.GRASS_BLOCK, Material.SAND,
                Material.GRAVEL, Material.CLAY, Material.SOUL_SAND, Material.SNOW_BLOCK);
        int half = side / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dy = 0; dy < side; dy++) {
                for (int dz = -half; dz <= half; dz++) {
                    Block block = base.clone().add(dx, dy, dz).getBlock();
                    if (soft.contains(block.getType())) {
                        placeTemp(block.getLocation(), Material.AIR, restore);
                        ParticleEngine.burst(p.getWorld(), Particle.BLOCK,
                                block.getLocation().add(0.5, 0.5, 0.5), 6, 0.3,
                                block.getType().createBlockData());
                    }
                }
            }
        }
        AbilityFx.crack(base, Material.DIRT, 40, side / 2.0);
        SoundEngine.world(base, Sound.BLOCK_GRAVEL_BREAK, 1.2f, 0.9f);
    }

    // ==================================================================
    // 14. CHICKEN - Feather Fall: sky launch + glide + wind landing
    // ==================================================================
    private void featherFall(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        SoundEngine.play(p, Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.4f);
        p.setVelocity(new Vector(0, Math.max(0.8, a.getKnockback()), 0));
        potion(p, "SLOW_FALLING", "SLOW_FALLING",
                (int) Math.max(40.0, a.getDurationSeconds() * 20.0), 0);
        AbilityFx.impact(p.getLocation(), List.of(Particle.CLOUD, Particle.SNOWFLAKE), 30, 0.8);
        int watchTicks = (int) Math.max(60.0, a.getDurationSeconds() * 20.0);
        final boolean[] landed = {false};
        scheduleSteps(watchTicks, 2L, i -> {
            if (!p.isOnline() || landed[0]) {
                return;
            }
            ParticleEngine.burst(p.getWorld(), Particle.CLOUD, p.getLocation(), 3, 0.3);
            ParticleEngine.burst(p.getWorld(), Particle.SNOWFLAKE, p.getLocation(), 2, 0.3);
            if (p.isOnGround() && i > 4) {
                landed[0] = true;
                Location at = p.getLocation();
                SoundEngine.world(at, Sound.ENTITY_PHANTOM_FLAP, 1.2f, 0.8f);
                AbilityFx.ring(at, a.getRadius(), List.of(Particle.CLOUD, Particle.SNOWFLAKE), 32);
                AbilityFx.impact(at, List.of(Particle.CLOUD), 40, 1.2);
                for (LivingEntity victim : radiusVictims(p, at, a.getRadius())) {
                    hit(victim, a, p);
                    knock(victim, at, a.getKnockback(), true);
                }
            }
        });
    }

    // ==================================================================
    // 15. SNOW GOLEM - Frozen Heart: rapid freeze snowballs
    // ==================================================================
    private void frozenHeart(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        int shots = Math.max(2, a.getCount());
        double perHit = a.getDamage() / shots;
        for (int i = 0; i < shots; i++) {
            final int index = i;
            later(index * 5L, () -> {
                if (!p.isOnline()) {
                    return;
                }
                Vector dir = p.getEyeLocation().getDirection().normalize();
                SoundEngine.world(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0f, 1.2f);
                travel(p, a, p.getEyeLocation(), dir, a.getRange(), 1, (victim, at) -> {
                    hitFor(victim, p, perHit, a.isTrueDamage());
                    // Powder-snow freeze: heavy slow + no jumping.
                    potion(victim, "SLOWNESS", "SLOW", (int) (a.getDurationSeconds() * 20.0), 4);
                    potion(victim, "JUMP_BOOST", "JUMP", (int) (a.getDurationSeconds() * 20.0), 128);
                    AbilityFx.impact(at, List.of(Particle.SNOWFLAKE, Particle.ITEM_SNOWBALL), 26, 0.6);
                    AbilityFx.ring(at, 1.4, List.of(Particle.SNOWFLAKE), 20);
                    SoundEngine.world(at, Sound.BLOCK_POWDER_SNOW_STEP, 1.2f, 0.9f);
                });
            });
        }
    }

    // ==================================================================
    // 16. BEE - Buzz Rush: stinger dive + poison + bounce back
    // ==================================================================
    private void buzzRush(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location at = aimPoint(p, a.getRange());
        LivingEntity target = nearestEnemy(p, at, a.getRadius() * 2.0);
        Location hitAt = target == null ? at : target.getLocation();
        Location start = p.getLocation().clone();
        Vector toTarget = hitAt.toVector().subtract(start.toVector());
        if (toTarget.lengthSquared() < 0.01) {
            toTarget = new Vector(0, 0, 1);
        }
        final Vector dir = toTarget.normalize();
        int steps = 10;
        scheduleSteps(steps, 1L, i -> {
            if (!p.isOnline()) {
                return;
            }
            p.setVelocity(dir.clone().multiply(0.9).setY(0.15));
            p.setFallDistance(0f);
            SoundEngine.play(p, Sound.ENTITY_BEE_LOOP, 0.8f, 1.4f);
            ParticleEngine.burst(p.getWorld(), Particle.CRIT, p.getLocation(), 4, 0.3);
            ParticleEngine.burst(p.getWorld(), Particle.WAX_ON, p.getLocation(), 3, 0.3);
        });
        later(steps + 1L, () -> {
            Location impact = p.getLocation();
            SoundEngine.world(impact, Sound.ENTITY_BEE_STING, 1.2f, 1.0f);
            AbilityFx.impact(impact, List.of(Particle.CRIT, Particle.WAX_ON), 30, 1.0);
            for (LivingEntity victim : radiusVictims(p, impact, a.getRadius())) {
                hit(victim, a, p);
                potion(victim, "POISON", "POISON", (int) (a.getDurationSeconds() * 20.0), 1);
                AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                        List.of(Particle.WAX_ON, Particle.CRIT), 20, 0.5);
            }
            // Safe bounce back.
            p.setVelocity(dir.clone().multiply(-1.1).setY(0.5));
            potion(p, "SLOW_FALLING", "SLOW_FALLING", 60, 0);
        });
    }

    // ==================================================================
    // 17. HORSE - Stallion Sprint: high jump + landing shockwave
    // ==================================================================
    private void stallionSprint(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        SoundEngine.play(p, Sound.ENTITY_HORSE_JUMP, 1.2f, 1.0f);
        Vector dir = p.getEyeLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 0.001) {
            dir = new Vector(0, 0, 1);
        }
        p.setVelocity(dir.normalize().multiply(Math.max(0.6, a.getSpeed()))
                .setY(Math.max(1.0, a.getKnockback())));
        final boolean[] landed = {false};
        scheduleSteps(80, 2L, i -> {
            if (!p.isOnline() || landed[0]) {
                return;
            }
            ParticleEngine.burst(p.getWorld(), Particle.CLOUD, p.getLocation(), 4, 0.3);
            AbilityFx.dust(p.getLocation(), AbilityFx.WHITE, 3, 0.3);
            if (p.isOnGround() && i > 5) {
                landed[0] = true;
                Location at = p.getLocation();
                SoundEngine.world(at, Sound.ENTITY_HORSE_GALLOP, 1.4f, 0.9f);
                AbilityFx.ring(at, a.getRadius(), List.of(Particle.CLOUD, Particle.CRIT), 34);
                AbilityFx.crack(at, Material.DIRT, 30, a.getRadius() / 2.0);
                for (LivingEntity victim : radiusVictims(p, at, a.getRadius())) {
                    hit(victim, a, p);
                    knock(victim, at, a.getKnockback(), true);
                }
            }
        });
    }

    // ==================================================================
    // 18. BAT - Echolocation: sonar pulse outlines everything
    // ==================================================================
    private void echolocation(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location at = p.getLocation();
        SoundEngine.play(p, Sound.ENTITY_BAT_AMBIENT, 1.4f, 1.6f);
        int waves = 6;
        scheduleSteps(waves, 3L, i -> AbilityFx.ring(at.clone().add(0, 1, 0),
                a.getRange() * (i + 1) / waves, List.of(Particle.SCULK_SOUL, Particle.SQUID_INK), 48));
        int glowTicks = (int) Math.max(40.0, a.getDurationSeconds() * 20.0);
        int outlined = 0;
        for (Entity entity : at.getWorld().getNearbyEntities(at, a.getRange(), a.getRange(), a.getRange())) {
            if (entity instanceof LivingEntity living && !entity.equals(p)) {
                living.setGlowing(true);
                outlined++;
                later(glowTicks, () -> {
                    if (living.isValid()) {
                        living.setGlowing(false);
                    }
                });
            }
        }
        plugin.messages().send(p, "messages.bat-sonar",
                "&b&lSONAR &7Outlined &f{count} &7entities within {range} blocks.",
                "{count}", String.valueOf(outlined),
                "{range}", String.valueOf((int) a.getRange()));
    }

    // ==================================================================
    // 19. SQUID - Ink Cloud: blinding ink blast
    // ==================================================================
    private void inkCloud(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 2L);
        Location at = p.getLocation();
        SoundEngine.world(at, Sound.ENTITY_SQUID_SQUIRT, 1.4f, 0.8f);
        AbilityFx.impact(at, List.of(Particle.SQUID_INK, Particle.CLOUD, Particle.SMOKE), 80, 2.0);
        for (int ring = 1; ring <= 3; ring++) {
            AbilityFx.ring(at, a.getRadius() * ring / 3.0,
                    List.of(Particle.SQUID_INK, Particle.BUBBLE), 40);
        }
        int ticks = (int) Math.max(40.0, a.getDurationSeconds() * 20.0);
        for (LivingEntity victim : radiusVictims(p, at, a.getRadius())) {
            potion(victim, "BLINDNESS", "BLINDNESS", ticks, 0);
            potion(victim, "SLOWNESS", "SLOW", ticks, 2);
            hit(victim, a, p);
            AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                    List.of(Particle.SQUID_INK), 24, 0.6);
        }
    }

    // ==================================================================
    // 20. WITCH - Brew Master: support splash + debuff on attackers
    // ==================================================================
    private void brewMaster(Player p, TokenTier.AbilitySpec a) {
        castFx(p, a);
        soundSequence(p, a.getSounds(), 3L);
        SoundEngine.play(p, Sound.ENTITY_WITCH_AMBIENT, 1.2f, 1.0f);
        // Beneficial tier-II splash on the caster.
        potion(p, "REGENERATION", null, (int) Math.max(100.0, a.getDurationSeconds() * 20.0), 1);
        potion(p, "RESISTANCE", "DAMAGE_RESISTANCE",
                (int) Math.max(100.0, a.getDurationSeconds() * 20.0), 1);
        potion(p, "SPEED", null, (int) Math.max(100.0, a.getDurationSeconds() * 20.0), 1);
        AbilityFx.impact(p.getLocation().add(0, 1, 0),
                List.of(Particle.WITCH, Particle.ENCHANT, Particle.HEART), 40, 1.0);
        AbilityFx.ring(p.getLocation(), 1.4, List.of(Particle.WITCH), 26);
        // Random debuffs on everyone nearby.
        int ticks = (int) Math.max(60.0, a.getDurationSeconds() * 20.0);
        for (LivingEntity victim : radiusVictims(p, p.getLocation(), a.getRange())) {
            hit(victim, a, p);
            switch ((int) (Math.random() * 3)) {
                case 0 -> potion(victim, "POISON", "POISON", ticks, 0);
                case 1 -> potion(victim, "WEAKNESS", "WEAKNESS", ticks, 1);
                default -> potion(victim, "SLOWNESS", "SLOW", ticks, 1);
            }
            AbilityFx.impact(victim.getLocation().add(0, 1, 0),
                    List.of(Particle.WITCH), 20, 0.5);
        }
        SoundEngine.world(p.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);
    }

    // ==================================================================
    // ADMIN (isolated)
    // ==================================================================
    private void netherShockwave(Player p, TokenTier.AbilitySpec a) {
        Location at = p.getLocation();
        SoundEngine.world(at, Sound.ENTITY_WITHER_SHOOT, 1.4f, 0.8f);
        AbilityFx.impact(at, List.of(Particle.SOUL, Particle.SMOKE), 50, 1.4);
        AbilityFx.ring(at, a.getRadius(), List.of(Particle.SOUL), 40);
        PhysicalDamageEngine.areaDamage(p, at, a.getRadius(), a.getDamage(),
                a.getKnockback(), a.isTrueDamage());
    }

    private void chronoFreeze(Player p, TokenTier.AbilitySpec a) {
        SoundEngine.play(p, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.6f);
        for (LivingEntity victim : radiusVictims(p, p.getLocation(), a.getRadius())) {
            if (victim instanceof Player target) {
                freezeManager.freeze(target, a.getDurationSeconds());
            } else {
                applyStun(victim, (int) (a.getDurationSeconds() * 20.0));
            }
        }
        AbilityFx.ring(p.getLocation(), a.getRadius(), List.of(Particle.SNOWFLAKE), 40);
    }

    private void serverJudgment(Player p, TokenTier.AbilitySpec a) {
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        SoundEngine.world(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.6f, 0.9f);
        AbilityFx.line(eye, dir, a.getRange(), List.of(Particle.SONIC_BOOM, Particle.GUST));
        for (LivingEntity victim : lineVictims(p, eye, dir, a.getRange(), 1.6)) {
            hit(victim, a, p);
            knock(victim, p.getLocation(), a.getKnockback(), true);
        }
    }

    // ==================================================================
    // Shared helpers
    // ==================================================================

    /** Cast flourish at the caster. */
    private void castFx(Player p, TokenTier.AbilitySpec a) {
        AbilityFx.cast(p, a.getParticles(), a.getParticleCount());
    }

    /** Plays the ability's sound list in a short staggered sequence. */
    private void soundSequence(Player p, List<Sound> sounds, long spacing) {
        for (int i = 0; i < sounds.size(); i++) {
            final Sound sound = sounds.get(i);
            later(i * spacing, () -> {
                if (p.isOnline()) {
                    p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
                }
            });
        }
    }

    /** Runs a step function N times on a fixed period (registered + self-ending). */
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

    /** Runs a task once after a delay (registered for shutdown cleanup). */
    private void later(long ticks, Runnable task) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };
        runnable.runTaskLater(plugin, Math.max(0L, ticks));
        scheduler.register(runnable);
    }

    /** Applies a potion effect (version-safe names). */
    private void potion(LivingEntity victim, String modern, String legacy, int ticks, int amplifier) {
        PotionEffectType type = VersionCompatibility.potionType(modern, legacy);
        if (type != null && ticks > 0) {
            victim.addPotionEffect(new PotionEffect(type, ticks, Math.max(0, amplifier), false, true));
        }
    }

    /** Roots a target: players are frozen, mobs lose their AI briefly. */
    private void applyStun(LivingEntity victim, int ticks) {
        victim.setVelocity(new Vector(0, 0, 0));
        if (victim instanceof Player target) {
            freezeManager.freeze(target, ticks / 20.0);
            return;
        }
        if (victim instanceof Mob mob) {
            mob.setAI(false);
            later(Math.max(1, ticks), () -> {
                if (mob.isValid()) {
                    mob.setAI(true);
                }
            });
        }
    }

    /** Living enemies around a point (never the caster). */
    private List<LivingEntity> radiusVictims(Player caster, Location center, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !entity.equals(caster) && !living.isDead()) {
                out.add(living);
            }
        }
        return out;
    }

    /** Living enemies along a line (penetrating attacks). */
    private List<LivingEntity> lineVictims(Player caster, Location start, Vector dir,
                                           double length, double hitRadius) {
        List<LivingEntity> out = new ArrayList<>();
        Vector step = dir.clone().normalize().multiply(0.6);
        Location point = start.clone();
        double travelled = 0.0;
        while (travelled < length) {
            point.add(step);
            travelled += 0.6;
            for (Entity entity : point.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                if (entity instanceof LivingEntity living && !entity.equals(caster)
                        && !living.isDead() && !out.contains(living)) {
                    out.add(living);
                }
            }
        }
        return out;
    }

    /** The point the player is looking at (up to a max range). */
    private Location aimPoint(Player caster, double range) {
        Location eye = caster.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Location point = eye.clone();
        Vector step = dir.multiply(0.4);
        double travelled = 0.0;
        while (travelled < range) {
            point.add(step);
            travelled += 0.4;
            if (point.getBlock().getType().isSolid()) {
                return point.clone().subtract(step);
            }
        }
        return point;
    }

    private void hit(LivingEntity target, TokenTier.AbilitySpec a, Player caster) {
        hitFor(target, caster, a.getDamage(), a.isTrueDamage());
    }

    private void hitFor(LivingEntity target, Player caster, double damage, boolean trueDamage) {
        if (damage <= 0.0) {
            return;
        }
        if (trueDamage) {
            PhysicalDamageEngine.dealTrueDamage(target, damage, caster);
        } else {
            PhysicalDamageEngine.dealDamage(target, damage, caster);
        }
    }

    private void knock(LivingEntity target, Location from, double strength, boolean upward) {
        Vector away = target.getLocation().toVector().subtract(from.toVector());
        if (away.lengthSquared() < 0.001) {
            away = new Vector(0, 1, 0);
        }
        Vector push = away.normalize().multiply(Math.max(0.1, strength));
        push.setY(upward ? Math.max(0.4, strength * 0.4) : Math.max(0.2, push.getY()));
        target.setVelocity(push);
    }

    /** The nearest hostile target (monsters + survival players). */
    private LivingEntity nearestEnemy(Player owner, Location from, double radius) {
        LivingEntity best = null;
        double bestDistance = radius * radius;
        for (Entity entity : from.getWorld().getNearbyEntities(from, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(owner) || living.isDead()) {
                continue;
            }
            boolean hostile = living instanceof Monster
                    || (living instanceof Player other
                    && !other.equals(owner)
                    && other.getGameMode() == org.bukkit.GameMode.SURVIVAL);
            if (!hostile) {
                continue;
            }
            double distance = living.getLocation().distanceSquared(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = living;
            }
        }
        return best;
    }

    /** Fires a particle projectile that damages the first target it meets. */
    private void travel(Player caster, TokenTier.AbilitySpec a, Location start, Vector dir,
                        double distance, int pierce, Impact impact) {
        Location pos = start.clone();
        double speed = Math.max(0.4, 0.75 * a.getSpeed());
        Vector step = dir.clone().normalize().multiply(speed);
        int maxSteps = (int) Math.ceil(distance / speed);
        final int[] index = {0};
        final int[] left = {Math.max(1, pierce)};
        final List<LivingEntity> hitList = new ArrayList<>();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (index[0]++ >= maxSteps) {
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
                    if (entity instanceof LivingEntity living && !entity.equals(caster)
                            && !hitList.contains(living)) {
                        hitList.add(living);
                        impact.apply(living, living.getLocation());
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

    @FunctionalInterface
    private interface Impact {
        void apply(LivingEntity victim, Location at);
    }

    /** Places a temporary block that is restored automatically after a delay. */
    private void placeTemp(Location at, Material material, int restoreTicks) {
        Block block = at.getBlock();
        Material original = block.getType();
        if (material == original) {
            return;
        }
        if (original == Material.BEDROCK || original == Material.BARRIER) {
            return;
        }
        BlockState state = block.getState();
        block.setType(material, false);
        later(Math.max(20L, restoreTicks), () -> state.update(true, false));
    }
}
