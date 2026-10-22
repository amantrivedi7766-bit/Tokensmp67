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
 * Slime Token (Common).
 * Passive: Fall Damage Immunity, Jump Boost I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class SlimeToken extends AbstractToken {

    public SlimeToken(ConfigManager config) {
        super("slime", "Slime", TokenRarity.COMMON, Material.SLIME_BALL, false, config);

        addTier(TokenTier.of(1)
                .fallImmunity(true)
                .potion(jumpBoost(), 0)
                .passiveDescription("Fall Damage Immunity, Jump Boost I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier1.task-count", 400), "Kill 400 Slimes")
                .cost(Material.SLIME_BALL, num("tier1.cost-slime-ball", 64))
                .build());

        addTier(TokenTier.of(2)
                .fallImmunity(true)
                .potion(jumpBoost(), 0)
                .passiveDescription("Fall Damage Immunity, Jump Boost I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier2.task-count", 1000), "Kill 1000 Slimes")
                .cost(Material.SLIME_BLOCK, num("tier2.cost-slime-block", 16))
                .build());

        addTier(TokenTier.of(3)
                .fallImmunity(true)
                .potion(jumpBoost(), 0)
                .passiveDescription("Fall Damage Immunity, Jump Boost I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier3.task-count", 2500), "Kill 2500 Slimes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
