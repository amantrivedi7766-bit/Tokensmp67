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
 * Ghast Token (Epic): artillery identity.
 * T3 "Fireball": fires a REAL large explosive fireball projectile -
 * powerful blast damage + burn on impact (no terrain destruction).
 */
public final class GhastToken extends AbstractToken {

    public GhastToken(ConfigManager config) {
        super("ghast", "Ghast", TokenRarity.EPIC, Material.GHAST_TEAR, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Fire Resistance, Regeneration I")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier1.task-count", 300), "Kill 300 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier1.cost-ghast-tears", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .potion(regeneration(), 1)
                .passiveDescription("Fire Resistance, Regeneration II")
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier2.task-count", 700), "Kill 700 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier2.cost-ghast-tears", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .potion(regeneration(), 1)
                .passiveDescription("Fire Resistance, Regeneration II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FIREBALL_LAUNCH, "Ghast Fireball",
                        num("tier3.ability-cooldown", 55))
                        .damage(num("tier3.ability-damage", 15))
                        .radius(num("tier3.ability-impact-radius", 4))
                        .duration(num("tier3.ability-burn-seconds", 5))
                        .description("Large fireball: 15 blast damage + 5s burn on impact"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier3.task-count", 1000), "Kill 1,000 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier3.cost-ghast-tears", 64))
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 4))
                .build());
    }
}
