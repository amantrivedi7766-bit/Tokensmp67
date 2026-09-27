package com.amantrivedi.tokensmp.core;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Loads every token (and its three tiers) from config.yml into memory.
 * Fully config-driven: adding a new token to config.yml requires no code change.
 */
public final class TokenRegistry {

    private final TokenSmpPlugin plugin;
    private final Map<String, TokenDefinition> tokens = new LinkedHashMap<>();

    public TokenRegistry(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** (Re)loads all token definitions from config.yml. */
    public void load() {
        tokens.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("tokens");
        if (root == null) {
            plugin.getLogger().severe("No 'tokens' section found in config.yml!");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            try {
                tokens.put(id.toLowerCase(Locale.ROOT), parseToken(id, section));
            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to load token '" + id + "': " + ex.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + tokens.size() + " tokens: " + String.join(", ", tokens.keySet()));
    }

    private TokenDefinition parseToken(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        String rarity = section.getString("rarity", "Common");
        String rarityColor = section.getString("rarity-color", "§f");
        Material material = Material.matchMaterial(section.getString("material", "PAPER"));
        if (material == null) {
            material = Material.PAPER;
        }
        int slot = section.getInt("slot", -1);
        boolean adminOnly = section.getBoolean("admin-only", false);

        TokenDefinition token = new TokenDefinition(id.toLowerCase(Locale.ROOT), name, rarity, rarityColor,
                material, slot, adminOnly);

        for (int tier = 1; tier <= 3; tier++) {
            ConfigurationSection tierSection = section.getConfigurationSection("tiers." + tier);
            if (tierSection == null) {
                plugin.getLogger().warning("Token '" + id + "' is missing tier " + tier + " in config.yml!");
                continue;
            }
            token.setTier(tier, parseTier(tier, tierSection));
        }
        return token;
    }

    private TierDefinition parseTier(int tier, ConfigurationSection section) {
        List<String> rawPassives = new ArrayList<>(section.getStringList("passives"));
        String passiveDescription = section.getString("passive-description", "");
        TierDefinition def = new TierDefinition(tier, rawPassives, passiveDescription);

        for (String raw : rawPassives) {
            if (raw != null && !raw.isBlank()) {
                def.parsePassive(raw.trim(), plugin);
            }
        }

        // ---- active ability ----
        if (section.contains("ability")) {
            TierDefinition.AbilitySpec ability = new TierDefinition.AbilitySpec();
            ability.name = section.getString("ability.name", "Ability");
            ability.type = section.getString("ability.type", "NONE").toUpperCase(Locale.ROOT);
            ability.cooldown = section.getInt("ability.cooldown", 45);
            ability.duration = section.getDouble("ability.duration", 10.0);
            ability.radius = section.getDouble("ability.radius", 8.0);
            ability.damage = section.getDouble("ability.damage", 10.0);
            ability.knockback = section.getDouble("ability.knockback", 1.0);
            ability.amplifier = section.getInt("ability.amplifier", 0);
            ability.description = section.getString("ability.description", "");
            def.setAbility(ability);
        }

        // ---- unlock/upgrade task ----
        TierDefinition.TaskSpec task = new TierDefinition.TaskSpec();
        String taskType = section.getString("task.type", "KILLS").toUpperCase(Locale.ROOT);
        try {
            task.type = TierDefinition.TaskType.valueOf(taskType);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Unknown task type '" + taskType + "', defaulting to KILLS.");
        }
        for (String mob : section.getStringList("task.mobs")) {
            try {
                task.mobs.add(EntityType.valueOf(mob.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown mob type '" + mob + "' in task - skipping.");
            }
        }
        task.count = section.getInt("task.count", 100);
        task.description = section.getString("task.description", "Kill " + task.count + " mobs");
        def.setTask(task);

        // ---- material cost ----
        for (Map<?, ?> entry : section.getMapList("cost")) {
            Object materialName = entry.get("material");
            Object amount = entry.get("amount");
            if (materialName == null || amount == null) {
                continue;
            }
            Material material = Material.matchMaterial(String.valueOf(materialName));
            if (material != null) {
                def.getCost().add(new TierDefinition.MaterialCost(material, Integer.parseInt(String.valueOf(amount))));
            }
        }
        return def;
    }

    public TokenDefinition get(String id) {
        return id == null ? null : tokens.get(id.toLowerCase(Locale.ROOT));
    }

    public Set<String> getIds() {
        return tokens.keySet();
    }

    public List<TokenDefinition> all() {
        return new ArrayList<>(tokens.values());
    }
}
