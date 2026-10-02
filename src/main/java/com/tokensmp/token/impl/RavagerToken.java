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
 * Ravager Token (Legendary): a heavy, pure-domination token. Crimson red and
 * burnt orange identity, slow but every hit shakes the ground.
 *
 * Passive (all tiers): Movement Speed -10%, Knockback Resistance +50% and a
 * light Ravager breathing sound every 10 seconds.
 *
 * T1 "Terrifying Roar"     (RIGHT CLICK)    - 6-block fear roar: Slowness IV,
 *                                            Weakness II, no jumping, knockback.
 * T2 "Bloodthirsty Stampede"(SHIFT + LEFT)  - 8-block unstoppable dash + slam.
 * T3 "Ravager's Wrath"     (SHIFT + RIGHT)  - 2s channel -> 12-block true-damage
 *                                            nova + Spectral Ravager ally.
 */
public final class RavagerToken extends AbstractToken {

    public RavagerToken(ConfigManager config) {
        super("ravager", "Ravager", TokenRarity.LEGENDARY, Material.RAVAGER_SPAWN_EGG, false, config);

        addTier(TokenTier.of(1)
                .movementSpeedPenalty(-0.10)
                .knockbackResistance(0.5)
                .ravagerBreathing(true)
                .passiveDescription("Movement Speed -10%, Knockback Resistance +50%, heavy breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.RAVAGER_ROAR, "Terrifying Roar",
                        anum(1, "cooldown", 8))
                        .trigger(atrigger(1, AbilityTrigger.RIGHT_CLICK))
                        .damage(anum(1, "damage", 0))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 6.0))
                        .knockback(adnum(1, "knockback", 2.0))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "fear-seconds", 3.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.SOUL, Particle.FLAME, Particle.CLOUD,
                                Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_WARDEN_SONIC_BOOM,
                                Sound.ENTITY_RAVAGER_STEP))
                        .description("Right click: stomp and roar - enemies in 6 blocks freeze for 3s "
                                + "(Slowness IV, Weakness II, no jumping). Pure crowd control."))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier1.task-count", 25), "Kill 25 Ravagers")
                .cost(Material.SADDLE, num("tier1.cost-saddle", 16))
                .build());

        addTier(TokenTier.of(2)
                .movementSpeedPenalty(-0.10)
                .knockbackResistance(0.5)
                .ravagerBreathing(true)
                .passiveDescription("Movement Speed -10%, Knockback Resistance +50%, heavy breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.RAVAGER_STAMPEDE, "Bloodthirsty Stampede",
                        anum(2, "cooldown", 15))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_LEFT_CLICK))
                        .damage(anum(2, "damage", 12))
                        .range(adnum(2, "range", 8.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 2.5))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 0.45))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 34))
                        .particles(aparticles(2, Particle.CLOUD, Particle.LARGE_SMOKE, Particle.CRIT))
                        .sounds(asounds(2, Sound.ENTITY_RAVAGER_STEP, Sound.ENTITY_RAVAGER_ATTACK,
                                Sound.BLOCK_ANVIL_LAND))
                        .description("Shift + left click: unstoppable 8-block dash (knockback + fall immune) "
                                + "that tramples enemies, then a ground slam"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier2.task-count", 75), "Kill 75 Ravagers")
                .cost(Material.SADDLE, num("tier2.cost-saddle", 32))
                .cost(Material.DIAMOND, num("tier2.cost-diamond", 16))
                .build());

        addTier(TokenTier.of(3)
                .movementSpeedPenalty(-0.10)
                .knockbackResistance(0.5)
                .ravagerBreathing(true)
                .passiveDescription("Movement Speed -10%, Knockback Resistance +50%, heavy breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.RAVAGERS_WRATH, "Ravager's Wrath",
                        anum(3, "cooldown", 45))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 16))
                        .range(adnum(3, "range", 12.0))
                        .radius(adnum(3, "radius", 12.0))
                        .knockback(adnum(3, "knockback", 4.0))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "channel-seconds", 2.0))
                        .trueDamage(abool(3, "true-damage", true))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.FLAME, Particle.SOUL, Particle.CLOUD,
                                Particle.SMOKE))
                        .sounds(asounds(3, Sound.BLOCK_ANVIL_LAND, Sound.ENTITY_GENERIC_EXPLODE,
                                Sound.ENTITY_RAVAGER_ROAR))
                        .description("Shift + right click: 2s channel into a 12-block true-damage nova "
                                + "(8 hearts, launch, 1.5s stun) and summon a Spectral Ravager"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.RAVAGER),
                        num("tier3.task-count", 200), "Kill 200 Ravagers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .cost(Material.SADDLE, num("tier3.cost-saddle", 64))
                .build());
    }
}
