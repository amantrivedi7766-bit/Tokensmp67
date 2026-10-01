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
 * Wither Token (Legendary): dark barrage identity. T1 Wither Skull, T2 Triple Skull Volley, T3 Wither Barrage.
 */
public final class WitherToken extends AbstractToken {

    public WitherToken(ConfigManager config) {
        super("wither", "Wither", TokenRarity.LEGENDARY, Material.WITHER_SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .potion(resistance(), 0)
                .passiveDescription("Resistance I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WITHER_SKULL, "Wither Skull",
                        anum(1, "cooldown", 30))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 22.0))
                        .radius(adnum(1, "radius", 2.5))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.2))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.SMOKE, Particle.SOUL, Particle.ASH))
                        .sounds(asounds(1, Sound.ENTITY_WITHER_SHOOT))
                        .description("Launch a controlled wither skull: 20 damage, no status effect"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier1.task-count", 3), "Kill 3 Withers")
                .cost(Material.COAL, num("tier1.cost-coal", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(resistance(), 1)
                .passiveDescription("Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TRIPLE_SKULL_VOLLEY, "Triple Skull Volley",
                        anum(2, "cooldown", 45))
                        .damage(anum(2, "damage", 32))
                        .range(adnum(2, "range", 22.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.4))
                        .count(anum(2, "projectiles", 3))
                        .speed(adnum(2, "speed", 1.1))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.SMOKE, Particle.SOUL, Particle.ASH))
                        .sounds(asounds(2, Sound.ENTITY_WITHER_SHOOT, Sound.ENTITY_WITHER_SHOOT))
                        .description("Three skulls on separate trajectories: 32 total damage in three impacts"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier2.task-count", 10), "Kill 10 Withers")
                .cost(Material.NETHERITE_INGOT, num("tier2.cost-netherite-ingot", 2))
                .build());

        addTier(TokenTier.of(3)
                .potion(resistance(), 1)
                .potion(strength(), 0)
                .passiveDescription("Resistance II, Strength I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.WITHER_BARRAGE, "Wither Barrage",
                        anum(3, "cooldown", 72))
                        .damage(anum(3, "damage", 60))
                        .range(adnum(3, "range", 24.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "projectiles", 8))
                        .speed(adnum(3, "speed", 1.1))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 52))
                        .particles(aparticles(3, Particle.SOUL, Particle.SMOKE, Particle.ASH, Particle.EXPLOSION))
                        .sounds(asounds(3, Sound.ENTITY_WITHER_AMBIENT, Sound.ENTITY_WITHER_SHOOT, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A rotating spiral barrage of skulls converges: 60 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER),
                        num("tier3.task-count", 25), "Kill 25 Withers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .cost(Material.NETHER_STAR, num("tier3.cost-nether-star", 1))
                .build());
    }
}
