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
 * Vex Token (Rare).
 * Passive: Speed III, Slow Falling, tiny hit-box.
 * Ability: Wrathful Spirit - 5s floating charge mode with free wall passing.
 */
public final class VexToken extends AbstractToken {

    public VexToken(ConfigManager config) {
        super("vex", "Vex", TokenRarity.RARE, Material.IRON_SWORD, false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .passiveDescription("Speed III, Slow Falling, tiny hit-box")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WRATHFUL_SPIRIT, "Wrathful Spirit",
                        anum(1, "cooldown", 35))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 14))
                        .range(adnum(1, "range", 10.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 5.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.CRIT, Particle.END_ROD))
                        .sounds(asounds(1, Sound.ENTITY_VEX_CHARGE, Sound.ENTITY_VEX_AMBIENT))
                        .description("5s floating charge mode with free wall passing"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VEX),
                        num("tier1.task-count", 50), "Kill 50 Vexs")
                .cost(Material.PHANTOM_MEMBRANE, num("tier1.cost-phantom-membrane", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .passiveDescription("Speed III, Slow Falling, tiny hit-box")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WRATHFUL_SPIRIT, "Wrathful Spirit",
                        anum(2, "cooldown", 30))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 24))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.CRIT, Particle.END_ROD))
                        .sounds(asounds(2, Sound.ENTITY_VEX_CHARGE, Sound.ENTITY_VEX_AMBIENT))
                        .description("5s floating charge mode with free wall passing"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VEX),
                        num("tier2.task-count", 150), "Kill 150 Vexs")
                .cost(Material.AMETHYST_SHARD, num("tier2.cost-amethyst-shard", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 2)
                .potion(slowFalling(), 0)
                .passiveDescription("Speed III, Slow Falling, tiny hit-box")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WRATHFUL_SPIRIT, "Wrathful Spirit",
                        anum(3, "cooldown", 25))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 36))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 5.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.CRIT, Particle.END_ROD))
                        .sounds(asounds(3, Sound.ENTITY_VEX_CHARGE, Sound.ENTITY_VEX_AMBIENT))
                        .description("5s floating charge mode with free wall passing"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VEX),
                        num("tier3.task-count", 400), "Kill 400 Vexs")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
