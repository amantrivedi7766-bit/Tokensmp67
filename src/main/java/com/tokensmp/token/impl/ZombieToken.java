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
 * Zombie Token (Common).
 * Passive: Strength I, Night Vision, Regeneration I.
 * Ability: Undead Vitality - Instant heal + poison every nearby enemy.
 */
public final class ZombieToken extends AbstractToken {

    public ZombieToken(ConfigManager config) {
        super("zombie", "Zombie", TokenRarity.COMMON, Material.ZOMBIE_HEAD, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Strength I, Night Vision, Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.UNDEAD_VITALITY, "Undead Vitality",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 4))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 6.0))
                        .knockback(adnum(1, "knockback", 0.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.HAPPY_VILLAGER, Particle.HEART, Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_ZOMBIE_INFECT))
                        .description("Instant heal + poison every nearby enemy"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier1.task-count", 1000), "Kill 1000 Zombies")
                .cost(Material.DIAMOND, num("tier1.cost-diamond", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Strength I, Night Vision, Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.UNDEAD_VITALITY, "Undead Vitality",
                        anum(2, "cooldown", 28))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 6))
                        .range(adnum(2, "range", 8.0))
                        .radius(adnum(2, "radius", 8.0))
                        .knockback(adnum(2, "knockback", 0.0))
                        .count(anum(2, "count", 2))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.HAPPY_VILLAGER, Particle.HEART, Particle.SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_ZOMBIE_INFECT))
                        .description("Instant heal + poison every nearby enemy"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier2.task-count", 2500), "Kill 2500 Zombies")
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Strength I, Night Vision, Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.UNDEAD_VITALITY, "Undead Vitality",
                        anum(3, "cooldown", 26))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 8))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 10.0))
                        .knockback(adnum(3, "knockback", 0.0))
                        .count(anum(3, "count", 3))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.HAPPY_VILLAGER, Particle.HEART, Particle.SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_ZOMBIE_INFECT))
                        .description("Instant heal + poison every nearby enemy"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier3.task-count", 5000), "Kill 5000 Zombies")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-block", 2))
                .build());

    }
}
