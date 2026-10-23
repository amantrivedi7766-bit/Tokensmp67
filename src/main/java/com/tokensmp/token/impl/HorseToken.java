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
 * Horse Token (Rare).
 * Passive: Speed IV, Jump Boost III.
 * Ability: Stallion Sprint - High leap clearing 6+ blocks with a landing shockwave.
 */
public final class HorseToken extends AbstractToken {

    public HorseToken(ConfigManager config) {
        super("horse", "Horse", TokenRarity.RARE, com.tokensmp.core.VersionCompatibility.material("HORSE_SPAWN_EGG", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 3)
                .potion(jumpBoost(), 2)
                .passiveDescription("Speed IV, Jump Boost III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.STALLION_SPRINT, "Stallion Sprint",
                        anum(1, "cooldown", 25))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 18))
                        .range(adnum(1, "range", 10.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.2))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 0.8))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, "CLOUD", "CRIT"))
                        .sounds(asounds(1, "ENTITY_HORSE_GALLOP", "ENTITY_HORSE_JUMP"))
                        .description("High leap clearing 6+ blocks with a landing shockwave"))
                .task(TokenTier.TaskType.KILLS, mobs("HORSE"),
                        num("tier1.task-count", 300), "Kill 300 Horses")
                .cost(Material.LEATHER, num("tier1.cost-leather", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 3)
                .potion(jumpBoost(), 2)
                .passiveDescription("Speed IV, Jump Boost III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.STALLION_SPRINT, "Stallion Sprint",
                        anum(2, "cooldown", 22))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 28))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.6))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 0.9))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, "CLOUD", "CRIT"))
                        .sounds(asounds(2, "ENTITY_HORSE_GALLOP", "ENTITY_HORSE_JUMP"))
                        .description("High leap clearing 6+ blocks with a landing shockwave"))
                .task(TokenTier.TaskType.KILLS, mobs("HORSE"),
                        num("tier2.task-count", 800), "Kill 800 Horses")
                .cost(Material.HAY_BLOCK, num("tier2.cost-hay-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 3)
                .potion(jumpBoost(), 2)
                .passiveDescription("Speed IV, Jump Boost III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.STALLION_SPRINT, "Stallion Sprint",
                        anum(3, "cooldown", 20))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 42))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, "CLOUD", "CRIT"))
                        .sounds(asounds(3, "ENTITY_HORSE_GALLOP", "ENTITY_HORSE_JUMP"))
                        .description("High leap clearing 6+ blocks with a landing shockwave"))
                .task(TokenTier.TaskType.KILLS, mobs("HORSE"),
                        num("tier3.task-count", 2000), "Kill 2000 Horses")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
