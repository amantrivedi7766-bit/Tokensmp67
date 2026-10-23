package com.tokensmp.core;

import com.tokensmp.TokenSMP;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lives + elimination + the Revival Ritual.
 *
 * Every player has a limited number of lives. Running out eliminates them
 * (banned / ghost). A living player crafts a Revive Card, right-clicks it to
 * open the Revival Altar, picks an eliminated player and performs the ritual:
 * lightning, particles, sounds, then the player is revived with low health.
 * Repeated revives on the same player escalate the level, and higher levels
 * cost the reviver permanent max hearts.
 */
public final class LifeManager {

    /** One eliminated player entry. */
    public static final class Entry {
        private final UUID id;
        private final String name;
        private final int level;

        public Entry(UUID id, String name, int level) {
            this.id = id;
            this.name = name;
            this.level = level;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public int level() {
            return level;
        }
    }

    private final TokenSMP plugin;
    private final File file;
    private FileConfiguration store;

    public LifeManager(TokenSMP plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "eliminated.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ignored) {
                // fall through - an empty store is fine
            }
        }
        store = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try {
            store.save(file);
        } catch (IOException ignored) {
            // the registry is rebuilt from the ban list if this fails
        }
    }

    public boolean isEnabled() {
        return plugin.config().getBoolean("lives.enabled", true);
    }

    private NamespacedKey livesKey() {
        return new NamespacedKey(plugin, "lives");
    }

    private NamespacedKey sacrificeKey() {
        return new NamespacedKey(plugin, "heart_sacrifice");
    }

    private NamespacedKey reviveHealthKey() {
        return new NamespacedKey(plugin, "revive_health_pending");
    }

    private NamespacedKey reviveCooldownKey() {
        return new NamespacedKey(plugin, "revive_cooldown_until");
    }

    // ------------------------------------------------------------------
    // Lives
    // ------------------------------------------------------------------

    public int getLives(Player player) {
        Integer stored = player.getPersistentDataContainer()
                .get(livesKey(), PersistentDataType.INTEGER);
        if (stored == null) {
            int start = plugin.config().getInt("lives.starting-lives", 10);
            setLives(player, start);
            return start;
        }
        return stored;
    }

    public void setLives(Player player, int lives) {
        player.getPersistentDataContainer().set(livesKey(), PersistentDataType.INTEGER,
                Math.max(0, lives));
    }

    /** Handles a death: loses a life, and eliminates at zero. */
    public boolean handleDeath(Player player) {
        if (!isEnabled()) {
            return false;
        }
        int left = getLives(player) - 1;
        setLives(player, left);
        if (left <= 0) {
            eliminate(player);
            return true;
        }
        plugin.messages().send(player, "messages.lives-lost",
                "&c&lLIVES &7You died! &f{lives} &7life(s) remaining.",
                "{lives}", String.valueOf(left));
        return false;
    }

    /** Eliminates a player (ban or ghost mode, per config). */
    public void eliminate(Player player) {
        String action = plugin.config().getString("lives.elimination", "BAN").toUpperCase();
        String reason = plugin.config().getString("lives.elimination-reason",
                "&cYou ran out of lives on TokenSMP!");
        String plain = com.tokensmp.util.ColorUtil.strip(reason);
        if ("BAN".equals(action)) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), plain, null, "TokenSMP");
            player.kickPlayer(com.tokensmp.util.ColorUtil.color(reason));
        } else if ("SPECTATOR".equals(action)) {
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        }
        store.set("eliminated." + player.getUniqueId() + ".name", player.getName());
        if (!store.contains("eliminated." + player.getUniqueId() + ".level")) {
            store.set("eliminated." + player.getUniqueId() + ".level", 1);
        }
        save();
        plugin.messages().broadcastLine(plugin.messages().msg("messages.eliminated-broadcast",
                "&c&lELIMINATED &f{player} &7has run out of lives!",
                "{player}", player.getName()));
    }

    // ------------------------------------------------------------------
    // Revival
    // ------------------------------------------------------------------

    /** Every eliminated player currently recorded. */
    public List<Entry> eliminated() {
        List<Entry> out = new ArrayList<>();
        if (store.getConfigurationSection("eliminated") == null) {
            return out;
        }
        for (String key : store.getConfigurationSection("eliminated").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                out.add(new Entry(id,
                        store.getString("eliminated." + key + ".name", "unknown"),
                        store.getInt("eliminated." + key + ".level", 1)));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
        return out;
    }

    public boolean isEliminated(UUID id) {
        return store.contains("eliminated." + id);
    }

    public long reviveCooldownRemaining(Player player) {
        Long until = player.getPersistentDataContainer()
                .get(reviveCooldownKey(), PersistentDataType.LONG);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    /** Performs the ritual: unbans/restores the target and charges the reviver. */
    public boolean revive(Player reviver, Entry entry) {
        if (reviveCooldownRemaining(reviver) > 0L) {
            plugin.messages().send(reviver, "messages.revive-cooldown",
                    "&cThe ritual is on cooldown for another &f{seconds}s&c.",
                    "{seconds}", String.valueOf((reviveCooldownRemaining(reviver) + 999L) / 1000L));
            return false;
        }
        int level = Math.max(1, entry.level());
        double sacrifice = Math.max(0.0, (level - 1)
                * plugin.config().getDouble("lives.heart-sacrifice-per-level", 1.0));
        int maxLevel = plugin.config().getInt("lives.max-revive-level", 5);
        if (level > maxLevel) {
            plugin.messages().send(reviver, "messages.revive-max-level",
                    "&c{player} has already been revived the maximum number of times.",
                    "{player}", entry.name());
            return false;
        }
        // Ritual FX at the reviver.
        org.bukkit.Location at = reviver.getLocation();
        reviver.getWorld().strikeLightningEffect(at);
        com.tokensmp.animation.SoundEngine.world(at, org.bukkit.com.tokensmp.core.VersionCompatibility.sound("ENTITY_LIGHTNING_BOLT_THUNDER"),
                1.0f, 1.0f);
        com.tokensmp.animation.SoundEngine.world(at, org.bukkit.com.tokensmp.core.VersionCompatibility.sound("BLOCK_BEACON_ACTIVATE"),
                1.0f, 1.0f);
        com.tokensmp.animation.ParticleEngine.burst(reviver.getWorld(),
                org.bukkit.com.tokensmp.core.VersionCompatibility.particle("SOUL_FIRE_FLAME"), at.clone().add(0, 1, 0), 60, 1.2);
        com.tokensmp.animation.ParticleEngine.burst(reviver.getWorld(),
                org.bukkit.com.tokensmp.core.VersionCompatibility.particle("END_ROD"), at.clone().add(0, 1, 0), 40, 1.0);
        com.tokensmp.animation.ParticleEngine.burst(reviver.getWorld(),
                org.bukkit.com.tokensmp.core.VersionCompatibility.particle("TOTEM_OF_UNDYING"), at.clone().add(0, 1.5, 0), 40, 0.8);

        // Restore the target.
        Bukkit.getBanList(BanList.Type.NAME).pardon(entry.name());
        store.set("eliminated." + entry.id() + ".level", level + 1);
        store.set("eliminated." + entry.id(), null);
        save();
        Player online = Bukkit.getPlayer(entry.id());
        if (online != null) {
            online.setGameMode(org.bukkit.GameMode.SURVIVAL);
            markReviveHealth(online);
        } else {
            // Applied the next time they log in.
            OfflinePlayer offline = Bukkit.getOfflinePlayer(entry.id());
            if (offline.getName() != null) {
                store.set("pending-health." + entry.id(), true);
                save();
            }
        }
        // Charge the reviver: permanent max hearts + cooldown.
        if (sacrifice > 0.0) {
            addSacrifice(reviver, sacrifice);
            plugin.messages().send(reviver, "messages.revive-sacrifice",
                    "&cThe ritual cost you &f{hearts} &cpermanent heart(s).",
                    "{hearts}", String.valueOf(sacrifice));
        }
        long cooldown = plugin.config().getLong("lives.revive-cooldown-seconds", 300L);
        reviver.getPersistentDataContainer().set(reviveCooldownKey(), PersistentDataType.LONG,
                System.currentTimeMillis() + cooldown * 1000L);
        plugin.messages().broadcastLine(plugin.messages().msg("messages.revive-success",
                "&a&lREVIVAL &f{player} &7has been brought back to life by &f{reviver}&7!",
                "{player}", entry.name(), "{reviver}", reviver.getName()));
        return true;
    }

    /** Marks the revived player to spawn with the configured low health. */
    private void markReviveHealth(Player player) {
        player.getPersistentDataContainer().set(reviveHealthKey(), PersistentDataType.BYTE, (byte) 1);
        applyRevivedHealth(player);
    }

    /** Applies the low-health revive penalty (called on join / right after a revive). */
    public void applyRevivedHealth(Player player) {
        Byte pending = player.getPersistentDataContainer()
                .get(reviveHealthKey(), PersistentDataType.BYTE);
        if (pending == null || pending != (byte) 1) {
            return;
        }
        double hearts = plugin.config().getDouble("lives.revive-hearts", 3.0);
        double health = Math.max(1.0, hearts * 2.0);
        double max = player.getAttribute(Attribute.MAX_HEALTH) == null ? 20.0
                : player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(health, max));
        player.getPersistentDataContainer().set(reviveHealthKey(), PersistentDataType.BYTE, (byte) 0);
    }

    // ------------------------------------------------------------------
    // Revive Card
    // ------------------------------------------------------------------

    private NamespacedKey reviveCardKey() {
        return new NamespacedKey(plugin, "revive_card");
    }

    /** Builds the Revive Card item used at the Revival Altar. */
    public org.bukkit.inventory.ItemStack createReviveCard() {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(com.tokensmp.util.ColorUtil.color(
                    plugin.config().getString("lives.revive-card.name", "&d&lRevive Card")));
            meta.setLore(java.util.List.of(
                    com.tokensmp.util.ColorUtil.color("&7Right-click to open the &fRevival Altar"),
                    com.tokensmp.util.ColorUtil.color("&7and bring an eliminated player back.")));
            meta.getPersistentDataContainer().set(reviveCardKey(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** True when the item is a Revive Card. */
    public boolean isReviveCard(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta() == null) {
            return false;
        }
        Byte marker = item.getItemMeta().getPersistentDataContainer()
                .get(reviveCardKey(), PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

    // ------------------------------------------------------------------
    // Heart sacrifice (permanent max-health cost of high level revives)
    // ------------------------------------------------------------------

    public double getSacrifice(Player player) {
        Double value = player.getPersistentDataContainer()
                .get(sacrificeKey(), PersistentDataType.DOUBLE);
        return value == null ? 0.0 : value;
    }

    public void addSacrifice(Player player, double hearts) {
        player.getPersistentDataContainer().set(sacrificeKey(), PersistentDataType.DOUBLE,
                getSacrifice(player) + hearts);
        applySacrifice(player);
    }

    /** Applies (or refreshes) the permanent max-health reduction. */
    public void applySacrifice(Player player) {
        double sacrifice = getSacrifice(player);
        if (sacrifice <= 0.0) {
            return;
        }
        Attribute maxHealth = VersionCompatibility.maxHealthAttribute();
        if (maxHealth == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(maxHealth);
        if (instance == null) {
            return;
        }
        NamespacedKey key = new NamespacedKey(plugin, "revive_sacrifice");
        AttributeModifier existing = instance.getModifier(key);
        if (existing != null) {
            instance.removeModifier(existing);
        }
        instance.addModifier(new AttributeModifier(key, -sacrifice * 2.0,
                AttributeModifier.Operation.ADD_NUMBER));
    }
}
