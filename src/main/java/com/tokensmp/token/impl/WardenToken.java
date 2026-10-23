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
 * Warden Token (Legendary).
 * Passive: Darkness Immunity, sonar detection (nearby players glow).
 * Ability: Sonic Shriek - Penetrating sonic beam - knockback + heavy damage through blocks.
 */
public final class WardenToken extends AbstractToken {

    public WardenToken(ConfigManager config) {
        super("warden", "Warden", TokenRarity.LEGENDARY, com.tokensmp.core.VersionCompatibility.material("SCULK", Material.STONE), false, config);

        addTier(TokenTier.of(1)
                .darknessImmunity(true)
                .sonarGlow(true)
                .passiveDescription("Darkness Immunity, sonar detection (nearby players glow)")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_SHRIEK, "Sonic Shriek",
                        anum(1, "cooldown", 25))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 15.0))
                        .radius(adnum(1, "radius", 1.4))
                        .knockback(adnum(1, "knockback", 2.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, "SONIC_BOOM", "SCULK_SOUL"))
                        .sounds(asounds(1, "ENTITY_WARDEN_ROAR", "ENTITY_WARDEN_SONIC_BOOM"))
                        .description("Penetrating sonic beam - knockback + heavy damage through blocks"))
                .task(TokenTier.TaskType.KILLS, mobs("WARDEN"),
                        num("tier1.task-count", 25), "Kill 25 Wardens")
                .cost(Material.ECHO_SHARD, num("tier1.cost-echo-shard", 16))
                .build());

        addTier(TokenTier.of(2)
                .darknessImmunity(true)
                .sonarGlow(true)
                .passiveDescription("Darkness Immunity, sonar detection (nearby players glow)")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_SHRIEK, "Sonic Shriek",
                        anum(2, "cooldown", 22))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 30))
                        .range(adnum(2, "range", 22.0))
                        .radius(adnum(2, "radius", 1.4))
                        .knockback(adnum(2, "knockback", 2.5))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, "SONIC_BOOM", "SCULK_SOUL"))
                        .sounds(asounds(2, "ENTITY_WARDEN_ROAR", "ENTITY_WARDEN_SONIC_BOOM"))
                        .description("Penetrating sonic beam - knockback + heavy damage through blocks"))
                .task(TokenTier.TaskType.KILLS, mobs("WARDEN"),
                        num("tier2.task-count", 60), "Kill 60 Wardens")
                .cost(Material.ECHO_SHARD, num("tier2.cost-echo-shard", 32))
                .build());

        addTier(TokenTier.of(3)
                .darknessImmunity(true)
                .sonarGlow(true)
                .passiveDescription("Darkness Immunity, sonar detection (nearby players glow)")
                .ability(new TokenTier.AbilitySpec(TokenAbility.SONIC_SHRIEK, "Sonic Shriek",
                        anum(3, "cooldown", 20))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 45))
                        .range(adnum(3, "range", 30.0))
                        .radius(adnum(3, "radius", 1.4))
                        .knockback(adnum(3, "knockback", 3.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, "SONIC_BOOM", "SCULK_SOUL"))
                        .sounds(asounds(3, "ENTITY_WARDEN_ROAR", "ENTITY_WARDEN_SONIC_BOOM"))
                        .description("Penetrating sonic beam - knockback + heavy damage through blocks"))
                .task(TokenTier.TaskType.KILLS, mobs("WARDEN"),
                        num("tier3.task-count", 100), "Kill 100 Wardens")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
