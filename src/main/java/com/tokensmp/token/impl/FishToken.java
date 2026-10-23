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
 * Fish Token (Common).
 * Passive: Water Breathing.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class FishToken extends AbstractToken {

    public FishToken(ConfigManager config) {
        super("fish", "Fish", TokenRarity.COMMON, com.tokensmp.core.VersionCompatibility.material("TROPICAL_FISH", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .task(TokenTier.TaskType.KILLS, mobs("COD"),
                        num("tier1.task-count", 300), "Kill 300 Fishs")
                .cost(Material.COD, num("tier1.cost-cod", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .task(TokenTier.TaskType.KILLS, mobs("COD"),
                        num("tier2.task-count", 800), "Kill 800 Fishs")
                .cost(Material.PRISMARINE_SHARD, num("tier2.cost-prismarine-shard", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .task(TokenTier.TaskType.KILLS, mobs("COD"),
                        num("tier3.task-count", 2000), "Kill 2000 Fishs")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
