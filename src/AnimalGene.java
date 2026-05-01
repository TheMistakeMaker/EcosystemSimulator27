public enum AnimalGene {
    MAX_HEALTH("maxHealth", 100.0, 1.0, 10000.0),
    MAX_CALORIES("maxCalories", 100.0, 1.0, 10000.0),
    MAX_HYDRATION("maxHydration", 100.0, 1.0, 10000.0),
    MAX_ENERGY("maxEnergy", 100.0, 1.0, 10000.0),

    STARTING_CALORIES_RATIO("startingCaloriesRatio", 0.75, 0.0, 1.0),
    STARTING_HYDRATION_RATIO("startingHydrationRatio", 0.75, 0.0, 1.0),
    STARTING_ENERGY_RATIO("startingEnergyRatio", 0.75, 0.0, 1.0),

    METABOLISM_RATE("metabolismRate", 0.35, 0.0, 1000.0),
    HYDRATION_LOSS_RATE("hydrationLossRate", 0.25, 0.0, 1000.0),
    ENERGY_RECOVERY_RATE("energyRecoveryRate", 1.2, 0.0, 1000.0),

    STARVATION_DAMAGE("starvationDamage", 1.5, 0.0, 1000.0),
    DEHYDRATION_DAMAGE("dehydrationDamage", 2.0, 0.0, 1000.0),
    OLD_AGE_DAMAGE("oldAgeDamage", 0.5, 0.0, 1000.0),
    MAX_AGE_TICKS("maxAgeTicks", 2500.0, 1.0, 1000000.0),

    VISION_RANGE("visionRange", 8.0, 0.0, 1000.0),

    MOVEMENT_ENERGY_COST("movementEnergyCost", 1.0, 0.0, 1000.0),
    MOVEMENT_CALORIE_COST("movementCalorieCost", 0.25, 0.0, 1000.0),

    ATTACK_RANGE("attackRange", 1.0, 0.0, 1000.0),
    ATTACK_DAMAGE("attackDamage", 20.0, 0.0, 1000.0),
    ATTACK_ACCURACY("attackAccuracy", 0.75, 0.0, 1.0),
    ATTACK_ENERGY_COST("attackEnergyCost", 5.0, 0.0, 1000.0),
    ATTACK_CALORIE_COST("attackCalorieCost", 2.0, 0.0, 1000.0),
    DEFENSE("defense", 1.0, 0.0, 1000.0),
    EVASION("evasion", 0.10, 0.0, 1.0),

    EDIBLE_CALORIES_RATIO("edibleCaloriesRatio", 0.65, 0.0, 1.0),

    REPRODUCTION_AGE_TICKS("reproductionAgeTicks", 200.0, 0.0, 1000000.0),
    REPRODUCTION_COOLDOWN_TICKS("reproductionCooldownTicks", 250.0, 0.0, 1000000.0),
    REPRODUCTION_CALORIES_REQUIRED("reproductionCaloriesRequired", 70.0, 0.0, 10000.0),
    REPRODUCTION_HYDRATION_REQUIRED("reproductionHydrationRequired", 60.0, 0.0, 10000.0),
    REPRODUCTION_ENERGY_REQUIRED("reproductionEnergyRequired", 60.0, 0.0, 10000.0),
    REPRODUCTION_HEALTH_REQUIRED("reproductionHealthRequired", 70.0, 0.0, 10000.0),
    REPRODUCTION_CALORIES_COST("reproductionCaloriesCost", 35.0, 0.0, 10000.0),
    REPRODUCTION_ENERGY_COST("reproductionEnergyCost", 35.0, 0.0, 10000.0),

    MUTATION_RATE("mutationRate", 0.04, 0.0, 1.0),
    MUTATION_STRENGTH("mutationStrength", 0.03, 0.0, 1.0),
    MAX_MATING_GENETIC_DISTANCE("maxMatingGeneticDistance", 0.25, 0.0, 1000.0);

    private final String key;
    private final double defaultValue;
    private final double minimumValue;
    private final double maximumValue;

    AnimalGene(String key, double defaultValue, double minimumValue, double maximumValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public String getKey() {
        return key;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public double cleanValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = defaultValue;
        }

        return Math.max(minimumValue, Math.min(maximumValue, value));
    }

    public static boolean isKnownGene(String key) {
        return findByKey(key) != null;
    }

    public static AnimalGene findByKey(String key) {
        if (key == null) {
            return null;
        }

        for (AnimalGene gene : values()) {
            if (gene.key.equals(key)) {
                return gene;
            }
        }

        return null;
    }
}