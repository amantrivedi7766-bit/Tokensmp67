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
 * Enderman Token (Epic): void displacement identity. T1 Void Pierce, T2 Void Rift, T3 Ender Collapse.
 */
public final class EndermanToken extends AbstractToken {

    public EndermanToken(ConfigManager config) {
        super("enderman", "Enderman", TokenRarity.EPIC, Material.ENDER_PEARL, false, config);

        addTier(TokenTier.of(1)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .passiveDescription("Void Affinity (no Ender Pearl damage), Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_PIERCE, "Void Pierce",
                        anum(1, "cooldown", 25))
                        .damage(anum(1, "damage", 12))
                        .range(adnum(1, "range", 18.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.6))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.4))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.PORTAL, Particle.DRAGON_BREATH, Particle.REVERSE_PORTAL))
                        .sounds(asounds(1, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.BLOCK_PORTAL_TRIGGER))
                        .description("Fire a razor-thin void spear: 12 damage piercing up to 3 enemies across 18 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier1.task-count", 400), "Kill 400 Endermen")
                .cost(Material.ENDER_EYE, num("tier1.cost-ender-eye", 16))
                .build());

        addTier(TokenTier.of(2)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Void Affinity, Night Vision, Speed I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOID_RIFT, "Void Rift",
                        anum(2, "cooldown", 38))
                        .damage(anum(2, "damage", 22))
                        .range(adnum(2, "range", 22.0))
                        .radius(adnum(2, "radius", 2.5))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.PORTAL, Particle.DRAGON_BREATH, Particle.END_ROD))
                        .sounds(asounds(2, Sound.BLOCK_PORTAL_TRAVEL, Sound.ENTITY_ENDERMAN_TELEPORT))
                        .description("Tear a purple rift along the ground toward the target: 22 damage + upward knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMITE),
                        num("tier2.task-count", 1000), "Kill 1,000 Endermites")
                .cost(Material.ENDER_PEARL, num("tier2.cost-ender-pearl", 32))
                .build());

        addTier(TokenTier.of(3)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 1)
                .passiveDescription("Void Affinity, Night Vision, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ENDER_COLLAPSE, "Ender Collapse",
                        anum(3, "cooldown", 55))
                        .damage(anum(3, "damage", 38))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.PORTAL, Particle.DRAGON_BREATH, Particle.END_ROD, Particle.REVERSE_PORTAL))
                        .sounds(asounds(3, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.BLOCK_PORTAL_TRIGGER, Sound.ENTITY_ENDERMAN_TELEPORT))
                        .description("Collapse an unstable void sphere: pulls enemies in for 1.5s then implodes for 38 damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier3.task-count", 2500), "Kill 2,500 Endermen")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
