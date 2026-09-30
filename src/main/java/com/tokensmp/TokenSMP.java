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
import com.tokensmp.command.TokenTabCompleter;
import com.tokensmp.command.TokensAdminCommand;
import com.tokensmp.core.ConfigManager;
import com.tokensmp.core.MessageManager;
import com.tokensmp.core.SchedulerManager;
import com.tokensmp.data.CooldownManager;
import com.tokensmp.data.TokenDataManager;
import com.tokensmp.data.TokenItemService;
import com.tokensmp.gui.AdminGUI;
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
        saveDefaultConfig();

        // Keys first - the data layer needs them.
        tokenItemKey = new NamespacedKey(this, "token_id");
        tierKey = new NamespacedKey(this, "token_tier");
        instanceKey = new NamespacedKey(this, "token_instance_id");
        ownerKey = new NamespacedKey(this, "token_owner");
        adminFlagKey = new NamespacedKey(this, "admin_token");
        claimableKey = new NamespacedKey(this, "token_claimable");
        itemVersionKey = new NamespacedKey(this, "token_item_version");

        // Core services.
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this, configManager);
        schedulerManager = new SchedulerManager(this);

        applyEngineConfig();

        // Tokens + data.
        tokenRegistry = new TokenRegistry(configManager);
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

        // Commands.
        PluginCommand tokenCommand = getCommand("token");
        if (tokenCommand != null) {
            tokenCommand.setExecutor(new TokenCommand(this));
            tokenCommand.setTabCompleter(new TokenTabCompleter(this));
        }
        PluginCommand adminCommand = getCommand("tokensadmin");
        if (adminCommand != null) {
            TokensAdminCommand adminExecutor = new TokensAdminCommand(this);
            adminCommand.setExecutor(adminExecutor);
            adminCommand.setTabCompleter(new TokenTabCompleter(this));
        }

        getLogger().info("TokenSMP enabled: " + tokenRegistry.playerTokens().size()
                + " player tokens + 1 isolated admin token registered.");
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
