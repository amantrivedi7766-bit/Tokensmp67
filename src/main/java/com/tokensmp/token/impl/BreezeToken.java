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
 * Breeze Token (Rare): wind identity.
 * T3 "Cyclone Burst": a physical cyclone that launches all nearby enemies
 * into the air with real knockback, dealing fall-prone damage.
 */
public final class BreezeToken extends AbstractToken {

    public BreezeToken(ConfigManager config) {
        super("breeze", "Breeze", TokenRarity.RARE, Material.BREEZE_ROD, false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 1)
                .potion(jumpBoost(), 0)
                .passiveDescription("Speed II, Jump Boost I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BREEZE),
                        num("tier1.task-count", 300), "Kill 300 Breezes")
                .cost(Material.BREEZE_ROD, num("tier1.cost-breeze-rods", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 1)
                .potion(jumpBoost(), 1)
                .fallDamageReduction(0.5)
                .passiveDescription("Speed II, Jump Boost II, -50% Fall Damage")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BREEZE),
                        num("tier2.task-count", 800), "Kill 800 Breezes")
                .cost(Material.BREEZE_ROD, num("tier2.cost-breeze-rods", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 2)
                .potion(jumpBoost(), 1)
                .fallDamageReduction(0.75)
                .passiveDescription("Speed III, Jump Boost II, -75% Fall Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.CYCLONE_BURST, "Cyclone Burst",
                        num("tier3.ability-cooldown", 40))
                        .damage(num("tier3.ability-damage", 6))
                        .radius(num("tier3.ability-radius", 6))
                        .knockback(dnum("tier3.ability-launch-power", 2.0))
                        .description("Cyclone: 6 damage + launches all nearby enemies into the air"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BREEZE),
                        num("tier3.task-count", 1500), "Kill 1,500 Breezes")
                .cost(Material.BREEZE_ROD, num("tier3.cost-breeze-rods", 48))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
