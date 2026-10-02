package com.tokensmp.token;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One progressive tier (1-3) of a token: passive effects, active ability
 * parameters, the unlock/upgrade task and the material cost. Built fluently
 * by the token implementations in token.impl.
 */
public final class TokenTier {

    public enum TaskType { KILLS, NETHER_KILLS, WITHER_STREAK, WARDEN_BAREHAND }

    public record PotionPassive(PotionEffectType type, int amplifier) { }

    public record TaskSpec(TaskType type, List<EntityType> mobs, int count, String description) { }

    public record MaterialCost(Material material, int amount) { }

    /** Mutable-during-construction active ability parameters. */
    public static final class AbilitySpec {
        private TokenAbility type = TokenAbility.NONE;
        private String name = "Ability";
        private int cooldownSeconds = 45;
        private double durationSeconds = 10.0;
        private double radius = 8.0;
        private double damage = 10.0;
        private double knockback = 1.0;
        private int amplifier = 0;
        private double range = 18.0;
        private int count = 1;
        private double speed = 1.0;
        private boolean trueDamage = false;
        private int particleCount = 30;
        private AbilityTrigger trigger = AbilityTrigger.SHIFT_RIGHT_CLICK;
        private List<Particle> particles = List.of();
        private List<Sound> sounds = List.of();
        private String description = "";

        public AbilitySpec(TokenAbility type, String name, int cooldownSeconds) {
            this.type = type;
            this.name = name;
            this.cooldownSeconds = cooldownSeconds;
        }

        public AbilitySpec duration(double seconds) { this.durationSeconds = seconds; return this; }
        public AbilitySpec radius(double radius) { this.radius = radius; return this; }
        public AbilitySpec damage(double damage) { this.damage = damage; return this; }
        public AbilitySpec knockback(double blocks) { this.knockback = blocks; return this; }
        public AbilitySpec amplifier(int amplifier) { this.amplifier = amplifier; return this; }
        public AbilitySpec range(double blocks) { this.range = blocks; return this; }
        public AbilitySpec count(int count) { this.count = Math.max(1, count); return this; }
        public AbilitySpec speed(double speed) { this.speed = speed; return this; }
        public AbilitySpec trueDamage(boolean trueDamage) { this.trueDamage = trueDamage; return this; }
        public AbilitySpec particleCount(int particleCount) { this.particleCount = particleCount; return this; }
        public AbilitySpec trigger(AbilityTrigger trigger) { this.trigger = trigger; return this; }
        public AbilitySpec particles(List<Particle> particles) { this.particles = particles; return this; }
        public AbilitySpec sounds(List<Sound> sounds) { this.sounds = sounds; return this; }
        public AbilitySpec description(String description) { this.description = description; return this; }

        public TokenAbility getType() { return type; }
        public String getName() { return name; }
        public int getCooldownSeconds() { return cooldownSeconds; }
        public double getDurationSeconds() { return durationSeconds; }
        public double getRadius() { return radius; }
        public double getDamage() { return damage; }
        public double getKnockback() { return knockback; }
        public int getAmplifier() { return amplifier; }
        public double getRange() { return range; }
        public int getCount() { return count; }
        public double getSpeed() { return speed; }
        public boolean isTrueDamage() { return trueDamage; }
        public int getParticleCount() { return particleCount; }
        public AbilityTrigger getTrigger() { return trigger; }
        public List<Particle> getParticles() { return particles; }
        public List<Sound> getSounds() { return sounds; }
        public String getDescription() { return description; }
    }

    private final int tier;
    private final List<PotionPassive> potions = new ArrayList<>();
    private final List<MaterialCost> cost = new ArrayList<>();
    private int extraHearts = 0;
    private double bowDamageBonus = 0.0;
    private double explosionImmunity = 0.0;
    private boolean fallImmunity = false;
    private boolean wallClimbing = false;
    private boolean flight = false;
    private boolean arrowTrail = false;
    private boolean cloudJumps = false;
    private boolean pearlDamageImmunity = false;
    private boolean burningAura = false;
    private AbilitySpec ability = null;
    private TaskSpec task = null;
    private String passiveDescription = "";

    private TokenTier(int tier) {
        this.tier = tier;
    }

    public static Builder of(int tier) {
        return new Builder(tier);
    }

    public int getTier() { return tier; }
    public List<PotionPassive> getPotions() { return Collections.unmodifiableList(potions); }
    public List<MaterialCost> getCost() { return Collections.unmodifiableList(cost); }
    public int getExtraHearts() { return extraHearts; }
    public double getBowDamageBonus() { return bowDamageBonus; }
    public double getExplosionImmunity() { return explosionImmunity; }
    public boolean hasFallImmunity() { return fallImmunity; }
    public boolean hasWallClimbing() { return wallClimbing; }
    public boolean hasFlight() { return flight; }
    public boolean hasArrowTrail() { return arrowTrail; }
    public boolean hasCloudJumps() { return cloudJumps; }
    public boolean hasPearlDamageImmunity() { return pearlDamageImmunity; }
    public boolean hasBurningAura() { return burningAura; }
    public AbilitySpec getAbility() { return ability; }
    public TaskSpec getTask() { return task; }
    public String getPassiveDescription() { return passiveDescription; }

    /** Fluent builder - each token implementation declares its three tiers with this. */
    public static final class Builder {
        private final TokenTier built;

        private Builder(int tier) {
            this.built = new TokenTier(tier);
        }

        public Builder potion(PotionEffectType type, int amplifier) {
            built.potions.add(new PotionPassive(type, amplifier));
            return this;
        }

        public Builder extraHearts(int hearts) {
            built.extraHearts = hearts;
            return this;
        }

        public Builder bowDamageBonus(double bonus) {
            built.bowDamageBonus = bonus;
            return this;
        }

        public Builder explosionImmunity(double fraction) {
            built.explosionImmunity = fraction;
            return this;
        }

        public Builder fallImmunity(boolean immune) {
            built.fallImmunity = immune;
            return this;
        }

        public Builder wallClimbing(boolean enabled) {
            built.wallClimbing = enabled;
            return this;
        }

        public Builder flight(boolean enabled) {
            built.flight = enabled;
            return this;
        }

        public Builder arrowTrail(boolean enabled) {
            built.arrowTrail = enabled;
            return this;
        }

        public Builder cloudJumps(boolean enabled) {
            built.cloudJumps = enabled;
            return this;
        }

        public Builder pearlDamageImmunity(boolean enabled) {
            built.pearlDamageImmunity = enabled;
            return this;
        }

        public Builder burningAura(boolean enabled) {
            built.burningAura = enabled;
            return this;
        }

        public Builder ability(AbilitySpec spec) {
            built.ability = spec;
            return this;
        }

        public Builder task(TaskType type, List<EntityType> mobs, int count, String description) {
            built.task = new TaskSpec(type, mobs == null ? List.of() : mobs, count, description);
            return this;
        }

        public Builder task(TaskType type, int count, String description) {
            return task(type, null, count, description);
        }

        public Builder cost(Material material, int amount) {
            built.cost.add(new MaterialCost(material, amount));
            return this;
        }

        public Builder passiveDescription(String description) {
            built.passiveDescription = description;
            return this;
        }

        public TokenTier build() {
            if (built.task == null) {
                built.task = new TaskSpec(TaskType.KILLS, List.of(), 100, "Kill mobs");
            }
            return built;
        }
    }
}
