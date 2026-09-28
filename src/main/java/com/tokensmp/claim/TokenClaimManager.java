package com.tokensmp.claim;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.ParticleManager;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The single authority for claiming and unclaiming tokens. THE RULE:
 * ONE PLAYER = ONE CLAIMED TOKEN. Every claim path in the plugin (GUI,
 * right-click item, death claim, admin action) routes through here, so the
 * rule is enforced server-side everywhere - never only in GUI state.
 *
 * Claiming is NEVER a spin: a claim plays a short confirmation animation
 * and message only. The full cinematic spin is reserved for the initial
 * unlock and admin-triggered spins.
 */
public final class TokenClaimManager {

    private final TokenSMP plugin;
    private final TokenDataManager data;
    private final TokenOwnershipManager ownership;

    public TokenClaimManager(TokenSMP plugin, TokenDataManager data, TokenOwnershipManager ownership) {
        this.plugin = plugin;
        this.data = data;
        this.ownership = ownership;
    }

    // ------------------------------------------------------------------
    // Claim
    // ------------------------------------------------------------------

    /**
     * Claims an already-unlocked token for the player (no spin).
     *
     * @return true when the token is now active
     */
    public boolean claim(Player player, Token token) {
        if (token == null) {
            return false;
        }
        if (data.isClaimed(player, token.getId())) {
            plugin.messages().send(player, "messages.already-active",
                    "&eThis token is already your active token.");
            return true;
        }
        if (!data.hasUnlocked(player, token.getId())) {
            plugin.messages().send(player, "messages.token-locked",
                    "&cYou have not unlocked this token yet!");
            SoundManager.denied(player);
            return false;
        }
        // THE CORE RULE: one active token per player, enforced server-side.
        if (!ownership.canClaimAnotherToken(player)) {
            plugin.messages().send(player, "messages.claim-blocked-active",
                    "&cYou already have an active token!");
            plugin.messages().send(player, "messages.claim-blocked-active-2",
                    "&7Unclaim your current token before claiming another one.");
            SoundManager.denied(player);
            return false;
        }

        int tier = data.getTier(player, token.getId());
        data.claim(player, token.getId());
        plugin.passiveManager().applyPassives(player);

        // Safe item regeneration: exactly one authorized instance, no duplicates.
        if (plugin.config().getBoolean("settings.claim-gives-item", true)) {
            plugin.tokenItems().reconcile(player, token, tier);
        }

        playClaimEffects(player, token, tier);
        plugin.messages().send(player, "messages.token-claimed",
                "&aSuccessfully claimed your {token} Token!",
                "{token}", token.getDisplayName());
        plugin.messages().send(player, "messages.token-claimed-tier",
                "&7Tier: {tier}", "{tier}", String.valueOf(tier));
        plugin.messages().send(player, "messages.token-claimed-ready",
                "&eYour ability is now ready to use. Hold the token item and press Shift + Right Click!");
        return true;
    }

    /** Short confirmation animation - never a full spin. */
    private void playClaimEffects(Player player, Token token, int tier) {
        SoundManager.levelUp(player);
        SoundManager.play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
        ParticleManager.burst(player.getWorld(), token.getRarity() == com.tokensmp.token.TokenRarity.MYTHIC
                ? Particle.END_ROD : Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 24, 0.5);
        ParticleManager.ring(player.getWorld(), Particle.END_ROD, player.getLocation(), 1.2, 20);
        plugin.messages().title(player, "&a&lTOKEN CLAIMED",
                token.getRarity().getColorCode() + "&l" + token.getDisplayName()
                        + " Token &7(Tier " + tier + ")", 5, 30, 10);
    }

    // ------------------------------------------------------------------
    // Unclaim
    // ------------------------------------------------------------------

