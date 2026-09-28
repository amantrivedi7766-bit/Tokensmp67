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
 * Witch Token (Rare): support identity.
 * T3 "Hex Brew": throws a volley of REAL splash potions (poison, slowness,
 * harming) with actual negative effects and real damage.
 */
public final class WitchToken extends AbstractToken {

    public WitchToken(ConfigManager config) {
        super("witch", "Witch", TokenRarity.RARE, Material.SPLASH_POTION, false, config);

        addTier(TokenTier.of(1)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITCH),
                        num("tier1.task-count", 500), "Kill 500 Witches")
                .cost(Material.GLASS_BOTTLE, num("tier1.cost-bottles", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(regeneration(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Regeneration I, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITCH),
                        num("tier2.task-count", 1200), "Kill 1,200 Witches")
                .cost(Material.REDSTONE_BLOCK, num("tier2.cost-redstone-blocks", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(regeneration(), 1)
                .potion(resistance(), 0)
                .passiveDescription("Regeneration II, Resistance I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.HEX_BREW, "Hex Brew",
                        num("tier3.ability-cooldown", 35))
                        .damage(num("tier3.ability-damage", 4))
                        .radius(num("tier3.ability-potions", 3))
                        .description("Splash potion volley: poison + slowness + 4 damage to the target area"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITCH),
                        num("tier3.task-count", 1800), "Kill 1,800 Witches")
                .cost(Material.FERMENTED_SPIDER_EYE, num("tier3.cost-fermented-eyes", 32))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
