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
 * Iron Golem Token (Epic).
 * Passive: +20 Max Hearts, knockback immunity, melee Strength V.
 * Ability: Fortress Vanguard - Ground punch that tosses enemies up and shreds their armour.
 */
public final class GolemToken extends AbstractToken {

    public GolemToken(ConfigManager config) {
        super("golem", "Iron Golem", TokenRarity.EPIC, Material.IRON_BLOCK, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(20)
                .knockbackResistance(1.0)
                .potion(strength(), 4)
                .passiveDescription("+20 Max Hearts, knockback immunity, melee Strength V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FORTRESS_VANGUARD, "Fortress Vanguard",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 12.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.2))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.CRIT, Particle.CLOUD, Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_IRON_GOLEM_ATTACK, Sound.ENTITY_IRON_GOLEM_DAMAGE))
                        .description("Ground punch that tosses enemies up and shreds their armour"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier1.task-count", 100), "Kill 100 Iron Golems")
                .cost(Material.IRON_INGOT, num("tier1.cost-iron-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(20)
                .knockbackResistance(1.0)
                .potion(strength(), 4)
                .passiveDescription("+20 Max Hearts, knockback immunity, melee Strength V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FORTRESS_VANGUARD, "Fortress Vanguard",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 34))
                        .range(adnum(2, "range", 14.0))
                        .radius(adnum(2, "radius", 4.0))
                        .knockback(adnum(2, "knockback", 1.5))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.CRIT, Particle.CLOUD, Particle.SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_IRON_GOLEM_ATTACK, Sound.ENTITY_IRON_GOLEM_DAMAGE))
                        .description("Ground punch that tosses enemies up and shreds their armour"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier2.task-count", 300), "Kill 300 Iron Golems")
                .cost(Material.IRON_BLOCK, num("tier2.cost-iron-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(20)
                .knockbackResistance(1.0)
                .potion(strength(), 4)
                .passiveDescription("+20 Max Hearts, knockback immunity, melee Strength V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FORTRESS_VANGUARD, "Fortress Vanguard",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 50))
                        .range(adnum(3, "range", 16.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 1.8))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.CRIT, Particle.CLOUD, Particle.SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_IRON_GOLEM_ATTACK, Sound.ENTITY_IRON_GOLEM_DAMAGE))
                        .description("Ground punch that tosses enemies up and shreds their armour"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier3.task-count", 800), "Kill 800 Iron Golems")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
