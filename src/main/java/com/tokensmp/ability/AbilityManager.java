package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.AbilityAnimation;
import com.tokensmp.animation.ParticleEngine;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.MessageManager;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * The ability engine: validates every activation request through the full
 * chain (ownership, claim state, tier, availability, cooldown, targets) and
 * executes the ability behavior. Server data is authoritative - the held
 * item's lore is never trusted.
 */
public final class AbilityManager {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final CooldownManager cooldowns;
    private final MessageManager messages;
    private final FreezeManager freezeManager;
    private final AbilityAnimationEngine animations;

    public AbilityManager(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                          CooldownManager cooldowns, MessageManager messages,
                          FreezeManager freezeManager, AbilityAnimationEngine animations) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.cooldowns = cooldowns;
        this.messages = messages;
        this.freezeManager = freezeManager;
        this.animations = animations;
    }

    // ------------------------------------------------------------------
    // Activation entry point (SHIFT + RIGHT CLICK)
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
        Token token = registry.get(activeId);
        // 2/3. Is the token valid and claimed?
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
        // Frozen players cannot use abilities.
        if (freezeManager.isFrozen(player)) {
            SoundEngine.denied(player);
            return;
        }
        // 6. Cooldown check (server-side timestamp).
        long remaining = cooldowns.remainingMillis(player, activeId);
        if (remaining > 0L) {
            SoundEngine.denied(player);
            messages.actionBar(player, plugin.config().getString("messages.ability-denied",
                            "&c&l[!]&c Ability on cooldown! Wait {seconds}s")
                    .replace("{seconds}", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }

        // Everything validated: start cooldown, run the ability, announce.
        cooldowns.start(player, activeId, ability.getCooldownSeconds());
        execute(player, token, tierDef, ability);
        SoundEngine.play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        messages.actionBar(player, plugin.config().getString("messages.ability-activated",
                "&a&l[!] &2{ability} Activated!").replace("{ability}", ability.getName()));
        cooldowns.showHud(player, activeId, ability.getCooldownSeconds());
    }

    // ------------------------------------------------------------------
    // Ability dispatch
    // ------------------------------------------------------------------

    private void execute(Player player, Token token, TokenTier tier, TokenTier.AbilitySpec ability) {
        switch (ability.getType()) {
            case UNDEAD_ENRAGE -> undeadEnrage(player, ability);
            case CHARGED_OVERLOAD -> chargedOverload(player, ability);
            case BLAZING_WRATH -> blazingWrath(player, ability);
            case WARP_STRIKE -> warpStrike(player, ability);
            case SONIC_BOOM -> sonicBoom(player, ability);
            case NETHER_SHOCKWAVE -> netherShockwave(player, ability);
            case CHRONO_FREEZE -> chronoFreeze(player, ability);
            case SERVER_JUDGMENT -> serverJudgment(player, ability);
            case NONE -> { /* nothing */ }
        }
    }

    /** Zombie T3 - "Undead Enrage": 15s Health Absorption IV + green trail. */
    private void undeadEnrage(Player player, TokenTier.AbilitySpec ability) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                (int) (ability.getDurationSeconds() * 20), ability.getAmplifier(), false, true));
        SoundEngine.play(player, Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.8f);
        animations.enrageTrail(player, (int) ability.getDurationSeconds());
    }

    /** Creeper T3 - "Charged Overload": real AoE explosion nova + knockback. */
    private void chargedOverload(Player player, TokenTier.AbilitySpec ability) {
        SoundEngine.world(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        ParticleEngine.burst(player.getWorld(), Particle.EXPLOSION, player.getLocation(), 3, 0.5);
        AbilityAnimation.shockwave(player, ability.getRadius());
        PhysicalDamageEngine.areaDamage(player, player.getLocation(), ability.getRadius(),
                ability.getDamage(), ability.getKnockback(), false);
        SoundEngine.play(player, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.5f);
    }

    /** Blaze T3 - "Blazing Wrath": ignite + true damage to everything nearby. */
    private void blazingWrath(Player player, TokenTier.AbilitySpec ability) {
        SoundEngine.world(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.7f);
        ParticleEngine.burst(player.getWorld(), Particle.FLAME,
                player.getLocation().add(0, 1, 0), 80, ability.getRadius() / 2);
        animations.flameAura(player, (int) ability.getDurationSeconds());
        int burnTicks = (int) (ability.getDurationSeconds() * 20);
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof LivingEntity target) {
                target.setFireTicks(Math.max(target.getFireTicks(), burnTicks));
                PhysicalDamageEngine.dealTrueDamage(target, ability.getDamage(), player);
            }
        }
    }

    /** Enderman T3 - "Warp Strike": teleport forward, true damage on landing. */
    private void warpStrike(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        SoundEngine.world(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        animations.warpBurst(player.getLocation());

        Location landing = com.tokensmp.util.LocationUtil.forward(eye, ability.getRadius());
        landing.setDirection(direction);
        landing.setY(player.getLocation().getY());
        player.teleport(landing);
        animations.warpBurst(landing);
        PhysicalDamageEngine.areaDamage(player, landing, 3.0, ability.getDamage(), 0.4, true);
    }

    /** Warden T3 - "Sonic Boom": forward sonic ray, 25 true damage + knockback. */
    private void sonicBoom(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        SoundEngine.world(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);
        animations.sonicRay(player, eye, direction, 25);

        List<LivingEntity> victims = rayVictims(player, eye, direction, 25.0, 1.8);
        for (LivingEntity victim : victims) {
            PhysicalDamageEngine.dealTrueDamage(victim, ability.getDamage(), player);
            Vector push = direction.clone().multiply(ability.getKnockback());
            push.setY(Math.max(push.getY(), 0.5));
            victim.setVelocity(push);
        }
    }

    /** Admin T1 - "Nether Shockwave": 15-block true damage shockwave. */
    private void netherShockwave(Player player, TokenTier.AbilitySpec ability) {
        SoundEngine.world(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        SoundEngine.world(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.5f, 0.8f);
        AbilityAnimation.shockwave(player, ability.getRadius());
        ParticleEngine.burst(player.getWorld(), Particle.FLAME, player.getLocation(), 40, 1.0);
        PhysicalDamageEngine.areaDamage(player, player.getLocation(), ability.getRadius(),
                ability.getDamage(), ability.getKnockback(), true);
    }

    /** Admin T2 - "Chrono Freeze": freezes every player in a 10-block radius. */
    private void chronoFreeze(Player player, TokenTier.AbilitySpec ability) {
        AbilityAnimationEngine.freezeSound(player);
        ParticleEngine.burst(player.getWorld(), Particle.SNOWFLAKE,
                player.getLocation().add(0, 1, 0), 60, ability.getRadius() / 2);
        for (Entity entity : player.getNearbyEntities(ability.getRadius(), ability.getRadius(), ability.getRadius())) {
            if (entity instanceof Player target) {
                freezeManager.freeze(target, ability.getDurationSeconds());
            }
        }
    }

    /** Admin T3 - "Server Judgment": god-tier sonic projectile, 20-block knockback. */
    private void serverJudgment(Player player, TokenTier.AbilitySpec ability) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        SoundEngine.world(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        animations.sonicRay(player, eye, direction, 30);

        List<LivingEntity> victims = rayVictims(player, eye, direction, 30.0, 3.0);
        for (LivingEntity victim : victims) {
            PhysicalDamageEngine.dealTrueDamage(victim, ability.getDamage(), player);
            Vector push = direction.clone().multiply(ability.getKnockback() / 8.0);
            push.setY(1.2);
            victim.setVelocity(victim.getVelocity().add(push));
        }
        // Cinematic sky judgment above the caster.
        ParticleEngine.column(player.getWorld(), Particle.END_ROD, player.getLocation(), 4, 12);
    }

    // ------------------------------------------------------------------

    /** Collects unique living entities along a forward ray (skips the caster). */
    private List<LivingEntity> rayVictims(Player player, Location start, Vector direction,
                                          double length, double hitRadius) {
        List<LivingEntity> victims = new ArrayList<>();
        for (double distance = 1.0; distance <= length; distance += 1.5) {
            Location point = start.clone().add(direction.clone().multiply(distance));
            for (Entity entity : player.getWorld().getNearbyEntities(point, hitRadius, hitRadius, hitRadius)) {
                if (entity instanceof LivingEntity living
                        && !entity.equals(player)
                        && !victims.contains(living)) {
                    victims.add(living);
                }
            }
        }
        return victims;
    }
}
