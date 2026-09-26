package com.amantrivedi.tokensmp;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Main plugin class. Handles setup, data configuration initialization,
 * cooldown bookkeeping, token passive (re-)application and heart item helpers.
 */
public final class TokenSmpPlugin extends org.bukkit.plugin.java.JavaPlugin {

    private final Map<UUID, PlayerData> playerCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<TokenType, Long>> cooldowns = new ConcurrentHashMap<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    private NamespacedKey heartItemKey;
    private NamespacedKey heartBonusKey;
    private AttributeModifier heartBonusModifier;
    private Attribute maxHealthAttribute;
    private PotionEffectType resistanceEffectType;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create plugin data folder.");
        }
        dataFile = new File(getDataFolder(), "data.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        heartItemKey = new NamespacedKey(this, "extra_heart_item");
        heartBonusKey = new NamespacedKey(this, "token_heart_bonus");
        heartBonusModifier = new AttributeModifier(heartBonusKey,
                4.0, AttributeModifier.Operation.ADD_NUMBER);

        maxHealthAttribute = resolveMaxHealthAttribute();
        if (maxHealthAttribute == null) {
            getLogger().severe("Could not resolve the max-health attribute on this server version - disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        resistanceEffectType = resolveResistance();
        if (resistanceEffectType == null) {
            getLogger().warning("Could not resolve the resistance potion effect; Warden resistance passive is disabled.");
        }

        TokenSmpCommand commandHandler = new TokenSmpCommand(this);
        org.bukkit.command.PluginCommand command = getCommand("token");
        if (command != null) {
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        getServer().getPluginManager().registerEvents(new CoreListener(this), this);
        getServer().getPluginManager().registerEvents(new TokenMenu(this), this);

        // Periodic passive state re-verification for every online player.
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyTokenPassives(player);
            }
        }, 100L, 100L);

        getLogger().info("TokenSMP enabled. GUI: /token  |  Withdraw: /token withdraw <amount>");
    }

    @Override
    public void onDisable() {
        // Server shutdown: persist synchronously (async tasks no longer run reliably here).
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save data.yml on shutdown", ex);
        }
        getLogger().info("TokenSMP disabled. All player data saved.");
    }

    // ------------------------------------------------------------------
    // Max health attribute (version safe for 1.21 - 1.21.4)
    // ------------------------------------------------------------------

    /**
     * The max-health attribute moved registries between 1.21.1 (generic.max_health)
     * and 1.21.2+ (max_health). Resolving via the registry keeps the plugin
     * compatible with every 1.21 sub-version.
     */
    private Attribute resolveMaxHealthAttribute() {
        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
        if (attribute == null) {
            attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
        }
        return attribute;
    }

    public Attribute getMaxHealthAttribute() {
        return maxHealthAttribute;
    }

    /**
     * The resistance effect constant was renamed between 1.21.1 (DAMAGE_RESISTANCE)
     * and 1.21.3+ (RESISTANCE). Resolving via reflection keeps the plugin
     * compatible with every 1.21 sub-version.
     */
    private PotionEffectType resolveResistance() {
        for (String fieldName : new String[]{"RESISTANCE", "DAMAGE_RESISTANCE"}) {
            try {
                return (PotionEffectType) PotionEffectType.class.getField(fieldName).get(null);
            } catch (ReflectiveOperationException ignored) {
                // try the next known constant name
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Player data
    // ------------------------------------------------------------------

    /** Loads (or fetches cached) data for the given player. */
    public PlayerData getData(Player player) {
        return playerCache.computeIfAbsent(player.getUniqueId(), id -> {
            PlayerData data = new PlayerData();
            data.setActiveToken(dataConfig.getString("players." + id + ".active-token", null));
            data.setExtraHeartHp(dataConfig.getDouble("players." + id + ".extra-heart-hp", 0.0));
            return data;
        });
    }

    /** Writes a player's cached data into the data configuration and persists asynchronously. */
    public void persistPlayerData(UUID playerId) {
        PlayerData data = playerCache.get(playerId);
        if (data == null) {
            return;
        }
        dataConfig.set("players." + playerId + ".active-token", data.getActiveToken());
        dataConfig.set("players." + playerId + ".extra-heart-hp", data.getExtraHeartHp());
        playerCache.remove(playerId);
        saveDataAsync();
    }

    /**
     * Saves the whole data.yml completely asynchronously so the main
     * thread never touches the disk (prevents tick lag on PlayerQuitEvent).
     */
    public void saveDataAsync() {
        final String snapshot = dataConfig.saveToString();
        final File target = this.dataFile;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Files.writeString(target.toPath(), snapshot, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    getLogger().log(Level.SEVERE, "Could not asynchronously save data.yml", ex);
                }
            }
        }.runTaskAsynchronously(this);
    }

    // ------------------------------------------------------------------
    // Token passives
    // ------------------------------------------------------------------

    /** Equips a token: clears stale passives and applies the new token's passive state. */
    public void setActiveToken(Player player, TokenType type) {
        getData(player).setActiveToken(type == null ? null : type.name());
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        if (resistanceEffectType != null) {
            player.removePotionEffect(resistanceEffectType);
        }
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        applyTokenPassives(player);
    }

    /** (Re-)applies the passive state of the player's equipped token. */
    public void applyTokenPassives(Player player) {
        TokenType active = TokenType.fromId(getData(player).getActiveToken());

        AttributeInstance maxHealth = player.getAttribute(maxHealthAttribute);
        if (maxHealth != null) {
            AttributeModifier existing = maxHealth.getModifier(heartBonusKey);
            if (active == TokenType.ZOMBIE && existing == null) {
                maxHealth.addModifier(heartBonusModifier);
            } else if (active != TokenType.ZOMBIE && existing != null) {
                maxHealth.removeModifier(heartBonusKey);
            }
        }

        if (active == TokenType.BLAZE) {
            addInfiniteEffect(player, PotionEffectType.FIRE_RESISTANCE, 0);
        } else if (active == TokenType.WARDEN) {
            if (resistanceEffectType != null) {
                addInfiniteEffect(player, resistanceEffectType, 0);
            }
            addInfiniteEffect(player, PotionEffectType.NIGHT_VISION, 0);
        }
    }

    private void addInfiniteEffect(Player player, PotionEffectType type, int amplifier) {
        PotionEffect current = player.getPotionEffect(type);
        PotionEffect wanted = new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, true, false);
        if (current == null || !current.equals(wanted)) {
            player.addPotionEffect(wanted);
        }
    }

    /** Restores the persistent base max-health (20.0 base + stacked hearts) after join. */
    public void restoreBaseHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(maxHealthAttribute);
        if (maxHealth == null) {
            return;
        }
        maxHealth.setBaseValue(20.0 + getData(player).getExtraHeartHp());
    }

    // ------------------------------------------------------------------
    // Cooldown engine
    // ------------------------------------------------------------------

    /** Remaining cooldown in milliseconds; 0 when ready. */
    public long getCooldownRemaining(UUID playerId, TokenType type) {
        Map<TokenType, Long> map = cooldowns.get(playerId);
        if (map == null) {
            return 0L;
        }
        Long until = map.get(type);
        if (until == null) {
            return 0L;
        }
        long remaining = until - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    /** Starts the configured cooldown for the given token ability. */
    public void startCooldown(UUID playerId, TokenType type) {
        int seconds = getConfig().getInt("tokens." + type.name().toLowerCase() + ".cooldown-seconds", 45);
        cooldowns.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>())
                .put(type, System.currentTimeMillis() + (seconds * 1000L));
    }

    // ------------------------------------------------------------------
    // Physical heart item
    // ------------------------------------------------------------------

    /** Builds the physical "Extra Heart" item (RED_DYE with persistent tag). */
    public ItemStack createHeartItem(int amount) {
        ItemStack item = new ItemStack(org.bukkit.Material.RED_DYE, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4§lExtra Heart");
            meta.setLore(List.of("§7Right-click to claim +1 Permanent Heart."));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(heartItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** True when the given stack is a tagged Extra Heart item. */
    public boolean isHeartItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getItemMeta() == null) {
            return false;
        }
        Byte tag = item.getItemMeta().getPersistentDataContainer().get(heartItemKey, PersistentDataType.BYTE);
        return tag != null && tag == (byte) 1;
    }
}
