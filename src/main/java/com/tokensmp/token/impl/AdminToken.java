package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;

/**
 * Admin Token (Mythic) - absolute isolation: generated ONLY through
 * /tokensadmin give <player> admin, never in spins, GUIs or statistics
 * for normal players.
 *
 * T1 Nether Overlord (shockwave, 15r, 10 true damage, 45s),
 * T2 Ruler of the Realm (Chrono Freeze, 10r, 7s, 60s),
 * T3 GOD MODE (Server Judgment: 35 true damage, 20-block knockback, 30s).
 */
public final class AdminToken extends AbstractToken {

    public AdminToken(ConfigManager config) {
        super("admin", "Admin", TokenRarity.MYTHIC, Material.NETHER_STAR, true, config);

        // TIER 1 - Nether Overlord
        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.NETHER_SHOCKWAVE, "Nether Shockwave",
                        num("tier1.ability-cooldown", 45))
                        .radius(num("tier1.ability-radius", 15))
                        .damage(num("tier1.ability-damage", 10))
                        .knockback(dnum("tier1.ability-knockback", 1.5))
                        .description("Shockwave: 10 true damage to everything within 15 blocks"))
                .task(TokenTier.TaskType.NETHER_KILLS, null,
                        num("tier1.task-count", 5000), "Kill 5,000 total mobs inside the Nether")
                .cost(Material.NETHERITE_BLOCK, num("tier1.cost-netherite-blocks", 4))
                .build());

        // TIER 2 - Ruler of the Realm
        addTier(TokenTier.of(2)
                .extraHearts(num("tier2.extra-hearts", 10))
                .potion(resistance(), 2)
                .potion(strength(), 1)
                .potion(fireResistance(), 0)
                .passiveDescription("+10 Extra Hearts, Resistance III, Strength II, Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.CHRONO_FREEZE, "Chrono Freeze",
                        num("tier2.ability-cooldown", 60))
                        .radius(num("tier2.ability-radius", 10))
                        .duration(num("tier2.ability-duration", 7))
                        .description("Freeze every player within 10 blocks for 7 seconds"))
                .task(TokenTier.TaskType.WITHER_STREAK, null,
                        num("tier2.task-count", 3), "Kill 3 Withers in one streak without dying")
                .cost(Material.DIAMOND_BLOCK, num("tier2.cost-diamond-blocks", 8))
                .cost(Material.DRAGON_BREATH, num("tier2.cost-dragons-breath", 1))
                .build());

        // TIER 3 - GOD MODE
        addTier(TokenTier.of(3)
                .extraHearts(num("tier3.extra-hearts", 20))
                .potion(strength(), 2)
                .potion(speed(), 1)
                .flight(true)
                .passiveDescription("+20 Extra Hearts, Strength III, Speed II, Permanent Flight")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SERVER_JUDGMENT, "Server Judgment",
                        num("tier3.ability-cooldown", 30))
                        .damage(num("tier3.ability-damage", 35))
                        .knockback(dnum("tier3.ability-knockback", 20))
                        .description("Sonic judgment: 35 true damage, 20-block knockback, ignores armor"))
                .task(TokenTier.TaskType.WARDEN_BAREHAND, null, 1,
                        "Defeat a Warden barehanded with zero armor equipped")
                .cost(Material.DRAGON_EGG, num("tier3.cost-dragon-eggs", 1))
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-blocks", 4))
                .cost(Material.ENCHANTED_GOLDEN_APPLE, num("tier3.cost-god-apples", 1))
                .build());
    }
}
