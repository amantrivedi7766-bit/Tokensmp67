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
 * Zombie Token (Common): the grind starter.
 * T3 "Undead Shockwave": a physical undead shockwave centered on the player
 * dealing real AoE damage with knockback, followed by a 15-second green
 * enrage trail animation sequence.
 */
public final class ZombieToken extends AbstractToken {

    public ZombieToken(ConfigManager config) {
        super("zombie", "Zombie", TokenRarity.COMMON, Material.ZOMBIE_HEAD, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .passiveDescription("Strength I, Night Vision")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier1.task-count", 1000), "Kill 1,000 Zombies")
                .cost(Material.DIAMOND, num("tier1.cost-diamonds", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Strength I, Night Vision, Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier2.task-count", 2500), "Kill 2,500 Zombies")
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 1)
                .potion(nightVision(), 0)
                .potion(regeneration(), 1)
                .passiveDescription("Strength II, Night Vision, Regeneration II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.UNDEAD_SHOCKWAVE, "Undead Shockwave",
                        num("tier3.ability-cooldown", 45))
                        .radius(num("tier3.ability-radius", 6))
                        .damage(num("tier3.ability-damage", 8))
                        .knockback(dnum("tier3.ability-knockback", 1.2))
                        .duration(num("tier3.ability-duration", 15))
                        .description("Physical undead shockwave: 8 damage + knockback to everything nearby"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier3.task-count", 5000), "Kill 5,000 Zombies")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 2))
                .build());
    }
}
