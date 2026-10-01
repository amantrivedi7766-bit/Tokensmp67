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
 * Slime Token (Common): bouncing identity. T1 Slime Slam, T2 Bouncy Crush, T3 Mega Slime Impact.
 */
public final class SlimeToken extends AbstractToken {

    public SlimeToken(ConfigManager config) {
        super("slime", "Slime", TokenRarity.COMMON, Material.SLIME_BALL, false, config);

        addTier(TokenTier.of(1)
                .fallImmunity(true)
                .passiveDescription("Fall Damage Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SLIME_SLAM, "Slime Slam",
                        anum(1, "cooldown", 24))
                        .damage(anum(1, "damage", 14))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.ITEM_SLIME, Particle.CLOUD))
                        .sounds(asounds(1, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_SLIME_SQUISH))
                        .description("Jump forward and crash down: 14 area damage in a green shockwave"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier1.task-count", 400), "Kill 400 Slimes")
                .cost(Material.SLIME_BALL, num("tier1.cost-slime-ball", 64))
                .build());

        addTier(TokenTier.of(2)
                .fallImmunity(true)
                .potion(jumpBoost(), 0)
                .passiveDescription("Fall Immunity, Jump Boost I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOUNCY_CRUSH, "Bouncy Crush",
                        anum(2, "cooldown", 42))
                        .damage(anum(2, "damage", 26))
                        .range(adnum(2, "range", 12.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "projectiles", 3))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 34))
                        .particles(aparticles(2, Particle.ITEM_SLIME, Particle.CLOUD))
                        .sounds(asounds(2, Sound.ENTITY_SLIME_SQUISH, Sound.ENTITY_SLIME_SQUISH))
                        .description("Bounce between up to three targets: 26 total damage across the landings"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier2.task-count", 1000), "Kill 1,000 Slimes")
                .cost(Material.SLIME_BLOCK, num("tier2.cost-slime-block", 16))
                .build());

        addTier(TokenTier.of(3)
                .fallImmunity(true)
                .potion(jumpBoost(), 1)
                .passiveDescription("Fall Immunity, Jump Boost II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MEGA_SLIME_IMPACT, "Mega Slime Impact",
                        anum(3, "cooldown", 60))
                        .damage(anum(3, "damage", 44))
                        .range(adnum(3, "range", 8.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 46))
                        .particles(aparticles(3, Particle.ITEM_SLIME, Particle.CLOUD, Particle.EXPLOSION))
                        .sounds(asounds(3, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Launch high then slam down: 44 damage with three green shockwaves"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier3.task-count", 2500), "Kill 2,500 Slimes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
