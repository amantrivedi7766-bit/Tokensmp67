package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
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
 * Blaze Token (Epic): fire identity. T1 Flame Lance, T2 Infernal Spiral, T3 Solar Burst.
 */
public final class BlazeToken extends AbstractToken {

    public BlazeToken(ConfigManager config) {
        super("blaze", "Blaze", TokenRarity.EPIC, Material.BLAZE_ROD, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FLAME_LANCE, "Flame Lance",
                        anum(1, "cooldown", 25))
                        .damage(anum(1, "damage", 17))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.4))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 28))
                        .particles(aparticles(1, Particle.FLAME, Particle.SMALL_FLAME, Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_BLAZE_AMBIENT, Sound.ENTITY_BLAZE_SHOOT))
                        .description("Concentrated flame projectile: 17 damage in a narrow hitbox"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier1.task-count", 350), "Kill 350 Blazes")
                .cost(Material.BLAZE_ROD, num("tier1.cost-blaze-rod", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .burningAura(true)
                .passiveDescription("Fire Resistance, Burning Aura (attackers ignite)")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INFERNAL_SPIRAL, "Infernal Spiral",
                        anum(2, "cooldown", 44))
                        .damage(anum(2, "damage", 29))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.4))
                        .count(anum(2, "projectiles", 3))
                        .speed(adnum(2, "speed", 1.1))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.FLAME, Particle.LAVA))
                        .sounds(asounds(2, Sound.ENTITY_BLAZE_BURN, Sound.ENTITY_BLAZE_SHOOT))
                        .description("Fire projectiles spiral toward the target: 29 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier2.task-count", 900), "Kill 900 Blazes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .burningAura(true)
                .passiveDescription("Fire Resistance, Burning Aura")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SOLAR_BURST, "Solar Burst",
                        anum(3, "cooldown", 67))
                        .damage(anum(3, "damage", 52))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.FLAME, Particle.LAVA, Particle.END_ROD))
                        .sounds(asounds(3, Sound.ENTITY_BLAZE_SHOOT, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A compressed solar sphere detonates: 52 damage + heavy knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier3.task-count", 2200), "Kill 2,200 Blazes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
