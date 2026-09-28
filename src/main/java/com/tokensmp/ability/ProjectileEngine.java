package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleManager;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.core.VersionCompatibility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The ability projectile engine: launches and drives every REAL projectile
 * used by token abilities (arrow barrages, web harpoons, wither skulls,
 * fireballs, magma meteors, splash potion volleys, the returning gilded
 * axe) and resolves their impacts into genuine server-side damage through
 * the PhysicalDamageEngine. Also owns the cosmetic arrow-trail passive.
 */
public final class ProjectileEngine implements Listener {

    private static final int MAX_CONCURRENT_TRAILS = 48;

    private final TokenSMP plugin;
    private final SchedulerManager scheduler;
    private final Map<UUID, Integer> activeTrails = new HashMap<>();

    public ProjectileEngine(TokenSMP plugin, SchedulerManager scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    // ------------------------------------------------------------------
    // Arrow trail passive (Skeleton T3)
    // ------------------------------------------------------------------

    /** Attaches a particle trail to an arrow if the shooter has the passive. */
    public void attachTrail(Arrow arrow, Player shooter, boolean enabled) {
        if (!enabled || shooter == null) {
            return;
        }
        UUID id = shooter.getUniqueId();
        int current = activeTrails.getOrDefault(id, 0);
        if (current >= MAX_CONCURRENT_TRAILS) {
            return;
        }
        activeTrails.put(id, current + 1);

        BukkitRunnable trail = new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isDead() || arrow.isOnGround()) {
                    activeTrails.merge(id, -1, Integer::sum);
                    if (activeTrails.getOrDefault(id, 0) <= 0) {
                        activeTrails.remove(id);
                    }
                    cancel();
                    return;
                }
                ParticleManager.spawn(shooter.getWorld(), Particle.CRIT, arrow.getLocation(),
                        4, 0.05, 0.05, 0.05, 0.01);
                ParticleManager.spawn(shooter.getWorld(), Particle.SNOWFLAKE, arrow.getLocation(),
                        2, 0.05, 0.05, 0.05, 0.0);
            }
        };
        trail.runTaskTimer(plugin, 1L, 1L);
        scheduler.register(trail);
    }

    /** Cleans the counter when a trailing player disconnects. */
    public void clear(Player player) {
        activeTrails.remove(player.getUniqueId());
    }

    // ------------------------------------------------------------------
    // Real ability projectiles
    // ------------------------------------------------------------------

    /** Skeleton T3 - one barrage arrow with an individual trajectory. */
    public void launchBarrageArrow(Player shooter, Vector direction, double damage) {
        Arrow arrow = shooter.getWorld().spawn(shooter.getEyeLocation(), Arrow.class, entity -> {
            entity.setShooter(shooter);
            entity.setVelocity(direction);
            entity.setDamage(damage);
            entity.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            entity.setCritical(true);
            entity.setMetadata("tokensmp_barrage", new FixedMetadataValue(plugin, true));
        });
        attachSimpleTrail(arrow, Particle.CRIT);
        SoundManager.play(shooter, Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.5f);
    }

    /** Spider T3 - the physical web harpoon projectile. */
    public void launchWeb(Player shooter, double damage, int slowSeconds) {
        Vector direction = shooter.getEyeLocation().getDirection().normalize();
        Snowball web = shooter.launchProjectile(Snowball.class, direction.multiply(1.8));
        web.setMetadata("tokensmp_web", new FixedMetadataValue(plugin, damage));
        web.setMetadata("tokensmp_web_slow", new FixedMetadataValue(plugin, slowSeconds));
        attachSimpleTrail(web, Particle.ITEM_COBWEB);
        SoundManager.play(shooter, Sound.ENTITY_SPIDER_AMBIENT, 1.0f, 1.6f);
    }

    /** Wither T3 - one wither skull on its own trajectory. */
    public void launchWitherSkull(Player shooter, Vector direction) {
        WitherSkull skull = shooter.launchProjectile(WitherSkull.class, direction);
        skull.setCharged(true);
        skull.setMetadata("tokensmp_skull", new FixedMetadataValue(plugin, true));
        attachSimpleTrail(skull, Particle.SMOKE);
        SoundManager.play(shooter, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);
    }

    /** Ghast T3 - the large explosive fireball (no terrain damage). */
    public void launchFireball(Player shooter, double damage, double impactRadius, int burnSeconds) {
        Vector direction = shooter.getEyeLocation().getDirection().normalize();
        Fireball fireball = shooter.launchProjectile(Fireball.class, direction.multiply(1.1));
        fireball.setYield(0f); // never destroys terrain
        fireball.setIsIncendiary(false);
        fireball.setMetadata("tokensmp_fireball", new FixedMetadataValue(plugin, damage));
        fireball.setMetadata("tokensmp_fireball_radius", new FixedMetadataValue(plugin, impactRadius));
        fireball.setMetadata("tokensmp_fireball_burn", new FixedMetadataValue(plugin, burnSeconds));
        attachSimpleTrail(fireball, Particle.FLAME);
        SoundManager.play(shooter, Sound.ENTITY_GHAST_SHOOT, 1.2f, 1.0f);
    }

    /** Magma Cube T3 - one small magma meteor raining onto the area. */
    public void launchMeteor(Player shooter, Location targetPoint, double damage, int burnSeconds) {
        Location start = targetPoint.clone().add(
                (Math.random() - 0.5) * 4, 12 + Math.random() * 3, (Math.random() - 0.5) * 4);
        SmallFireball meteor = shooter.getWorld().spawn(start, SmallFireball.class, entity -> {
            entity.setShooter(shooter);
            Vector toTarget = targetPoint.toVector().subtract(start.toVector()).normalize();
            entity.setVelocity(toTarget.multiply(0.9));
            entity.setIsIncendiary(false);
            entity.setYield(0f);
            entity.setMetadata("tokensmp_meteor", new FixedMetadataValue(plugin, damage));
            entity.setMetadata("tokensmp_meteor_burn", new FixedMetadataValue(plugin, burnSeconds));
        });
        attachSimpleTrail(meteor, Particle.LAVA);
    }

    /** Witch T3 - one real splash potion of the given effect. */
    public void launchHexPotion(Player shooter, Vector direction, PotionEffectType effect,
                                int durationTicks, int amplifier) {
        ThrownPotion potion = shooter.launchProjectile(ThrownPotion.class, direction);
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        if (item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta meta) {
            meta.addCustomEffect(new PotionEffect(effect, durationTicks, amplifier), true);
            item.setItemMeta(meta);
        }
        potion.setItem(item);
        potion.setMetadata("tokensmp_hex", new FixedMetadataValue(plugin, true));
        SoundManager.play(shooter, Sound.ENTITY_WITCH_THROW, 1.0f, 1.0f);
    }

    /**
     * Piglin T3 - the returning Gilded Axe: a projectile that flies out,
     * shreds everything near its path and physically boomerangs back.
     */
    public void throwGildedAxe(Player shooter, double damage, double range) {
        Vector direction = shooter.getEyeLocation().getDirection().normalize();
        Arrow axe = shooter.getWorld().spawn(shooter.getEyeLocation(), Arrow.class, entity -> {
            entity.setShooter(shooter);
            entity.setVelocity(direction.clone().multiply(1.4));
            entity.setDamage(0.0);
            entity.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            entity.setMetadata("tokensmp_axe", new FixedMetadataValue(plugin, true));
        });

        Set<UUID> alreadyHit = new HashSet<>();
        final int[] ticks = {0};
        BukkitRunnable flight = new BukkitRunnable() {
            @Override
            public void run() {
                ticks[0]++;
                if (!axe.isValid() || !shooter.isOnline() || ticks[0] > 80) {
                    if (axe.isValid()) {
                        axe.remove();
                    }
                    cancel();
                    return;
                }
                // Outward leg: damage everything near the flight path once.
                if (ticks[0] <= 10) {
                    for (Entity entity : axe.getNearbyEntities(1.2, 1.2, 1.2)) {
                        if (AbilityTargeting.validTarget(shooter, entity)
                                && alreadyHit.add(entity.getUniqueId())) {
                            PhysicalDamageEngine.dealDamage((org.bukkit.entity.LivingEntity) entity,
                                    damage, shooter);
                            ParticleManager.burst(shooter.getWorld(), Particle.CRIT,
                                    entity.getLocation().add(0, 1, 0), 12, 0.3);
                            SoundManager.play(shooter, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
                        }
                    }
                    return;
                }
                // Return leg: steer back to the thrower.
                Vector toShooter = shooter.getEyeLocation().toVector()
                        .subtract(axe.getLocation().toVector());
                if (toShooter.lengthSquared() < 2.5 || ticks[0] > 70) {
                    axe.remove();
                    cancel();
                    return;
                }
                axe.setVelocity(toShooter.normalize().multiply(0.9));
                ParticleManager.point(shooter.getWorld(), Particle.ENCHANTED_HIT, axe.getLocation());
            }
        };
        flight.runTaskTimer(plugin, 1L, 1L);
        scheduler.register(flight);
        attachSimpleTrail(axe, Particle.WAX_ON);
        SoundManager.play(shooter, Sound.ITEM_TRIDENT_THROW, 1.0f, 1.4f);
    }

    /** Small particle trail that follows any projectile until it dies. */
    private void attachSimpleTrail(Projectile projectile, Particle particle) {
        BukkitRunnable trail = new BukkitRunnable() {
            @Override
            public void run() {
                if (!projectile.isValid() || projectile.isDead() || projectile.isOnGround()) {
                    cancel();
                    return;
                }
                ParticleManager.point(projectile.getWorld(), particle, projectile.getLocation());
            }
        };
        trail.runTaskTimer(plugin, 1L, 1L);
        scheduler.register(trail);
    }

    // ------------------------------------------------------------------
    // Impact resolution (server-authoritative damage on hit)
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }

        if (projectile.hasMetadata("tokensmp_web")) {
            resolveWeb(shooter, projectile, event);
        } else if (projectile.hasMetadata("tokensmp_fireball")) {
            resolveFireball(shooter, projectile);
        } else if (projectile.hasMetadata("tokensmp_meteor")) {
            resolveMeteor(shooter, projectile);
        }
    }

    /** Web Harpoon impact: pull the victim to the impact point + damage. */
    private void resolveWeb(Player shooter, Projectile web, ProjectileHitEvent event) {
        Location impact = web.getLocation();
        ParticleManager.burst(shooter.getWorld(), Particle.ITEM_COBWEB, impact, 20, 0.5);
        SoundManager.world(impact, Sound.BLOCK_WOOL_BREAK, 1.0f, 0.7f);

        Entity hit = event.getHitEntity();
        java.util.List<org.bukkit.entity.LivingEntity> victims = new java.util.ArrayList<>();
        if (hit != null && AbilityTargeting.validTarget(shooter, hit)) {
            victims.add((org.bukkit.entity.LivingEntity) hit);
        }
        double damage = metadata(web, "tokensmp_web", 6.0);
        int slowSeconds = (int) metadata(web, "tokensmp_web_slow", 3);
        PotionEffectType slowness = VersionCompatibility.potionType("SLOWNESS", "SLOW");

        for (org.bukkit.entity.LivingEntity victim : victims) {
            PhysicalDamageEngine.dealDamage(victim, damage, shooter);
            // Pull the victim toward the impact point.
            Vector pull = impact.toVector().subtract(victim.getLocation().toVector());
            if (pull.lengthSquared() > 0.05) {
                pull.normalize().multiply(1.3).setY(0.35);
                victim.setVelocity(pull);
            }
            if (slowness != null) {
                victim.addPotionEffect(new PotionEffect(slowness, slowSeconds * 20, 2));
            }
        }
    }

    /** Ghast fireball impact: real AoE damage + burn, no terrain damage. */
    private void resolveFireball(Player shooter, Projectile fireball) {
        Location impact = fireball.getLocation();
        double damage = metadata(fireball, "tokensmp_fireball", 15.0);
        double radius = metadata(fireball, "tokensmp_fireball_radius", 4.0);
        int burn = (int) metadata(fireball, "tokensmp_fireball_burn", 5);

        SoundManager.world(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.0f);
        ParticleManager.burst(shooter.getWorld(), Particle.EXPLOSION, impact, 3, 0.5);
        ParticleManager.burst(shooter.getWorld(), Particle.FLAME, impact, 40, 1.0);
        PhysicalDamageEngine.areaDamage(shooter, impact, radius, damage, 1.0, false);
        for (org.bukkit.entity.LivingEntity victim
                : AbilityTargeting.areaTargets(shooter, impact, radius)) {
            victim.setFireTicks(Math.max(victim.getFireTicks(), burn * 20));
        }
    }

    /** Magma meteor impact: single-target damage + burn. */
    private void resolveMeteor(Player shooter, Projectile meteor) {
        Location impact = meteor.getLocation();
        double damage = metadata(meteor, "tokensmp_meteor", 6.0);
        int burn = (int) metadata(meteor, "tokensmp_meteor_burn", 3);

        SoundManager.world(impact, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.6f);
        ParticleManager.burst(shooter.getWorld(), Particle.LAVA, impact, 8, 0.4);
        ParticleManager.burst(shooter.getWorld(), Particle.FLAME, impact, 12, 0.4);
        for (org.bukkit.entity.LivingEntity victim
                : AbilityTargeting.areaTargets(shooter, impact, 1.6)) {
            PhysicalDamageEngine.dealDamage(victim, damage, shooter);
            victim.setFireTicks(Math.max(victim.getFireTicks(), burn * 20));
        }
    }

    private double metadata(Projectile projectile, String key, double def) {
        if (projectile.getMetadata(key).isEmpty()) {
            return def;
        }
        Object value = projectile.getMetadata(key).get(0).value();
        return value instanceof Number number ? number.doubleValue() : def;
    }

    /** Launch confirmation click used by abilities that spawn projectiles. */
    public static void shootSound(Player player) {
        SoundManager.play(player, Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.5f);
    }
}
