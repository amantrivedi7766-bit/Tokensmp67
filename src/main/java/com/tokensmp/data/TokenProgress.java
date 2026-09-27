package com.tokensmp.data;

import com.tokensmp.token.Token;
import com.tokensmp.token.TokenTier;

/**
 * Live task-grind state for one (player, token, target tier) combination:
 * current kills vs. required kills plus rendering helpers.
 */
public final class TokenProgress {

    private final Token token;
    private final TokenTier targetTier;
    private final int current;

    public TokenProgress(Token token, TokenTier targetTier, int current) {
        this.token = token;
        this.targetTier = targetTier;
        this.current = current;
    }

    public Token getToken() {
        return token;
    }

    /** The tier this grind leads to (e.g. progress toward tier 2). */
    public TokenTier getTargetTier() {
        return targetTier;
    }

    public int getCurrent() {
        return current;
    }

    public int getRequired() {
        return targetTier.getTask().count();
    }

    public boolean isComplete() {
        return current >= getRequired();
    }

    public String describeTask() {
        return targetTier.getTask().description();
    }
}
