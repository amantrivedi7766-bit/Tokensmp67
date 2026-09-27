package com.amantrivedi.tokensmp.ui;

import com.amantrivedi.tokensmp.core.TierDefinition;
import com.amantrivedi.tokensmp.core.TokenDefinition;
import com.amantrivedi.tokensmp.core.TokenSmpPlugin;
import com.amantrivedi.tokensmp.data.TokenDataManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the three exact hover-lore variants (locked / unlocked / maxed)
 * from the configurable lore templates in config.yml.
 */
public final class LoreBuilder {

    private final TokenSmpPlugin plugin;

    public LoreBuilder(TokenSmpPlugin plugin) {
        this.plugin = plugin;
    }

    /** LOCKED: "§c§l[LOCKED] §7{Token_Name} Token" + locked lore template. */
    public List<String> buildLocked(TokenDefinition token, Player player) {
        TokenDataManager data = plugin.getData();
        TierDefinition tier1 = token.tier(1);
        int progress = data.getProgress(player, token.getId());
        int required = tier1 != null ? tier1.getTask().getCount() : 0;

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("lore.locked")) {
            lore.add(applyCommon(line, token)
                    .replace("{progress_bar}", plugin.buildProgressBar(progress, required))
                    .replace("{current}", String.valueOf(progress))
                    .replace("{required}", String.valueOf(required))
                    .replace("{mob_name}", tier1 != null ? tier1.getTask().getDescription() : "Unknown")
                    .replace("{ability_name}", tier1 != null && tier1.getAbility() != null ? tier1.getAbility().getName() : "None")
                    .replace("{ability_description}", tier1 != null && tier1.getAbility() != null ? tier1.getAbility().getDescription() : "None")
                    .replace("{passive_type}", tier1 != null && tier1.getAbility() != null ? "Active" : "Passive")
                    .replace("{cooldown}", tier1 != null && tier1.getAbility() != null ? String.valueOf(tier1.getAbility().getCooldown()) : "0"));
        }
        return lore;
    }

    /** UNLOCKED (Tier 1 or 2): current stats + next tier upgrade task. */
    public List<String> buildUnlocked(TokenDefinition token, Player player, int tier) {
        TokenDataManager data = plugin.getData();
        TierDefinition current = token.tier(tier);
        TierDefinition next = token.tier(tier + 1);

        int progress = data.getProgress(player, token.getId());
        int required = next != null ? next.getTask().getCount() : 0;
        int cooldown = current != null && current.getAbility() != null ? current.getAbility().getCooldown() : 0;

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("lore.unlocked")) {
            lore.add(applyCommon(line, token)
                    .replace("{current_tier}", String.valueOf(tier))
                    .replace("{passive_effects}", current != null ? current.getPassiveDescription() : "")
                    .replace("{ability_name}", current != null && current.getAbility() != null ? current.getAbility().getName() : "Passive")
                    .replace("{ability_description}", current != null && current.getAbility() != null ? current.getAbility().getDescription() : "Passive token - no active ability at this tier.")
                    .replace("{cooldown}", String.valueOf(cooldown))
                    .replace("{progress_bar}", plugin.buildProgressBar(progress, required))
                    .replace("{current}", String.valueOf(progress))
                    .replace("{required}", String.valueOf(required))
                    .replace("{mob_name}", next != null ? next.getTask().getDescription() : "")
                    .replace("{materials_list}", next != null ? materialsList(next) : "")
                    .replace("{next_ability_description}", next != null && next.getAbility() != null ? next.getAbility().getDescription() : "")
                    .replace("{next_passive_description}", next != null ? next.getPassiveDescription() : ""));
        }
        return lore;
    }

    /** MAX TIER (Tier 3 / God Mode). */
    public List<String> buildMaxed(TokenDefinition token, int tier) {
        TierDefinition max = token.tier(3);
        int cooldown = max != null && max.getAbility() != null ? max.getAbility().getCooldown() : 0;

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("lore.maxed")) {
            lore.add(applyCommon(line, token)
                    .replace("{max_passive_effects}", max != null ? max.getPassiveDescription() : "")
                    .replace("{ability_name}", max != null && max.getAbility() != null ? max.getAbility().getName() : "Passive")
                    .replace("{max_ability_description}", max != null && max.getAbility() != null ? max.getAbility().getDescription() : "")
                    .replace("{cooldown}", String.valueOf(cooldown)));
        }
        return lore;
    }

    private String applyCommon(String line, TokenDefinition token) {
        return line
                .replace("{separator}", plugin.getConfig().getString("lore.separator", "§8-----------------------------"))
                .replace("{token_name}", token.getName())
                .replace("{rarity}", token.getRarity())
                .replace("{rarity_color}", token.getRarityColor());
    }

    /** "32x Diamond, 4x Netherite Block" style list of the tier's upgrade cost. */
    public static String materialsList(TierDefinition tier) {
        if (tier.getCost().isEmpty()) {
            return "Free";
        }
        StringBuilder builder = new StringBuilder();
        for (TierDefinition.MaterialCost cost : tier.getCost()) {
            if (builder.length() > 0) {
                builder.append("§7, ");
            }
            builder.append("§f").append(cost.amount()).append("x ")
                    .append(prettyName(cost.material().name()));
        }
        return builder.toString();
    }

    public static String prettyName(String enumName) {
        String[] words = enumName.toLowerCase().replace('_', ' ').split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return builder.toString().trim();
    }
}
