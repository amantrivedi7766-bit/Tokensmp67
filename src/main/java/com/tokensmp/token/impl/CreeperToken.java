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
 * Creeper Token (Rare).
 * Passive: Explosion Immunity, Fire Resistance, Resistance III.
 * Ability: Mega Detonator - Lethal 5-7 block explosion that never hurts the owner.
 */
public final class CreeperToken extends AbstractToken {

    public CreeperToken(ConfigManager config) {
        super("creeper", "Creeper", TokenRarity.RARE, Material.CREEPER_HEAD, false, config);

        addTier(TokenTier.of(1)
                .explosionImmunity(1.0)
                .potion(fireResistance(), 0)
                .potion(resistance(), 2)
                .passiveDescription("Explosion Immunity, Fire Resistance, Resistance III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MEGA_DETONATOR, "Mega Detonator",
                        anum(1, "cooldown", 25))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 30))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 5.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 60))
                        .particles(aparticles(1, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.CLOUD))
                        .sounds(asounds(1, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Lethal 5-7 block explosion that never hurts the owner"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier1.task-count", 300), "Kill 300 Creepers")
                .cost(Material.GUNPOWDER, num("tier1.cost-gunpowder", 64))
                .build());

        addTier(TokenTier.of(2)
                .explosionImmunity(1.0)
                .potion(fireResistance(), 0)
                .potion(resistance(), 2)
                .passiveDescription("Explosion Immunity, Fire Resistance, Resistance III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MEGA_DETONATOR, "Mega Detonator",
                        anum(2, "cooldown", 22))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 45))
                        .range(adnum(2, "range", 6.0))
                        .radius(adnum(2, "radius", 6.0))
                        .knockback(adnum(2, "knockback", 2.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 60))
                        .particles(aparticles(2, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.CLOUD))
                        .sounds(asounds(2, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Lethal 5-7 block explosion that never hurts the owner"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier2.task-count", 800), "Kill 800 Creepers")
                .cost(Material.TNT, num("tier2.cost-tnt", 16))
                .build());

        addTier(TokenTier.of(3)
                .explosionImmunity(1.0)
                .potion(fireResistance(), 0)
                .potion(resistance(), 2)
                .passiveDescription("Explosion Immunity, Fire Resistance, Resistance III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MEGA_DETONATOR, "Mega Detonator",
                        anum(3, "cooldown", 20))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 65))
                        .range(adnum(3, "range", 6.0))
                        .radius(adnum(3, "radius", 7.0))
                        .knockback(adnum(3, "knockback", 2.5))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.EXPLOSION_EMITTER, Particle.FLAME, Particle.SMOKE, Particle.CLOUD))
                        .sounds(asounds(3, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Lethal 5-7 block explosion that never hurts the owner"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier3.task-count", 2000), "Kill 2000 Creepers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
