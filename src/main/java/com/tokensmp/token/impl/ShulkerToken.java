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
 * Shulker Token (Epic).
 * Passive: Armour Toughness +80%, 100% knockback resistance.
 * Ability: Levitation Matrix - Guided bullet that levitates targets high into the air.
 */
public final class ShulkerToken extends AbstractToken {

    public ShulkerToken(ConfigManager config) {
        super("shulker", "Shulker", TokenRarity.EPIC, com.tokensmp.core.VersionCompatibility.material("SHULKER_SHELL", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(resistance(), 1)
                .knockbackResistance(1.0)
                .passiveDescription("Armour Toughness +80%, 100% knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.LEVITATION_MATRIX, "Levitation Matrix",
                        anum(1, "cooldown", 35))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 14))
                        .range(adnum(1, "range", 24.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 0.4))
                        .count(anum(1, "count", 4))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "END_ROD", "PORTAL"))
                        .sounds(asounds(1, "ENTITY_SHULKER_OPEN", "ENTITY_SHULKER_BULLET_HIT"))
                        .description("Guided bullet that levitates targets high into the air"))
                .task(TokenTier.TaskType.KILLS, mobs("SHULKER"),
                        num("tier1.task-count", 100), "Kill 100 Shulkers")
                .cost(Material.SHULKER_SHELL, num("tier1.cost-shulker-shell", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(resistance(), 1)
                .knockbackResistance(1.0)
                .passiveDescription("Armour Toughness +80%, 100% knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.LEVITATION_MATRIX, "Levitation Matrix",
                        anum(2, "cooldown", 30))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 24))
                        .range(adnum(2, "range", 28.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 0.4))
                        .count(anum(2, "count", 6))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "END_ROD", "PORTAL"))
                        .sounds(asounds(2, "ENTITY_SHULKER_OPEN", "ENTITY_SHULKER_BULLET_HIT"))
                        .description("Guided bullet that levitates targets high into the air"))
                .task(TokenTier.TaskType.KILLS, mobs("SHULKER"),
                        num("tier2.task-count", 300), "Kill 300 Shulkers")
                .cost(Material.SHULKER_SHELL, num("tier2.cost-shulker-shell", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(resistance(), 1)
                .knockbackResistance(1.0)
                .passiveDescription("Armour Toughness +80%, 100% knockback resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.LEVITATION_MATRIX, "Levitation Matrix",
                        anum(3, "cooldown", 25))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 36))
                        .range(adnum(3, "range", 32.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 0.4))
                        .count(anum(3, "count", 9))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "END_ROD", "PORTAL"))
                        .sounds(asounds(3, "ENTITY_SHULKER_OPEN", "ENTITY_SHULKER_BULLET_HIT"))
                        .description("Guided bullet that levitates targets high into the air"))
                .task(TokenTier.TaskType.KILLS, mobs("SHULKER"),
                        num("tier3.task-count", 800), "Kill 800 Shulkers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
