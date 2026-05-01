public enum AnimalGene {
    MAX_HEALTH("maxHealth", 200.0, 1.0, 10000.0),
    MAX_CALORIES("maxCalories", 300.0, 1.0, 10000.0),
    MAX_HYDRATION("maxHydration", 100.0, 1.0, 10000.0),
    MAX_ENERGY("maxEnergy", 200.0, 1.0, 10000.0),

    STARTING_CALORIES_RATIO("startingCaloriesRatio", 0.9, 0.0, 1.0),
    STARTING_HYDRATION_RATIO("startingHydrationRatio", 0.82, 0.0, 1.0),
    STARTING_ENERGY_RATIO("startingEnergyRatio", 0.78, 0.0, 1.0),

    BODY_SIZE("bodySize", 1.0, 0.10, 20.0),
    BODY_FAT_RATIO("bodyFatRatio", 0.18, 0.01, 0.80),
    MUSCLE_DENSITY("muscleDensity", 1.0, 0.10, 10.0),
    BONE_DENSITY("boneDensity", 1.0, 0.10, 10.0),
    HIDE_THICKNESS("hideThickness", 1.0, 0.0, 10.0),
    BLOOD_CLOTTING("bloodClotting", 1.0, 0.0, 10.0),
    IMMUNE_STRENGTH("immuneStrength", 1.0, 0.0, 10.0),
    PAIN_TOLERANCE("painTolerance", 0.50, 0.0, 1.0),
    WOUND_RECOVERY_RATE("woundRecoveryRate", 0.05, 0.0, 10.0),

    METABOLISM_RATE("metabolismRate", 0.32, 0.0, 1000.0),
    HYDRATION_LOSS_RATE("hydrationLossRate", 0.22, 0.0, 1000.0),
    ENERGY_RECOVERY_RATE("energyRecoveryRate", 1.2, 0.0, 1000.0),
    REST_HEALING_RATE("restHealingRate", 0.08, 0.0, 100.0),
    DIGESTION_RATE("digestionRate", 1.0, 0.0, 100.0),
    ENERGY_TO_CALORIE_EFFICIENCY("energyToCalorieEfficiency", 0.20, 0.0, 10.0),
    THERMAL_EFFICIENCY("thermalEfficiency", 1.0, 0.0, 10.0),

    STARVATION_DAMAGE("starvationDamage", 1.5, 0.0, 1000.0),
    DEHYDRATION_DAMAGE("dehydrationDamage", 2.0, 0.0, 1000.0),
    OLD_AGE_DAMAGE("oldAgeDamage", 2.0, 0.0, 1000.0),
    MAX_AGE_TICKS("maxAgeTicks", 2500.0, 1.0, 1000000.0),

    VISION_RANGE("visionRange", 10.0, 0.0, 1000.0),
    SMELL_RANGE("smellRange", 8.0, 0.0, 1000.0),
    HEARING_RANGE("hearingRange", 6.0, 0.0, 1000.0),
    MOTION_SENSITIVITY("motionSensitivity", 0.50, 0.0, 1.0),
    DEPTH_PERCEPTION("depthPerception", 0.50, 0.0, 1.0),
    NIGHT_VISION("nightVision", 0.50, 0.0, 1.0),
    SCENT_DISCRIMINATION("scentDiscrimination", 0.50, 0.0, 1.0),
    ATTENTION_SPAN("attentionSpan", 35.0, 1.0, 10000.0),
    MEMORY_SPAN_TICKS("memorySpanTicks", 180.0, 1.0, 100000.0),
    MEMORY_DECAY_RATE("memoryDecayRate", 0.02, 0.0, 1.0),

    MOVEMENT_ENERGY_COST("movementEnergyCost", 0.6, 0.0, 1000.0),
    MOVEMENT_CALORIE_COST("movementCalorieCost", 0.2, 0.0, 1000.0),
    MAX_SPEED("maxSpeed", 1.0, 0.10, 10.0),
    ACCELERATION("acceleration", 1.0, 0.10, 10.0),
    TURNING_AGILITY("turningAgility", 1.0, 0.10, 10.0),
    STAMINA("stamina", 1.0, 0.10, 10.0),
    TERRAIN_CAUTION("terrainCaution", 0.30, 0.0, 1.0),
    WATER_AVERSION("waterAversion", 0.35, 0.0, 1.0),
    ROCK_AVERSION("rockAversion", 0.55, 0.0, 1.0),
    EXPLORATION_DRIVE("explorationDrive", 0.35, 0.0, 1.0),
    WANDER_NOISE("wanderNoise", 0.30, 0.0, 1.0),

    ATTACK_RANGE("attackRange", 2.0, 0.0, 1000.0),
    ATTACK_DAMAGE("attackDamage", 20.0, 0.0, 1000.0),
    ATTACK_ACCURACY("attackAccuracy", 0.75, 0.0, 1.0),
    ATTACK_ENERGY_COST("attackEnergyCost", 5.0, 0.0, 1000.0),
    ATTACK_CALORIE_COST("attackCalorieCost", 2.0, 0.0, 1000.0),
    DEFENSE("defense", 1.0, 0.0, 1000.0),
    EVASION("evasion", 0.10, 0.0, 1.0),
    BITE_FORCE("biteForce", 1.0, 0.0, 100.0),
    CLAW_SHARPNESS("clawSharpness", 1.0, 0.0, 100.0),
    STRIKE_SPEED("strikeSpeed", 1.0, 0.0, 100.0),
    GRAPPLE_STRENGTH("grappleStrength", 1.0, 0.0, 100.0),
    CRITICAL_HIT_CHANCE("criticalHitChance", 0.05, 0.0, 1.0),
    CRITICAL_HIT_MULTIPLIER("criticalHitMultiplier", 1.50, 1.0, 10.0),
    EDIBLE_CALORIES_RATIO("edibleCaloriesRatio", 0.65, 0.0, 1.0),

    AGGRESSION("aggression", 0.50, 0.0, 1.0),
    FEARFULNESS("fearfulness", 0.50, 0.0, 1.0),
    CURIOSITY("curiosity", 0.40, 0.0, 1.0),
    NEOPHOBIA("neophobia", 0.30, 0.0, 1.0),
    PATIENCE("patience", 0.50, 0.0, 1.0),
    IMPULSIVITY("impulsivity", 0.35, 0.0, 1.0),
    RISK_TOLERANCE("riskTolerance", 0.45, 0.0, 1.0),
    SOCIALITY("sociality", 0.45, 0.0, 1.0),
    TERRITORIALITY("territoriality", 0.35, 0.0, 1.0),
    DOMINANCE("dominance", 0.45, 0.0, 1.0),
    SUBMISSIVENESS("submissiveness", 0.35, 0.0, 1.0),
    LEARNING_RATE("learningRate", 0.25, 0.0, 1.0),
    STRESS_SENSITIVITY("stressSensitivity", 0.45, 0.0, 1.0),
    HUNGER_URGENCY("hungerUrgency", 0.75, 0.0, 1.0),
    THIRST_URGENCY("thirstUrgency", 0.75, 0.0, 1.0),
    REPRODUCTION_DRIVE("reproductionDrive", .75, 0.0, 1.0),
    REST_DRIVE("restDrive", 0.35, 0.0, 1.0),
    PAIN_AVERSION("painAversion", 0.55, 0.0, 1.0),
    FOCUS_STABILITY("focusStability", 0.45, 0.0, 1.0),
    FRUSTRATION_RECOVERY("frustrationRecovery", 0.03, 0.0, 1.0),

    HERDING_DRIVE("herdingDrive", 0.45, 0.0, 1.0),
    HERD_DISTANCE_PREFERENCE("herdDistancePreference", 3.0, 0.0, 100.0),
    ISOLATION_STRESS("isolationStress", 0.35, 0.0, 1.0),
    GROUP_SAFETY_BONUS("groupSafetyBonus", 0.25, 0.0, 1.0),
    ALARM_CALL_TENDENCY("alarmCallTendency", 0.30, 0.0, 1.0),

    PLANT_BITE_SIZE("plantBiteSize", 18.0, 0.0, 1000.0),
    PLANT_DIGESTION_EFFICIENCY("plantDigestionEfficiency", 0.85, 0.0, 10.0),
    LEAF_AREA_PREFERENCE("leafAreaPreference", 1.25, 0.0, 20.0),
    LEAF_AREA_PICKINESS("leafAreaPickiness", 0.50, 0.0, 10.0),
    PLANT_HEIGHT_PREFERENCE("plantHeightPreference", 1.75, 0.0, 20.0),
    PLANT_HEIGHT_PICKINESS("plantHeightPickiness", 0.35, 0.0, 10.0),
    PLANT_CALORIE_PREFERENCE("plantCaloriePreference", 65.0, 0.0, 1000.0),
    PLANT_CALORIE_PICKINESS("plantCaloriePickiness", 0.35, 0.0, 10.0),
    PLANT_TOXICITY_AVERSION("plantToxicityAversion", 0.75, 0.0, 10.0),
    ROOT_FORAGING_TENDENCY("rootForagingTendency", 0.10, 0.0, 1.0),
    GRAZING_PATIENCE("grazingPatience", 0.45, 0.0, 1.0),
    OVERGRAZE_TOLERANCE("overgrazeTolerance", 0.35, 0.0, 1.0),

    PREFERRED_PREDATOR_DISTANCE("preferredPredatorDistance", 7.0, 0.0, 1000.0),
    PANIC_DISTANCE("panicDistance", 3.0, 0.0, 1000.0),
    FLIGHT_INITIATION_DISTANCE("flightInitiationDistance", 5.0, 0.0, 1000.0),
    ESCAPE_ZIGZAG("escapeZigzag", 0.35, 0.0, 1.0),
    FREEZE_RESPONSE("freezeResponse", 0.10, 0.0, 1.0),
    HIDING_TENDENCY("hidingTendency", 0.20, 0.0, 1.0),
    PREDATOR_MEMORY_WEIGHT("predatorMemoryWeight", 0.60, 0.0, 1.0),

    PREY_SIZE_PREFERENCE("preySizePreference", 1.0, 0.0, 25.0),
    PREY_SIZE_PICKINESS("preySizePickiness", 0.35, 0.0, 10.0),
    PREY_FAT_PREFERENCE("preyFatPreference", 0.22, 0.0, 1.0),
    PREY_FAT_PICKINESS("preyFatPickiness", 0.50, 0.0, 10.0),
    PREY_WEAKNESS_PREFERENCE("preyWeaknessPreference", 0.65, 0.0, 1.0),
    PREY_YOUTH_PREFERENCE("preyYouthPreference", 0.20, 0.0, 1.0),
    PREY_DISTANCE_AVERSION("preyDistanceAversion", 0.08, 0.0, 10.0),
    CHASE_PERSISTENCE("chasePersistence", 0.8, 0.0, 1.0),
    AMBUSH_PREFERENCE("ambushPreference", 0.5, 0.0, 1.0),
    STALKING_DISTANCE("stalkingDistance", 2.0, 0.0, 1000.0),
    STRIKE_PATIENCE("strikePatience", 0.45, 0.0, 1.0),
    PACK_HUNTING_DRIVE("packHuntingDrive", 0.20, 0.0, 1.0),
    SCAVENGING_PREFERENCE("scavengingPreference", 0.25, 0.0, 1.0),
    CARRION_DETECTION_RANGE("carrionDetectionRange", 5.0, 0.0, 1000.0),
    KILL_SECURITY_DISTANCE("killSecurityDistance", 2.0, 0.0, 1000.0),

    WATER_SEEK_THRESHOLD("waterSeekThreshold", 0.75, 0.0, 1.0),
    FOOD_SEEK_THRESHOLD("foodSeekThreshold", 0.75, 0.0, 1.0),
    ENERGY_REST_THRESHOLD("energyRestThreshold", 0.25, 0.0, 1.0),
    HEALTH_RETREAT_THRESHOLD("healthRetreatThreshold", 0.35, 0.0, 1.0),
    DRINK_AMOUNT("drinkAmount", 25.0, 0.0, 1000.0),

    REPRODUCTION_AGE_TICKS("reproductionAgeTicks", 90.0, 0.0, 1000000.0),
    REPRODUCTION_COOLDOWN_TICKS("reproductionCooldownTicks", 330.0, 0.0, 1000000.0),
    REPRODUCTION_CALORIES_REQUIRED("reproductionCaloriesRequired", 82.0, 0.0, 10000.0),
    REPRODUCTION_HYDRATION_REQUIRED("reproductionHydrationRequired", 68.0, 0.0, 10000.0),
    REPRODUCTION_ENERGY_REQUIRED("reproductionEnergyRequired", 72.0, 0.0, 10000.0),
    REPRODUCTION_HEALTH_REQUIRED("reproductionHealthRequired", 76.0, 0.0, 10000.0),
    REPRODUCTION_CALORIES_COST("reproductionCaloriesCost", 44.0, 0.0, 10000.0),
    REPRODUCTION_ENERGY_COST("reproductionEnergyCost", 42.0, 0.0, 10000.0),
    MATE_SELECTIVITY("mateSelectivity", 0.35, 0.0, 1.0),
    MATE_VIGOR_PREFERENCE("mateVigorPreference", 0.45, 0.0, 1.0),
    MATE_NOVELTY_PREFERENCE("mateNoveltyPreference", 0.15, 0.0, 1.0),
    PARENTAL_INVESTMENT("parentalInvestment", 0.20, 0.0, 1.0),

    MUTATION_RATE("mutationRate", 0.1, 0.0, 1.0),
    MUTATION_STRENGTH("mutationStrength", 0.1, 0.0, 1.0),
    FOUNDER_VARIATION_STRENGTH("founderVariationStrength", 0.12, 0.0, 1.0),
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
