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
 * Slime Token (Common): knockback identity.
 * T3 "Bounce Slam": leaps the player up and slams down, dealing real AoE
 * damage and launching enemies into the air.
 */
public final class SlimeToken extends AbstractToken {

    public SlimeToken(ConfigManager config) {
        super("slime", "Slime", TokenRarity.COMMON, Material.SLIME_BALL, false, config);

        addTier(TokenTier.of(1)
                .potion(jumpBoost(), 0)
                .fallDamageReduction(0.5)
                .passiveDescription("Jump Boost I, -50% Fall Damage")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier1.task-count", 800), "Kill 800 Slimes")
                .cost(Material.SLIME_BALL, num("tier1.cost-slime-balls", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(jumpBoost(), 1)
                .fallDamageReduction(0.75)
                .passiveDescription("Jump Boost II, -75% Fall Damage")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier2.task-count", 2000), "Kill 2,000 Slimes")
                .cost(Material.SLIME_BLOCK, num("tier2.cost-slime-blocks", 16))
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(jumpBoost(), 2)
                .fallImmunity(true)
                .passiveDescription("Jump Boost III, Full Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOUNCE_SLAM, "Bounce Slam",
                        num("tier3.ability-cooldown", 45))
                        .radius(num("tier3.ability-radius", 5))
                        .damage(num("tier3.ability-damage", 10))
                        .knockback(dnum("tier3.ability-launch-power", 1.0))
                        .description("Leap up and slam down: 10 AoE damage, launching enemies into the air"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier3.task-count", 3500), "Kill 3,500 Slimes")
                .cost(Material.SLIME_BLOCK, num("tier3.cost-slime-blocks", 32))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
