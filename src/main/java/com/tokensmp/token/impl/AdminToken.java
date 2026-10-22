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
 * Admin Token (Legendary).
 * Passive: True Sight, Speed V, Permanent Resistance V.
 * Ability: Judgement Strike - Structural lightning that drops the target's token and freezes them.
 * RESERVED: never appears in spins or player-facing GUIs.
 */
public final class AdminToken extends AbstractToken {

    public AdminToken(ConfigManager config) {
        super("admin", "Admin", TokenRarity.LEGENDARY, Material.NETHERITE_AXE, true, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 4)
                .potion(resistance(), 4)
                .potion(nightVision(), 0)
                .passiveDescription("True Sight, Speed V, Permanent Resistance V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.JUDGEMENT_STRIKE, "Judgement Strike",
                        anum(1, "cooldown", 60))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 30))
                        .range(adnum(1, "range", 40.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 10.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 60))
                        .particles(aparticles(1, Particle.ELECTRIC_SPARK, Particle.END_ROD, Particle.CRIT))
                        .sounds(asounds(1, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Sound.BLOCK_ANVIL_LAND))
                        .description("Structural lightning that drops the target's token and freezes them"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier1.task-count", 1), "Kill 1 Admins")
                .cost(Material.NETHERITE_INGOT, num("tier1.cost-netherite-ingot", 4))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 4)
                .potion(resistance(), 4)
                .potion(nightVision(), 0)
                .passiveDescription("True Sight, Speed V, Permanent Resistance V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.JUDGEMENT_STRIKE, "Judgement Strike",
                        anum(2, "cooldown", 45))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 50))
                        .range(adnum(2, "range", 40.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.8))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 10.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 60))
                        .particles(aparticles(2, Particle.ELECTRIC_SPARK, Particle.END_ROD, Particle.CRIT))
                        .sounds(asounds(2, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Sound.BLOCK_ANVIL_LAND))
                        .description("Structural lightning that drops the target's token and freezes them"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier2.task-count", 1), "Kill 1 Admins")
                .cost(Material.NETHERITE_BLOCK, num("tier2.cost-netherite-block", 2))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 4)
                .potion(resistance(), 4)
                .potion(nightVision(), 0)
                .passiveDescription("True Sight, Speed V, Permanent Resistance V")
                .ability(new TokenTier.AbilitySpec(TokenAbility.JUDGEMENT_STRIKE, "Judgement Strike",
                        anum(3, "cooldown", 30))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 80))
                        .range(adnum(3, "range", 40.0))
                        .radius(adnum(3, "radius", 3.0))
                        .knockback(adnum(3, "knockback", 2.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 10.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.ELECTRIC_SPARK, Particle.END_ROD, Particle.CRIT))
                        .sounds(asounds(3, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Sound.BLOCK_ANVIL_LAND))
                        .description("Structural lightning that drops the target's token and freezes them"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDER_DRAGON),
                        num("tier3.task-count", 1), "Kill 1 Admins")
                .cost(Material.NETHER_STAR, num("tier3.cost-nether-star", 1))
                .build());

    }
}
