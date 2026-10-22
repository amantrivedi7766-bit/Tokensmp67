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
 * Ghast Token (Rare).
 * Passive: Glide control while sneaking, Fire Resistance, arrow absorption.
 * Ability: Hellfire Artillery - Long-range tracking fireball with double creeper blast damage.
 */
public final class GhastToken extends AbstractToken {

    public GhastToken(ConfigManager config) {
        super("ghast", "Ghast", TokenRarity.RARE, Material.GHAST_TEAR, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Glide control while sneaking, Fire Resistance, arrow absorption")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HELLFIRE_ARTILLERY, "Hellfire Artillery",
                        anum(1, "cooldown", 40))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 30))
                        .range(adnum(1, "range", 30.0))
                        .radius(adnum(1, "radius", 5.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 0.9))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 60))
                        .particles(aparticles(1, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.LAVA))
                        .sounds(asounds(1, Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_GHAST_SCREAM))
                        .description("Long-range tracking fireball with double creeper blast damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier1.task-count", 250), "Kill 250 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier1.cost-ghast-tear", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Glide control while sneaking, Fire Resistance, arrow absorption")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HELLFIRE_ARTILLERY, "Hellfire Artillery",
                        anum(2, "cooldown", 34))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 48))
                        .range(adnum(2, "range", 40.0))
                        .radius(adnum(2, "radius", 6.0))
                        .knockback(adnum(2, "knockback", 1.8))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 0.9))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 60))
                        .particles(aparticles(2, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.LAVA))
                        .sounds(asounds(2, Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_GHAST_SCREAM))
                        .description("Long-range tracking fireball with double creeper blast damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier2.task-count", 600), "Kill 600 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier2.cost-ghast-tear", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Glide control while sneaking, Fire Resistance, arrow absorption")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HELLFIRE_ARTILLERY, "Hellfire Artillery",
                        anum(3, "cooldown", 28))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 70))
                        .range(adnum(3, "range", 50.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 2.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 0.9))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.LAVA))
                        .sounds(asounds(3, Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_GHAST_SCREAM))
                        .description("Long-range tracking fireball with double creeper blast damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier3.task-count", 1500), "Kill 1500 Ghasts")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
