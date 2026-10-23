package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbilityTrigger;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Illusioner Token (Epic).
 * Passive: Night Vision, Speed I.
 * Active ability: not configured yet (cleared - awaiting the owner's spec).
 */
public final class IllusionerToken extends AbstractToken {

    public IllusionerToken(ConfigManager config) {
        super("illusioner", "Illusioner", TokenRarity.EPIC, com.tokensmp.core.VersionCompatibility.material("BOW", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Night Vision, Speed I")
                .task(TokenTier.TaskType.KILLS, mobs("ILLUSIONER"),
                        num("tier1.task-count", 50), "Kill 50 Illusioners")
                .cost(Material.ARROW, num("tier1.cost-arrow", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Night Vision, Speed I")
                .task(TokenTier.TaskType.KILLS, mobs("ILLUSIONER"),
                        num("tier2.task-count", 150), "Kill 150 Illusioners")
                .cost(Material.SPECTRAL_ARROW, num("tier2.cost-spectral-arrow", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Night Vision, Speed I")
                .task(TokenTier.TaskType.KILLS, mobs("ILLUSIONER"),
                        num("tier3.task-count", 400), "Kill 400 Illusioners")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
