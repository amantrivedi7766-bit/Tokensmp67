package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Spider Token (Common): mobility identity.
 * T3 "Web Harpoon": launches a physical web projectile that PULLS the victim
 * toward the impact point, deals real damage and briefly restricts movement.
 */
public final class SpiderToken extends AbstractToken {

    public SpiderToken(ConfigManager config) {
        super("spider", "Spider", TokenRarity.COMMON, Material.SPIDER_EYE, false, config);

        addTier(TokenTier.of(1)
                .wallClimbing(true)
                .passiveDescription("Wall Climbing")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SPIDER),
                        num("tier1.task-count", 500), "Kill 500 Spiders")
                .cost(Material.STRING, num("tier1.cost-string", 64))
                .build());

        addTier(TokenTier.of(2)
                .wallClimbing(true)
                .potion(speed(), 0)
                .passiveDescription("Wall Climbing, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CAVE_SPIDER),
                        num("tier2.task-count", 1500), "Kill 1,500 Cave Spiders")
                .cost(Material.FERMENTED_SPIDER_EYE, num("tier2.cost-fermented-eyes", 32))
                .build());

        addTier(TokenTier.of(3)
                .wallClimbing(true)
                .potion(speed(), 1)
                .potion(jumpBoost(), 2)
                .fallImmunity(true)
                .passiveDescription("Wall Climbing, Speed II, Jump Boost III, Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WEB_HARPOON, "Web Harpoon",
                        num("tier3.ability-cooldown", 35))
                        .damage(num("tier3.ability-damage", 6))
                        .radius(num("tier3.ability-range", 16))
                        .duration(num("tier3.ability-slow-seconds", 3))
                        .description("Web projectile: pulls the target to the impact point, damages and roots them"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SPIDER, EntityType.CAVE_SPIDER),
                        num("tier3.task-count", 3000), "Kill 3,000 total Spiders")
                .cost(Material.SLIME_BLOCK, num("tier3.cost-slime-blocks", 8))
                .build());
    }
}
