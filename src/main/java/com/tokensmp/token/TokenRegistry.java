package com.tokensmp.token;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.impl.AdminToken;
import com.tokensmp.token.impl.BlazeToken;
import com.tokensmp.token.impl.BreezeToken;
import com.tokensmp.token.impl.CreeperToken;
import com.tokensmp.token.impl.ElderGuardianToken;
import com.tokensmp.token.impl.EndermanToken;
import com.tokensmp.token.impl.EvokerToken;
import com.tokensmp.token.impl.GhastToken;
import com.tokensmp.token.impl.GuardianToken;
import com.tokensmp.token.impl.MagmaCubeToken;
import com.tokensmp.token.impl.PhantomToken;
import com.tokensmp.token.impl.PiglinToken;
import com.tokensmp.token.impl.RavagerToken;
import com.tokensmp.token.impl.SkeletonToken;
import com.tokensmp.token.impl.SlimeToken;
import com.tokensmp.token.impl.SpiderToken;
import com.tokensmp.token.impl.VindicatorToken;
import com.tokensmp.token.impl.WardenToken;
import com.tokensmp.token.impl.WitchToken;
import com.tokensmp.token.impl.WitherToken;
import com.tokensmp.token.impl.ZombieToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of every token in the system: exactly 20 player tokens plus the
 * absolutely isolated Admin Token. The Admin Token is registered last and
 * is never exposed through player-facing iteration (see playerTokens()).
 */
public final class TokenRegistry {

    private final List<Token> tokens = new ArrayList<>();
    private final List<Token> playerTokens = new ArrayList<>();

    public TokenRegistry(ConfigManager config) {
        register(new ZombieToken(config));
        register(new SkeletonToken(config));
        register(new SpiderToken(config));
        register(new CreeperToken(config));
        register(new EndermanToken(config));
        register(new BlazeToken(config));
        register(new WardenToken(config));
        register(new WitherToken(config));
        register(new SlimeToken(config));
        register(new PhantomToken(config));
        register(new GuardianToken(config));
        register(new EvokerToken(config));
        register(new PiglinToken(config));
        register(new GhastToken(config));
        register(new MagmaCubeToken(config));
        register(new WitchToken(config));
        register(new VindicatorToken(config));
        register(new RavagerToken(config));
        register(new BreezeToken(config));
        register(new ElderGuardianToken(config));
        // Admin token: absolute isolation - registered but never in the player pool.
        registerAdmin(new AdminToken(config));
    }

    private void register(Token token) {
        tokens.add(token);
        playerTokens.add(token);
    }

    private void registerAdmin(Token token) {
        tokens.add(token);
    }

    /** All tokens including the admin token (admin tooling only). */
    public List<Token> all() {
        return tokens;
    }

    /** Every token that can appear in GUIs, spins and stats for normal players. */
    public List<Token> playerTokens() {
        return playerTokens;
    }

    public Token get(String id) {
        if (id == null) {
            return null;
        }
        for (Token token : tokens) {
            if (token.getId().equalsIgnoreCase(id)) {
                return token;
            }
        }
        return null;
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>(tokens.size());
        for (Token token : tokens) {
            ids.add(token.getId());
        }
        return ids;
    }

    /** Whether the id belongs to the admin token. */
    public boolean isAdmin(String id) {
        Token token = get(id);
        return token != null && token.isAdminToken();
    }
}
