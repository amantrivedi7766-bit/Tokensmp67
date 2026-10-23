package com.tokensmp.ability;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundEngine;
import com.tokensmp.core.MessageManager;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.AbilityTrigger;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenTier;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The ability activation entry point: validates every request through the
 * full chain (ownership, claim state, tier, availability, trigger, cooldown,
 * freeze) and delegates execution to the {@link AbilityEngine}, which
 * implements all 45 player abilities plus the isolated Admin abilities.
 *
 * Every ability declares its own trigger, so different tiers of a token can
 * use different keybinds (e.g. the Creeper uses right click, shift + left
 * click and shift + right click across its three tiers).
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
    // Trigger lookup
    // ------------------------------------------------------------------

    /** The trigger of the player's active tier ability, or null when none is usable. */
    public AbilityTrigger activeTrigger(Player player) {
        TokenTier.AbilitySpec ability = activeAbility(player);
        return ability == null ? null : ability.getTrigger();
    }

    /** The ability of the player's currently active tier, or null. */
    private TokenTier.AbilitySpec activeAbility(Player player) {
        String activeId = data.getActiveToken(player);
        if (activeId == null) {
            return null;
        }
        Token token = registry.get(activeId);
        if (token == null || !data.isClaimed(player, activeId)) {
            return null;
        }
        TokenTier tierDef = token.tier(data.getTier(player, activeId));
        if (tierDef == null || tierDef.getAbility() == null) {
            return null;
        }
        TokenTier.AbilitySpec ability = tierDef.getAbility();
        return ability.getType() == TokenAbility.NONE ? null : ability;
    }

    // ------------------------------------------------------------------
    // Activation entry points
    // ------------------------------------------------------------------

    /**
     * Attempts to activate the player's ACTIVE token ability for a given
     * trigger. Requests whose trigger does not match the ability's declared
     * trigger are ignored silently.
     */
    public void activate(Player player, AbilityTrigger trigger) {
        // 0. Founder's World Rewrite can mute a player's abilities entirely.
        if (engine.isMuted(player)) {
            messages.actionBar(player, plugin.config().getString("messages.abilities-muted",
                    "&4Your abilities are muted!"));
            return;
        }
        // 0b. Grace period may disable abilities entirely (configurable).
        if (plugin.grace().isActive()
                && plugin.config().getBoolean("grace-period.block-abilities", false)) {
            messages.actionBar(player, plugin.config().getString("messages.grace-blocks-ability",
                    "&7Abilities are disabled during the grace period!"));
            return;
        }
        // 1. Does the player have an active, claimed token with an ability?
        TokenTier.AbilitySpec ability = activeAbility(player);
        if (ability == null) {
            String activeId = data.getActiveToken(player);
            if (activeId == null) {
                messages.actionBar(player, plugin.config().getString("messages.no-active-token",
                        "&7No active token - claim one via &f/tokens&7!"));
            } else {
                messages.actionBar(player, plugin.config().getString("messages.no-ability-at-tier",
                        "&7This tier has no active ability yet!"));
            }
            return;
        }
        // 2. Does the ability use this keybind?
        if (ability.getTrigger() != trigger) {
            return;
        }
        // 3. The token item must be HELD in the main hand - that is how every
        //    token's ability is used, with no commands involved.
        String activeId = data.getActiveToken(player);
        String heldId = plugin.tokenItems().tokenOf(player.getInventory().getItemInMainHand());
        if (heldId == null || !heldId.equals(activeId)) {
            Token active = activeId == null ? null : registry.get(activeId);
            messages.actionBar(player, plugin.config().getString("messages.ability-need-item",
                            "&7Hold your &f{token} Token &7in your hand to use its ability!")
                    .replace("{token}", active == null ? "token" : active.getDisplayName()));
            return;
        }
        // 4. Frozen players cannot use abilities.
        if (freezeManager.isFrozen(player)) {
            SoundEngine.denied(player);
            return;
        }
        // 5. Cooldown check (server-side timestamp). Multi-step abilities
        //    (e.g. the two-portal link) defer the cooldown to their final step.
        long remaining = cooldowns.remainingMillis(player, activeId);
        boolean defer = false;
        if (!defer) {
            if (remaining > 0L) {
                SoundEngine.denied(player);
                messages.actionBar(player, plugin.config().getString("messages.ability-denied",
                                "&c&l[!]&c Ability on cooldown! Wait {seconds}s")
                        .replace("{seconds}", String.valueOf((remaining + 999L) / 1000L)));
                return;
            }
        }

        // Everything validated: start cooldown, run the ability, announce.
        if (!defer) {
            cooldowns.start(player, activeId, ability.getCooldownSeconds());
        }
        // The ability ALWAYS fires straight away, so the damage always lands.
        engine.execute(player, ability);
        // Big tokens additionally offer the OPTIONAL cinematic camera shot.
        Token activeToken = registry.get(activeId);
        if (activeToken != null) {
            plugin.cinematics().offer(player, activeToken, ability.getName());
        }
        SoundEngine.play(player, com.tokensmp.core.VersionCompatibility.sound("ENTITY_EXPERIENCE_ORB_PICKUP"), 1.0f, 1.2f);
        messages.actionBar(player, plugin.config().getString("messages.ability-activated",
                "&a&l[!] &2{ability} Activated!").replace("{ability}", ability.getName()));
        if (!defer) {
            cooldowns.showHud(player, activeId, ability.getCooldownSeconds());
            // Cooldown alert chime.
            com.tokensmp.animation.SoundEngine.play(player,
                    com.tokensmp.core.VersionCompatibility.sound("BLOCK_NOTE_BLOCK_PLING"), 0.8f, 1.6f);
        }
    }


    /** True while the Guardian water shield is up (projectile deflect). */
    public boolean isShielded(Player player) {
        return engine.isShielded(player);
    }

    /** Thorns damage a shielded Guardian reflects on melee attackers. */
    public double shieldThorns() {
        return engine.shieldThorns();
    }

    /** True while the Founder's World Rewrite has muted the player's abilities. */
    public boolean isMuted(Player player) {
        return engine.isMuted(player);
    }
}
