public enum PlantGene {
    MAX_HEALTH(80.0, 10.0, 500.0),
    MAX_CALORIES(120.0, 10.0, 500.0),
    MAX_WATER_STORAGE(100.0, 10.0, 500.0),

    STARTING_CALORIES(35.0, 0.0, 500.0),
    STARTING_WATER_RATIO(0.55, 0.0, 1.0),
    STARTING_HEIGHT(0.25, 0.0, 12.0),

    MAX_HEIGHT(5.0, 1.0, 12.0),
    GROWTH_RATE(0.035, 0.001, 1.0),
    GROWTH_CALORIE_COST(1.6, 0.0, 100.0),
    GROWTH_WATER_COST(0.9, 0.0, 100.0),
    LEAF_AREA(1.0, 0.30, 4.0),
    ROOT_DEPTH(2.0, 0.50, 6.0),

    PHOTOSYNTHESIS_RATE(0.85, 0.001, 10.0),
    PHOTOSYNTHESIS_EFFICIENCY(1.0, 0.25, 2.25),
    PHOTOSYNTHESIS_WATER_COST(0.18, 0.0, 10.0),

    WATER_ABSORPTION_RATE(1.15, 0.0, 10.0),
    DRY_SOIL_WATER_FACTOR(0.22, 0.0, 1.0),

    BASE_METABOLISM(0.12, 0.0, 10.0),
    HEIGHT_MAINTENANCE_COST(0.07, 0.0, 10.0),
    LEAF_MAINTENANCE_COST(0.09, 0.0, 10.0),
    ROOT_MAINTENANCE_COST(0.06, 0.0, 10.0),
    EFFICIENCY_MAINTENANCE_COST(0.10, 0.0, 10.0),

    LIGHT_COMPETITION_PENALTY(0.12, 0.0, 1.0),
    CROWDING_TOLERANCE(3.0, 0.0, 20.0),
    CROWDING_DAMAGE(0.30, 0.0, 100.0),

    TOXICITY(0.0, 0.0, 35.0),
    TOXICITY_MAINTENANCE_COST(0.03, 0.0, 10.0),
    NUTRITION_MULTIPLIER(1.0, 0.0, 1.0),
    EATING_DAMAGE_MULTIPLIER(0.20, 0.0, 10.0),

    STARVATION_DAMAGE(0.55, 0.0, 100.0),
    DEHYDRATION_DAMAGE(0.85, 0.0, 100.0),
    OLD_AGE_DAMAGE(0.20, 0.0, 100.0),
    MAX_AGE_TICKS(3000.0, 1.0, 100000.0),

    REPAIR_RATE(0.30, 0.0, 100.0),
    REPAIR_CALORIE_COST(1.0, 0.0, 100.0),
    REPAIR_WATER_COST(0.6, 0.0, 100.0),

    REPRODUCTION_AGE_TICKS(120.0, 0.0, 100000.0),
    REPRODUCTION_COOLDOWN_TICKS(160.0, 0.0, 100000.0),
    SEED_CALORIES_REQUIRED(75.0, 0.0, 500.0),
    SEED_WATER_REQUIRED(25.0, 0.0, 500.0),
    SEED_HEALTH_REQUIRED(50.0, 0.0, 500.0),
    SEED_HEIGHT_REQUIRED(1.6, 0.0, 12.0),

    SEED_COUNT(2.0, 1.0, 5.0),
    SEED_SPREAD_DISTANCE(3.0, 1.0, 8.0),
    SEED_CALORIE_COST(35.0, 0.0, 500.0),
    SEED_WATER_COST(14.0, 0.0, 500.0),

    GROWTH_PRIORITY(0.55, 0.05, 1.5),
    REPRODUCTION_PRIORITY(0.38, 0.05, 1.5),
    SURVIVAL_PRIORITY(0.70, 0.05, 1.5),
    REPAIR_PRIORITY(0.20, 0.05, 1.5),

    MUTATION_RATE(0.06, 0.0, 0.25),
    MUTATION_STRENGTH(0.03, 0.0, 0.20),
    SPREAD_METHOD_MUTATION_RATE(0.02, 0.0, 0.20);

    private final double defaultValue;
    private final double minimumValue;
    private final double maximumValue;

    PlantGene(double defaultValue, double minimumValue, double maximumValue) {
        this.defaultValue = defaultValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
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
}