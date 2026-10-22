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
 * Enderman Token (Epic).
 * Passive: Projectile Dodge Chance, Ender Pearl damage immunity.
 * Ability: Void Shift - Instant line-of-sight teleport up to 100 blocks.
 */
public final class EndermanToken extends AbstractToken {

    public EndermanToken(ConfigManager config) {
        super("enderman", "Enderman", TokenRarity.EPIC, Material.ENDER_PEARL, false, config);

        addTier(TokenTier.of(1)
                .projectileDodge(0.25)
                .pearlDamageImmunity(true)
                .passiveDescription("Projectile Dodge Chance, Ender Pearl damage immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_SHIFT, "Void Shift",
                        anum(1, "cooldown", 15))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 12))
                        .range(adnum(1, "range", 30.0))
                        .radius(adnum(1, "radius", 2.5))
                        .knockback(adnum(1, "knockback", 0.6))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.PORTAL, Particle.REVERSE_PORTAL, Particle.DRAGON_BREATH))
                        .sounds(asounds(1, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.ENTITY_ENDERMAN_SCREAM))
                        .description("Instant line-of-sight teleport up to 100 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier1.task-count", 400), "Kill 400 Endermans")
                .cost(Material.ENDER_EYE, num("tier1.cost-ender-eye", 16))
                .build());

        addTier(TokenTier.of(2)
                .projectileDodge(0.25)
                .pearlDamageImmunity(true)
                .passiveDescription("Projectile Dodge Chance, Ender Pearl damage immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_SHIFT, "Void Shift",
                        anum(2, "cooldown", 12))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 20))
                        .range(adnum(2, "range", 60.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 0.8))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.PORTAL, Particle.REVERSE_PORTAL, Particle.DRAGON_BREATH))
                        .sounds(asounds(2, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.ENTITY_ENDERMAN_SCREAM))
                        .description("Instant line-of-sight teleport up to 100 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier2.task-count", 1000), "Kill 1000 Endermans")
                .cost(Material.ENDER_PEARL, num("tier2.cost-ender-pearl", 32))
                .build());

        addTier(TokenTier.of(3)
                .projectileDodge(0.25)
                .pearlDamageImmunity(true)
                .passiveDescription("Projectile Dodge Chance, Ender Pearl damage immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_SHIFT, "Void Shift",
                        anum(3, "cooldown", 10))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 30))
                        .range(adnum(3, "range", 100.0))
                        .radius(adnum(3, "radius", 3.5))
                        .knockback(adnum(3, "knockback", 1.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.PORTAL, Particle.REVERSE_PORTAL, Particle.DRAGON_BREATH))
                        .sounds(asounds(3, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.ENTITY_ENDERMAN_SCREAM))
                        .description("Instant line-of-sight teleport up to 100 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier3.task-count", 2500), "Kill 2500 Endermans")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
