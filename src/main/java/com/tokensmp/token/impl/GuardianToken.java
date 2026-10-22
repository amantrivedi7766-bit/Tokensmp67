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
 * Guardian Token (Rare).
 * Passive: Infinite Water Breathing, Dolphin's Grace, Night Vision, Regeneration.
 * Ability: Ocean's Grace - Water shield that deflects projectiles and reflects thorns damage.
 */
public final class GuardianToken extends AbstractToken {

    public GuardianToken(ConfigManager config) {
        super("guardian", "Guardian", TokenRarity.RARE, Material.PRISMARINE_SHARD, false, config);

        addTier(TokenTier.of(1)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 1)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Infinite Water Breathing, Dolphin's Grace, Night Vision, Regeneration")
                .ability(new TokenTier.AbilitySpec(TokenAbility.OCEANS_GRACE, "Ocean's Grace",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 10))
                        .range(adnum(1, "range", 8.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 0.8))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 6.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.BUBBLE, Particle.BUBBLE_POP, Particle.NAUTILUS, Particle.SPLASH))
                        .sounds(asounds(1, Sound.ENTITY_GUARDIAN_ATTACK, Sound.ENTITY_ELDER_GUARDIAN_CURSE))
                        .description("Water shield that deflects projectiles and reflects thorns damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GUARDIAN),
                        num("tier1.task-count", 300), "Kill 300 Guardians")
                .cost(Material.PRISMARINE_SHARD, num("tier1.cost-prismarine-shard", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 1)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Infinite Water Breathing, Dolphin's Grace, Night Vision, Regeneration")
                .ability(new TokenTier.AbilitySpec(TokenAbility.OCEANS_GRACE, "Ocean's Grace",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 16))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 4.0))
                        .knockback(adnum(2, "knockback", 1.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 8.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.BUBBLE, Particle.BUBBLE_POP, Particle.NAUTILUS, Particle.SPLASH))
                        .sounds(asounds(2, Sound.ENTITY_GUARDIAN_ATTACK, Sound.ENTITY_ELDER_GUARDIAN_CURSE))
                        .description("Water shield that deflects projectiles and reflects thorns damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GUARDIAN),
                        num("tier2.task-count", 800), "Kill 800 Guardians")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier2.cost-prismarine-crystals", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(waterBreathing(), 0)
                .potion(dolphinsGrace(), 1)
                .potion(nightVision(), 0)
                .potion(regeneration(), 0)
                .passiveDescription("Infinite Water Breathing, Dolphin's Grace, Night Vision, Regeneration")
                .ability(new TokenTier.AbilitySpec(TokenAbility.OCEANS_GRACE, "Ocean's Grace",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 24))
                        .range(adnum(3, "range", 12.0))
                        .radius(adnum(3, "radius", 5.0))
                        .knockback(adnum(3, "knockback", 1.2))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 10.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.BUBBLE, Particle.BUBBLE_POP, Particle.NAUTILUS, Particle.SPLASH))
                        .sounds(asounds(3, Sound.ENTITY_GUARDIAN_ATTACK, Sound.ENTITY_ELDER_GUARDIAN_CURSE))
                        .description("Water shield that deflects projectiles and reflects thorns damage"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.GUARDIAN),
                        num("tier3.task-count", 2000), "Kill 2000 Guardians")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
