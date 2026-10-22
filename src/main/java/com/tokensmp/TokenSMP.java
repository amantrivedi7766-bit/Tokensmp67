package com.tokensmp;

import com.tokensmp.ability.AbilityAnimationEngine;
import com.tokensmp.ability.AbilityListener;
import com.tokensmp.ability.AbilityManager;
import com.tokensmp.ability.FreezeManager;
import com.tokensmp.ability.PassiveManager;
import com.tokensmp.ability.ProjectileEngine;
import com.tokensmp.animation.GroundVortexAnimation;
import com.tokensmp.animation.SpinLauncher;
import com.tokensmp.animation.TokenSpinAnimation;
import com.tokensmp.command.TokenCommand;
import com.tokensmp.command.AbilityCommand;
import com.tokensmp.command.CinematicCommand;
import com.tokensmp.command.CooldownCommand;
import com.tokensmp.command.TokenTabCompleter;
import com.tokensmp.command.TokensAdminCommand;
import com.tokensmp.core.ConfigManager;
import com.tokensmp.animation.CinematicManager;
import com.tokensmp.core.GraceManager;
import com.tokensmp.core.LifeManager;
import com.tokensmp.core.TrustManager;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.data.TokenItemService;
import com.tokensmp.gui.AdminGUI;
import com.tokensmp.gui.RevivalGUI;
import com.tokensmp.gui.PlayerTokenGUI;
import com.tokensmp.gui.TokenSelectionGUI;
import com.tokensmp.gui.TokenUpgradeGUI;
import com.tokensmp.listener.EntityDamageListener;
import com.tokensmp.listener.EntityDeathListener;
import com.tokensmp.listener.InventoryListener;
import com.tokensmp.listener.PlayerDeathListener;
import com.tokensmp.listener.PlayerDropListener;
import com.tokensmp.listener.PlayerInteractListener;
import com.tokensmp.listener.PlayerJoinListener;
import com.tokensmp.listener.ProjectileListener;
import com.tokensmp.token.TokenRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TokenSMP - enterprise-grade token progression plugin for Paper/Spigot/Purpur
 * (Minecraft 1.21+). This is the composition root: it wires the managers,
 * GUIs, listeners and commands together and owns the plugin-wide
 * NamespacedKeys used for PersistentDataContainer storage.
 */
public final class TokenSMP extends JavaPlugin {

    // Core services
    private ConfigManager configManager;
    private GraceManager graceManager;
    private LifeManager lifeManager;
    private CinematicManager cinematicManager;
    private TrustManager trustManager;
    private RevivalGUI revivalGUI;
    private MessageManager messageManager;
    private SchedulerManager schedulerManager;

    // Data + tokens
    private TokenDataManager dataManager;
    private TokenItemService tokenItemService;
    private CooldownManager cooldownManager;
    private TokenRegistry tokenRegistry;

    // Abilities + animations
    private AbilityManager abilityManager;
    private AbilityAnimationEngine abilityAnimations;
    private PassiveManager passiveManager;
    private FreezeManager freezeManager;
    private ProjectileEngine projectileEngine;
    private GroundVortexAnimation groundVortex;
    private TokenSpinAnimation spinAnimation;
    private SpinLauncher spinLauncher;

    // GUIs
    private TokenSelectionGUI selectionGUI;
    private TokenUpgradeGUI tokenUpgradeGUI;
    private PlayerTokenGUI playerTokenGUI;
    private AdminGUI adminGUI;

    // PDC keys
    private NamespacedKey tokenItemKey;
    private NamespacedKey tierKey;
    private NamespacedKey instanceKey;
    private NamespacedKey ownerKey;
    private NamespacedKey adminFlagKey;
    private NamespacedKey claimableKey;
    private NamespacedKey itemVersionKey;

