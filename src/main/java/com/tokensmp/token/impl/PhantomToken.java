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
 * Phantom Token (Rare): aerial identity.
 * T3 "Phantom Dive": a forward swooping dash damaging everything along the
 * path (real collision damage), followed by slow-falling glide recovery.
 */
public final class PhantomToken extends AbstractToken {

    public PhantomToken(ConfigManager config) {
        super("phantom", "Phantom", TokenRarity.RARE, Material.PHANTOM_MEMBRANE, false, config);

        addTier(TokenTier.of(1)
                .potion(slowFalling(), 0)
                .passiveDescription("Slow Falling (night flight)")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier1.task-count", 400), "Kill 400 Phantoms")
                .cost(Material.PHANTOM_MEMBRANE, num("tier1.cost-membranes", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(slowFalling(), 0)
                .potion(speed(), 0)
                .passiveDescription("Slow Falling, Speed I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier2.task-count", 1000), "Kill 1,000 Phantoms")
                .cost(Material.PHANTOM_MEMBRANE, num("tier2.cost-membranes", 16))
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(slowFalling(), 0)
                .potion(speed(), 1)
                .fallImmunity(true)
                .passiveDescription("Slow Falling, Speed II, Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PHANTOM_DIVE, "Phantom Dive",
                        num("tier3.ability-cooldown", 40))
                        .damage(num("tier3.ability-damage", 8))
                        .radius(num("tier3.ability-distance", 10))
                        .description("Swooping dive dash: 8 damage to everything along a 10-block path"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier3.task-count", 1500), "Kill 1,500 Phantoms")
                .cost(Material.PHANTOM_MEMBRANE, num("tier3.cost-membranes", 48))
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .build());
    }
}
