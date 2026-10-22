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
 * Wither Skeleton Token (Rare).
 * Passive: Complete Wither Immunity, melee attacks apply Wither.
 * Ability: Dark Presence - Black pulse that withers nearby enemies and drains their health.
 */
public final class WitherSkeletonToken extends AbstractToken {

    public WitherSkeletonToken(ConfigManager config) {
        super("wither_skeleton", "Wither Skeleton", TokenRarity.RARE, Material.WITHER_SKELETON_SKULL, false, config);

        addTier(TokenTier.of(1)
                .witherImmunity(true)
                .witherAura(true)
                .passiveDescription("Complete Wither Immunity, melee attacks apply Wither")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DARK_PRESENCE, "Dark Presence",
                        anum(1, "cooldown", 28))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 18))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 5.0))
                        .knockback(adnum(1, "knockback", 1.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.SOUL, Particle.SMOKE, Particle.ASH, Particle.SOUL_FIRE_FLAME))
                        .sounds(asounds(1, Sound.ENTITY_WITHER_SKELETON_AMBIENT, Sound.ENTITY_WITHER_SHOOT))
                        .description("Black pulse that withers nearby enemies and drains their health"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER_SKELETON),
                        num("tier1.task-count", 400), "Kill 400 Wither Skeletons")
                .cost(Material.COAL, num("tier1.cost-coal", 64))
                .build());

        addTier(TokenTier.of(2)
                .witherImmunity(true)
                .witherAura(true)
                .passiveDescription("Complete Wither Immunity, melee attacks apply Wither")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DARK_PRESENCE, "Dark Presence",
                        anum(2, "cooldown", 24))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 28))
                        .range(adnum(2, "range", 7.0))
                        .radius(adnum(2, "radius", 6.0))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.SOUL, Particle.SMOKE, Particle.ASH, Particle.SOUL_FIRE_FLAME))
                        .sounds(asounds(2, Sound.ENTITY_WITHER_SKELETON_AMBIENT, Sound.ENTITY_WITHER_SHOOT))
                        .description("Black pulse that withers nearby enemies and drains their health"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER_SKELETON),
                        num("tier2.task-count", 1000), "Kill 1000 Wither Skeletons")
                .cost(Material.NETHERITE_SCRAP, num("tier2.cost-netherite-scrap", 2))
                .build());

        addTier(TokenTier.of(3)
                .witherImmunity(true)
                .witherAura(true)
                .passiveDescription("Complete Wither Immunity, melee attacks apply Wither")
                .ability(new TokenTier.AbilitySpec(TokenAbility.DARK_PRESENCE, "Dark Presence",
                        anum(3, "cooldown", 20))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 40))
                        .range(adnum(3, "range", 8.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 1.4))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.SOUL, Particle.SMOKE, Particle.ASH, Particle.SOUL_FIRE_FLAME))
                        .sounds(asounds(3, Sound.ENTITY_WITHER_SKELETON_AMBIENT, Sound.ENTITY_WITHER_SHOOT))
                        .description("Black pulse that withers nearby enemies and drains their health"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WITHER_SKELETON),
                        num("tier3.task-count", 2500), "Kill 2500 Wither Skeletons")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
