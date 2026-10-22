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
 * Wolf Token (Rare).
 * Passive: Haste II, Strength I pack aura.
 * Ability: Pack Leader - Summons three ghostly wolves that hunt your targets.
 */
public final class WolfToken extends AbstractToken {

    public WolfToken(ConfigManager config) {
        super("wolf", "Wolf", TokenRarity.RARE, Material.WOLF_SPAWN_EGG, false, config);

        addTier(TokenTier.of(1)
                .potion(haste(), 1)
                .potion(strength(), 0)
                .passiveDescription("Haste II, Strength I pack aura")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PACK_LEADER, "Pack Leader",
                        anum(1, "cooldown", 35))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 16))
                        .range(adnum(1, "range", 12.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 3))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 15.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.SOUL, Particle.HEART))
                        .sounds(asounds(1, Sound.ENTITY_WOLF_HOWL, Sound.ENTITY_WOLF_GROWL))
                        .description("Summons three ghostly wolves that hunt your targets"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WOLF),
                        num("tier1.task-count", 400), "Kill 400 Wolfs")
                .cost(Material.BONE, num("tier1.cost-bone", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(haste(), 1)
                .potion(strength(), 0)
                .passiveDescription("Haste II, Strength I pack aura")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PACK_LEADER, "Pack Leader",
                        anum(2, "cooldown", 30))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 26))
                        .range(adnum(2, "range", 14.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 3))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 20.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.SOUL, Particle.HEART))
                        .sounds(asounds(2, Sound.ENTITY_WOLF_HOWL, Sound.ENTITY_WOLF_GROWL))
                        .description("Summons three ghostly wolves that hunt your targets"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WOLF),
                        num("tier2.task-count", 1000), "Kill 1000 Wolfs")
                .cost(Material.BONE_BLOCK, num("tier2.cost-bone-block", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(haste(), 1)
                .potion(strength(), 0)
                .passiveDescription("Haste II, Strength I pack aura")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PACK_LEADER, "Pack Leader",
                        anum(3, "cooldown", 25))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 38))
                        .range(adnum(3, "range", 16.0))
                        .radius(adnum(3, "radius", 3.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 3))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 25.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.SOUL, Particle.HEART))
                        .sounds(asounds(3, Sound.ENTITY_WOLF_HOWL, Sound.ENTITY_WOLF_GROWL))
                        .description("Summons three ghostly wolves that hunt your targets"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.WOLF),
                        num("tier3.task-count", 2500), "Kill 2500 Wolfs")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
