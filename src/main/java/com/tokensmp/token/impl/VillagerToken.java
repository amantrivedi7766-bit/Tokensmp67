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
 * Villager Token (Common): emerald identity. T1 Emerald Lance, T2 Trade Breaker, T3 Emerald Judgment.
 */
public final class VillagerToken extends AbstractToken {

    public VillagerToken(ConfigManager config) {
        super("villager", "Villager", TokenRarity.COMMON, Material.EMERALD, false, config);

        addTier(TokenTier.of(1)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.EMERALD_LANCE, "Emerald Lance",
                        anum(1, "cooldown", 20))
                        .damage(anum(1, "damage", 10))
                        .range(adnum(1, "range", 18.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.5))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 26))
                        .particles(aparticles(1, Particle.CRIT, Particle.END_ROD))
                        .sounds(asounds(1, Sound.ENTITY_VILLAGER_TRADE, Sound.ENTITY_ARROW_HIT))
                        .description("Launch a compressed emerald spear: 10 damage, emerald shard burst"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier1.task-count", 200), "Kill 200 Villagers")
                .cost(Material.EMERALD, num("tier1.cost-emerald", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(regeneration(), 0)
                .passiveDescription("Regeneration I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TRADE_BREAKER, "Trade Breaker",
                        anum(2, "cooldown", 37))
                        .damage(anum(2, "damage", 22))
                        .range(adnum(2, "range", 20.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "projectiles", 3))
                        .speed(adnum(2, "speed", 1.1))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 34))
                        .particles(aparticles(2, Particle.END_ROD, Particle.CRIT))
                        .sounds(asounds(2, Sound.ENTITY_VILLAGER_TRADE, Sound.ENTITY_VILLAGER_TRADE))
                        .description("Three emerald blades strike from different angles: 22 total damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier2.task-count", 500), "Kill 500 Villagers")
                .cost(Material.EMERALD_BLOCK, num("tier2.cost-emerald-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(regeneration(), 1)
                .passiveDescription("Regeneration II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.EMERALD_JUDGMENT, "Emerald Judgment",
                        anum(3, "cooldown", 55))
                        .damage(anum(3, "damage", 39))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 1.8))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.2))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 44))
                        .particles(aparticles(3, Particle.END_ROD, Particle.ENCHANTED_HIT, Particle.CRIT))
                        .sounds(asounds(3, Sound.BLOCK_NOTE_BLOCK_BELL, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A huge emerald projectile charges for 1s then shatters: 39 damage + shards"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.VILLAGER),
                        num("tier3.task-count", 1200), "Kill 1,200 Villagers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
