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
 * Piglin Token (Rare): the melee brute.
 * T3 "Gilded Axe": throws a REAL returning axe projectile (boomerang) that
 * damages everything on the way out and back.
 */
public final class PiglinToken extends AbstractToken {

    public PiglinToken(ConfigManager config) {
        super("piglin", "Piglin", TokenRarity.RARE, Material.GOLDEN_AXE, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .passiveDescription("Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN),
                        num("tier1.task-count", 1000), "Kill 1,000 Piglins")
                .cost(Material.GOLD_INGOT, num("tier1.cost-gold-ingots", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .meleeDamageBonus(0.10)
                .passiveDescription("Strength I, +10% Melee Damage")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN_BRUTE),
                        num("tier2.task-count", 2500), "Kill 2,500 Piglin Brutes")
                .cost(Material.GOLD_BLOCK, num("tier2.cost-gold-blocks", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 1)
                .meleeDamageBonus(0.25)
                .passiveDescription("Strength II, +25% Melee Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GILDED_AXE, "Gilded Axe",
                        num("tier3.ability-cooldown", 40))
                        .damage(num("tier3.ability-damage", 10))
                        .radius(num("tier3.ability-range", 12))
                        .description("Returning golden axe: 10 damage on the way out AND back"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE),
                        num("tier3.task-count", 4000), "Kill 4,000 Piglins")
                .cost(Material.GOLD_BLOCK, num("tier3.cost-gold-blocks", 32))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
