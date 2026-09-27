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
 * Blaze Token (Epic): fire identity.
 * T1 Fire Resistance, T2 + Burning Aura (attackers catch fire),
 * T3 "Blazing Wrath": ignite every enemy in a 7-block radius and deal
 * true damage on a 50s cooldown.
 */
public final class BlazeToken extends AbstractToken {

    public BlazeToken(ConfigManager config) {
        super("blaze", "Blaze", TokenRarity.EPIC, Material.BLAZE_ROD, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier1.task-count", 350), "Kill 350 Blazes")
                .cost(Material.BLAZE_ROD, num("tier1.cost-blaze-rods", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .burningAura(true)
                .passiveDescription("Fire Resistance, Burning Aura (attackers ignite)")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier2.task-count", 900), "Kill 900 Blazes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .burningAura(true)
                .passiveDescription("Fire Resistance, Burning Aura")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BLAZING_WRATH, "Blazing Wrath",
                        num("tier3.ability-cooldown", 50))
                        .radius(num("tier3.ability-radius", 7))
                        .damage(num("tier3.ability-damage", 10))
                        .duration(num("tier3.ability-burn-seconds", 5))
                        .description("Ignite all enemies within 7 blocks for 5s + 10 true damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BLAZE),
                        num("tier3.task-count", 2200), "Kill 2,200 Blazes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
