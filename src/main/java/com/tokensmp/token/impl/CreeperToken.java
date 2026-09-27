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
 * Creeper Token (Rare): blast resistance progression capped by the unique
 * T3 "Charged Overload" - a self-centered explosion nova that deals real
 * physical AoE damage with heavy knockback (never harms the user).
 */
public final class CreeperToken extends AbstractToken {

    public CreeperToken(ConfigManager config) {
        super("creeper", "Creeper", TokenRarity.RARE, Material.CREEPER_HEAD, false, config);

        addTier(TokenTier.of(1)
                .explosionImmunity(0.5)
                .passiveDescription("50% Blast Damage Protection")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier1.task-count", 300), "Kill 300 Creepers")
                .cost(Material.GUNPOWDER, num("tier1.cost-gunpowder", 64))
                .build());

        addTier(TokenTier.of(2)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier2.task-count", 800), "Kill 800 Creepers")
                .cost(Material.TNT, num("tier2.cost-tnt", 16))
                .build());

        addTier(TokenTier.of(3)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.CHARGED_OVERLOAD, "Charged Overload",
                        num("tier3.ability-cooldown", 60))
                        .radius(num("tier3.ability-radius", 6))
                        .damage(num("tier3.ability-damage", 14))
                        .knockback(dnum("tier3.ability-knockback", 2.0))
                        .description("Detonate a charged nova: 14 AoE damage + heavy knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier3.task-count", 2000), "Kill 2,000 Creepers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
