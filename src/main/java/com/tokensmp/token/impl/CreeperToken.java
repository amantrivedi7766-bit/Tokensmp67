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
 * Creeper Token (Rare): blast identity. T1 Blast Fist, T2 Volatile Charge, T3 Cataclysm Detonation.
 */
public final class CreeperToken extends AbstractToken {

    public CreeperToken(ConfigManager config) {
        super("creeper", "Creeper", TokenRarity.RARE, Material.CREEPER_HEAD, false, config);

        addTier(TokenTier.of(1)
                .explosionImmunity(0.5)
                .passiveDescription("50% Blast Damage Protection")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BLAST_FIST, "Blast Fist",
                        anum(1, "cooldown", 24))
                        .damage(anum(1, "damage", 14))
                        .range(adnum(1, "range", 4.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.EXPLOSION, Particle.SMOKE, Particle.CLOUD))
                        .sounds(asounds(1, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Compressed explosive shock-punch: 14 damage in a short cone with knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier1.task-count", 300), "Kill 300 Creepers")
                .cost(Material.GUNPOWDER, num("tier1.cost-gunpowder", 64))
                .build());

        addTier(TokenTier.of(2)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOLATILE_CHARGE, "Volatile Charge",
                        anum(2, "cooldown", 40))
                        .damage(anum(2, "damage", 25))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.5))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.FLAME, Particle.SMOKE, Particle.EXPLOSION))
                        .sounds(asounds(2, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Launch a glowing explosive charge: 25 damage blast on impact"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier2.task-count", 800), "Kill 800 Creepers")
                .cost(Material.TNT, num("tier2.cost-tnt", 16))
                .build());

        addTier(TokenTier.of(3)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.CATACLYSM_DETONATION, "Cataclysm Detonation",
                        anum(3, "cooldown", 60))
                        .damage(anum(3, "damage", 45))
                        .range(adnum(3, "range", 20.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.EXPLOSION, Particle.SMOKE, Particle.CLOUD, Particle.LARGE_SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Massive controlled explosion at the target: 45 damage in a 7-block radius"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier3.task-count", 2000), "Kill 2,000 Creepers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
