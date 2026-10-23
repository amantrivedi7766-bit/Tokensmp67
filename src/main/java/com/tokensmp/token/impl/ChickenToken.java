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
 * Chicken Token (Common).
 * Passive: 100% Fall Damage Immunity.
 * Ability: Feather Fall - Sky launch + 25s slow-fall glide, then a wind knockback on landing.
 */
public final class ChickenToken extends AbstractToken {

    public ChickenToken(ConfigManager config) {
        super("chicken", "Chicken", TokenRarity.COMMON, com.tokensmp.core.VersionCompatibility.material("FEATHER", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .fallImmunity(true)
                .passiveDescription("100% Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FEATHER_FALL, "Feather Fall",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 14))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.2))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 25.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, "CLOUD", "SNOWFLAKE"))
                        .sounds(asounds(1, "ENTITY_CHICKEN_AMBIENT", "ENTITY_PHANTOM_FLAP"))
                        .description("Sky launch + 25s slow-fall glide, then a wind knockback on landing"))
                .task(TokenTier.TaskType.KILLS, mobs("CHICKEN"),
                        num("tier1.task-count", 400), "Kill 400 Chickens")
                .cost(Material.FEATHER, num("tier1.cost-feather", 64))
                .build());

        addTier(TokenTier.of(2)
                .fallImmunity(true)
                .passiveDescription("100% Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FEATHER_FALL, "Feather Fall",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 22))
                        .range(adnum(2, "range", 6.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.6))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 25.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, "CLOUD", "SNOWFLAKE"))
                        .sounds(asounds(2, "ENTITY_CHICKEN_AMBIENT", "ENTITY_PHANTOM_FLAP"))
                        .description("Sky launch + 25s slow-fall glide, then a wind knockback on landing"))
                .task(TokenTier.TaskType.KILLS, mobs("CHICKEN"),
                        num("tier2.task-count", 1000), "Kill 1000 Chickens")
                .cost(Material.EGG, num("tier2.cost-egg", 64))
                .build());

        addTier(TokenTier.of(3)
                .fallImmunity(true)
                .passiveDescription("100% Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FEATHER_FALL, "Feather Fall",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 32))
                        .range(adnum(3, "range", 6.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 25.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, "CLOUD", "SNOWFLAKE"))
                        .sounds(asounds(3, "ENTITY_CHICKEN_AMBIENT", "ENTITY_PHANTOM_FLAP"))
                        .description("Sky launch + 25s slow-fall glide, then a wind knockback on landing"))
                .task(TokenTier.TaskType.KILLS, mobs("CHICKEN"),
                        num("tier3.task-count", 2500), "Kill 2500 Chickens")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
