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
 * Skeleton Token (Common): archer identity with escalating bow damage.
 * T3 "Phantom Arrow Barrage": multiple REAL arrow projectiles with different
 * trajectories that physically travel and deal actual damage.
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
                .ability(new TokenTier.AbilitySpec(TokenAbility.ARROW_BARRAGE, "Phantom Arrow Barrage",
                        num("tier3.ability-cooldown", 40))
                        .damage(num("tier3.ability-damage", 5))
                        .radius(num("tier3.ability-range", 15))
                        .duration(num("tier3.ability-arrows", 7))
                        .description("Barrage: 7 real arrows with different trajectories, each dealing real damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier3.task-count", 4500), "Kill 4,500 Skeletons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
