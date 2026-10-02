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
 * Enderman Token (Epic): void displacement identity. Each tier has its own
 * keybind and its own mechanic:
 *
 * T1 "Blink Chain"   (RIGHT CLICK)      - blink through 5 positions, 1s apart.
 * T2 "Portal Link"   (SHIFT + LEFT)     - open two linked portals for 60s; walk
 *                                         into one to come out of the other.
 * T3 "Ender Assembly"(SHIFT + RIGHT)    - spiral animation + a player-head menu
 *                                         to teleport any player to any player.
 */
public final class EndermanToken extends AbstractToken {

    public EndermanToken(ConfigManager config) {
        super("enderman", "Enderman", TokenRarity.EPIC, Material.ENDER_PEARL, false, config);

        addTier(TokenTier.of(1)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .passiveDescription("Void Affinity (no Ender Pearl damage), Night Vision")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BLINK_CHAIN, "Blink Chain",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 12.0))
                        .radius(adnum(1, "radius", 10.0))
                        .knockback(adnum(1, "knockback", 0.4))
                        .count(anum(1, "blinks", 5))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.PORTAL, Particle.DRAGON_BREATH,
                                Particle.REVERSE_PORTAL))
                        .sounds(asounds(1, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.BLOCK_PORTAL_TRIGGER))
                        .description("Right click: blink through 5 positions one second apart "
                                + "(20 void damage in total)"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier1.task-count", 400), "Kill 400 Endermen")
                .cost(Material.ENDER_EYE, num("tier1.cost-ender-eye", 16))
                .build());

        addTier(TokenTier.of(2)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 0)
                .passiveDescription("Void Affinity, Night Vision, Speed I")
                .ability(new TokenTier.AbilitySpec(TokenAbility.PORTAL_LINK, "Portal Link",
                        anum(2, "cooldown", 70))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_LEFT_CLICK))
                        .damage(anum(2, "damage", 30))
                        .range(adnum(2, "range", 24.0))
                        .radius(adnum(2, "radius", 4.0))
                        .knockback(adnum(2, "knockback", 1.4))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.PORTAL, Particle.REVERSE_PORTAL,
                                Particle.DRAGON_BREATH))
                        .sounds(asounds(2, Sound.BLOCK_PORTAL_TRIGGER, Sound.BLOCK_PORTAL_TRAVEL))
                        .description("Shift + left click: open the first portal, then the second one "
                                + "where you want to travel - they stay linked for 60s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMITE),
                        num("tier2.task-count", 1000), "Kill 1,000 Endermites")
                .cost(Material.ENDER_PEARL, num("tier2.cost-ender-pearl", 32))
                .build());

        addTier(TokenTier.of(3)
                .pearlDamageImmunity(true)
                .potion(nightVision(), 0)
                .potion(speed(), 1)
                .passiveDescription("Void Affinity, Night Vision, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ENDER_ASSEMBLY, "Ender Assembly",
                        anum(3, "cooldown", 90))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 40))
                        .range(adnum(3, "range", 20.0))
                        .radius(adnum(3, "radius", 4.0))
                        .knockback(adnum(3, "knockback", 1.6))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.PORTAL, Particle.REVERSE_PORTAL,
                                Particle.END_ROD, Particle.DRAGON_BREATH))
                        .sounds(asounds(3, Sound.ENTITY_ENDERMAN_TELEPORT, Sound.BLOCK_PORTAL_TRAVEL))
                        .description("Shift + right click: a void spiral opens a player-head menu - "
                                + "pick who to teleport, then pick their destination"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.ENDERMAN),
                        num("tier3.task-count", 2500), "Kill 2,500 Endermen")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
