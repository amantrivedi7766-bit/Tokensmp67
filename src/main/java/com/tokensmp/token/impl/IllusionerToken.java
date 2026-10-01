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
 * Illusioner Token (Epic): deception identity. T1 Phantom Arrow, T2 Mirror Volley, T3 Reality Fracture.
 */
public final class IllusionerToken extends AbstractToken {

    public IllusionerToken(ConfigManager config) {
        super("illusioner", "Illusioner", TokenRarity.EPIC, Material.BOW, false, config);

        addTier(TokenTier.of(1)
                .potion(nightVision(), 0)
                .passiveDescription("Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PHANTOM_ARROW, "Phantom Arrow",
                        anum(1, "cooldown", 24))
                        .damage(anum(1, "damage", 13))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.5))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 28))
                        .particles(aparticles(1, Particle.WITCH, Particle.CRIT, Particle.END_ROD))
                        .sounds(asounds(1, Sound.ENTITY_ARROW_SHOOT, Sound.ENTITY_ILLUSIONER_CAST_SPELL))
                        .description("A precise illusion projectile that briefly splits: 13 damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ILLUSIONER),
                        num("tier1.task-count", 50), "Kill 50 Illusioners")
                .cost(Material.ARROW, num("tier1.cost-arrow", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Night Vision, Speed I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MIRROR_VOLLEY, "Mirror Volley",
                        anum(2, "cooldown", 40))
                        .damage(anum(2, "damage", 27))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "projectiles", 3))
                        .speed(adnum(2, "speed", 1.1))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 34))
                        .particles(aparticles(2, Particle.WITCH, Particle.END_ROD, Particle.CRIT))
                        .sounds(asounds(2, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, Sound.ENTITY_ILLUSIONER_CAST_SPELL))
                        .description("Illusion projectiles attack from different angles: 27 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ILLUSIONER),
                        num("tier2.task-count", 150), "Kill 150 Illusioners")
                .cost(Material.SPECTRAL_ARROW, num("tier2.cost-spectral-arrow", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(nightVision(), 0)
                .potion(speed(), 1)
                .passiveDescription("Night Vision, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.REALITY_FRACTURE, "Reality Fracture",
                        anum(3, "cooldown", 62))
                        .damage(anum(3, "damage", 46))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 2.0))
                        .count(anum(3, "projectiles", 4))
                        .speed(adnum(3, "speed", 1.2))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 46))
                        .particles(aparticles(3, Particle.WITCH, Particle.REVERSE_PORTAL, Particle.ENCHANTED_HIT))
                        .sounds(asounds(3, Sound.ENTITY_ILLUSIONER_CAST_SPELL, Sound.BLOCK_GLASS_BREAK))
                        .description("Blades strike a fractured target from every angle: 46 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ILLUSIONER),
                        num("tier3.task-count", 400), "Kill 400 Illusioners")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
