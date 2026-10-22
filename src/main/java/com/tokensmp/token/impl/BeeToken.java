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
 * Bee Token (Rare).
 * Passive: Speed III, Slow Falling, Saturation II.
 * Ability: Buzz Rush - Stinger dive that poisons the target, then bounces you safely back.
 */
public final class BeeToken extends AbstractToken {

    public BeeToken(ConfigManager config) {
        super("bee", "Bee", TokenRarity.RARE, Material.HONEYCOMB, false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, Slow Falling, Saturation II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BUZZ_RUSH, "Buzz Rush",
                        anum(1, "cooldown", 20))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 14.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.CRIT, Particle.WAX_ON))
                        .sounds(asounds(1, Sound.ENTITY_BEE_LOOP, Sound.ENTITY_BEE_STING))
                        .description("Stinger dive that poisons the target, then bounces you safely back"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BEE),
                        num("tier1.task-count", 400), "Kill 400 Bees")
                .cost(Material.HONEYCOMB, num("tier1.cost-honeycomb", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, Slow Falling, Saturation II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BUZZ_RUSH, "Buzz Rush",
                        anum(2, "cooldown", 18))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 32))
                        .range(adnum(2, "range", 16.0))
                        .radius(adnum(2, "radius", 3.5))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.CRIT, Particle.WAX_ON))
                        .sounds(asounds(2, Sound.ENTITY_BEE_LOOP, Sound.ENTITY_BEE_STING))
                        .description("Stinger dive that poisons the target, then bounces you safely back"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BEE),
                        num("tier2.task-count", 1000), "Kill 1000 Bees")
                .cost(Material.HONEY_BLOCK, num("tier2.cost-honey-block", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, Slow Falling, Saturation II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BUZZ_RUSH, "Buzz Rush",
                        anum(3, "cooldown", 16))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 46))
                        .range(adnum(3, "range", 18.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 1.4))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.CRIT, Particle.WAX_ON))
                        .sounds(asounds(3, Sound.ENTITY_BEE_LOOP, Sound.ENTITY_BEE_STING))
                        .description("Stinger dive that poisons the target, then bounces you safely back"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BEE),
                        num("tier3.task-count", 2500), "Kill 2500 Bees")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
