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
 * Warden Token (Legendary): deep-dark sonic identity. T1 Sonic Jab, T2 Sonic Beam, T3 Sonic Rupture.
 */
public final class WardenToken extends AbstractToken {

    public WardenToken(ConfigManager config) {
        super("warden", "Warden", TokenRarity.LEGENDARY, Material.SCULK, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(num("tier1.extra-hearts", 5))
                .potion(nightVision(), 0)
                .passiveDescription("+5 Extra Hearts, Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_JAB, "Sonic Jab",
                        anum(1, "cooldown", 28))
                        .damage(anum(1, "damage", 18))
                        .range(adnum(1, "range", 12.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.SONIC_BOOM, Particle.SCULK_SOUL))
                        .sounds(asounds(1, Sound.ENTITY_WARDEN_SONIC_CHARGE, Sound.ENTITY_WARDEN_SONIC_BOOM))
                        .description("Short-range concentrated sonic blast: 18 damage in a narrow cone"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier1.task-count", 25), "Kill 25 Wardens")
                .cost(Material.ECHO_SHARD, num("tier1.cost-echo-shard", 16))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(num("tier2.extra-hearts", 10))
                .potion(nightVision(), 0)
                .potion(strength(), 0)
                .passiveDescription("+10 Extra Hearts, Night Vision, Strength I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_BEAM, "Sonic Beam",
                        anum(2, "cooldown", 45))
                        .damage(anum(2, "damage", 30))
                        .range(adnum(2, "range", 25.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 2.0))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.SONIC_BOOM, Particle.SCULK_CHARGE))
                        .sounds(asounds(2, Sound.ENTITY_WARDEN_SONIC_CHARGE, Sound.ENTITY_WARDEN_SONIC_BOOM))
                        .description("A focused sonic beam: 30 damage to every enemy along 25 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier2.task-count", 60), "Kill 60 Wardens")
                .cost(Material.ECHO_SHARD, num("tier2.cost-echo-shard", 32))
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 16))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(num("tier3.extra-hearts", 15))
                .potion(nightVision(), 0)
                .potion(strength(), 1)
                .passiveDescription("+15 Extra Hearts, Night Vision, Strength II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_RUPTURE, "Sonic Rupture",
                        anum(3, "cooldown", 70))
                        .damage(anum(3, "damage", 55))
                        .range(adnum(3, "range", 24.0))
                        .radius(adnum(3, "radius", 8.0))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.SONIC_BOOM, Particle.SCULK_SOUL, Particle.SONIC_BOOM))
                        .sounds(asounds(3, Sound.ENTITY_WARDEN_SONIC_BOOM, Sound.ENTITY_WARDEN_ROAR))
                        .description("A massive sonic wave expands from the target: 55 damage + heavy knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WARDEN),
                        num("tier3.task-count", 100), "Kill 100 Wardens")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .cost(Material.SCULK, num("tier3.cost-sculk", 64))
                .build());
    }
}
