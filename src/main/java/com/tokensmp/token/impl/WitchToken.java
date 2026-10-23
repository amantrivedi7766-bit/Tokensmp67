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
 * Witch Token (Rare).
 * Passive: Poison damage resistance, instant potion drinking.
 * Ability: Brew Master - Defensive tier-II potion splash + random debuffs on attackers.
 */
public final class WitchToken extends AbstractToken {

    public WitchToken(ConfigManager config) {
        super("witch", "Witch", TokenRarity.RARE, com.tokensmp.core.VersionCompatibility.material("GLASS_BOTTLE", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .poisonImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Poison damage resistance, instant potion drinking")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BREW_MASTER, "Brew Master",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 12))
                        .range(adnum(1, "range", 8.0))
                        .radius(adnum(1, "radius", 8.0))
                        .knockback(adnum(1, "knockback", 0.6))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 6.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "WITCH", "ENCHANT", "HEART"))
                        .sounds(asounds(1, "ENTITY_WITCH_AMBIENT", "ENTITY_WITCH_CELEBRATE"))
                        .description("Defensive tier-II potion splash + random debuffs on attackers"))
                .task(TokenTier.TaskType.KILLS, mobs("WITCH"),
                        num("tier1.task-count", 300), "Kill 300 Witchs")
                .cost(Material.REDSTONE, num("tier1.cost-redstone", 64))
                .build());

        addTier(TokenTier.of(2)
                .poisonImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Poison damage resistance, instant potion drinking")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BREW_MASTER, "Brew Master",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 20))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 10.0))
                        .knockback(adnum(2, "knockback", 0.8))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 8.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "WITCH", "ENCHANT", "HEART"))
                        .sounds(asounds(2, "ENTITY_WITCH_AMBIENT", "ENTITY_WITCH_CELEBRATE"))
                        .description("Defensive tier-II potion splash + random debuffs on attackers"))
                .task(TokenTier.TaskType.KILLS, mobs("WITCH"),
                        num("tier2.task-count", 800), "Kill 800 Witchs")
                .cost(Material.GLOWSTONE_DUST, num("tier2.cost-glowstone-dust", 64))
                .build());

        addTier(TokenTier.of(3)
                .poisonImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Poison damage resistance, instant potion drinking")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BREW_MASTER, "Brew Master",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 30))
                        .range(adnum(3, "range", 12.0))
                        .radius(adnum(3, "radius", 12.0))
                        .knockback(adnum(3, "knockback", 1.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 10.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "WITCH", "ENCHANT", "HEART"))
                        .sounds(asounds(3, "ENTITY_WITCH_AMBIENT", "ENTITY_WITCH_CELEBRATE"))
                        .description("Defensive tier-II potion splash + random debuffs on attackers"))
                .task(TokenTier.TaskType.KILLS, mobs("WITCH"),
                        num("tier3.task-count", 2000), "Kill 2000 Witchs")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
