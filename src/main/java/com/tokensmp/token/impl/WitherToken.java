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
 * Wither Token (Legendary).
 * Passive: Resistance II, Strength I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class WitherToken extends AbstractToken {

    public WitherToken(ConfigManager config) {
        super("wither", "Wither", TokenRarity.LEGENDARY, Material.WITHER_SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .potion(resistance(), 1)
                .potion(strength(), 0)
                .passiveDescription("Resistance II, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier1.task-count", 3), "Kill 3 Withers")
                .cost(Material.COAL, num("tier1.cost-coal", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(resistance(), 1)
                .potion(strength(), 0)
                .passiveDescription("Resistance II, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier2.task-count", 10), "Kill 10 Withers")
                .cost(Material.NETHERITE_INGOT, num("tier2.cost-netherite-ingot", 2))
                .build());

        addTier(TokenTier.of(3)
                .potion(resistance(), 1)
                .potion(strength(), 0)
                .passiveDescription("Resistance II, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier3.task-count", 25), "Kill 25 Withers")
                .cost(Material.NETHER_STAR, num("tier3.cost-nether-star", 1))
                .build());

    }
}
