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
 * Magma Cube Token (Rare).
 * Passive: Fire Resistance.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class MagmaCubeToken extends AbstractToken {

    public MagmaCubeToken(ConfigManager config) {
        super("magma_cube", "Magma Cube", TokenRarity.RARE, Material.MAGMA_CREAM, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier1.task-count", 350), "Kill 350 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier1.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier2.task-count", 900), "Kill 900 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier3.task-count", 2200), "Kill 2200 Magma Cubes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
