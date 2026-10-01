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
 * Golem Token (Epic): iron juggernaut identity. T1 Iron Fist, T2 Ironquake, T3 Colossus Impact.
 */
public final class GolemToken extends AbstractToken {

    public GolemToken(ConfigManager config) {
        super("golem", "Golem", TokenRarity.EPIC, Material.IRON_BLOCK, false, config);

        addTier(TokenTier.of(1)
                .extraHearts(num("tier1.extra-hearts", 4))
                .potion(resistance(), 0)
                .passiveDescription("+4 Extra Hearts, Resistance I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.IRON_FIST, "Iron Fist",
                        anum(1, "cooldown", 28))
                        .damage(anum(1, "damage", 19))
                        .range(adnum(1, "range", 4.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 2.0))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 28))
                        .particles(aparticles(1, Particle.CRIT, Particle.BLOCK, Particle.CLOUD))
                        .sounds(asounds(1, Sound.ENTITY_IRON_GOLEM_ATTACK, Sound.BLOCK_ANVIL_LAND))
                        .description("A devastating close-range punch: 19 damage + heavy knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier1.task-count", 100), "Kill 100 Iron Golems")
                .cost(Material.IRON_INGOT, num("tier1.cost-iron-ingot", 64))
                .build());

        addTier(TokenTier.of(2)
                .extraHearts(num("tier2.extra-hearts", 8))
                .potion(resistance(), 1)
                .passiveDescription("+8 Extra Hearts, Resistance II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.IRONQUAKE, "Ironquake",
                        anum(2, "cooldown", 48))
                        .damage(anum(2, "damage", 33))
                        .range(adnum(2, "range", 18.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 2.2))
                        .count(anum(2, "projectiles", 2))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.BLOCK, Particle.CLOUD, Particle.EXPLOSION))
                        .sounds(asounds(2, Sound.BLOCK_ANVIL_LAND, Sound.BLOCK_STONE_BREAK))
                        .description("Punch the ground: two shockwave rings push enemies backward for 33 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier2.task-count", 300), "Kill 300 Iron Golems")
                .cost(Material.IRON_BLOCK, num("tier2.cost-iron-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .extraHearts(num("tier3.extra-hearts", 12))
                .potion(resistance(), 2)
                .passiveDescription("+12 Extra Hearts, Resistance III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.COLOSSUS_IMPACT, "Colossus Impact",
                        anum(3, "cooldown", 75))
                        .damage(anum(3, "damage", 58))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 9.0))
                        .knockback(adnum(3, "knockback", 3.5))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 52))
                        .particles(aparticles(3, Particle.BLOCK, Particle.CLOUD, Particle.CRIT, Particle.EXPLOSION))
                        .sounds(asounds(3, Sound.BLOCK_ANVIL_LAND, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("The ultimate slam: three enormous shockwaves for 58 damage + massive knockback"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.IRON_GOLEM),
                        num("tier3.task-count", 800), "Kill 800 Iron Golems")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
