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
 * Guardian Token (Epic): underwater identity.
 * T3 "Prismatic Beam": target lock (visual circle) followed by a charging
 * beam that fires instant real damage at the locked target.
 */
public final class GuardianToken extends AbstractToken {

    public GuardianToken(ConfigManager config) {
        super("guardian", "Guardian", TokenRarity.EPIC, Material.PRISMARINE_SHARD, false, config);

        addTier(TokenTier.of(1)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 0)
                .passiveDescription("Water Breathing, Dolphin's Grace")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GUARDIAN),
                        num("tier1.task-count", 400), "Kill 400 Guardians")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier1.cost-crystals", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Water Breathing, Dolphin's Grace, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ELDER_GUARDIAN),
                        num("tier2.task-count", 900), "Kill 900 Guardians/Elders")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier2.cost-crystals", 16))
                .cost(Material.DIAMOND, num("tier2.cost-diamonds", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 0)
                .potion(resistance(), 1)
                .passiveDescription("Water Breathing, Dolphin's Grace, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PRISMATIC_BEAM, "Prismatic Beam",
                        num("tier3.ability-cooldown", 50))
                        .damage(num("tier3.ability-damage", 12))
                        .radius(num("tier3.ability-range", 15))
                        .description("Target lock then beam: 12 instant true damage on the locked target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ELDER_GUARDIAN),
                        num("tier3.task-count", 1200), "Kill 1,200 Guardians/Elders")
                .cost(Material.HEART_OF_THE_SEA, num("tier3.cost-hearts-of-the-sea", 1))
                .cost(Material.PRISMARINE_CRYSTALS, num("tier3.cost-crystals", 32))
                .build());
    }
}
