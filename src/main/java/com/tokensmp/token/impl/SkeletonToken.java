package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Skeleton Token (Common): pure archery progression.
 * T1 +15% bow damage, T2 +30% + Speed I, T3 "Archer's Focus" (passive):
 * +50% bow/crossbow damage, Speed II, Strength I and CRIT/SNOWFLAKE arrow trails.
 */
public final class SkeletonToken extends AbstractToken {

    public SkeletonToken(ConfigManager config) {
        super("skeleton", "Skeleton", TokenRarity.COMMON, Material.SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .bowDamageBonus(0.15)
                .passiveDescription("+15% Bow Damage")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier1.task-count", 800), "Kill 800 Skeletons")
                .cost(Material.BONE, num("tier1.cost-bones", 64))
                .build());

        addTier(TokenTier.of(2)
                .bowDamageBonus(0.30)
                .potion(speed(), 0)
                .passiveDescription("+30% Bow Damage, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier2.task-count", 2000), "Kill 2,000 Skeletons")
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .bowDamageBonus(0.50)
                .potion(speed(), 1)
                .potion(strength(), 0)
                .arrowTrail(true)
                .passiveDescription("+50% Bow/Crossbow Damage, Speed II, Strength I, Arrow Trails")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier3.task-count", 4500), "Kill 4,500 Skeletons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
