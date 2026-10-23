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
 * Founder Token (Legendary).
 * Passive: Infinite Creative Flight, Absolute Damage Immunity, God Mode Health.
 * Ability: World Rewrite - Locks a 50-block radius, cages the target in bedrock and mutes their abilities.
 * RESERVED: never appears in spins or player-facing GUIs.
 */
public final class FounderToken extends AbstractToken {

    public FounderToken(ConfigManager config) {
        super("founder", "Founder", TokenRarity.LEGENDARY, com.tokensmp.core.VersionCompatibility.material("NETHER_STAR", Material.STONE), true, config);

        addTier(TokenTier.of(1)
                .flight(true)
                .potion(resistance(), 4)
                .extraHearts(180)
                .passiveDescription("Infinite Creative Flight, Absolute Damage Immunity, God Mode Health")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WORLD_REWRITE, "World Rewrite",
                        anum(1, "cooldown", 120))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 50.0))
                        .radius(adnum(1, "radius", 25.0))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 8.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 80))
                        .particles(aparticles(1, "ENCHANT", "END_ROD", "TOTEM_OF_UNDYING", "HAPPY_VILLAGER"))
                        .sounds(asounds(1, "ENTITY_WITHER_SPAWN", "BLOCK_BEACON_ACTIVATE"))
                        .description("Locks a 50-block radius, cages the target in bedrock and mutes their abilities"))
                .task(TokenTier.TaskType.KILLS, mobs("ENDER_DRAGON"),
                        num("tier1.task-count", 1), "Kill 1 Founders")
                .cost(Material.NETHER_STAR, num("tier1.cost-nether-star", 1))
                .build());

        addTier(TokenTier.of(2)
                .flight(true)
                .potion(resistance(), 4)
                .extraHearts(180)
                .passiveDescription("Infinite Creative Flight, Absolute Damage Immunity, God Mode Health")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WORLD_REWRITE, "World Rewrite",
                        anum(2, "cooldown", 90))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 35))
                        .range(adnum(2, "range", 50.0))
                        .radius(adnum(2, "radius", 35.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 12.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 80))
                        .particles(aparticles(2, "ENCHANT", "END_ROD", "TOTEM_OF_UNDYING", "HAPPY_VILLAGER"))
                        .sounds(asounds(2, "ENTITY_WITHER_SPAWN", "BLOCK_BEACON_ACTIVATE"))
                        .description("Locks a 50-block radius, cages the target in bedrock and mutes their abilities"))
                .task(TokenTier.TaskType.KILLS, mobs("ENDER_DRAGON"),
                        num("tier2.task-count", 1), "Kill 1 Founders")
                .cost(Material.NETHER_STAR, num("tier2.cost-nether-star", 2))
                .build());

        addTier(TokenTier.of(3)
                .flight(true)
                .potion(resistance(), 4)
                .extraHearts(180)
                .passiveDescription("Infinite Creative Flight, Absolute Damage Immunity, God Mode Health")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WORLD_REWRITE, "World Rewrite",
                        anum(3, "cooldown", 60))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 50))
                        .range(adnum(3, "range", 50.0))
                        .radius(adnum(3, "radius", 50.0))
                        .knockback(adnum(3, "knockback", 1.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 16.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 80))
                        .particles(aparticles(3, "ENCHANT", "END_ROD", "TOTEM_OF_UNDYING", "HAPPY_VILLAGER"))
                        .sounds(asounds(3, "ENTITY_WITHER_SPAWN", "BLOCK_BEACON_ACTIVATE"))
                        .description("Locks a 50-block radius, cages the target in bedrock and mutes their abilities"))
                .task(TokenTier.TaskType.KILLS, mobs("ENDER_DRAGON"),
                        num("tier3.task-count", 1), "Kill 1 Founders")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-block", 8))
                .build());

    }
}
