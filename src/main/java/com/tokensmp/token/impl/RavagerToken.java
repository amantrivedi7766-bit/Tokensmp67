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
 * Ravager Token (Legendary): the unstoppable charger.
 * T3 "Ravager Charge": a physical directional charge - the user rams
 * everything on the path with real damage, ending in a ground shockwave.
 */
public final class RavagerToken extends AbstractToken {

    public RavagerToken(ConfigManager config) {
        super("ravager", "Ravager", TokenRarity.LEGENDARY, Material.RAVAGER_HIDE, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(num("tier1.extra-hearts", 5))
                .potion(strength(), 0)
                .passiveDescription("+5 Extra Hearts, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier1.task-count", 150), "Kill 150 Ravagers")
                .cost(Material.IRON_BLOCK, num("tier1.cost-iron-blocks", 16))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(num("tier2.extra-hearts", 10))
                .potion(strength(), 0)
                .potion(resistance(), 0)
                .passiveDescription("+10 Extra Hearts, Strength I, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier2.task-count", 300), "Kill 300 Ravagers")
                .cost(Material.DIAMOND_BLOCK, num("tier2.cost-diamond-blocks", 8))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(num("tier3.extra-hearts", 15))
                .potion(strength(), 1)
                .potion(resistance(), 1)
                .passiveDescription("+15 Extra Hearts, Strength II, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.RAVAGER_CHARGE, "Ravager Charge",
                        num("tier3.ability-cooldown", 60))
                        .damage(num("tier3.ability-damage", 12))
                        .radius(num("tier3.ability-shock-radius", 3))
                        .duration(num("tier3.ability-distance", 10))
                        .knockback(dnum("tier3.ability-knockback", 1.5))
                        .description("Ram charge: 12 damage on the path + ground shockwave at the end"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier3.task-count", 500), "Kill 500 Ravagers")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 4))
                .cost(Material.DIAMOND_BLOCK, num("tier3.cost-diamond-blocks", 8))
                .build());
    }
}
