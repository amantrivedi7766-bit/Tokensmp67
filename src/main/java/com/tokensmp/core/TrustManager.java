package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The per-player trust list. A trusted player is never hit by the owner's own
 * token abilities (damage and crowd control), which keeps friends and team-ups
 * safe from dangerous AoE abilities.
 */
public final class TrustManager {

    private final TokenSMP plugin;
    private final NamespacedKey key;

    public TrustManager(TokenSMP plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "trusted_players");
    }

    /** Every trusted UUID. */
    public List<UUID> trusted(Player owner) {
        List<UUID> out = new ArrayList<>();
        String raw = owner.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        for (String part : raw.split(",")) {
            try {
                out.add(UUID.fromString(part.trim()));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
        return out;
    }

    /** True when {@code target} is on {@code owner}'s trust list. */
    public boolean isTrusted(Player owner, Player target) {
        return owner != null && target != null && trusted(owner).contains(target.getUniqueId());
    }

    /** Adds a player to the trust list (true when it changed). */
    public boolean trust(Player owner, UUID target) {
        List<UUID> list = trusted(owner);
        if (list.contains(target)) {
            return false;
        }
        list.add(target);
        save(owner, list);
        return true;
    }

    /** Removes a player from the trust list (true when it changed). */
    public boolean untrust(Player owner, UUID target) {
        List<UUID> list = trusted(owner);
        if (!list.remove(target)) {
            return false;
        }
        save(owner, list);
        return true;
    }

    private void save(Player owner, List<UUID> list) {
        StringBuilder builder = new StringBuilder();
        for (UUID id : list) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        owner.getPersistentDataContainer().set(key, PersistentDataType.STRING, builder.toString());
    }
}
