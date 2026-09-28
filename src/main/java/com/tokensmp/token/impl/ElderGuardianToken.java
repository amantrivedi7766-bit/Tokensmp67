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
 * Elder Guardian Token (Mythic): the final grind.
 * T3 "Curse Beam": a sustained curse beam that applies real damage over
 * time plus mining fatigue to the beam target.
 */
public final class ElderGuardianToken extends AbstractToken {

    public ElderGuardianToken(ConfigManager config) {
        super("elderguardian", "Elder Guardian", TokenRarity.MYTHIC, Material.WET_SPONGE, false, config);

        addTier(TokenTier.of(1)
                .potion(waterBreathing(), 0)
                .potion(resistance(), 0)
                .passiveDescription("Water Breathing, Resistance I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ELDER_GUARDIAN),
                        num("tier1.task-count", 60), "Kill 60 Elder Guardians")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier1.cost-crystals", 16))
                .cost(Material.SPONGE, num("tier1.cost-sponges", 8))
                .build());

        addTier(TokenTier.of(2)
                .potion(waterBreathing(), 0)
                .potion(resistance(), 1)
                .potion(dolphinsGrace(), 0)
                .passiveDescription("Water Breathing, Resistance II, Dolphin's Grace")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ELDER_GUARDIAN),
                        num("tier2.task-count", 100), "Kill 100 Elder Guardians")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier2.cost-crystals", 32))
                .cost(Material.HEART_OF_THE_SEA, num("tier2.cost-hearts-of-the-sea", 1))
                .build());

        addTier(TokenTier.of(3)
                .potion(waterBreathing(), 0)
                .potion(resistance(), 1)
                .potion(dolphinsGrace(), 0)
                .passiveDescription("Water Breathing, Resistance II, Dolphin's Grace")
                .ability(new TokenTier.AbilitySpec(TokenAbility.CURSE_BEAM, "Curse Beam",
                        num("tier3.ability-cooldown", 75))
                        .damage(num("tier3.ability-damage", 6))
                        .radius(num("tier3.ability-range", 15))
                        .duration(num("tier3.ability-duration", 3))
                        .description("Curse beam: 6 damage per second for 3s + mining fatigue on the target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ELDER_GUARDIAN),
                        num("tier3.task-count", 150), "Kill 150 Elder Guardians")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 4))
                .cost(Material.HEART_OF_THE_SEA, num("tier3.cost-hearts-of-the-sea", 2))
                .build());
    }
}
