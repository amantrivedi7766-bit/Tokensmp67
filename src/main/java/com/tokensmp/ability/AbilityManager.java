package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.AbilityAnimation;
import com.tokensmp.animation.AnimationManager;
import com.tokensmp.animation.ParticleManager;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.VersionCompatibility;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import com.tokensmp.token.TokenTier;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The ability engine. Every activation runs through the full server-side
 * validation chain BEFORE anything happens:
 *
 * 1. claimed (active) token exists
 * 2. ownership + claim state
 * 3. valid token definition and tier
 * 4. the player is HOLDING the matching token item (PDC-verified)
 * 5. ability available at this tier
 * 6. not frozen
 * 7. cooldown (server-side timestamp)
 * 8. target validity (through AbilityTargeting)
 *
 * Every dispatched ability is a real physical combat ability - particles
 * and sounds only ever layer on top of genuine server-side damage.
 */
public final class AbilityManager {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final CooldownManager cooldowns;
    private final MessageManager messages;
    private final FreezeManager freezeManager;
    private final AnimationManager animations;

    public AbilityManager(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                          CooldownManager cooldowns, MessageManager messages,
                          FreezeManager freezeManager, AnimationManager animations) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.cooldowns = cooldowns;
        this.messages = messages;
        this.freezeManager = freezeManager;
        this.animations = animations;
    }

    // ------------------------------------------------------------------
    // Activation entry point (SHIFT + RIGHT CLICK while holding the token)
    // ------------------------------------------------------------------

    /** Attempts to activate the player's ACTIVE token ability. */
    public void activate(Player player) {
        // 1. Does the player have an active token at all?
        String activeId = data.getActiveToken(player);
        if (activeId == null) {
            messages.actionBar(player, plugin.config().getString("messages.no-active-token",
                    "&7No active token - claim one via &f/tokens&7!"));
            return;
        }
        // 2/3. Is the token valid and claimed?
        Token token = registry.get(activeId);
        if (token == null || !data.isClaimed(player, activeId)) {
            return;
        }
        // 4. Correct tier with an ability?
        int tier = data.getTier(player, activeId);
        TokenTier tierDef = token.tier(tier);
        if (tierDef == null || tierDef.getAbility() == null) {
            messages.actionBar(player, plugin.config().getString("messages.no-ability-at-tier",
                    "&7This tier has no active ability yet!"));
            return;
        }
        TokenTier.AbilitySpec ability = tierDef.getAbility();
        // 5. Is an ability available?
        if (ability.getType() == com.tokensmp.token.TokenAbility.NONE) {
            return;
        }
        // 6. HELD TOKEN VALIDATION: the ability only works while the player
        //    is actually holding their own PDC-verified token item.
        if (plugin.config().getBoolean("abilities.require-held-token", true)
                && !isHoldingTokenItem(player, activeId)) {
            messages.actionBar(player, plugin.config().getString("messages.ability-not-held",
                    "&eHold your {token} Token item to use its ability!")
                    .replace("{token}", token.getDisplayName()));
            SoundManager.denied(player);
            return;
        }
        // 7. Frozen players cannot use abilities.
        if (freezeManager.isFrozen(player)) {
            SoundManager.denied(player);
            return;
        }
        // 8. Cooldown check (server-side timestamp).
        long remaining = cooldowns.remainingMillis(player, activeId);
        if (remaining > 0L) {
            SoundManager.denied(player);
            messages.actionBar(player, plugin.config().getString("messages.ability-denied",
                            "&c&l[!]&c Ability on cooldown! Wait {seconds}s")
                    .replace("{seconds}", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }

        // Everything validated: start cooldown, run the ability, announce.
        cooldowns.start(player, activeId, ability.getCooldownSeconds());
        execute(player, token, tierDef, ability);
        SoundManager.play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        messages.actionBar(player, plugin.config().getString("messages.ability-activated",
                "&a&l[!] &2{ability} Activated!").replace("{ability}", ability.getName()));
        cooldowns.showHud(player, activeId, ability.getCooldownSeconds());
    }

    /** True when the main hand holds a genuine, owned instance of the token. */
    private boolean isHoldingTokenItem(Player player, String tokenId) {
        ItemStack held = player.getInventory().getItemInMainHand();
        return plugin.tokenItems().isValidHeldItem(player, held, tokenId);
    }

    // ------------------------------------------------------------------
    // Ability dispatch
    // ------------------------------------------------------------------

    private void execute(Player player, Token token, TokenTier tier, TokenTier.AbilitySpec ability) {
        switch (ability.getType()) {
            case UNDEAD_SHOCKWAVE -> undeadShockwave(player, ability);
            case ARROW_BARRAGE -> arrowBarrage(player, ability);
            case WEB_HARPOON -> webHarpoon(player, ability);
            case VOLATILE_SURGE -> volatileSurge(player, ability);
            case VOID_RIFT -> voidRift(player, ability);
            case INFERNO_LANCE -> infernoLance(player, ability);
            case SONIC_OBLITERATION -> sonicObliteration(player, ability);
            case WITHER_BARRAGE -> witherBarrage(player, ability);
            case BOUNCE_SLAM -> bounceSlam(player, ability);
            case PHANTOM_DIVE -> phantomDive(player, ability);
            case PRISMATIC_BEAM -> prismaticBeam(player, ability);
            case FANG_VOLLEY -> fangVolley(player, ability);
            case GILDED_AXE -> gildedAxe(player, ability);
            case FIREBALL_LAUNCH -> fireballLaunch(player, ability);
            case METEOR_SPLIT -> meteorSplit(player, ability);
            case HEX_BREW -> hexBrew(player, ability);
            case AXE_RAMPAGE -> axeRampage(player, ability);
            case RAVAGER_CHARGE -> ravagerCharge(player, ability);
            case CYCLONE_BURST -> cycloneBurst(player, ability);
            case CURSE_BEAM -> curseBeam(player, ability);
            case NETHER_SHOCKWAVE -> netherShockwave(player, ability);
            case CHRONO_FREEZE -> chronoFreeze(player, ability);
            case SERVER_JUDGMENT -> serverJudgment(player, ability);
            case NONE -> { /* nothing */ }
        }
    }

    // ------------------------------------------------------------------
    // The 20 player token abilities
    // ------------------------------------------------------------------

    /** Zombie T3 - "Undead Shockwave": AoE damage + knockback + 15s trail. */
    private void undeadShockwave(Player player, TokenTier.AbilitySpec ability) {
        SoundManager.world(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.2f, 0.7f);
        SoundManager.world(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.4f);
        AbilityAnimation.shockwave(player, ability.getRadius());
        ParticleManager.burst(player.getWorld(), Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 40, ability.getRadius() / 2);
        PhysicalDamageEngine.areaDamage(player, player.getLocation(), ability.getRadius(),
                ability.getDamage(), ability.getKnockback(), false);
        animations.enrageTrail(player, (int) ability.getDurationSeconds());
    }

    /** Skeleton T3 - "Phantom Arrow Barrage": real arrows, real trajectories. */
    private void arrowBarrage(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector forward = eye.getDirection().normalize();
        int arrows = (int) ability.getDurationSeconds();
        for (int i = 0; i < arrows; i++) {
            double spread = (i - arrows / 2.0) * 0.12;
            Vector direction = rotateAroundY(forward.clone(), spread);
            direction.setY(direction.getY() + 0.04 * (i % 3));
            plugin.projectileEngine().launchBarrageArrow(player, direction, ability.getDamage());
        }
    }

    /** Spider T3 - "Web Harpoon": real web projectile that pulls the victim in. */
    private void webHarpoon(Player player, TokenTier.AbilitySpec ability) {
        plugin.projectileEngine().launchWeb(player, ability.getDamage(), (int) ability.getDurationSeconds());
    }

    /** Creeper T3 - "Volatile Surge": charge, warning ring, detonation nova. */
    private void volatileSurge(Player player, TokenTier.AbilitySpec ability) {
        int chargeTicks = (int) ability.getDurationSeconds();
        SoundManager.play(player, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.5f);
        animations.chargeSparks(player, chargeTicks);
        ParticleManager.ring(player.getWorld(), Particle.SMOKE,
                player.getLocation(), ability.getRadius(), 32);
        BukkitRunnable detonate = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                SoundManager.world(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.1f);
                ParticleManager.burst(player.getWorld(), Particle.EXPLOSION, player.getLocation(), 3, 0.5);
                ParticleManager.burst(player.getWorld(), Particle.ELECTRIC_SPARK,
                        player.getLocation().add(0, 1, 0), 50, ability.getRadius() / 2);
                AbilityAnimation.shockwave(player, ability.getRadius());
                PhysicalDamageEngine.areaDamage(player, player.getLocation(), ability.getRadius(),
                        ability.getDamage(), ability.getKnockback(), false);
            }
        };
        detonate.runTaskLater(plugin, Math.max(5, chargeTicks));
        plugin.scheduler().register(detonate);
    }

    /** Enderman T3 - "Void Rift": teleport strike + secondary void pulse. */
    private void voidRift(Player player, TokenTier.AbilitySpec ability) {
        double range = ability.getDurationSeconds();
        LivingEntity target = AbilityTargeting.nearestTarget(player, range);
        SoundManager.world(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        animations.warpBurst(player.getLocation());

        Location landing;
        if (target != null) {
            // Appear directly behind the victim.
            Vector behind = target.getEyeLocation().getDirection().multiply(-1.5);
            landing = target.getLocation().add(behind);
            landing.setDirection(target.getEyeLocation().getDirection());
        } else {
            landing = com.tokensmp.util.LocationUtil.forward(player.getEyeLocation(), 6.0);
            landing.setY(player.getLocation().getY());
        }
        player.teleport(landing);
        animations.warpBurst(landing);
        ParticleManager.burst(player.getWorld(), Particle.PORTAL, landing, 60, 0.8);

        if (target != null) {
            PhysicalDamageEngine.dealTrueDamage(target, ability.getDamage(), player);
            // Secondary void pulse around the landing point.
            PhysicalDamageEngine.areaDamage(player, landing, ability.getRadius(),
                    ability.getDamage() * 0.6, 0.5, true);
        }
    }

    /** Blaze T3 - "Inferno Lance": piercing line of fire, damage + burn. */
    private void infernoLance(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double length = ability.getDurationSeconds();
        SoundManager.world(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.7f);

        for (double d = 0.5; d <= length; d += 0.5) {
            Location point = eye.clone().add(direction.clone().multiply(d));
            ParticleManager.point(player.getWorld(), Particle.FLAME, point);
            ParticleManager.point(player.getWorld(), Particle.LAVA, point);
        }
        int burnTicks = (int) ability.getRadius() * 20;
        for (LivingEntity victim : AbilityTargeting.rayTargets(player, eye, direction, length, 1.4)) {
            PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
            victim.setFireTicks(Math.max(victim.getFireTicks(), burnTicks));
        }
    }

    /** Warden T3 - "Sonic Obliteration": sonic cone, true damage + huge knockback. */
    private void sonicObliteration(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        SoundManager.world(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);
        animations.sonicRay(player, eye, direction, ability.getDurationSeconds());

        for (LivingEntity victim : AbilityTargeting.rayTargets(player, eye, direction,
                ability.getDurationSeconds(), 2.5)) {
            PhysicalDamageEngine.dealTrueDamage(victim, ability.getDamage(), player);
            Vector push = direction.clone().multiply(ability.getKnockback());
            push.setY(Math.max(push.getY(), 0.6));
            victim.setVelocity(push);
        }
    }

    /** Wither T3 - "Wither Barrage": three real skulls, three trajectories. */
    private void witherBarrage(Player player, TokenTier.AbilitySpec ability) {
        Vector forward = player.getEyeLocation().getDirection().normalize();
        int skulls = (int) ability.getDurationSeconds();
        for (int i = 0; i < skulls; i++) {
            Vector direction = rotateAroundY(forward.clone(), (i - 1) * 0.25);
            BukkitRunnable launch = new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.projectileEngine().launchWitherSkull(player, direction);
                    }
                }
            };
            launch.runTaskLater(plugin, i * 5L);
            plugin.scheduler().register(launch);
        }
    }

    /** Slime T3 - "Bounce Slam": leap, then an AoE slam that launches victims. */
    private void bounceSlam(Player player, TokenTier.AbilitySpec ability) {
        player.setVelocity(new Vector(0, 1.6, 0));
        SoundManager.play(player, Sound.ENTITY_SLIME_JUMP, 1.0f, 0.7f);
        final int[] waited = {0};
        BukkitRunnable slam = new BukkitRunnable() {
            @Override
            public void run() {
                waited[0] += 2;
                if (!player.isOnline() || waited[0] > 80) {
                    cancel();
                    return;
                }
                if (waited[0] < 10 || !player.isOnGround()) {
                    return; // still airborne
                }
                // SLAM!
                SoundManager.world(player.getLocation(), Sound.ENTITY_SLIME_ATTACK, 1.4f, 0.5f);
                SoundManager.world(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                ParticleManager.burst(player.getWorld(), Particle.SLIME,
                        player.getLocation(), 40, ability.getRadius() / 2);
                AbilityAnimation.shockwave(player, ability.getRadius());
                double launch = Math.max(0.8, ability.getDurationSeconds());
                for (LivingEntity victim : AbilityTargeting.areaTargets(player,
                        player.getLocation(), ability.getRadius())) {
                    PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
                    Vector away = victim.getLocation().toVector()
                            .subtract(player.getLocation().toVector());
                    if (away.lengthSquared() < 0.01) {
                        away = new Vector(0, 1, 0);
                    }
                    victim.setVelocity(away.normalize().multiply(0.4).setY(launch * 0.7));
                }
                cancel();
            }
        };
        slam.runTaskTimer(plugin, 2L, 2L);
        plugin.scheduler().register(slam);
    }

    /** Phantom T3 - "Phantom Dive": swooping dash damaging the flight path. */
    private void phantomDive(Player player, TokenTier.AbilitySpec ability) {
        Vector direction = player.getEyeLocation().getDirection().normalize();
        player.setVelocity(direction.clone().multiply(1.8).setY(direction.getY() * 1.2 + 0.35));
        SoundManager.play(player, Sound.ENTITY_PHANTOM_SWOOP, 1.2f, 1.0f);
        Set<UUID> hit = new HashSet<>();
        final int[] ticks = {0};
        BukkitRunnable flight = new BukkitRunnable() {
            @Override
            public void run() {
                ticks[0]++;
                if (!player.isOnline() || ticks[0] > 12) {
                    cancel();
                    return;
                }
                ParticleManager.burst(player.getWorld(), Particle.PHANTOM,
                        player.getLocation(), 6, 0.5);
                for (LivingEntity victim : AbilityTargeting.areaTargets(player,
                        player.getLocation(), 1.8)) {
                    if (hit.add(victim.getUniqueId())) {
                        PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
                        ParticleManager.burst(player.getWorld(), Particle.CRIT,
                                victim.getLocation().add(0, 1, 0), 10, 0.3);
                    }
                }
            }
        };
        flight.runTaskTimer(plugin, 1L, 1L);
        plugin.scheduler().register(flight);
    }

    /** Guardian T3 - "Prismatic Beam": target lock, charge, damage beam. */
    private void prismaticBeam(Player player, TokenTier.AbilitySpec ability) {
        LivingEntity target = AbilityTargeting.nearestTarget(player, ability.getRadius());
        if (target == null) {
            messages.actionBar(player, plugin.config().getString("messages.ability-no-target",
                    "&eNo target in range!"));
            return;
        }
        SoundManager.play(player, Sound.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.4f);
        // Charging animation: converging rings.
        for (int i = 3; i >= 1; i--) {
            final double r = i;
            BukkitRunnable ring = new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        ParticleManager.ring(player.getWorld(), Particle.ENCHANTED_HIT,
                                player.getLocation().add(0, 1, 0), r, 24);
                    }
                }
            };
            ring.runTaskLater(plugin, (4 - i) * 5L);
            plugin.scheduler().register(ring);
        }
        BukkitRunnable beam = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || target.isDead()) {
                    return;
                }
                SoundManager.play(player, Sound.ENTITY_GUARDIAN_ATTACK, 1.4f, 0.7f);
                AbilityAnimation.burstBetween(player.getEyeLocation(),
                        target.getLocation().add(0, 1, 0), Particle.END_ROD, 16);
                AbilityAnimation.burstBetween(player.getEyeLocation(),
                        target.getLocation().add(0, 1, 0), Particle.ENCHANTED_HIT, 10);
                PhysicalDamageEngine.dealTrueDamage(target, ability.getDamage(), player);
            }
        };
        beam.runTaskLater(plugin, 20L);
        plugin.scheduler().register(beam);
    }

    /** Evoker T3 - "Fang Volley": a REAL line of advancing evoker fangs. */
    private void fangVolley(Player player, TokenTier.AbilitySpec ability) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        int fangs = (int) ability.getDamage();
        SoundManager.play(player, Sound.ENTITY_EVOKER_CAST_SPELL, 1.2f, 1.0f);
        for (int i = 0; i < fangs; i++) {
            final Location point = player.getLocation().add(direction.clone().multiply(1.5 + i * 1.2));
            BukkitRunnable spawn = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    player.getWorld().spawn(point, EvokerFangs.class, fangs1 -> fangs1.setOwner(player));
                    SoundManager.world(point, Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.8f, 1.2f);
                }
            };
            spawn.runTaskLater(plugin, i * 3L);
            plugin.scheduler().register(spawn);
        }
    }

    /** Piglin T3 - "Gilded Axe": thrown, damaging, returning boomerang. */
    private void gildedAxe(Player player, TokenTier.AbilitySpec ability) {
        plugin.projectileEngine().throwGildedAxe(player, ability.getDamage(), ability.getDurationSeconds());
    }

    /** Ghast T3 - "Fireball Launch": real large fireball, manual impact damage. */
    private void fireballLaunch(Player player, TokenTier.AbilitySpec ability) {
        plugin.projectileEngine().launchFireball(player, ability.getDamage(),
                ability.getRadius(), (int) ability.getDurationSeconds());
    }

    /** Magma Cube T3 - "Meteor Split": magma meteors rain on the area ahead. */
    private void meteorSplit(Player player, TokenTier.AbilitySpec ability) {
        Location center = com.tokensmp.util.LocationUtil.forward(
                player.getLocation(), 6.0);
        SoundManager.world(player.getLocation(), Sound.ENTITY_MAGMA_CUBE_JUMP, 1.2f, 0.6f);
        int meteors = (int) ability.getDurationSeconds();
        for (int i = 0; i < meteors; i++) {
            final Location target = center.clone().add(
                    (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6);
            BukkitRunnable drop = new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.projectileEngine().launchMeteor(player, target,
                                ability.getDamage(), (int) ability.getRadius());
                    }
                }
            };
            drop.runTaskLater(plugin, i * 4L);
            plugin.scheduler().register(drop);
        }
    }

    /** Witch T3 - "Hex Brew": real splash potion volley at the target. */
    private void hexBrew(Player player, TokenTier.AbilitySpec ability) {
        LivingEntity target = AbilityTargeting.nearestTarget(player, 12);
        Vector aim = target != null
                ? target.getLocation().add(0, 1, 0).toVector()
                        .subtract(player.getEyeLocation().toVector()).normalize()
                : player.getEyeLocation().getDirection().normalize();
        PotionEffectType poison = VersionCompatibility.potionType("POISON", null);
        PotionEffectType slowness = VersionCompatibility.potionType("SLOWNESS", "SLOW");
        PotionEffectType harming = VersionCompatibility.potionType("INSTANT_DAMAGE", "HARM");

        if (poison != null) {
            plugin.projectileEngine().launchHexPotion(player, aim.clone(), poison, 8 * 20, 1);
        }
        if (slowness != null) {
            plugin.projectileEngine().launchHexPotion(player,
                    rotateAroundY(aim.clone(), 0.1), slowness, 6 * 20, 1);
        }
        if (harming != null) {
            plugin.projectileEngine().launchHexPotion(player,
                    rotateAroundY(aim.clone(), -0.1), harming, 1, 0);
        }
        // Direct bonus hit so the brew always deals physical damage.
        if (target != null) {
            PhysicalDamageEngine.dealDamage(target, ability.getDamage(), player);
        }
    }

    /** Vindicator T3 - "Axe Rampage": chain dash strikes between enemies. */
    private void axeRampage(Player player, TokenTier.AbilitySpec ability) {
        List<LivingEntity> targets = AbilityTargeting.areaTargets(player,
                player.getLocation(), ability.getRadius());
        int max = (int) ability.getDurationSeconds();
        if (targets.isEmpty()) {
            messages.actionBar(player, plugin.config().getString("messages.ability-no-target",
                    "&eNo target in range!"));
            return;
        }
        List<LivingEntity> chain = targets.subList(0, Math.min(max, targets.size()));
        for (int i = 0; i < chain.size(); i++) {
            final LivingEntity victim = chain.get(i);
            BukkitRunnable strike = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || victim.isDead()) {
                        return;
                    }
                    // Dash to the target and strike.
                    Vector side = victim.getLocation().getDirection().multiply(-1);
                    player.teleport(victim.getLocation().add(side));
                    PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
                    ParticleManager.burst(player.getWorld(), Particle.CRIT,
                            victim.getLocation().add(0, 1, 0), 14, 0.4);
                    SoundManager.play(player, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
                    SoundManager.play(player, Sound.ENTITY_VINDICATOR_CELEBRATE, 0.6f, 1.5f);
                }
            };
            strike.runTaskLater(plugin, i * 5L);
            plugin.scheduler().register(strike);
        }
    }

    /** Ravager T3 - "Ravager Charge": directional charge + ground shockwave. */
    private void ravagerCharge(Player player, TokenTier.AbilitySpec ability) {
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        player.setVelocity(direction.clone().multiply(1.9).setY(0.3));
        SoundManager.play(player, Sound.ENTITY_RAVAGER_ROAR, 1.2f, 1.1f);
        Set<UUID> hit = new HashSet<>();
        final int[] ticks = {0};
        final int chargeTicks = (int) (ability.getDurationSeconds() * 2);
        BukkitRunnable charge = new BukkitRunnable() {
            @Override
            public void run() {
                ticks[0]++;
                if (!player.isOnline() || ticks[0] > chargeTicks) {
                    finishCharge();
                    return;
                }
                ParticleManager.burst(player.getWorld(), Particle.CRIT,
                        player.getLocation(), 5, 0.4);
                for (LivingEntity victim : AbilityTargeting.areaTargets(player,
                        player.getLocation(), 1.8)) {
                    if (hit.add(victim.getUniqueId())) {
                        PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
                        Vector push = victim.getLocation().toVector()
                                .subtract(player.getLocation().toVector());
                        if (push.lengthSquared() > 0.01) {
                            victim.setVelocity(push.normalize()
                                    .multiply(ability.getKnockback()).setY(0.5));
                        }
                    }
                }
                if (ticks[0] == chargeTicks) {
                    finishCharge();
                }
            }

            private void finishCharge() {
                cancel();
                if (!player.isOnline()) {
                    return;
                }
                // Ground shockwave at the end of the charge.
                SoundManager.world(player.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1.4f, 0.8f);
                AbilityAnimation.shockwave(player, ability.getRadius());
                PhysicalDamageEngine.areaDamage(player, player.getLocation(),
                        ability.getRadius(), ability.getDamage() * 0.4, 0.8, false);
            }
        };
        charge.runTaskTimer(plugin, 1L, 1L);
        plugin.scheduler().register(charge);
    }

    /** Breeze T3 - "Cyclone Burst": wind blast launching enemies skyward. */
    private void cycloneBurst(Player player, TokenTier.AbilitySpec ability) {
        SoundManager.world(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.2f, 0.8f);
        SoundManager.world(player.getLocation(), Sound.ENTITY_BREEZE_DEFLECT, 1.0f, 1.4f);
        ParticleManager.ring(player.getWorld(), Particle.CLOUD,
                player.getLocation(), ability.getRadius() / 2, 32);
        ParticleManager.burst(player.getWorld(), Particle.GUST,
                player.getLocation().add(0, 1, 0), 10, 0.8);
        double launch = Math.max(1.0, ability.getDurationSeconds());
        for (LivingEntity victim : AbilityTargeting.areaTargets(player,
                player.getLocation(), ability.getRadius())) {
            PhysicalDamageEngine.dealDamage(victim, ability.getDamage(), player);
            Vector away = victim.getLocation().toVector()
                    .subtract(player.getLocation().toVector());
            if (away.lengthSquared() < 0.01) {
                away = new Vector(0, 1, 0);
            }
            victim.setVelocity(away.normalize().multiply(0.4).setY(launch * 0.7));
        }
    }

    /** Elder Guardian T3 - "Curse Beam": sustained beam, damage + fatigue. */
    private void curseBeam(Player player, TokenTier.AbilitySpec ability) {
        LivingEntity target = AbilityTargeting.nearestTarget(player, ability.getRadius());
        if (target == null) {
            messages.actionBar(player, plugin.config().getString("messages.ability-no-target",
                    "&eNo target in range!"));
            return;
        }
        PotionEffectType fatigue = VersionCompatibility.potionType("MINING_FATIGUE", "SLOW_DIGGING");
        SoundManager.world(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
        int durationSeconds = (int) ability.getDurationSeconds();
        final int[] pulses = {0};
        int totalPulses = durationSeconds * 2; // one pulse every half second
        BukkitRunnable beam = new BukkitRunnable() {
            @Override
            public void run() {
                pulses[0]++;
                if (!player.isOnline() || target.isDead() || pulses[0] > totalPulses) {
                    cancel();
                    return;
                }
                AbilityAnimation.burstBetween(player.getEyeLocation(),
                        target.getLocation().add(0, 1, 0), Particle.END_ROD, 12);
                AbilityAnimation.burstBetween(player.getEyeLocation(),
                        target.getLocation().add(0, 1, 0), Particle.SONIC_BOOM, 4);
                PhysicalDamageEngine.dealTrueDamage(target, ability.getDamage() * 0.5, player);
                if (fatigue != null) {
                    target.addPotionEffect(new PotionEffect(fatigue, 40, 2));
                }
            }
        };
        beam.runTaskTimer(plugin, 10L, 10L);
        plugin.scheduler().register(beam);
    }

    // ------------------------------------------------------------------
    // Admin token abilities
    // ------------------------------------------------------------------

    /** Admin T1 - "Nether Shockwave": 15-block true damage shockwave. */
    private void netherShockwave(Player player, TokenTier.AbilitySpec ability) {
        SoundManager.world(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        SoundManager.world(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.5f, 0.8f);
        AbilityAnimation.shockwave(player, ability.getRadius());
        ParticleManager.burst(player.getWorld(), Particle.FLAME, player.getLocation(), 40, 1.0);
        PhysicalDamageEngine.areaDamage(player, player.getLocation(), ability.getRadius(),
                ability.getDamage(), ability.getKnockback(), true);
    }

    /** Admin T2 - "Chrono Freeze": freezes every player in a 10-block radius. */
    private void chronoFreeze(Player player, TokenTier.AbilitySpec ability) {
        AnimationManager.freezeSound(player);
        ParticleManager.burst(player.getWorld(), Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0), 60, ability.getRadius() / 2);
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(
                ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof Player target) {
                freezeManager.freeze(target, ability.getDurationSeconds());
            }
        }
    }

    /** Admin T3 - "Server Judgment": god-tier forward judgment ray. */
    private void serverJudgment(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        SoundManager.world(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        animations.sonicRay(player, eye, direction, 30);

        for (LivingEntity victim : AbilityTargeting.rayTargets(player, eye, direction, 30, 3.0)) {
            PhysicalDamageEngine.dealTrueDamage(victim, ability.getDamage(), player);
            Vector push = direction.clone().multiply(ability.getKnockback() / 8.0);
            push.setY(1.2);
            victim.setVelocity(victim.getVelocity().add(push));
        }
        ParticleManager.column(player.getWorld(), Particle.END_ROD, player.getLocation(), 4, 12);
    }

    // ------------------------------------------------------------------

    /** Rotates a (normalized) direction around the Y axis by the given radians. */
    private static Vector rotateAroundY(Vector vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector(vector.getX() * cos - vector.getZ() * sin, vector.getY(),
                vector.getX() * sin + vector.getZ() * cos);
    }
}
