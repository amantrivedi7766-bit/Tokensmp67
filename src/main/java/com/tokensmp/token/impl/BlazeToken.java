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
 * Blaze Token (Epic).
 * Passive: Fire & Lava Immunity, Strength II, melee hits ignite attackers.
 * Ability: Infernal Aura - Three consecutive explosive fireballs in the aimed direction.
 */
public final class BlazeToken extends AbstractToken {

    public BlazeToken(ConfigManager config) {
        super("blaze", "Blaze", TokenRarity.EPIC, Material.BLAZE_ROD, false, config);

        addTier(TokenTier.of(1)
                .fireImmunity(true)
                .potion(strength(), 1)
                .burningAura(true)
                .passiveDescription("Fire & Lava Immunity, Strength II, melee hits ignite attackers")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INFERNAL_AURA, "Infernal Aura",
                        anum(1, "cooldown", 22))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 30))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 3))
                        .speed(adnum(1, "speed", 1.2))
                        .duration(adnum(1, "duration", 3.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.FLAME, Particle.LAVA, Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_BLAZE_AMBIENT, Sound.ENTITY_BLAZE_SHOOT))
                        .description("Three consecutive explosive fireballs in the aimed direction"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier1.task-count", 350), "Kill 350 Blazes")
                .cost(Material.BLAZE_ROD, num("tier1.cost-blaze-rod", 32))
                .build());

        addTier(TokenTier.of(2)
                .fireImmunity(true)
                .potion(strength(), 1)
                .burningAura(true)
                .passiveDescription("Fire & Lava Immunity, Strength II, melee hits ignite attackers")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INFERNAL_AURA, "Infernal Aura",
                        anum(2, "cooldown", 20))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 48))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 3.5))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 3))
                        .speed(adnum(2, "speed", 1.2))
                        .duration(adnum(2, "duration", 4.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.FLAME, Particle.LAVA, Particle.SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_BLAZE_AMBIENT, Sound.ENTITY_BLAZE_SHOOT))
                        .description("Three consecutive explosive fireballs in the aimed direction"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier2.task-count", 900), "Kill 900 Blazes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(3)
                .fireImmunity(true)
                .potion(strength(), 1)
                .burningAura(true)
                .passiveDescription("Fire & Lava Immunity, Strength II, melee hits ignite attackers")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INFERNAL_AURA, "Infernal Aura",
                        anum(3, "cooldown", 18))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 70))
                        .range(adnum(3, "range", 28.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 3))
                        .speed(adnum(3, "speed", 1.2))
                        .duration(adnum(3, "duration", 5.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.FLAME, Particle.LAVA, Particle.SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_BLAZE_AMBIENT, Sound.ENTITY_BLAZE_SHOOT))
                        .description("Three consecutive explosive fireballs in the aimed direction"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier3.task-count", 2200), "Kill 2200 Blazes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
