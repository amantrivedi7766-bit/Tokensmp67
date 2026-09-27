package com.amantrivedi.tokensmp.core;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Defines one progressive tier (1-3) of a token: passives, active ability,
 * unlock/upgrade task and material cost. All values are parsed from config.yml.
 */
public final class TierDefinition {

    public enum TaskType { KILLS, NETHER_KILLS, WITHER_STREAK, WARDEN_BAREHAND }

    /** Simple parsed potion passive. */
    public record PotionPassive(PotionEffectType type, int amplifier) { }

    /** Task grind spec. */
    public static final class TaskSpec {
        TaskType type = TaskType.KILLS;
        final List<EntityType> mobs = new ArrayList<>();
        int count = 100;
        String description = "Kill mobs";

        public TaskType getType() { return type; }
        public List<EntityType> getMobs() { return mobs; }
        public int getCount() { return count; }
        public String getDescription() { return description; }
    }

    /** Material upgrade cost entry. */
    public record MaterialCost(Material material, int amount) { }

    /** Active ability parameters. */
    public static final class AbilitySpec {
        String name = "Ability";
        String type = "NONE";
        int cooldown = 45;
        double duration = 10.0;
        double radius = 8.0;
        double damage = 10.0;
        double knockback = 1.0;
        int amplifier = 0;
        String description = "";

        public String getName() { return name; }
        public String getType() { return type; }
        public int getCooldown() { return cooldown; }
        public double getDuration() { return duration; }
        public double getRadius() { return radius; }
        public double getDamage() { return damage; }
        public double getKnockback() { return knockback; }
        public int getAmplifier() { return amplifier; }
        public String getDescription() { return description; }
    }

    private final int tier;
    private final List<String> rawPassives = new ArrayList<>();
    private final List<PotionPassive> potions = new ArrayList<>();
    private int extraHearts = 0;
    private double bowDamageBonus = 0.0;
    private double explosionImmunity = 0.0;
    private boolean fallImmunity = false;
    private boolean wallClimbing = false;
    private boolean flight = false;
    private boolean arrowTrail = false;
    private boolean cloudJumps = false;
    private AbilitySpec ability;
    private TaskSpec task = new TaskSpec();
    private final List<MaterialCost> cost = new ArrayList<>();
    private final String passiveDescription;

    public TierDefinition(int tier, List<String> rawPassives, String passiveDescription) {
        this.tier = tier;
        this.passiveDescription = passiveDescription;
        if (rawPassives != null) {
            this.rawPassives.addAll(rawPassives);
        }
    }

    /** Parses a passive token like "POTION:STRENGTH:1" or "HEARTS:5". */
    public void parsePassive(String raw, TokenSmpPlugin plugin) {
        String[] parts = raw.toUpperCase(Locale.ROOT).split(":");
        switch (parts[0]) {
            case "HEARTS" -> extraHearts = Integer.parseInt(parts[1]);
            case "BOW_DAMAGE" -> bowDamageBonus = Double.parseDouble(parts[1]);
            case "EXPLOSION_IMMUNITY" -> explosionImmunity = parts.length > 1 ? Double.parseDouble(parts[1]) : 1.0;
            case "FALL_IMMUNITY" -> fallImmunity = true;
            case "WALL_CLIMBING" -> wallClimbing = true;
            case "FLIGHT" -> flight = true;
            case "ARROW_TRAIL" -> arrowTrail = true;
            case "CLOUD_JUMPS" -> cloudJumps = true;
            default -> {
                String name = parts[0];
                int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                if (name.equals("POTION") && parts.length > 2) {
                    name = parts[1];
                    amplifier = Integer.parseInt(parts[2]);
                }
                PotionEffectType type = plugin.resolvePotion(name);
                if (type != null) {
                    potions.add(new PotionPassive(type, amplifier));
                } else {
                    plugin.getLogger().warning("Unknown passive '" + raw + "' - skipping.");
                }
            }
        }
    }

    // ---- getters ----
    public int getTier() { return tier; }
    public List<PotionPassive> getPotions() { return potions; }
    public int getExtraHearts() { return extraHearts; }
    public double getBowDamageBonus() { return bowDamageBonus; }
    public double getExplosionImmunity() { return explosionImmunity; }
    public boolean hasFallImmunity() { return fallImmunity; }
    public boolean hasWallClimbing() { return wallClimbing; }
    public boolean hasFlight() { return flight; }
    public boolean hasArrowTrail() { return arrowTrail; }
    public boolean hasCloudJumps() { return cloudJumps; }
    public AbilitySpec getAbility() { return ability; }
    public void setAbility(AbilitySpec ability) { this.ability = ability; }
    public TaskSpec getTask() { return task; }
    public void setTask(TaskSpec task) { this.task = task; }
    public List<MaterialCost> getCost() { return cost; }
    public List<String> getRawPassives() { return rawPassives; }
    public String getPassiveDescription() { return passiveDescription; }
}
