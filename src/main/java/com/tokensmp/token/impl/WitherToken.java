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
 * Wither Token (Mythic): boss-endurance identity with THE hard unlock
 * streak grind. T3 "Wither Barrage": launches multiple REAL wither skull
 * projectiles with different trajectories.
 */
public final class WitherToken extends AbstractToken {

    public WitherToken(ConfigManager config) {
        super("wither", "Wither", TokenRarity.MYTHIC, Material.WITHER_SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 1)
                .potion(resistance(), 0)
                .passiveDescription("Strength II, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER_SKELETON),
                        num("tier1.task-count", 2000), "Kill 2,000 Wither Skeletons")
                .cost(Material.WITHER_SKELETON_SKULL, num("tier1.cost-skulls", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 1)
                .potion(resistance(), 1)
                .passiveDescription("Strength II, Resistance II")
                .task(TokenTier.TaskType.WITHER_STREAK, null,
                        num("tier2.task-count", 25), "Kill 25 Withers without dying")
                .cost(Material.WITHER_SKELETON_SKULL, num("tier2.cost-skulls", 32))
                .cost(Material.DIAMOND_BLOCK, num("tier2.cost-diamond-blocks", 8))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 2)
                .potion(resistance(), 1)
                .passiveDescription("Strength III, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WITHER_BARRAGE, "Wither Barrage",
                        num("tier3.ability-cooldown", 60))
                        .damage(num("tier3.ability-damage", 8))
                        .duration(num("tier3.ability-skulls", 3))
                        .description("Barrage: 3 wither skulls on different trajectories, each 8 damage"))
                .task(TokenTier.TaskType.WITHER_STREAK, null,
                        num("tier3.task-count", 50), "Kill 50 Withers without dying")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 8))
                .cost(Material.DRAGON_BREATH, num("tier3.cost-dragons-breath", 2))
                .build());
    }
}
