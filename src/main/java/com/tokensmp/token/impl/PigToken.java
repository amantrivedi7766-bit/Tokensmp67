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
 * Pig Token (Common).
 * Passive: Speed III, infinite Hunger Saturation.
 * Ability: Hog Heal - Instant saturation burst + a 5x5x5 soft-block burrow.
 */
public final class PigToken extends AbstractToken {

    public PigToken(ConfigManager config) {
        super("pig", "Pig", TokenRarity.COMMON, com.tokensmp.core.VersionCompatibility.material("PORKCHOP", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 2)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, infinite Hunger Saturation")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HOG_HEAL, "Hog Heal",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 8))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.6))
                        .count(anum(1, "count", 5))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 10.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, "HAPPY_VILLAGER", "COMPOSTER", "BLOCK"))
                        .sounds(asounds(1, "ENTITY_PIG_AMBIENT", "ITEM_HONEYCOMB_WAX_ON"))
                        .description("Instant saturation burst + a 5x5x5 soft-block burrow"))
                .task(TokenTier.TaskType.KILLS, mobs("PIG"),
                        num("tier1.task-count", 400), "Kill 400 Pigs")
                .cost(Material.PORKCHOP, num("tier1.cost-porkchop", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 2)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, infinite Hunger Saturation")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HOG_HEAL, "Hog Heal",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 14))
                        .range(adnum(2, "range", 6.0))
                        .radius(adnum(2, "radius", 2.0))
                        .knockback(adnum(2, "knockback", 0.8))
                        .count(anum(2, "count", 5))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 15.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, "HAPPY_VILLAGER", "COMPOSTER", "BLOCK"))
                        .sounds(asounds(2, "ENTITY_PIG_AMBIENT", "ITEM_HONEYCOMB_WAX_ON"))
                        .description("Instant saturation burst + a 5x5x5 soft-block burrow"))
                .task(TokenTier.TaskType.KILLS, mobs("PIG"),
                        num("tier2.task-count", 1000), "Kill 1000 Pigs")
                .cost(Material.CARROT, num("tier2.cost-carrot", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 2)
                .potion(saturation(), 1)
                .passiveDescription("Speed III, infinite Hunger Saturation")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HOG_HEAL, "Hog Heal",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 20))
                        .range(adnum(3, "range", 6.0))
                        .radius(adnum(3, "radius", 2.0))
                        .knockback(adnum(3, "knockback", 1.0))
                        .count(anum(3, "count", 5))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 20.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, "HAPPY_VILLAGER", "COMPOSTER", "BLOCK"))
                        .sounds(asounds(3, "ENTITY_PIG_AMBIENT", "ITEM_HONEYCOMB_WAX_ON"))
                        .description("Instant saturation burst + a 5x5x5 soft-block burrow"))
                .task(TokenTier.TaskType.KILLS, mobs("PIG"),
                        num("tier3.task-count", 2500), "Kill 2500 Pigs")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
