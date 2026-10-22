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
 * Snow Golem Token (Common).
 * Passive: Speed I, Resistance II.
 * Ability: Frozen Heart - Rapid freeze snowballs that lock enemies in powder-snow.
 */
public final class SnowGolemToken extends AbstractToken {

    public SnowGolemToken(ConfigManager config) {
        super("snow_golem", "Snow Golem", TokenRarity.COMMON, Material.SNOWBALL, false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 0)
                .potion(resistance(), 1)
                .passiveDescription("Speed I, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FROZEN_HEART, "Frozen Heart",
                        anum(1, "cooldown", 22))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 16))
                        .range(adnum(1, "range", 16.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.4))
                        .count(anum(1, "count", 4))
                        .speed(adnum(1, "speed", 1.2))
                        .duration(adnum(1, "duration", 2.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.SNOWFLAKE, Particle.ITEM_SNOWBALL))
                        .sounds(asounds(1, Sound.BLOCK_SNOW_BREAK, Sound.BLOCK_POWDER_SNOW_STEP))
                        .description("Rapid freeze snowballs that lock enemies in powder-snow"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SNOW_GOLEM),
                        num("tier1.task-count", 300), "Kill 300 Snow Golems")
                .cost(Material.SNOWBALL, num("tier1.cost-snowball", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 0)
                .potion(resistance(), 1)
                .passiveDescription("Speed I, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FROZEN_HEART, "Frozen Heart",
                        anum(2, "cooldown", 20))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 26))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 2.0))
                        .knockback(adnum(2, "knockback", 0.4))
                        .count(anum(2, "count", 6))
                        .speed(adnum(2, "speed", 1.2))
                        .duration(adnum(2, "duration", 3.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.SNOWFLAKE, Particle.ITEM_SNOWBALL))
                        .sounds(asounds(2, Sound.BLOCK_SNOW_BREAK, Sound.BLOCK_POWDER_SNOW_STEP))
                        .description("Rapid freeze snowballs that lock enemies in powder-snow"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SNOW_GOLEM),
                        num("tier2.task-count", 800), "Kill 800 Snow Golems")
                .cost(Material.SNOW_BLOCK, num("tier2.cost-snow-block", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 0)
                .potion(resistance(), 1)
                .passiveDescription("Speed I, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.FROZEN_HEART, "Frozen Heart",
                        anum(3, "cooldown", 18))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 38))
                        .range(adnum(3, "range", 24.0))
                        .radius(adnum(3, "radius", 2.0))
                        .knockback(adnum(3, "knockback", 0.4))
                        .count(anum(3, "count", 8))
                        .speed(adnum(3, "speed", 1.2))
                        .duration(adnum(3, "duration", 4.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.SNOWFLAKE, Particle.ITEM_SNOWBALL))
                        .sounds(asounds(3, Sound.BLOCK_SNOW_BREAK, Sound.BLOCK_POWDER_SNOW_STEP))
                        .description("Rapid freeze snowballs that lock enemies in powder-snow"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SNOW_GOLEM),
                        num("tier3.task-count", 2000), "Kill 2000 Snow Golems")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
