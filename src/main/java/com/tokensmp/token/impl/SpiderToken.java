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
 * Spider Token (Common).
 * Passive: Wall Climbing, Jump Boost III, Speed II, No Fall Damage.
 * Ability: Web Trapper - Drops cobwebs around enemies to immobilise them.
 */
public final class SpiderToken extends AbstractToken {

    public SpiderToken(ConfigManager config) {
        super("spider", "Spider", TokenRarity.COMMON, com.tokensmp.core.VersionCompatibility.material("COBWEB", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .wallClimbing(true)
                .potion(jumpBoost(), 2)
                .potion(speed(), 1)
                .fallImmunity(true)
                .passiveDescription("Wall Climbing, Jump Boost III, Speed II, No Fall Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WEB_TRAPPER, "Web Trapper",
                        anum(1, "cooldown", 20))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 8))
                        .range(adnum(1, "range", 8.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 3.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, "ITEM_COBWEB", "WITCH"))
                        .sounds(asounds(1, "ENTITY_SPIDER_AMBIENT", "ENTITY_SPIDER_STEP"))
                        .description("Drops cobwebs around enemies to immobilise them"))
                .task(TokenTier.TaskType.KILLS, mobs("SPIDER"),
                        num("tier1.task-count", 500), "Kill 500 Spiders")
                .cost(Material.STRING, num("tier1.cost-string", 64))
                .build());

        addTier(TokenTier.of(2)
                .wallClimbing(true)
                .potion(jumpBoost(), 2)
                .potion(speed(), 1)
                .fallImmunity(true)
                .passiveDescription("Wall Climbing, Jump Boost III, Speed II, No Fall Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WEB_TRAPPER, "Web Trapper",
                        anum(2, "cooldown", 18))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 14))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 2.0))
                        .knockback(adnum(2, "knockback", 0.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 4.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, "ITEM_COBWEB", "WITCH"))
                        .sounds(asounds(2, "ENTITY_SPIDER_AMBIENT", "ENTITY_SPIDER_STEP"))
                        .description("Drops cobwebs around enemies to immobilise them"))
                .task(TokenTier.TaskType.KILLS, mobs("SPIDER"),
                        num("tier2.task-count", 1500), "Kill 1500 Spiders")
                .cost(Material.FERMENTED_SPIDER_EYE, num("tier2.cost-fermented-spider-eye", 32))
                .build());

        addTier(TokenTier.of(3)
                .wallClimbing(true)
                .potion(jumpBoost(), 2)
                .potion(speed(), 1)
                .fallImmunity(true)
                .passiveDescription("Wall Climbing, Jump Boost III, Speed II, No Fall Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WEB_TRAPPER, "Web Trapper",
                        anum(3, "cooldown", 16))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 20))
                        .range(adnum(3, "range", 12.0))
                        .radius(adnum(3, "radius", 2.0))
                        .knockback(adnum(3, "knockback", 0.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 5.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, "ITEM_COBWEB", "WITCH"))
                        .sounds(asounds(3, "ENTITY_SPIDER_AMBIENT", "ENTITY_SPIDER_STEP"))
                        .description("Drops cobwebs around enemies to immobilise them"))
                .task(TokenTier.TaskType.KILLS, mobs("SPIDER"),
                        num("tier3.task-count", 3000), "Kill 3000 Spiders")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
