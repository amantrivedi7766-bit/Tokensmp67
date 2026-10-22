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
 * Golem Token (Epic).
 * Passive: +4 Extra Hearts, Resistance I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class GolemToken extends AbstractToken {

    public GolemToken(ConfigManager config) {
        super("golem", "Golem", TokenRarity.EPIC, Material.IRON_BLOCK, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(4)
                .potion(resistance(), 0)
                .passiveDescription("+4 Extra Hearts, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier1.task-count", 100), "Kill 100 Golems")
                .cost(Material.IRON_INGOT, num("tier1.cost-iron-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(4)
                .potion(resistance(), 0)
                .passiveDescription("+4 Extra Hearts, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier2.task-count", 300), "Kill 300 Golems")
                .cost(Material.IRON_BLOCK, num("tier2.cost-iron-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(4)
                .potion(resistance(), 0)
                .passiveDescription("+4 Extra Hearts, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier3.task-count", 800), "Kill 800 Golems")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
