package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Spider Token (Common): mobility progression.
 * T1 wall climbing, T2 + Speed I (cave spider grind),
 * T3 "Web Walker": wall climbing, Speed II, Jump Boost III,
 * 100% fall damage immunity and CLOUD bursts on jumps.
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
                .cost(Material.FERMENTED_SPIDER_EYE, num("tier2.cost-spider-eyes", 32))
                .build());

        addTier(TokenTier.of(3)
                .wallClimbing(true)
                .potion(speed(), 1)
                .potion(jumpBoost(), 2)
                .fallImmunity(true)
                .cloudJumps(true)
                .passiveDescription("Wall Climbing, Speed II, Jump Boost III, Fall Immunity, Cloud Jumps")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SPIDER, EntityType.CAVE_SPIDER),
                        num("tier3.task-count", 3000), "Kill 3,000 total Spiders")
                .cost(Material.SLIME_BLOCK, num("tier3.cost-slime-blocks", 8))
                .build());
    }
}