    /**
     * Unclaims the player's active token: immediately disables its ability,
     * all token mechanics, passive effects, animations and temporary state.
     */
    public boolean unclaim(Player player) {
        String activeId = data.getActiveToken(player);
        if (activeId == null) {
            plugin.messages().send(player, "messages.no-active-token",
                    "&7No active token - claim one via &f/tokens&7!");
            SoundManager.denied(player);
            return false;
        }
        Token token = plugin.registry().get(activeId);
        if (token == null) {
            data.setActiveToken(player, null);
            return false;
        }
        data.unclaim(player, token.getId());
        plugin.passiveManager().clearPassives(player);
        if (plugin.config().getBoolean("settings.unclaim-removes-item", true)) {
            plugin.tokenItems().removeFrom(player, token);
        }
        SoundManager.click(player);
        plugin.messages().send(player, "messages.token-unclaimed",
                "&aSuccessfully unclaimed your {token} Token.",
                "{token}", token.getDisplayName());
        plugin.messages().send(player, "messages.token-unclaimed-2",
                "&7You can now claim another unlocked token.");
        return true;
    }

    /** Unclaims a specific token if it is the player's active one. */
    public boolean unclaim(Player player, Token token) {
        if (token != null && token.getId().equals(data.getActiveToken(player))) {
            return unclaim(player);
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Stolen-token claim (PvP death claim - direct, NEVER a spin)
    // ------------------------------------------------------------------

    /**
     * Claims the token stolen from a defeated victim. Direct claim (no
     * spin); blocked when the killer already has an active token.
     *
     * @return true when the stolen token became the killer's active token
     */
    public boolean claimStolen(Player killer, Token token, String victimName) {
        Player victim = victimName == null ? null : Bukkit.getPlayerExact(victimName);
        if (victim == null || !data.isClaimed(victim, token.getId())) {
            data.clearPendingSteal(killer);
            plugin.messages().send(killer, "messages.steal-expired",
                    "&cThe claim opportunity has expired.");
            SoundManager.denied(killer);
            return false;
        }
        if (data.isClaimed(killer, token.getId())) {
            return true;
        }
        if (!ownership.canClaimAnotherToken(killer)) {
            plugin.messages().send(killer, "messages.claim-blocked-active",
                    "&cYou cannot claim this token.");
            plugin.messages().send(killer, "messages.claim-blocked-active-2",
                    "&7Unclaim your current token first.");
            SoundManager.denied(killer);
            return false;
        }

        int victimTier = Math.max(1, data.getTier(victim, token.getId()));
        data.setTier(killer, token.getId(), victimTier);
        data.claim(killer, token.getId());
        data.clearPendingSteal(killer);
        plugin.passiveManager().applyPassives(killer);
        if (plugin.config().getBoolean("settings.claim-gives-item", true)) {
            plugin.tokenItems().reconcile(killer, token, victimTier);
        }

        // Victim side - server-authoritative, never a duplicated token.
        if (plugin.config().getBoolean("token-stealing.victim-loses-active-state", true)) {
            data.unclaim(victim, token.getId());
        }
        if (!plugin.config().getBoolean("token-stealing.victim-keeps-progress", true)) {
            data.setTier(victim, token.getId(), 0);
            data.setProgress(victim, token.getId(), 0);
        }
        plugin.passiveManager().clearPassives(victim);
        if (plugin.config().getBoolean("settings.unclaim-removes-item", true)) {
            plugin.tokenItems().removeFrom(victim, token);
        }

        playClaimEffects(killer, token, victimTier);
        plugin.messages().broadcast("messages.steal-claimed",
                "&6\u2694 &e{killer} &7claimed the {token} Token &7from &c{victim}&7!",
                "{killer}", killer.getName(),
                "{victim}", victimName,
                "{token}", token.getDisplayName());
        plugin.messages().send(victim, "messages.steal-victim",
                "&cYour {token} Token was claimed by {killer}!",
                "{killer}", killer.getName(),
                "{token}", token.getDisplayName());
        return true;
    }
}
