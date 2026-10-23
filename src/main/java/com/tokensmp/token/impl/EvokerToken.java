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
 * Evoker Token (Epic).
 * Passive: Secondary Totem of Undying, magic resistance.
 * Ability: Totem Summoner - Fang circle that pierces armour and shields.
 */
public final class EvokerToken extends AbstractToken {

    public EvokerToken(ConfigManager config) {
        super("evoker", "Evoker", TokenRarity.EPIC, com.tokensmp.core.VersionCompatibility.material("TOTEM_OF_UNDYING", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(resistance(), 0)
                .passiveDescription("Secondary Totem of Undying, magic resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TOTEM_SUMMONER, "Totem Summoner",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 16))
                        .range(adnum(1, "range", 14.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.4))
                        .count(anum(1, "count", 8))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "SQUID_INK", "SOUL_FIRE_FLAME"))
                        .sounds(asounds(1, "ENTITY_EVOKER_PREPARE_ATTACK", "ENTITY_EVOKER_FANGS_ATTACK"))
                        .description("Fang circle that pierces armour and shields"))
                .task(TokenTier.TaskType.KILLS, mobs("EVOKER"),
                        num("tier1.task-count", 50), "Kill 50 Evokers")
                .cost(Material.EMERALD, num("tier1.cost-emerald", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(resistance(), 0)
                .passiveDescription("Secondary Totem of Undying, magic resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TOTEM_SUMMONER, "Totem Summoner",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 28))
                        .range(adnum(2, "range", 16.0))
                        .radius(adnum(2, "radius", 3.5))
                        .knockback(adnum(2, "knockback", 0.4))
                        .count(anum(2, "count", 10))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "SQUID_INK", "SOUL_FIRE_FLAME"))
                        .sounds(asounds(2, "ENTITY_EVOKER_PREPARE_ATTACK", "ENTITY_EVOKER_FANGS_ATTACK"))
                        .description("Fang circle that pierces armour and shields"))
                .task(TokenTier.TaskType.KILLS, mobs("EVOKER"),
                        num("tier2.task-count", 150), "Kill 150 Evokers")
                .cost(Material.TOTEM_OF_UNDYING, num("tier2.cost-totem-of-undying", 1))
                .build());

        addTier(TokenTier.of(3)
                .potion(resistance(), 0)
                .passiveDescription("Secondary Totem of Undying, magic resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TOTEM_SUMMONER, "Totem Summoner",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 42))
                        .range(adnum(3, "range", 18.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 0.4))
                        .count(anum(3, "count", 12))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "SQUID_INK", "SOUL_FIRE_FLAME"))
                        .sounds(asounds(3, "ENTITY_EVOKER_PREPARE_ATTACK", "ENTITY_EVOKER_FANGS_ATTACK"))
                        .description("Fang circle that pierces armour and shields"))
                .task(TokenTier.TaskType.KILLS, mobs("EVOKER"),
                        num("tier3.task-count", 400), "Kill 400 Evokers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
