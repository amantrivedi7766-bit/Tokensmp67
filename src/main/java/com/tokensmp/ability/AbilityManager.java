package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.MessageManager;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenTier;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The ability activation entry point: validates every request through the
 * full chain (ownership, claim state, tier, availability, cooldown, freeze)
 * and delegates execution to the {@link AbilityEngine}, which implements all
 * 45 player abilities plus the isolated Admin abilities.
 *
 * Server data is authoritative - the held item's lore is never trusted.
 */
public final class AbilityManager {

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final CooldownManager cooldowns;
    private final MessageManager messages;
    private final FreezeManager freezeManager;
    private final AbilityAnimationEngine animations;
    private final AbilityEngine engine;

    public AbilityManager(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                          CooldownManager cooldowns, MessageManager messages,
                          FreezeManager freezeManager, AbilityAnimationEngine animations) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.cooldowns = cooldowns;
        this.messages = messages;
        this.freezeManager = freezeManager;
        this.animations = animations;
        this.engine = new AbilityEngine(plugin, plugin.scheduler(), freezeManager);
    }

    // ------------------------------------------------------------------
    // Activation entry point (SHIFT + RIGHT CLICK)
    // ------------------------------------------------------------------

    /** Attempts to activate the player's ACTIVE token ability. */
    public void activate(Player player) {
        // 1. Does the player have an active token at all?
        String activeId = data.getActiveToken(player);
        if (activeId == null) {
            messages.actionBar(player, plugin.config().getString("messages.no-active-token",
                    "&7No active token - claim one via &f/tokens&7!"));
            return;
        }
        Token token = registry.get(activeId);
        // 2/3. Is the token valid and claimed?
        if (token == null || !data.isClaimed(player, activeId)) {
            return;
        }
        // 4. Correct tier with an ability?
        int tier = data.getTier(player, activeId);
        TokenTier tierDef = token.tier(tier);
        if (tierDef == null || tierDef.getAbility() == null) {
            messages.actionBar(player, plugin.config().getString("messages.no-ability-at-tier",
                    "&7This tier has no active ability yet!"));
            return;
        }
        TokenTier.AbilitySpec ability = tierDef.getAbility();
        // 5. Is an ability available?
        if (ability.getType() == TokenAbility.NONE) {
            return;
        }
        // Frozen players cannot use abilities.
        if (freezeManager.isFrozen(player)) {
            SoundEngine.denied(player);
            return;
        }
        // 6. Cooldown check (server-side timestamp).
        long remaining = cooldowns.remainingMillis(player, activeId);
        if (remaining > 0L) {
            SoundEngine.denied(player);
            messages.actionBar(player, plugin.config().getString("messages.ability-denied",
                            "&c&l[!]&c Ability on cooldown! Wait {seconds}s")
                    .replace("{seconds}", String.valueOf((remaining + 999L) / 1000L)));
            return;
        }

        // Everything validated: start cooldown, run the ability, announce.
        cooldowns.start(player, activeId, ability.getCooldownSeconds());
        engine.execute(player, ability);
        SoundEngine.play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        messages.actionBar(player, plugin.config().getString("messages.ability-activated",
                "&a&l[!] &2{ability} Activated!").replace("{ability}", ability.getName()));
        cooldowns.showHud(player, activeId, ability.getCooldownSeconds());
    }
}
