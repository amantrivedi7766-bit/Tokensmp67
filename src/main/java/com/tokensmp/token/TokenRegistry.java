package com.tokensmp.token;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.impl.ZombieToken;
import com.tokensmp.token.impl.SkeletonToken;
import com.tokensmp.token.impl.SpiderToken;
import com.tokensmp.token.impl.CreeperToken;
import com.tokensmp.token.impl.BlazeToken;
import com.tokensmp.token.impl.EndermanToken;
import com.tokensmp.token.impl.WardenToken;
import com.tokensmp.token.impl.EnderDragonToken;
import com.tokensmp.token.impl.GuardianToken;
import com.tokensmp.token.impl.WitherSkeletonToken;
import com.tokensmp.token.impl.RavagerToken;
import com.tokensmp.token.impl.WolfToken;
import com.tokensmp.token.impl.PigToken;
import com.tokensmp.token.impl.ChickenToken;
import com.tokensmp.token.impl.SnowGolemToken;
import com.tokensmp.token.impl.BeeToken;
import com.tokensmp.token.impl.HorseToken;
import com.tokensmp.token.impl.BatToken;
import com.tokensmp.token.impl.SquidToken;
import com.tokensmp.token.impl.WitchToken;
import com.tokensmp.token.impl.GhastToken;
import com.tokensmp.token.impl.PiglinToken;
import com.tokensmp.token.impl.FishToken;
import com.tokensmp.token.impl.WitherToken;
import com.tokensmp.token.impl.VillagerToken;
import com.tokensmp.token.impl.SlimeToken;
import com.tokensmp.token.impl.MagmaCubeToken;
import com.tokensmp.token.impl.IllusionerToken;
import com.tokensmp.token.impl.GolemToken;
import com.tokensmp.token.impl.AdminToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of every token in the system. The Admin Token is registered last
 * and never exposed through player-facing iteration (see playerTokens()).
 */
public final class TokenRegistry {

    private final List<Token> tokens = new ArrayList<>();

    public TokenRegistry(ConfigManager config) {
        register(new ZombieToken(config));
        register(new SkeletonToken(config));
        register(new SpiderToken(config));
        register(new CreeperToken(config));
        register(new BlazeToken(config));
        register(new EndermanToken(config));
        register(new WardenToken(config));
        register(new EnderDragonToken(config));
        register(new GuardianToken(config));
        register(new WitherSkeletonToken(config));
        register(new RavagerToken(config));
        register(new WolfToken(config));
        register(new PigToken(config));
        register(new ChickenToken(config));
        register(new SnowGolemToken(config));
        register(new BeeToken(config));
        register(new HorseToken(config));
        register(new BatToken(config));
        register(new SquidToken(config));
        register(new WitchToken(config));
        register(new GhastToken(config));
        register(new PiglinToken(config));
        register(new FishToken(config));
        register(new WitherToken(config));
        register(new VillagerToken(config));
        register(new SlimeToken(config));
        register(new MagmaCubeToken(config));
        register(new IllusionerToken(config));
        register(new GolemToken(config));
        register(new AdminToken(config));
    }

    private void register(Token token) {
        tokens.add(token);
    }

    /** Every token including the Admin Token. */
    public List<Token> all() {
        return tokens;
    }

    /** Every player-facing token (the Admin Token is never included). */
    public List<Token> playerTokens() {
        List<Token> out = new ArrayList<>();
        for (Token token : tokens) {
            if (!token.isAdminToken()) {
                out.add(token);
            }
        }
        return out;
    }

    /** Every token id (admin token included) - used by tab completion. */
    public List<String> getIds() {
        List<String> out = new ArrayList<>();
        for (Token token : tokens) {
            out.add(token.getId());
        }
        return out;
    }

    /** A token by id, or null. */
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
}
