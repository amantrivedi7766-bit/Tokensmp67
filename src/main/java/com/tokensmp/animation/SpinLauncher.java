package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Launches token spins. Picks a random eligible PLAYER token the player does
 * not already own (the Admin Token can never be picked) and plays the full
 * cinematic spin, which unlocks the token at tier 1 (available - the player
 * still has to claim it to activate it).
 */
public final class SpinLauncher {

    private static final Random RANDOM = new Random();

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final TokenSpinAnimation spin;

    public SpinLauncher(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                       TokenSpinAnimation spin) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.spin = spin;
    }

    /** Picks a random not-yet-owned player token and plays the spin. */
    public void launchFor(Player player) {
        List<Token> eligible = new ArrayList<>();
        for (Token token : registry.playerTokens()) {
            if (!data.hasUnlocked(player, token.getId())) {
                eligible.add(token);
            }
        }
        if (eligible.isEmpty()) {
            // Everything already unlocked - re-roll a random one for the show.
            List<Token> pool = registry.playerTokens();
            spin.play(player, pool.get(RANDOM.nextInt(pool.size())));
            return;
        }
        spin.play(player, eligible.get(RANDOM.nextInt(eligible.size())));
    }
}
