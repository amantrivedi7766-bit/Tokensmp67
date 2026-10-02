package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbilityTrigger;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Blaze Token (Epic): pure fire aggression, burn and mobility. Bright orange,
 * golden yellow and deep red.
 *
 * Passive (all tiers): full Fire &amp; Lava immunity, Movement Speed +5%, a light
 * Blaze breathing sound every 10 seconds and a burning aura that sets nearby
 * enemies alight.
 *
 * T1 "Flame Burst"      (RIGHT CLICK)   - 7-block fireball, 4 hearts + burning
 *                                         ground for 5s (1 heart per second).
 * T2 "Blazing Wraith"   (SHIFT + LEFT)  - 15s invisible fire form firing soul
 *                                         bolts every 1.5s.
 * T3 "Meteor Judgement" (SHIFT + RIGHT) - 15s meteor ultimate: magma barrage,
 *                                         a big rock on 3 straight hits, then
 *                                         the meteor itself falls.
 */
public final class BlazeToken extends AbstractToken {

    public BlazeToken(ConfigManager config) {
        super("blaze", "Blaze", TokenRarity.EPIC, Material.BLAZE_ROD, false, config);

        addTier(TokenTier.of(1)
                .fireImmunity(true)
                .movementSpeedModifier(0.05)
                .fireAura(true)
                .ambient(Sound.ENTITY_BLAZE_AMBIENT, 200)
                .passiveDescription("Fire & Lava Immunity, Movement Speed +5%, Burning Aura, Blaze breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FLAME_BURST, "Flame Burst",
                        anum(1, "cooldown", 7))
                        .trigger(atrigger(1, AbilityTrigger.RIGHT_CLICK))
                        .damage(anum(1, "damage", 8))
                        .range(adnum(1, "range", 7.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "fire-seconds", 3.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.FLAME, Particle.LAVA, Particle.SMOKE,
                                Particle.SMALL_FLAME))
                        .sounds(asounds(1, Sound.ENTITY_BLAZE_SHOOT, Sound.ITEM_FIRECHARGE_USE,
                                Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Right click: throw a fireball that bursts after 7 blocks - 4 hearts "
                                + "in 3 blocks + 3s fire, and the ground burns for 5s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier1.task-count", 350), "Kill 350 Blazes")
                .cost(Material.BLAZE_ROD, num("tier1.cost-blaze-rod", 32))
                .build());

        addTier(TokenTier.of(2)
                .fireImmunity(true)
                .movementSpeedModifier(0.05)
                .fireAura(true)
                .ambient(Sound.ENTITY_BLAZE_AMBIENT, 200)
                .passiveDescription("Fire & Lava Immunity, Movement Speed +5%, Burning Aura, Blaze breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BLAZING_WRAITH, "Blazing Wraith",
                        anum(2, "cooldown", 18))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_LEFT_CLICK))
                        .damage(anum(2, "damage", 4))
                        .range(adnum(2, "range", 4.0))
                        .radius(adnum(2, "radius", 4.0))
                        .knockback(adnum(2, "knockback", 0.0))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "wraith-seconds", 15.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.FLAME, Particle.SOUL_FIRE_FLAME,
                                Particle.SOUL))
                        .sounds(asounds(2, Sound.ENTITY_BLAZE_BURN, Sound.ENTITY_BLAZE_HURT,
                                Sound.PARTICLE_SOUL_ESCAPE, Sound.BLOCK_FIRE_EXTINGUISH))
                        .description("Shift + left click: become an invisible fire wraith for 15s, "
                                + "firing soul bolts (2 hearts + 2s fire) every 1.5s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier2.task-count", 900), "Kill 900 Blazes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(3)
                .fireImmunity(true)
                .movementSpeedModifier(0.05)
                .fireAura(true)
                .ambient(Sound.ENTITY_BLAZE_AMBIENT, 200)
                .passiveDescription("Fire & Lava Immunity, Movement Speed +5%, Burning Aura, Blaze breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.METEOR_JUDGEMENT, "Meteor Judgement",
                        anum(3, "cooldown", 55))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 20))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 8.0))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "streak-required", 3))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "meteor-seconds", 15.0))
                        .trueDamage(abool(3, "true-damage", true))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.FLAME, Particle.LAVA, Particle.SMOKE,
                                Particle.SOUL_FIRE_FLAME, Particle.EXPLOSION_EMITTER))
                        .sounds(asounds(3, Sound.ENTITY_BLAZE_SHOOT, Sound.ENTITY_GHAST_SHOOT,
                                Sound.ENTITY_GENERIC_EXPLODE, Sound.ENTITY_ENDER_DRAGON_GROWL))
                        .description("Shift + right click: a 15s meteor hangs above you dropping magma "
                                + "blocks - 3 straight hits on one target call down a 10-heart TRUE "
                                + "damage rock, then the meteor itself falls"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier3.task-count", 2200), "Kill 2,200 Blazes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
