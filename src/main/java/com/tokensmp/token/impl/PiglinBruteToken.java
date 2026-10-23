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
 * Piglin Brute Token (Epic).
 * Passive: Gold armour defence, Axe damage +60%, Speed II.
 * Ability: Golden Retribution - Berserk: doubled speed and axe strikes that shatter shields.
 */
public final class PiglinBruteToken extends AbstractToken {

    public PiglinBruteToken(ConfigManager config) {
        super("piglin_brute", "Piglin Brute", TokenRarity.EPIC, com.tokensmp.core.VersionCompatibility.material("GOLDEN_AXE", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(resistance(), 0)
                .potion(strength(), 1)
                .potion(speed(), 1)
                .passiveDescription("Gold armour defence, Axe damage +60%, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GOLDEN_RETRIBUTION, "Golden Retribution",
                        anum(1, "cooldown", 40))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 18))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 6.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 8.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "WAX_ON", "CRIT", "ANGRY_VILLAGER"))
                        .sounds(asounds(1, "ENTITY_PIGLIN_BRUTE_ANGRY", "ENTITY_PIGLIN_ANGRY"))
                        .description("Berserk: doubled speed and axe strikes that shatter shields"))
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN_BRUTE"),
                        num("tier1.task-count", 300), "Kill 300 Piglin Brutes")
                .cost(Material.GOLD_INGOT, num("tier1.cost-gold-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(resistance(), 0)
                .potion(strength(), 1)
                .potion(speed(), 1)
                .passiveDescription("Gold armour defence, Axe damage +60%, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GOLDEN_RETRIBUTION, "Golden Retribution",
                        anum(2, "cooldown", 34))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 30))
                        .range(adnum(2, "range", 8.0))
                        .radius(adnum(2, "radius", 8.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 8.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "WAX_ON", "CRIT", "ANGRY_VILLAGER"))
                        .sounds(asounds(2, "ENTITY_PIGLIN_BRUTE_ANGRY", "ENTITY_PIGLIN_ANGRY"))
                        .description("Berserk: doubled speed and axe strikes that shatter shields"))
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN_BRUTE"),
                        num("tier2.task-count", 800), "Kill 800 Piglin Brutes")
                .cost(Material.GOLD_BLOCK, num("tier2.cost-gold-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(resistance(), 0)
                .potion(strength(), 1)
                .potion(speed(), 1)
                .passiveDescription("Gold armour defence, Axe damage +60%, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GOLDEN_RETRIBUTION, "Golden Retribution",
                        anum(3, "cooldown", 28))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 44))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 10.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 8.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "WAX_ON", "CRIT", "ANGRY_VILLAGER"))
                        .sounds(asounds(3, "ENTITY_PIGLIN_BRUTE_ANGRY", "ENTITY_PIGLIN_ANGRY"))
                        .description("Berserk: doubled speed and axe strikes that shatter shields"))
                .task(TokenTier.TaskType.KILLS, mobs("PIGLIN_BRUTE"),
                        num("tier3.task-count", 2000), "Kill 2000 Piglin Brutes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
