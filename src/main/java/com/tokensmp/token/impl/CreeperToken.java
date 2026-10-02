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
 * Creeper Token (Rare): explosive identity. Each tier has its own keybind and
 * its own mechanic:
 *
 * T1 "TNT Cannon"   (RIGHT CLICK)      - launch a TNT shot toward the cursor.
 * T2 "TNT Strike"   (SHIFT + LEFT)     - call a slow 5x5 TNT block down onto the
 *                                        cursor area: massive damage, terrain
 *                                        blown out and then restored.
 * T3 "Bomb Chickens"(SHIFT + RIGHT)    - spawn 5 bomb-headed chickens that hunt
 *                                        nearby players and detonate after 5s.
 */
public final class CreeperToken extends AbstractToken {

    public CreeperToken(ConfigManager config) {
        super("creeper", "Creeper", TokenRarity.RARE, Material.CREEPER_HEAD, false, config);

        addTier(TokenTier.of(1)
                .explosionImmunity(0.5)
                .passiveDescription("50% Blast Damage Protection")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TNT_CANNON, "TNT Cannon",
                        anum(1, "cooldown", 24))
                        .trigger(atrigger(1, AbilityTrigger.RIGHT_CLICK))
                        .damage(anum(1, "damage", 16))
                        .range(adnum(1, "range", 22.0))
                        .radius(adnum(1, "radius", 3.0))
                        .knockback(adnum(1, "knockback", 1.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 34))
                        .particles(aparticles(1, Particle.EXPLOSION, Particle.SMOKE, Particle.FLAME))
                        .sounds(asounds(1, Sound.ENTITY_CREEPER_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Right click: launch a TNT cannon shot toward your cursor (16 damage blast)"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier1.task-count", 300), "Kill 300 Creepers")
                .cost(Material.GUNPOWDER, num("tier1.cost-gunpowder", 64))
                .build());

        addTier(TokenTier.of(2)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TNT_STRIKE, "TNT Strike",
                        anum(2, "cooldown", 45))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_LEFT_CLICK))
                        .damage(anum(2, "damage", 42))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 7.0))
                        .knockback(adnum(2, "knockback", 2.5))
                        .count(anum(2, "projectiles", 5))
                        .speed(adnum(2, "speed", 0.35))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.EXPLOSION, Particle.LARGE_SMOKE,
                                Particle.SMOKE, Particle.CLOUD))
                        .sounds(asounds(2, Sound.ENTITY_TNT_PRIMED, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Shift + left click: drop a slow 5x5 TNT block on the cursor area "
                                + "(42 damage, ground blown out then restored)"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier2.task-count", 800), "Kill 800 Creepers")
                .cost(Material.TNT, num("tier2.cost-tnt", 16))
                .build());

        addTier(TokenTier.of(3)
                .explosionImmunity(1.0)
                .passiveDescription("100% Explosion Immunity")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOMB_CHICKENS, "Bomb Chickens",
                        anum(3, "cooldown", 60))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 50))
                        .range(adnum(3, "range", 20.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 1.8))
                        .count(anum(3, "projectiles", 5))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "fuse-seconds", 5.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 44))
                        .particles(aparticles(3, Particle.EXPLOSION, Particle.SMOKE, Particle.FIREWORK))
                        .sounds(asounds(3, Sound.ENTITY_CHICKEN_AMBIENT, Sound.ENTITY_CREEPER_PRIMED,
                                Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Shift + right click: spawn 5 bomb-headed chickens that hunt nearby "
                                + "players and detonate after 5s (50 damage total)"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.CREEPER),
                        num("tier3.task-count", 2000), "Kill 2,000 Creepers")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
