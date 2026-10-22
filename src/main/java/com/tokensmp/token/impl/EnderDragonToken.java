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
 * Ender Dragon Token (Legendary).
 * Passive: Temporary Creative Flight, Strength III, knockback protection.
 * Ability: Dragon's Might - Dragon fireball that leaves a lingering breath cloud.
 */
public final class EnderDragonToken extends AbstractToken {

    public EnderDragonToken(ConfigManager config) {
        super("ender_dragon", "Ender Dragon", TokenRarity.LEGENDARY, Material.DRAGON_EGG, false, config);

        addTier(TokenTier.of(1)
                .flight(true)
                .potion(strength(), 2)
                .knockbackResistance(1.0)
                .passiveDescription("Temporary Creative Flight, Strength III, knockback protection")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DRAGONS_MIGHT, "Dragon's Might",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 5.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.DRAGON_BREATH, Particle.PORTAL, Particle.END_ROD))
                        .sounds(asounds(1, Sound.ENTITY_ENDER_DRAGON_GROWL, Sound.ENTITY_ENDER_DRAGON_FLAP))
                        .description("Dragon fireball that leaves a lingering breath cloud"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier1.task-count", 3), "Kill 3 Ender Dragons")
                .cost(Material.DRAGON_BREATH, num("tier1.cost-dragon-breath", 8))
                .build());

        addTier(TokenTier.of(2)
                .flight(true)
                .potion(strength(), 2)
                .knockbackResistance(1.0)
                .passiveDescription("Temporary Creative Flight, Strength III, knockback protection")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DRAGONS_MIGHT, "Dragon's Might",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 32))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 4.0))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 6.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.DRAGON_BREATH, Particle.PORTAL, Particle.END_ROD))
                        .sounds(asounds(2, Sound.ENTITY_ENDER_DRAGON_GROWL, Sound.ENTITY_ENDER_DRAGON_FLAP))
                        .description("Dragon fireball that leaves a lingering breath cloud"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier2.task-count", 10), "Kill 10 Ender Dragons")
                .cost(Material.DRAGON_BREATH, num("tier2.cost-dragon-breath", 16))
                .build());

        addTier(TokenTier.of(3)
                .flight(true)
                .potion(strength(), 2)
                .knockbackResistance(1.0)
                .passiveDescription("Temporary Creative Flight, Strength III, knockback protection")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DRAGONS_MIGHT, "Dragon's Might",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 48))
                        .range(adnum(3, "range", 28.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 1.4))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 8.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.DRAGON_BREATH, Particle.PORTAL, Particle.END_ROD))
                        .sounds(asounds(3, Sound.ENTITY_ENDER_DRAGON_GROWL, Sound.ENTITY_ENDER_DRAGON_FLAP))
                        .description("Dragon fireball that leaves a lingering breath cloud"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier3.task-count", 25), "Kill 25 Ender Dragons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
