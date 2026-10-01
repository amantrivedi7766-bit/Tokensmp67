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
 * Zombie Token (Common): undead bruiser identity. T1 Rotten Smash, T2 Grave Breaker, T3 Undead Cataclysm.
 */
public final class ZombieToken extends AbstractToken {

    public ZombieToken(ConfigManager config) {
        super("zombie", "Zombie", TokenRarity.COMMON, Material.ZOMBIE_HEAD, false, config);

        addTier(TokenTier.of(1)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .passiveDescription("Strength I, Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ROTTEN_SMASH, "Rotten Smash",
                        anum(1, "cooldown", 23))
                        .damage(anum(1, "damage", 12))
                        .range(adnum(1, "range", 3.5))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 26))
                        .particles(aparticles(1, Particle.DAMAGE_INDICATOR, Particle.CRIT, Particle.SOUL))
                        .sounds(asounds(1, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, Sound.ENTITY_GENERIC_HURT))
                        .description("Heavy close-range strike: 12 damage with a ground dust ring"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier1.task-count", 1000), "Kill 1,000 Zombies")
                .cost(Material.DIAMOND, num("tier1.cost-diamond", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(strength(), 0)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Strength I, Night Vision, Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.GRAVE_BREAKER, "Grave Breaker",
                        anum(2, "cooldown", 40))
                        .damage(anum(2, "damage", 23))
                        .range(adnum(2, "range", 18.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.SOUL, Particle.CRIT, Particle.BLOCK))
                        .sounds(asounds(2, Sound.BLOCK_GRAVEL_BREAK, Sound.ENTITY_ZOMBIE_AMBIENT))
                        .description("A ground fissure travels toward the target: 23 damage with a ground eruption"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier2.task-count", 2500), "Kill 2,500 Zombies")
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(strength(), 1)
                .potion(nightVision(), 0)
                .potion(regeneration(), 1)
                .passiveDescription("Strength II, Night Vision, Regeneration II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.UNDEAD_CATACLYSM, "Undead Cataclysm",
                        anum(3, "cooldown", 58))
                        .damage(anum(3, "damage", 40))
                        .range(adnum(3, "range", 20.0))
                        .radius(adnum(3, "radius", 8.0))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.SOUL, Particle.ENCHANTED_HIT, Particle.HAPPY_VILLAGER))
                        .sounds(asounds(3, Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A huge undead shockwave erupts around you: 40 damage + heavy knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ZOMBIE),
                        num("tier3.task-count", 5000), "Kill 5,000 Zombies")
                .cost(Material.NETHERITE_BLOCK, num("tier3.cost-netherite-block", 2))
                .build());
    }
}