    @Override
    public void onEnable() {
        // Bring an older config folder forward BEFORE anything reads it.
        com.tokensmp.core.ConfigManager.migrate(this);
        saveDefaultConfig();

        // Keys first - the data layer needs them.
        tokenItemKey = new NamespacedKey(this, "token_id");
        tierKey = new NamespacedKey(this, "token_tier");
        instanceKey = new NamespacedKey(this, "token_instance_id");
        ownerKey = new NamespacedKey(this, "token_owner");
        adminFlagKey = new NamespacedKey(this, "admin_token");
        claimableKey = new NamespacedKey(this, "token_claimable");
        itemVersionKey = new NamespacedKey(this, "token_item_version");

        try {
        // Core services.
        configManager = new ConfigManager(this);
        graceManager = new GraceManager(this);
        lifeManager = new LifeManager(this);
        cinematicManager = new CinematicManager(this);
        trustManager = new TrustManager(this);
        com.tokensmp.ability.PhysicalDamageEngine.init(this);
        messageManager = new MessageManager(this, configManager);
        schedulerManager = new SchedulerManager(this);

        applyEngineConfig();
        getLogger().info("Config loaded (config.yml + tokens.yml).");

        // Tokens + data.
        tokenRegistry = new TokenRegistry(configManager);
        getLogger().info("Tokens built: " + tokenRegistry.all().size() + " total.");
        dataManager = new TokenDataManager(this);
        tokenItemService = new TokenItemService(this, dataManager);
        cooldownManager = new CooldownManager(this, tokenRegistry, schedulerManager, messageManager);

        // Abilities + animations.
        abilityAnimations = new AbilityAnimationEngine(this, schedulerManager);
        freezeManager = new FreezeManager(this, messageManager, schedulerManager);
        abilityManager = new AbilityManager(this, tokenRegistry, dataManager, cooldownManager,
                messageManager, freezeManager, abilityAnimations);
        passiveManager = new PassiveManager(this, tokenRegistry, dataManager);
        projectileEngine = new ProjectileEngine(this, schedulerManager);
        groundVortex = new GroundVortexAnimation(this, schedulerManager);
        spinAnimation = new TokenSpinAnimation(this, tokenRegistry, dataManager, messageManager, schedulerManager);
        spinLauncher = new SpinLauncher(this, tokenRegistry, dataManager, spinAnimation);

        // GUIs.
        selectionGUI = new TokenSelectionGUI(this);
        tokenUpgradeGUI = new TokenUpgradeGUI(this);
        playerTokenGUI = new PlayerTokenGUI(this);
        adminGUI = new AdminGUI(this);
        revivalGUI = new RevivalGUI(this);

        // Listeners.
        var server = getServer().getPluginManager();
        server.registerEvents(new PlayerJoinListener(this), this);
        server.registerEvents(new PlayerDeathListener(this, tokenRegistry, dataManager), this);
        server.registerEvents(new PlayerInteractListener(this), this);
        server.registerEvents(new PlayerDropListener(this, groundVortex), this);
        server.registerEvents(new EntityDamageListener(this, passiveManager), this);
        server.registerEvents(new EntityDeathListener(this), this);
        server.registerEvents(new ProjectileListener(this), this);
        server.registerEvents(new InventoryListener(this), this);
        server.registerEvents(freezeManager, this);
        server.registerEvents(new AbilityListener(abilityManager), this);
        server.registerEvents(passiveManager, this);
        server.registerEvents(selectionGUI, this);
        server.registerEvents(tokenUpgradeGUI, this);
        server.registerEvents(playerTokenGUI, this);
        server.registerEvents(adminGUI, this);
        server.registerEvents(revivalGUI, this);
        registerReviveCardRecipe();

        // Commands.
        PluginCommand tokenCommand = getCommand("token");
        if (tokenCommand != null) {
            tokenCommand.setExecutor(new TokenCommand(this));
            tokenCommand.setTabCompleter(new TokenTabCompleter(this));
        }
        PluginCommand tokensCommand = getCommand("tokens");
        if (tokensCommand != null) {
            tokensCommand.setExecutor(new TokenCommand(this, TokenCommand.Mode.GUI));
        }
        PluginCommand balCommand = getCommand("bal");
        if (balCommand != null) {
            balCommand.setExecutor(new TokenCommand(this, TokenCommand.Mode.BALANCE));
        }
        PluginCommand abilityCommand = getCommand("ability");
        if (abilityCommand != null) {
            abilityCommand.setExecutor(new AbilityCommand(this));
            abilityCommand.setTabCompleter(new TokenTabCompleter(this));
        }
        PluginCommand cooldownCommand = getCommand("cooldown");
        if (cooldownCommand != null) {
            cooldownCommand.setExecutor(new CooldownCommand(this));
            cooldownCommand.setTabCompleter(new TokenTabCompleter(this));
        }
        PluginCommand cinematicCommand = getCommand("tokencinematic");
        if (cinematicCommand != null) {
            cinematicCommand.setExecutor(new CinematicCommand(this));
        }
        PluginCommand adminCommand = getCommand("tokensadmin");
        if (adminCommand != null) {
            TokensAdminCommand adminExecutor = new TokensAdminCommand(this);
            adminCommand.setExecutor(adminExecutor);
            adminCommand.setTabCompleter(new TokenTabCompleter(this));
        }

        } catch (Throwable error) {
            // Never die silently: log the real cause so the console shows it.
            getLogger().severe("TokenSMP FAILED TO ENABLE: " + error);
            error.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("TokenSMP enabled: " + tokenRegistry.playerTokens().size()
                + " player tokens + 2 reserved tokens registered (v" + getDescription().getVersion() + ").");
        getLogger().info("Registered commands: /token /tokens /bal /ability /cooldown /tokencinematic /tokensadmin");
    }

    @Override
    public void onDisable() {
        // Cancels every spin, vortex, HUD, trail and freeze task - no leaks.
        if (schedulerManager != null) {
            schedulerManager.cancelAll();
        }
        if (passiveManager != null) {
            for (var player : getServer().getOnlinePlayers()) {
                passiveManager.clearPassives(player);
            }
        }
        getLogger().info("TokenSMP disabled.");
    }

    /** Atomic reload (config + token values) without a server reboot. */
    public void reloadPlugin() {
        configManager.reload();
        applyEngineConfig();
    }

    /**
     * Applies the engine-level configuration (particle/sound master toggles,
     * particle budget and rarity color overrides) atomically. Called on
     * enable and on every reload.
     */
    private void applyEngineConfig() {
        com.tokensmp.animation.ParticleEngine.configure(
                configManager.getBoolean("particles.enabled", true),
                configManager.getInt("performance.max-particles-per-effect", 128));
        com.tokensmp.animation.SoundEngine.configure(
                configManager.getBoolean("sounds.enabled", true));
        com.tokensmp.token.TokenRarity.reset();
        for (com.tokensmp.token.TokenRarity rarity : com.tokensmp.token.TokenRarity.values()) {
            String override = configManager.rawString("colors." + rarity.name().toLowerCase(),
                    rarity.getColorCode());
            if (override != null && !override.isBlank()) {
                com.tokensmp.token.TokenRarity.override(rarity, override);
            }
        }
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public ConfigManager config() {
        return configManager;
    }

    public GraceManager grace() {
        return graceManager;
    }

    public LifeManager lives() {
        return lifeManager;
    }

    public CinematicManager cinematics() {
        return cinematicManager;
    }

    public TrustManager trusts() {
        return trustManager;
    }

    public RevivalGUI revivalGUI() {
        return revivalGUI;
    }

    /** Registers the configurable Revive Card crafting recipe. */
    private void registerReviveCardRecipe() {
        if (!lifeManager.isEnabled()
                || !configManager.getBoolean("lives.revive-card.enabled", true)) {
            return;
        }
        org.bukkit.Material outer = org.bukkit.Material.matchMaterial(
                configManager.getString("lives.revive-card.outer", "SOUL_SAND"));
        org.bukkit.Material center = org.bukkit.Material.matchMaterial(
                configManager.getString("lives.revive-card.center", "NETHER_STAR"));
        if (outer == null || center == null) {
            return;
        }
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(this, "revive_card");
        getServer().removeRecipe(key);
        org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(
                key, lifeManager.createReviveCard());
        recipe.shape("MMM", "MCM", "MMM");
        recipe.setIngredient('M', outer);
        recipe.setIngredient('C', center);
        getServer().addRecipe(recipe);
    }

    public MessageManager messages() {
        return messageManager;
    }

    public SchedulerManager scheduler() {
        return schedulerManager;
    }

    public TokenDataManager data() {
        return dataManager;
    }

    public TokenItemService tokenItems() {
        return tokenItemService;
    }

    public CooldownManager cooldowns() {
        return cooldownManager;
    }

    public TokenRegistry registry() {
        return tokenRegistry;
    }

    public AbilityManager abilities() {
        return abilityManager;
    }

    public PassiveManager passiveManager() {
        return passiveManager;
    }

    public FreezeManager freezeManager() {
        return freezeManager;
    }

    public ProjectileEngine projectileEngine() {
        return projectileEngine;
    }

    public TokenSpinAnimation spinAnimation() {
        return spinAnimation;
    }

    public SpinLauncher spinLauncher() {
        return spinLauncher;
    }

    public TokenSelectionGUI selectionGUI() {
        return selectionGUI;
    }

    public TokenUpgradeGUI tokenUpgradeGUI() {
        return tokenUpgradeGUI;
    }

    public PlayerTokenGUI playerTokenGUI() {
        return playerTokenGUI;
    }

    public AdminGUI adminGUI() {
        return adminGUI;
    }

    public NamespacedKey tokenItemKey() {
        return tokenItemKey;
    }

    public NamespacedKey tierKey() {
        return tierKey;
    }

    public NamespacedKey instanceKey() {
        return instanceKey;
    }

    public NamespacedKey ownerKey() {
        return ownerKey;
    }

    public NamespacedKey adminFlagKey() {
        return adminFlagKey;
    }

    public NamespacedKey claimableKey() {
        return claimableKey;
    }

    public NamespacedKey itemVersionKey() {
        return itemVersionKey;
    }
}
