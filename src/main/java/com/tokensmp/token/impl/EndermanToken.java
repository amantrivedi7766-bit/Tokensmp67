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
 * Enderman Token (Epic): teleport identity.
 * T3 "Void Rift": teleports behind the target, executes a dimensional slash
 * (real true damage) and releases a secondary void pulse around the landing
 * point.
 */
public final class EndermanToken extends AbstractToken {

    public EndermanToken(ConfigManager config) {
        super("enderman", "Enderman", TokenRarity.EPIC, Material.ENDER_PEARL, false, config);

        addTier(TokenTier.of(1)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .passiveDescription("Ender Pearl Damage Immunity, Night Vision")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier1.task-count", 400), "Kill 400 Endermen")
                .cost(Material.ENDER_EYE, num("tier1.cost-ender-eyes", 16))
                .build());

        addTier(TokenTier.of(2)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Ender Pearl Damage Immunity, Night Vision, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier2.task-count", 1000), "Kill 1,000 Endermen")
                .cost(Material.ENDER_PEARL, num("tier2.cost-ender-pearls", 32))
                .build());

        addTier(TokenTier.of(3)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 1)
                .passiveDescription("Ender Pearl Damage Immunity, Night Vision, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_RIFT, "Void Rift",
                        num("tier3.ability-cooldown", 40))
                        .damage(num("tier3.ability-damage", 10))
                        .radius(num("tier3.ability-pulse-radius", 4))
                        .duration(num("tier3.ability-range", 12))
                        .description("Rift Strike: teleport behind the target, 10 true damage + void pulse"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier3.task-count", 2500), "Kill 2,500 Endermen")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
