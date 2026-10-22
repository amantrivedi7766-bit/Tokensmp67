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
 * Villager Token (Common).
 * Passive: Regeneration I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class VillagerToken extends AbstractToken {

    public VillagerToken(ConfigManager config) {
        super("villager", "Villager", TokenRarity.COMMON, Material.EMERALD, false, config);

        addTier(TokenTier.of(1)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier1.task-count", 200), "Kill 200 Villagers")
                .cost(Material.EMERALD, num("tier1.cost-emerald", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier2.task-count", 500), "Kill 500 Villagers")
                .cost(Material.EMERALD_BLOCK, num("tier2.cost-emerald-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier3.task-count", 1200), "Kill 1200 Villagers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
