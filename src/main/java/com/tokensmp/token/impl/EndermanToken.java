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
 * Enderman Token (Epic): displacement identity.
 * T1 void-touched (no ender pearl damage + Night Vision),
 * T2 + Speed I (end-themed grind), T3 "Warp Strike": teleport forward and
 * deal true damage at the landing point on a 40s cooldown.
 */
public final class EndermanToken extends AbstractToken {

    public EndermanToken(ConfigManager config) {
        super("enderman", "Enderman", TokenRarity.EPIC, Material.ENDER_PEARL, false, config);

        addTier(TokenTier.of(1)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .passiveDescription("Void Affinity (no Ender Pearl damage), Night Vision")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier1.task-count", 400), "Kill 400 Endermen")
                .cost(Material.ENDER_EYE, num("tier1.cost-ender-eyes", 16))
                .build());

        addTier(TokenTier.of(2)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Void Affinity, Night Vision, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMITE),
                        num("tier2.task-count", 1000), "Kill 1,000 Endermites")
                .cost(Material.ENDER_PEARL, num("tier2.cost-ender-pearls", 32))
                .build());

        addTier(TokenTier.of(3)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 1)
                .passiveDescription("Void Affinity, Night Vision, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WARP_STRIKE, "Warp Strike",
                        num("tier3.ability-cooldown", 40))
                        .radius(num("tier3.ability-radius", 10))
                        .damage(num("tier3.ability-damage", 12))
                        .description("Teleport 10 blocks forward; deal 12 true damage on landing"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier3.task-count", 2500), "Kill 2,500 Endermen")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
