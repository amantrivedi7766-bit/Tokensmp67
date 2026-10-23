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
 * Drowned Token (Rare).
 * Passive: Conduit Power V, Impaling bonus, Deep Night Vision.
 * Ability: Trident Tempest - Riptide thunderstorm + whirlpool that zeroes target mobility.
 */
public final class DrownedToken extends AbstractToken {

    public DrownedToken(ConfigManager config) {
        super("drowned", "Drowned", TokenRarity.RARE, com.tokensmp.core.VersionCompatibility.material("TRIDENT", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .potion(dolphinsGrace(), 2)
                .potion(nightVision(), 1)
                .potion(waterBreathing(), 0)
                .passiveDescription("Conduit Power V, Impaling bonus, Deep Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TRIDENT_TEMPEST, "Trident Tempest",
                        anum(1, "cooldown", 35))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 24))
                        .range(adnum(1, "range", 20.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "SPLASH", "BUBBLE", "ELECTRIC_SPARK", "BUBBLE_POP"))
                        .sounds(asounds(1, "ITEM_TRIDENT_THUNDER", "ENTITY_DROWNED_AMBIENT_WATER"))
                        .description("Riptide thunderstorm + whirlpool that zeroes target mobility"))
                .task(TokenTier.TaskType.KILLS, mobs("DROWNED"),
                        num("tier1.task-count", 400), "Kill 400 Drowneds")
                .cost(Material.PRISMARINE_SHARD, num("tier1.cost-prismarine-shard", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(dolphinsGrace(), 2)
                .potion(nightVision(), 1)
                .potion(waterBreathing(), 0)
                .passiveDescription("Conduit Power V, Impaling bonus, Deep Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TRIDENT_TEMPEST, "Trident Tempest",
                        anum(2, "cooldown", 30))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 38))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "SPLASH", "BUBBLE", "ELECTRIC_SPARK", "BUBBLE_POP"))
                        .sounds(asounds(2, "ITEM_TRIDENT_THUNDER", "ENTITY_DROWNED_AMBIENT_WATER"))
                        .description("Riptide thunderstorm + whirlpool that zeroes target mobility"))
                .task(TokenTier.TaskType.KILLS, mobs("DROWNED"),
                        num("tier2.task-count", 1000), "Kill 1000 Drowneds")
                .cost(Material.NAUTILUS_SHELL, num("tier2.cost-nautilus-shell", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(dolphinsGrace(), 2)
                .potion(nightVision(), 1)
                .potion(waterBreathing(), 0)
                .passiveDescription("Conduit Power V, Impaling bonus, Deep Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TRIDENT_TEMPEST, "Trident Tempest",
                        anum(3, "cooldown", 25))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 56))
                        .range(adnum(3, "range", 28.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "SPLASH", "BUBBLE", "ELECTRIC_SPARK", "BUBBLE_POP"))
                        .sounds(asounds(3, "ITEM_TRIDENT_THUNDER", "ENTITY_DROWNED_AMBIENT_WATER"))
                        .description("Riptide thunderstorm + whirlpool that zeroes target mobility"))
                .task(TokenTier.TaskType.KILLS, mobs("DROWNED"),
                        num("tier3.task-count", 2500), "Kill 2500 Drowneds")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
