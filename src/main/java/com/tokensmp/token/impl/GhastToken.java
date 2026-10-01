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
 * Ghast Token (Rare): nether firepower identity. T1 Ghast Orb, T2 Inferno Comet, T3 Netherfall.
 */
public final class GhastToken extends AbstractToken {

    public GhastToken(ConfigManager config) {
        super("ghast", "Ghast", TokenRarity.RARE, Material.GHAST_TEAR, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GHAST_ORB, "Ghast Orb",
                        anum(1, "cooldown", 25))
                        .damage(anum(1, "damage", 15))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 2.5))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 32))
                        .particles(aparticles(1, Particle.FLAME, Particle.SMOKE, Particle.LAVA))
                        .sounds(asounds(1, Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Launch a compact explosive orb: 15 damage fireball-style blast"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier1.task-count", 250), "Kill 250 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier1.cost-ghast-tear", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INFERNO_COMET, "Inferno Comet",
                        anum(2, "cooldown", 42))
                        .damage(anum(2, "damage", 27))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 3.5))
                        .knockback(adnum(2, "knockback", 1.5))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 0.9))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.FLAME, Particle.LAVA, Particle.SOUL_FIRE_FLAME))
                        .sounds(asounds(2, Sound.ENTITY_GHAST_SHOOT, Sound.ENTITY_GHAST_SCREAM))
                        .description("A large flaming comet arcs through the air: 27 damage on impact"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier2.task-count", 600), "Kill 600 Ghasts")
                .cost(Material.GHAST_TEAR, num("tier2.cost-ghast-tear", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.NETHERFALL, "Netherfall",
                        anum(3, "cooldown", 65))
                        .damage(anum(3, "damage", 48))
                        .range(adnum(3, "range", 26.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 2.0))
                        .count(anum(3, "projectiles", 5))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.FLAME, Particle.SMOKE, Particle.LAVA, Particle.EXPLOSION))
                        .sounds(asounds(3, Sound.ENTITY_GHAST_SCREAM, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Mark an area: 5 explosive projectiles fall from above for 48 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GHAST),
                        num("tier3.task-count", 1500), "Kill 1,500 Ghasts")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
