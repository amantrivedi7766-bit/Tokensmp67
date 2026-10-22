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
 * Ravager Token (Legendary).
 * Passive: Strength III, Resistance II, heavy knockback resistance.
 * Ability: Berserker Rage - Forward charge that knocks everything back and shatters shields.
 */
public final class RavagerToken extends AbstractToken {

    public RavagerToken(ConfigManager config) {
        super("ravager", "Ravager", TokenRarity.LEGENDARY, Material.RAVAGER_SPAWN_EGG, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 2)
                .potion(resistance(), 1)
                .knockbackResistance(0.5)
                .passiveDescription("Strength III, Resistance II, heavy knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BERSERKER_RAGE, "Berserker Rage",
                        anum(1, "cooldown", 20))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 8.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 2.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 0.55))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.CLOUD, Particle.CRIT, Particle.LARGE_SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_RAVAGER_ATTACK))
                        .description("Forward charge that knocks everything back and shatters shields"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier1.task-count", 25), "Kill 25 Ravagers")
                .cost(Material.SADDLE, num("tier1.cost-saddle", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 2)
                .potion(resistance(), 1)
                .knockbackResistance(0.5)
                .passiveDescription("Strength III, Resistance II, heavy knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BERSERKER_RAGE, "Berserker Rage",
                        anum(2, "cooldown", 18))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 32))
                        .range(adnum(2, "range", 8.0))
                        .radius(adnum(2, "radius", 3.5))
                        .knockback(adnum(2, "knockback", 2.5))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 0.55))
                        .duration(adnum(2, "duration", 1.2))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.CLOUD, Particle.CRIT, Particle.LARGE_SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_RAVAGER_ATTACK))
                        .description("Forward charge that knocks everything back and shatters shields"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier2.task-count", 75), "Kill 75 Ravagers")
                .cost(Material.SADDLE, num("tier2.cost-saddle", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 2)
                .potion(resistance(), 1)
                .knockbackResistance(0.5)
                .passiveDescription("Strength III, Resistance II, heavy knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BERSERKER_RAGE, "Berserker Rage",
                        anum(3, "cooldown", 16))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 48))
                        .range(adnum(3, "range", 8.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 0.55))
                        .duration(adnum(3, "duration", 1.5))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.CLOUD, Particle.CRIT, Particle.LARGE_SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_RAVAGER_ATTACK))
                        .description("Forward charge that knocks everything back and shatters shields"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier3.task-count", 200), "Kill 200 Ravagers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
