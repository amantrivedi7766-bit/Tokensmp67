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
 * Evoker Token (Epic): support identity.
 * T3 "Fang Volley": advances a line of REAL evoker fangs from the player
 * toward the target - each fang physically bites everything on its tile.
 */
public final class EvokerToken extends AbstractToken {

    public EvokerToken(ConfigManager config) {
        super("evoker", "Evoker", TokenRarity.EPIC, Material.TOTEM_OF_UNDYING, false, config);

        addTier(TokenTier.of(1)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.EVOKER),
                        num("tier1.task-count", 300), "Kill 300 Evokers")
                .cost(Material.EMERALD, num("tier1.cost-emeralds", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(regeneration(), 1)
                .passiveDescription("Regeneration II")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER),
                        num("tier2.task-count", 700), "Kill 700 raiders")
                .cost(Material.EMERALD, num("tier2.cost-emeralds", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(regeneration(), 1)
                .potion(luck(), 0)
                .passiveDescription("Regeneration II, Luck I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FANG_VOLLEY, "Fang Volley",
                        num("tier3.ability-cooldown", 45))
                        .duration(num("tier3.ability-length", 8))
                        .radius(num("tier3.ability-fangs", 7))
                        .description("Advancing fangs: a line of real evoker fangs bites toward the target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.EVOKER, EntityType.VINDICATOR, EntityType.RAVAGER),
                        num("tier3.task-count", 500), "Kill 500 elite raiders")
                .cost(Material.EMERALD_BLOCK, num("tier3.cost-emerald-blocks", 8))
                .cost(Material.TOTEM_OF_UNDYING, num("tier3.cost-totems", 2))
                .build());
    }
}
