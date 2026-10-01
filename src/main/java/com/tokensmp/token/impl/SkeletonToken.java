package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
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
 * Skeleton Token (Common): archery identity. T1 Bone Bolt, T2 Ricochet Shot, T3 Deadeye Barrage.
 */
public final class SkeletonToken extends AbstractToken {

    public SkeletonToken(ConfigManager config) {
        super("skeleton", "Skeleton", TokenRarity.COMMON, Material.SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .bowDamageBonus(0.15)
                .passiveDescription("+15% Bow Damage")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BONE_BOLT, "Bone Bolt",
                        anum(1, "cooldown", 20))
                        .damage(anum(1, "damage", 11))
                        .range(adnum(1, "range", 18.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.4))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.6))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 26))
                        .particles(aparticles(1, Particle.CRIT, Particle.WHITE_ASH, Particle.SNOWFLAKE))
                        .sounds(asounds(1, Sound.ENTITY_ARROW_SHOOT))
                        .description("Fire a spectral bone projectile: 11 damage across 18 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier1.task-count", 800), "Kill 800 Skeletons")
                .cost(Material.BONE, num("tier1.cost-bone", 64))
                .build());

        addTier(TokenTier.of(2)
                .bowDamageBonus(0.3)
                .potion(speed(), 0)
                .passiveDescription("+30% Bow Damage, Speed I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.RICOCHET_SHOT, "Ricochet Shot",
                        anum(2, "cooldown", 35))
                        .damage(anum(2, "damage", 20))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 2.0))
                        .knockback(adnum(2, "knockback", 0.5))
                        .count(anum(2, "projectiles", 2))
                        .speed(adnum(2, "speed", 1.3))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 30))
                        .particles(aparticles(2, Particle.CRIT, Particle.SNOWFLAKE))
                        .sounds(asounds(2, Sound.ENTITY_ARROW_SHOOT, Sound.ENTITY_ARROW_HIT))
                        .description("A shot that ricochets to a second target: 10 damage per hit, up to 2 targets"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier2.task-count", 2000), "Kill 2,000 Skeletons")
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 16))
                .build());

        addTier(TokenTier.of(3)
                .bowDamageBonus(0.5)
                .potion(speed(), 1)
                .potion(strength(), 0)
                .arrowTrail(true)
                .passiveDescription("+50% Bow/Crossbow Damage, Speed II, Strength I, Arrow Trails")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DEADEYE_BARRAGE, "Deadeye Barrage",
                        anum(3, "cooldown", 52))
                        .damage(anum(3, "damage", 34))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 3.0))
                        .knockback(adnum(3, "knockback", 0.6))
                        .count(anum(3, "projectiles", 4))
                        .speed(adnum(3, "speed", 1.2))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 34))
                        .particles(aparticles(3, Particle.CRIT, Particle.SNOWFLAKE, Particle.END_ROD))
                        .sounds(asounds(3, Sound.ENTITY_ARROW_SHOOT, Sound.ENTITY_ARROW_SHOOT))
                        .description("Cinematic precision volley: 4 arrows on separate trajectories, 34 damage total"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SKELETON),
                        num("tier3.task-count", 4500), "Kill 4,500 Skeletons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
