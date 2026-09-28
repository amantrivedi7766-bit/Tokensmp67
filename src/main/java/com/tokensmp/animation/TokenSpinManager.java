package com.tokensmp.animation;

import com.tokensmp.TokenSMP;
import com.tokensmp.animation.SoundManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.token.Token;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Owns the token spin lifecycle:
 *
 * - launchFor(): the cinematic spin itself (first-join completion or an
 *   admin-triggered spin).
 * - beginFirstJoinSequence(): the EXACT 60-second first-join timing - the
 *   preparation titles play immediately, a countdown runs on the action
 *   bar, the GET READY warning fires shortly before the spin, and only
 *   then does the spin start. The whole timer is server-side, survives
 *   inventory interactions, self-cancels when the player disconnects and
 *   never triggers twice (guarded by the persistent first_join flags).
 */
public final class TokenSpinManager {

    private static final Random RANDOM = new Random();

    private final TokenSMP plugin;
    private final TokenRegistry registry;
    private final TokenDataManager data;
    private final TokenSpinAnimation spin;

    public TokenSpinManager(TokenSMP plugin, TokenRegistry registry, TokenDataManager data,
                            TokenSpinAnimation spin) {
        this.plugin = plugin;
        this.registry = registry;
        this.data = data;
        this.spin = spin;
    }

    // ------------------------------------------------------------------
    // First-join: wait EXACTLY the configured delay, then spin
    // ------------------------------------------------------------------

    /**
     * Runs the full first-join sequence: preparation titles, countdown,
     * GET READY warning and finally the spin. Safe to call only while the
     * first-join spin is pending (callers guard against double runs).
     */
    public void beginFirstJoinSequence(Player player) {
        int delaySeconds = Math.max(0, plugin.config().getInt("first-join.delay-seconds", 60));
        int warningSeconds = Math.max(1, plugin.config().getInt("first-join.warning-seconds", 10));

        data.setSpinPending(player, true);

        // Immediate preparation announcement.
        plugin.messages().title(player,
                plugin.config().getString("messages.first-join-title", "&6&lTOKEN SMP"),
                plugin.config().getString("messages.first-join-subtitle",
                        "&fPrepare yourself... &eYour Token is waiting for you."),
                10, 50, 20);
        SoundManager.play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        SoundManager.play(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.6f);

        final int[] elapsed = {0};
        final int warningAt = Math.max(0, delaySeconds - warningSeconds);
        final boolean[] warned = {false};

        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                // Server-side: a disconnect cancels the countdown; the pending
                // flag stays so the sequence can restart after reconnect.
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                elapsed[0]++;
                int remaining = delaySeconds - elapsed[0];

                if (!warned[0] && elapsed[0] >= warningAt) {
                    warned[0] = true;
                    plugin.messages().title(player,
                            plugin.config().getString("messages.get-ready-title", "&6&lGET READY"),
                            plugin.config().getString("messages.get-ready-subtitle",
                                    "&fPrepare yourself for your Token!"),
                            5, 30, 10);
                    SoundManager.play(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 1.8f);
                    SoundManager.play(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.2f, 2.2f);
                }

                if (remaining > 0) {
                    if (remaining % 10 == 0 || remaining <= 5) {
                        plugin.messages().actionBar(player, plugin.config()
                                .getString("messages.first-join-countdown",
                                        "&eYour token spin begins in &f{seconds}s&!")
                                .replace("{seconds}", String.valueOf(remaining)));
                    }
                    return;
                }

                // Time! Spin, then permanently mark the first join complete.
                cancel();
                if (player.isOnline()) {
                    launchFor(player);
                }
                data.setFirstJoinDone(player);
            }
        };
        timer.runTaskTimer(plugin, 20L, 20L); // one tick per second, server-side
        plugin.scheduler().register(timer);

        // Belt-and-braces: if the timer was somehow cancelled without a spin
        // (e.g. plugin reload clears tasks), the pending flag makes the next
        // join restart the sequence instead of losing it forever.
    }

    // ------------------------------------------------------------------
    // The cinematic spin
    // ------------------------------------------------------------------

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
