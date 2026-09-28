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
 * Vindicator Token (Epic): the relentless melee duelist.
 * T3 "Axe Rampage": a chain of rapid dash strikes between up to 4 nearby
 * enemies - each strike deals real damage with a mini shockwave.
 */
public final class VindicatorToken extends AbstractToken {

    public VindicatorToken(ConfigManager config) {
        super("vindicator", "Vindicator", TokenRarity.EPIC, Material.IRON_AXE, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .potion(speed(), 0)
                .passiveDescription("Strength I, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VINDICATOR),
                        num("tier1.task-count", 800), "Kill 800 Vindicators")
                .cost(Material.EMERALD, num("tier1.cost-emeralds", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 1)
                .potion(speed(), 0)
                .passiveDescription("Strength II, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VINDICATOR, EntityType.PILLAGER),
                        num("tier2.task-count", 2000), "Kill 2,000 raiders")
                .cost(Material.IRON_BLOCK, num("tier2.cost-iron-blocks", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 1)
                .potion(speed(), 1)
                .passiveDescription("Strength II, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.AXE_RAMPAGE, "Axe Rampage",
                        num("tier3.ability-cooldown", 45))
                        .damage(num("tier3.ability-damage", 8))
                        .radius(num("tier3.ability-range", 8))
                        .duration(num("tier3.ability-max-targets", 4))
                        .description("Chain dash: strike up to 4 nearby enemies with 8 damage each"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VINDICATOR, EntityType.PILLAGER),
                        num("tier3.task-count", 3000), "Kill 3,000 raiders")
                .cost(Material.EMERALD_BLOCK, num("tier3.cost-emerald-blocks", 8))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
