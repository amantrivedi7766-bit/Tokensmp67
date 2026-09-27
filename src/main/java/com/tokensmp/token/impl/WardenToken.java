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
 * Warden Token (Legendary): tank identity.
 * T1 +5 extra hearts + Night Vision, T2 +10 hearts + Strength,
 * T3 "Sonic Boom": a forward sonic ray dealing 25 true damage with massive
 * knockback on a 90s cooldown.
 */
public final class WardenToken extends AbstractToken {

    public WardenToken(ConfigManager config) {
        super("warden", "Warden", TokenRarity.LEGENDARY, Material.SCULK, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(num("tier1.extra-hearts", 5))
                .potion(nightVision(), 0)
                .passiveDescription("+5 Extra Hearts, Night Vision")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier1.task-count", 25), "Kill 25 Wardens")
                .cost(Material.ECHO_SHARD, num("tier1.cost-echo-shards", 16))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(num("tier2.extra-hearts", 10))
                .potion(nightVision(), 0)
                .potion(strength(), 0)
                .passiveDescription("+10 Extra Hearts, Night Vision, Strength I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier2.task-count", 60), "Kill 60 Wardens")
                .cost(Material.ECHO_SHARD, num("tier2.cost-echo-shards", 32))
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(num("tier3.extra-hearts", 15))
                .potion(nightVision(), 0)
                .potion(strength(), 1)
                .passiveDescription("+15 Extra Hearts, Night Vision, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_BOOM, "Sonic Boom",
                        num("tier3.ability-cooldown", 90))
                        .damage(num("tier3.ability-damage", 25))
                        .knockback(num("tier3.ability-knockback", 3.0))
                        .description("Fire a sonic ray: 25 true damage + huge knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier3.task-count", 100), "Kill 100 Wardens")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingots", 4))
                .cost(Material.SCULK, num("tier3.cost-sculk", 64))
                .build());
    }
}
