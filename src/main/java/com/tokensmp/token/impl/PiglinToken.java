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
 * Piglin Token (Rare): golden warfare identity. T1 Golden Cleave, T2 Gold Spear, T3 Royal Execution.
 */
public final class PiglinToken extends AbstractToken {

    public PiglinToken(ConfigManager config) {
        super("piglin", "Piglin", TokenRarity.RARE, Material.GOLD_INGOT, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GOLDEN_CLEAVE, "Golden Cleave",
                        anum(1, "cooldown", 22))
                        .damage(anum(1, "damage", 13))
                        .range(adnum(1, "range", 4.5))
                        .radius(adnum(1, "radius", 3.5))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 28))
                        .particles(aparticles(1, Particle.CRIT, Particle.ENCHANTED_HIT, Particle.FLAME))
                        .sounds(asounds(1, Sound.ENTITY_PLAYER_ATTACK_SWEEP, Sound.ENTITY_GENERIC_HURT))
                        .description("A golden crescent slash: 13 damage in a short forward arc"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN),
                        num("tier1.task-count", 400), "Kill 400 Piglins")
                .cost(Material.GOLD_INGOT, num("tier1.cost-gold-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GOLD_SPEAR, "Gold Spear",
                        anum(2, "cooldown", 38))
                        .damage(anum(2, "damage", 24))
                        .range(adnum(2, "range", 22.0))
                        .radius(adnum(2, "radius", 2.5))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "projectiles", 2))
                        .speed(adnum(2, "speed", 1.3))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 32))
                        .particles(aparticles(2, Particle.CRIT, Particle.END_ROD, Particle.ENCHANTED_HIT))
                        .sounds(asounds(2, Sound.ITEM_TRIDENT_THROW, Sound.ITEM_TRIDENT_HIT))
                        .description("A spinning golden spear: 24 damage, pierces the first and hits a second target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN),
                        num("tier2.task-count", 1000), "Kill 1,000 Piglins")
                .cost(Material.GOLD_BLOCK, num("tier2.cost-gold-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 1)
                .passiveDescription("Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ROYAL_EXECUTION, "Royal Execution",
                        anum(3, "cooldown", 58))
                        .damage(anum(3, "damage", 42))
                        .range(adnum(3, "range", 5.0))
                        .radius(adnum(3, "radius", 4.5))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 44))
                        .particles(aparticles(3, Particle.CRIT, Particle.FLAME, Particle.ENCHANTED_HIT))
                        .sounds(asounds(3, Sound.ENTITY_PLAYER_ATTACK_SWEEP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Cinematic forward execution: dash into a giant golden crescent, 42 damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN),
                        num("tier3.task-count", 2500), "Kill 2,500 Piglins")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
