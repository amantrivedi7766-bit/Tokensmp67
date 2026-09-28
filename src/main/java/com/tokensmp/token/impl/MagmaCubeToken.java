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
 * Magma Cube Token (Rare): siege identity.
 * T3 "Meteor Split": rains 5 real small magma meteors on an area - each
 * meteor explodes with AoE damage and burn (no terrain destruction).
 */
public final class MagmaCubeToken extends AbstractToken {

    public MagmaCubeToken(ConfigManager config) {
        super("magmacube", "Magma Cube", TokenRarity.RARE, Material.MAGMA_CREAM, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier1.task-count", 500), "Kill 500 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier1.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .potion(strength(), 0)
                .passiveDescription("Fire Resistance, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier2.task-count", 1500), "Kill 1,500 Magma Cubes")
                .cost(Material.MAGMA_BLOCK, num("tier2.cost-magma-blocks", 16))
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .potion(strength(), 1)
                .passiveDescription("Fire Resistance, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.METEOR_SPLIT, "Meteor Split",
                        num("tier3.ability-cooldown", 50))
                        .damage(num("tier3.ability-damage", 6))
                        .radius(num("tier3.ability-meteors", 5))
                        .duration(num("tier3.ability-burn-seconds", 3))
                        .description("Meteor rain: 5 magma meteors, each 6 AoE damage + burn"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier3.task-count", 2500), "Kill 2,500 Magma Cubes")
                .cost(Material.MAGMA_BLOCK, num("tier3.cost-magma-blocks", 32))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
