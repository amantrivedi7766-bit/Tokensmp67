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
 * Piglin Token (Rare).
 * Passive: Strength I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class PiglinToken extends AbstractToken {

    public PiglinToken(ConfigManager config) {
        super("piglin", "Piglin", TokenRarity.RARE, com.tokensmp.core.VersionCompatibility.material("GOLD_INGOT", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN"),
                        num("tier1.task-count", 400), "Kill 400 Piglins")
                .cost(Material.GOLD_INGOT, num("tier1.cost-gold-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN"),
                        num("tier2.task-count", 1000), "Kill 1000 Piglins")
                .cost(Material.GOLD_BLOCK, num("tier2.cost-gold-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN"),
                        num("tier3.task-count", 2500), "Kill 2500 Piglins")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
