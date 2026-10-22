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
 * Ghast Token (Rare).
 * Passive: Fire Resistance.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class GhastToken extends AbstractToken {

    public GhastToken(ConfigManager config) {
        super("ghast", "Ghast", TokenRarity.RARE, Material.GHAST_TEAR, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier1.task-count", 250), "Kill 250 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier1.cost-ghast-tear", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier2.task-count", 600), "Kill 600 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier2.cost-ghast-tear", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier3.task-count", 1500), "Kill 1500 Ghasts")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
