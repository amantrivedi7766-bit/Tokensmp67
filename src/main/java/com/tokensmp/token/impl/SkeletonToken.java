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
 * Skeleton Token (Common).
 * Passive: +50% Bow Damage, Speed I, Strength II.
 * Ability: Archer's Focus - 360 degree arrow rain around the player.
 */
public final class SkeletonToken extends AbstractToken {

    public SkeletonToken(ConfigManager config) {
        super("skeleton", "Skeleton", TokenRarity.COMMON, Material.SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .bowDamageBonus(0.5)
                .potion(speed(), 0)
                .potion(strength(), 1)
                .passiveDescription("+50% Bow Damage, Speed I, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ARCHERS_FOCUS, "Archer's Focus",
                        anum(1, "cooldown", 25))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 24))
                        .range(adnum(1, "range", 16.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.3))
                        .count(anum(1, "count", 8))
                        .speed(adnum(1, "speed", 1.4))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.CRIT, Particle.SNOWFLAKE))
                        .sounds(asounds(1, Sound.ENTITY_SKELETON_SHOOT, Sound.ENTITY_ARROW_HIT_PLAYER))
                        .description("360 degree arrow rain around the player"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier1.task-count", 800), "Kill 800 Skeletons")
                .cost(Material.BONE, num("tier1.cost-bone", 64))
                .build());

        addTier(TokenTier.of(2)
                .bowDamageBonus(0.5)
                .potion(speed(), 0)
                .potion(strength(), 1)
                .passiveDescription("+50% Bow Damage, Speed I, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ARCHERS_FOCUS, "Archer's Focus",
                        anum(2, "cooldown", 22))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 40))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 2.0))
                        .knockback(adnum(2, "knockback", 0.3))
                        .count(anum(2, "count", 12))
                        .speed(adnum(2, "speed", 1.4))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 30))
                        .particles(aparticles(2, Particle.CRIT, Particle.SNOWFLAKE))
                        .sounds(asounds(2, Sound.ENTITY_SKELETON_SHOOT, Sound.ENTITY_ARROW_HIT_PLAYER))
                        .description("360 degree arrow rain around the player"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier2.task-count", 2000), "Kill 2000 Skeletons")
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 16))
                .build());

        addTier(TokenTier.of(3)
                .bowDamageBonus(0.5)
                .potion(speed(), 0)
                .potion(strength(), 1)
                .passiveDescription("+50% Bow Damage, Speed I, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ARCHERS_FOCUS, "Archer's Focus",
                        anum(3, "cooldown", 20))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 60))
                        .range(adnum(3, "range", 24.0))
                        .radius(adnum(3, "radius", 2.0))
                        .knockback(adnum(3, "knockback", 0.3))
                        .count(anum(3, "count", 16))
                        .speed(adnum(3, "speed", 1.4))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 30))
                        .particles(aparticles(3, Particle.CRIT, Particle.SNOWFLAKE))
                        .sounds(asounds(3, Sound.ENTITY_SKELETON_SHOOT, Sound.ENTITY_ARROW_HIT_PLAYER))
                        .description("360 degree arrow rain around the player"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier3.task-count", 4500), "Kill 4500 Skeletons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
