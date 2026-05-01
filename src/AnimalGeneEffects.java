public class AnimalGeneEffects {
    private AnimalGeneEffects() {
    }

    public static double startingCalories(Animal animal) {
        double storage = 1.0 + animal.getGene("bodyFatRatio") * 0.35 + animal.getGene("bodySize") * 0.04;
        double metabolicDrag = 1.0 - animal.getGene("metabolismRate") * 0.015;
        return animal.getGene("maxCalories")
                * animal.getGene("startingCaloriesRatio")
                * clamp(storage * metabolicDrag, 0.25, 1.25);
    }

    public static double startingHydration(Animal animal) {
        double bodyWater = 1.0 + animal.getGene("bodySize") * 0.025 - animal.getGene("bodyFatRatio") * 0.12;
        double dryness = 1.0 - animal.getGene("hydrationLossRate") * 0.01;
        return animal.getGene("maxHydration")
                * animal.getGene("startingHydrationRatio")
                * clamp(bodyWater * dryness, 0.25, 1.20);
    }

    public static double startingEnergy(Animal animal) {
        double muscle = 1.0 + animal.getGene("muscleDensity") * 0.08 + animal.getGene("stamina") * 0.04;
        double massDrag = 1.0 - animal.getGene("bodySize") * 0.018 - animal.getGene("bodyFatRatio") * 0.08;
        return animal.getGene("maxEnergy")
                * animal.getGene("startingEnergyRatio")
                * clamp(muscle * massDrag, 0.25, 1.25);
    }

    public static double passiveCaloriesBurn(Animal animal) {
        double bodyMaintenance = 0.55 + animal.getGene("bodySize") * 0.22 + animal.getGene("muscleDensity") * 0.09;
        double insulation = 1.0 - animal.getGene("bodyFatRatio") * 0.15 - animal.getGene("thermalEfficiency") * 0.04;
        double stressLoad = 1.0 + animal.getGene("stressSensitivity") * animal.rememberedDanger * 0.35;
        double curiosityCost = 1.0 + animal.getGene("curiosity") * 0.04 + animal.getGene("attentionSpan") / 1000.0;
        return animal.getGene("metabolismRate") * clamp(bodyMaintenance * insulation * stressLoad * curiosityCost, 0.15, 5.0);
    }

    public static double passiveHydrationLoss(Animal animal) {
        double surfaceArea = 0.70 + animal.getGene("bodySize") * 0.10;
        double metabolismHeat = 1.0 + animal.getGene("metabolismRate") * 0.08 + animal.getGene("maxSpeed") * 0.025;
        double cooling = 1.0 - animal.getGene("thermalEfficiency") * 0.08;
        double fearBreathing = 1.0 + animal.getGene("fearfulness") * animal.rememberedDanger * 0.22;
        return animal.getGene("hydrationLossRate") * clamp(surfaceArea * metabolismHeat * cooling * fearBreathing, 0.10, 5.0);
    }

    public static double starvationDamage(Animal animal) {
        double reserveProtection = 1.0 - animal.getGene("bodyFatRatio") * 0.45;
        double metabolismPressure = 1.0 + animal.getGene("metabolismRate") * 0.20;
        double immuneBuffer = 1.0 - animal.getGene("immuneStrength") * 0.025;
        double painBuffer = 1.0 - animal.getGene("painTolerance") * 0.10;
        return animal.getGene("starvationDamage") * clamp(reserveProtection * metabolismPressure * immuneBuffer * painBuffer, 0.05, 4.0);
    }

    public static double dehydrationDamage(Animal animal) {
        double reserveProtection = 1.0 + animal.getGene("bodyFatRatio") * 0.10;
        double thermalProtection = 1.0 - animal.getGene("thermalEfficiency") * 0.06;
        double urgencyStress = 1.0 + animal.getGene("thirstUrgency") * animal.getGene("stressSensitivity") * 0.18;
        double immuneBuffer = 1.0 - animal.getGene("immuneStrength") * 0.018;
        return animal.getGene("dehydrationDamage") * clamp(reserveProtection * thermalProtection * urgencyStress * immuneBuffer, 0.05, 4.0);
    }

    public static double oldAgeDamage(Animal animal) {
        double ageRatio = animal.safeRatio(animal.getAgeTicks(), animal.getGene("maxAgeTicks"));
        double structuralBuffer = 1.0 - animal.getGene("boneDensity") * 0.018 - animal.getGene("immuneStrength") * 0.018;
        double bodyBurden = 1.0 + animal.getGene("bodySize") * 0.025 + animal.getGene("metabolismRate") * 0.05;
        return animal.getGene("oldAgeDamage") * clamp((0.35 + ageRatio) * structuralBuffer * bodyBurden, 0.01, 5.0);
    }

    public static double energyRecovery(Animal animal) {
        double staminaRecovery = 1.0 + animal.getGene("stamina") * 0.10 + animal.getGene("thermalEfficiency") * 0.04;
        double restWillingness = 1.0 + animal.getGene("restDrive") * 0.50 + animal.getGene("patience") * 0.16;
        double anxietyPenalty = 1.0 - animal.rememberedDanger * animal.getGene("stressSensitivity") * 0.35;
        double painPenalty = 1.0 - (1.0 - animal.healthRatio()) * animal.getGene("painAversion") * 0.25;
        return animal.getGene("energyRecoveryRate") * clamp(staminaRecovery * restWillingness * anxietyPenalty * painPenalty, 0.05, 4.0);
    }

    public static double healingRate(Animal animal) {
        double repair = animal.getGene("restHealingRate") + animal.getGene("woundRecoveryRate");
        double clotting = 1.0 + animal.getGene("bloodClotting") * 0.04;
        double immune = 1.0 + animal.getGene("immuneStrength") * 0.06;
        double nutrition = 0.35 + animal.caloriesRatio() * 0.65;
        double hydration = 0.35 + animal.hydrationRatio() * 0.65;
        return repair * clamp(clotting * immune * nutrition * hydration, 0.05, 6.0);
    }

    public static double movementEnergyCost(Animal animal, WorldModel world, int row, int col) {
        double terrain = terrainPenalty(animal, world, row, col);
        double bodyLoad = 0.65 + animal.getGene("bodySize") * 0.22 + animal.getGene("bodyFatRatio") * 0.28;
        double gaitEfficiency = 1.0 - animal.getGene("turningAgility") * 0.05 - animal.getGene("acceleration") * 0.03;
        double staminaBuffer = 1.0 - animal.getGene("stamina") * 0.035;
        double speedLoad = 1.0 + Math.max(0, animal.getGene("maxSpeed") - 1.0) * 0.18;
        return animal.getGene("movementEnergyCost") * terrain * clamp(bodyLoad * gaitEfficiency * staminaBuffer * speedLoad, 0.05, 8.0);
    }

    public static double movementCalorieCost(Animal animal, WorldModel world, int row, int col) {
        double terrain = terrainPenalty(animal, world, row, col);
        double muscleBurn = 1.0 + animal.getGene("muscleDensity") * 0.06 + animal.getGene("acceleration") * 0.05;
        double metabolicBurn = 1.0 + animal.getGene("metabolismRate") * 0.12;
        double efficiency = 1.0 - animal.getGene("energyToCalorieEfficiency") * 0.04;
        return animal.getGene("movementCalorieCost") * terrain * clamp(muscleBurn * metabolicBurn * efficiency, 0.05, 8.0);
    }

    public static double terrainPenalty(Animal animal, WorldModel world, int row, int col) {
        double base = world.getMovementCost(row, col);
        TileType terrain = world.getTileType(row, col);
        double caution = 1.0 + animal.getGene("terrainCaution") * 0.35;

        if (terrain == TileType.WATER) {
            return base * caution * (1.0 + animal.getGene("waterAversion") * 1.25 - animal.getGene("curiosity") * 0.12);
        }

        if (terrain == TileType.ROCK) {
            return base * caution * (1.0 + animal.getGene("rockAversion") * 1.25 - animal.getGene("turningAgility") * 0.08);
        }

        return base * caution;
    }

    public static int movementSteps(Animal animal, boolean fleeing) {
        double locomotion = animal.getGene("maxSpeed")
                + animal.getGene("acceleration") * 0.28
                + animal.getGene("stamina") * 0.18
                + animal.getGene("turningAgility") * 0.10;
        double massPenalty = animal.getGene("bodySize") * 0.11 + animal.getGene("bodyFatRatio") * 0.30;
        double fearBoost = fleeing ? animal.getGene("fearfulness") * animal.getGene("stressSensitivity") * 0.80 : 0.0;
        double focusPenalty = (1.0 - animal.getGene("focusStability")) * 0.18;
        return Math.max(1, (int) Math.round(clamp(locomotion + fearBoost - massPenalty - focusPenalty, 1.0, 5.0)));
    }

    public static double detectionRange(Animal animal, String purpose) {
        double vision = animal.getGene("visionRange") * (0.78 + animal.getGene("nightVision") * 0.18 + animal.getGene("depthPerception") * 0.08);
        double smell = animal.getGene("smellRange") * (0.28 + animal.getGene("scentDiscrimination") * 0.33);
        double hearing = animal.getGene("hearingRange") * (0.20 + animal.getGene("motionSensitivity") * 0.24);
        double attention = clamp(animal.getGene("attentionSpan") / 55.0, 0.35, 2.50);
        double focus = 0.65 + animal.getGene("focusStability") * 0.55;
        double curiosity = purpose.equals("food") ? animal.getGene("curiosity") * 0.55 : animal.getGene("curiosity") * 0.22;
        double fear = purpose.equals("threat") ? animal.getGene("fearfulness") * 0.80 : 0.0;
        double neophobia = purpose.equals("novel") ? -animal.getGene("neophobia") * 0.50 : 0.0;
        return Math.max(1.0, (vision + smell + hearing) * attention * focus * (1.0 + curiosity + fear + neophobia));
    }

    public static double memoryRetention(Animal animal) {
        double span = clamp(animal.getGene("memorySpanTicks") / 200.0, 0.15, 10.0);
        double stability = 0.70 + animal.getGene("focusStability") * 0.50 + animal.getGene("learningRate") * 0.35;
        double stressEncoding = 1.0 + animal.getGene("stressSensitivity") * animal.rememberedDanger * 0.40;
        return clamp(span * stability * stressEncoding, 0.10, 15.0);
    }

    public static double dangerMemoryDecay(Animal animal) {
        double base = animal.getGene("memoryDecayRate");
        double retention = memoryRetention(animal);
        double fearRetention = 1.0 - animal.getGene("fearfulness") * animal.getGene("predatorMemoryWeight") * 0.30;
        return clamp(base * fearRetention / retention, 0.0005, 0.25);
    }

    public static double foodMemoryDecay(Animal animal) {
        double base = animal.getGene("memoryDecayRate") * 0.50;
        double learningRetention = 1.0 - animal.getGene("learningRate") * 0.25 - animal.getGene("curiosity") * 0.10;
        return clamp(base * learningRetention / memoryRetention(animal), 0.0005, 0.25);
    }

    public static double drinkAmount(Animal animal) {
        double thirst = 1.0 + animal.getGene("thirstUrgency") * (1.0 - animal.hydrationRatio());
        double bodyDemand = 0.65 + animal.getGene("bodySize") * 0.16 + animal.getGene("metabolismRate") * 0.05;
        return animal.getGene("drinkAmount") * clamp(thirst * bodyDemand, 0.10, 5.0);
    }

    public static double edibleCorpseCalories(Animal animal) {
        double bodyCalories = animal.getCalories()
                + animal.getGene("bodySize") * 10.0
                + animal.getGene("bodyFatRatio") * animal.getGene("maxCalories") * 0.60
                + animal.getGene("muscleDensity") * 5.0;
        double edibleRatio = animal.getGene("edibleCaloriesRatio");
        return Math.max(0.0, bodyCalories * edibleRatio);
    }

    public static double defendedDamage(Animal victim, double incomingDamage) {
        double armor = victim.getGene("defense")
                + victim.getGene("hideThickness") * 0.70
                + victim.getGene("boneDensity") * 0.45
                + victim.getGene("bloodClotting") * 0.30;
        double painBuffer = 1.0 - victim.getGene("painTolerance") * 0.18;
        double immuneShock = 1.0 - victim.getGene("immuneStrength") * 0.018;
        double evasionLuck = 1.0 - victim.getGene("evasion") * 0.10;
        return Math.max(0.0, (incomingDamage - armor) * clamp(painBuffer * immuneShock * evasionLuck, 0.05, 2.0));
    }

    public static double reproductionCaloriesRequired(Animal animal) {
        double bodyBurden = 1.0 + animal.getGene("bodySize") * 0.08 + animal.getGene("parentalInvestment") * 0.40;
        double fatReserve = 1.0 - animal.getGene("bodyFatRatio") * 0.12;
        return animal.getGene("reproductionCaloriesRequired") * clamp(bodyBurden * fatReserve, 0.25, 4.0);
    }

    public static double reproductionHydrationRequired(Animal animal) {
        double bodyWater = 1.0 + animal.getGene("bodySize") * 0.05 + animal.getGene("parentalInvestment") * 0.16;
        double thirstStrictness = 1.0 + animal.getGene("thirstUrgency") * 0.10;
        return animal.getGene("reproductionHydrationRequired") * clamp(bodyWater * thirstStrictness, 0.25, 4.0);
    }

    public static double reproductionEnergyRequired(Animal animal) {
        double vigor = 1.0 + animal.getGene("mateVigorPreference") * 0.20 + animal.getGene("reproductionDrive") * 0.10;
        double staminaBuffer = 1.0 - animal.getGene("stamina") * 0.025;
        return animal.getGene("reproductionEnergyRequired") * clamp(vigor * staminaBuffer, 0.25, 4.0);
    }

    public static double reproductionHealthRequired(Animal animal) {
        double selectivity = 1.0 + animal.getGene("mateSelectivity") * 0.25;
        double pain = 1.0 + animal.getGene("painAversion") * 0.20;
        double dominance = 1.0 + animal.getGene("dominance") * 0.10 - animal.getGene("submissiveness") * 0.05;
        return animal.getGene("reproductionHealthRequired") * clamp(selectivity * pain * dominance, 0.25, 4.0);
    }

    public static double reproductionCaloriesCost(Animal animal) {
        return animal.getGene("reproductionCaloriesCost")
                * clamp(1.0 + animal.getGene("parentalInvestment") * 0.55 + animal.getGene("bodySize") * 0.08, 0.25, 5.0);
    }

    public static double reproductionEnergyCost(Animal animal) {
        return animal.getGene("reproductionEnergyCost")
                * clamp(1.0 + animal.getGene("parentalInvestment") * 0.35 + animal.getGene("reproductionDrive") * 0.08, 0.25, 5.0);
    }

    public static int reproductionCooldown(Animal animal) {
        double patience = 1.0 + animal.getGene("parentalInvestment") * 0.40 + animal.getGene("mateSelectivity") * 0.12;
        double drive = 1.0 - animal.getGene("reproductionDrive") * 0.22;
        return Math.max(1, (int) Math.round(animal.getGene("reproductionCooldownTicks") * clamp(patience * drive, 0.20, 5.0)));
    }

    public static boolean canMate(Animal first, Animal second) {
        double distance = first.geneticDistanceTo(second);
        double tolerance = first.getGene("maxMatingGeneticDistance")
                * (1.0 + first.getGene("mateNoveltyPreference") * 0.35 - first.getGene("mateSelectivity") * 0.22);
        double socialThreshold = 0.10 + first.getGene("sociality") * 0.60 + first.getGene("submissiveness") * 0.08;
        double rivalry = first.getGene("dominance") * second.getGene("dominance") * 0.10;
        return distance <= tolerance && mateScore(first, second) + socialThreshold - rivalry > first.getGene("mateSelectivity") * 0.25;
    }

    public static double mateScore(Animal searcher, Animal candidate) {
        double vigor = (candidate.healthRatio() + candidate.energyRatio() + candidate.caloriesRatio()) / 3.0;
        double novelty = clamp(searcher.geneticDistanceTo(candidate), 0.0, 1.0);
        double compatibility = 1.0 - novelty * searcher.getGene("mateSelectivity");
        double noveltyBonus = novelty * searcher.getGene("mateNoveltyPreference");
        double vigorBonus = vigor * searcher.getGene("mateVigorPreference");
        double socialBonus = searcher.getGene("sociality") * candidate.getGene("sociality") * 0.15;
        double dominanceTension = Math.abs(searcher.getGene("dominance") - candidate.getGene("dominance")) * 0.12;
        double submissiveEase = (searcher.getGene("submissiveness") + candidate.getGene("submissiveness")) * 0.04;
        return compatibility + noveltyBonus + vigorBonus + socialBonus + submissiveEase - dominanceTension;
    }

    public static double herbivorePlantScore(Animal animal, Plant plant, double distance, int nearbyHerbivores) {
        double leafScore = preferenceScore(animal,
                plant.getGene(PlantGene.LEAF_AREA),
                animal.getGene("leafAreaPreference"),
                animal.getGene("leafAreaPickiness"));
        double heightScore = preferenceScore(animal,
                plant.getHeight(),
                animal.getGene("plantHeightPreference"),
                animal.getGene("plantHeightPickiness"));
        double calorieScore = preferenceScore(animal,
                plant.getCalories(),
                animal.getGene("plantCaloriePreference"),
                animal.getGene("plantCaloriePickiness"));
        double nutrition = plant.getGene(PlantGene.NUTRITION_MULTIPLIER) * animal.getGene("plantDigestionEfficiency");
        double toxicity = plant.getGene(PlantGene.TOXICITY) * animal.getGene("plantToxicityAversion") * (1.0 + animal.getGene("neophobia") * 0.40);
        double rootBonus = plant.getGene(PlantGene.ROOT_DEPTH) * animal.getGene("rootForagingTendency") * 0.22;
        double overgrazePenalty = Math.max(0, nearbyHerbivores - plant.getGene(PlantGene.CROWDING_TOLERANCE))
                * (1.0 - animal.getGene("overgrazeTolerance")) * 0.22;
        double noveltyPenalty = plantNovelty(animal, plant) * animal.getGene("neophobia") * 0.75;
        double curiosityBonus = plantNovelty(animal, plant) * animal.getGene("curiosity") * 0.30;
        double distancePenalty = distance * (0.04 + animal.getGene("terrainCaution") * 0.02 + animal.frustration * 0.03);
        return leafScore * 1.45
                + heightScore * 0.80
                + calorieScore * 1.30
                + nutrition
                + rootBonus
                + curiosityBonus
                - toxicity
                - overgrazePenalty
                - noveltyPenalty
                - distancePenalty;
    }

    public static double plantNovelty(Animal animal, Plant plant) {
        double leafDifference = Math.abs(plant.getGene(PlantGene.LEAF_AREA) - animal.getGene("leafAreaPreference"))
                / Math.max(1.0, animal.getGene("leafAreaPreference"));
        double heightDifference = Math.abs(plant.getHeight() - animal.getGene("plantHeightPreference"))
                / Math.max(1.0, animal.getGene("plantHeightPreference"));
        double calorieDifference = Math.abs(plant.getCalories() - animal.getGene("plantCaloriePreference"))
                / Math.max(1.0, animal.getGene("plantCaloriePreference"));
        double toxinSurprise = plant.getGene(PlantGene.TOXICITY) / 35.0;
        return clamp((leafDifference + heightDifference + calorieDifference + toxinSurprise) / 4.0, 0.0, 1.0);
    }

    public static double herbivoreBiteSize(Animal animal, Plant plant) {
        double hunger = 1.0 + animal.getGene("hungerUrgency") * (1.0 - animal.caloriesRatio());
        double patience = 0.80 + animal.getGene("grazingPatience") * 0.35;
        double rootForage = 1.0 + animal.getGene("rootForagingTendency") * plant.getGene(PlantGene.ROOT_DEPTH) * 0.08;
        double overgraze = 1.0 + animal.getGene("overgrazeTolerance") * 0.30;
        double mouth = 0.65 + animal.getGene("bodySize") * 0.20;
        return animal.getGene("plantBiteSize") * clamp(hunger * patience * rootForage * overgraze * mouth, 0.10, 6.0);
    }

    public static double digestedPlantCalories(Animal animal, Plant plant, double eatenCalories) {
        double digest = animal.getGene("plantDigestionEfficiency") * animal.getGene("digestionRate");
        double toxicityPenalty = 1.0 - plant.getGene(PlantGene.TOXICITY) * animal.getGene("plantToxicityAversion") * 0.012;
        double nutritionBonus = 0.25 + plant.getGene(PlantGene.NUTRITION_MULTIPLIER);
        double neophobiaStress = 1.0 - animal.getGene("neophobia") * plantNovelty(animal, plant) * 0.10;
        return eatenCalories * clamp(digest * toxicityPenalty * nutritionBonus * neophobiaStress, 0.0, 5.0);
    }

    public static double flightPressure(Animal herbivore, Animal threat, WorldModel world, int localAllies) {
        double distance = herbivore.distanceTo(threat);
        double preferred = Math.max(0.1, herbivore.getGene("preferredPredatorDistance"));
        double panic = distance <= herbivore.getGene("panicDistance") ? 1.0 : 0.0;
        double initiation = clamp((herbivore.getGene("flightInitiationDistance") - distance) / preferred, 0.0, 1.0);
        double fearMemory = herbivore.rememberedDanger * herbivore.getGene("predatorMemoryWeight");
        double alarm = herbivore.getGene("alarmCallTendency") * Math.min(1.0, localAllies / 5.0) * 0.25;
        double hiding = herbivore.getGene("hidingTendency") * nearbyRockOrWaterCover(herbivore, world) * 0.30;
        double herdSafety = herdSafety(herbivore, threat, localAllies);
        double personality = herbivore.getGene("fearfulness") + herbivore.getGene("stressSensitivity") - herbivore.getGene("riskTolerance");
        return clamp(panic + initiation + fearMemory + alarm + personality * 0.25 - hiding - herdSafety, 0.0, 3.0);
    }

    public static double freezeChance(Animal herbivore, Animal threat, WorldModel world) {
        double distancePressure = 1.0 - clamp(herbivore.distanceTo(threat) / Math.max(1.0, herbivore.getGene("panicDistance")), 0.0, 1.0);
        double cover = nearbyRockOrWaterCover(herbivore, world);
        return clamp(herbivore.getGene("freezeResponse")
                * herbivore.getGene("fearfulness")
                * (1.0 - herbivore.getGene("riskTolerance"))
                * (1.0 + cover * herbivore.getGene("hidingTendency"))
                * (0.25 + distancePressure), 0.0, 0.95);
    }

    public static double herdSafety(Animal herbivore, Animal threat, int localAllies) {
        double allyScore = clamp(localAllies / 5.0, 0.0, 1.0);
        double spacing = clamp(herbivore.distanceTo(threat) / Math.max(1.0, herbivore.getGene("preferredPredatorDistance")), 0.0, 1.0);
        double herdComfort = 1.0 - Math.abs(localAllies - herbivore.getGene("herdDistancePreference"))
                / Math.max(1.0, herbivore.getGene("herdDistancePreference") + 1.0);
        double isolationPenalty = localAllies == 0 ? herbivore.getGene("isolationStress") : 0.0;
        return clamp(herbivore.getGene("herdingDrive")
                * herbivore.getGene("sociality")
                * herbivore.getGene("groupSafetyBonus")
                * (allyScore + herdComfort) * 0.5
                * spacing
                - isolationPenalty * 0.15, 0.0, 1.0);
    }

    public static double predatorThreatScore(Animal herbivore, Animal predator, double distance) {
        double detection = detectionRange(herbivore, "threat");
        double closeness = 1.0 - clamp(distance / Math.max(1.0, detection), 0.0, 1.0);
        double lethality = predator.getGene("attackDamage")
                + predator.getGene("biteForce") * 2.0
                + predator.getGene("clawSharpness")
                + predator.getGene("strikeSpeed");
        double escapeConfidence = herbivore.getGene("evasion")
                + herbivore.getGene("turningAgility") * 0.08
                + herbivore.getGene("maxSpeed") * 0.04;
        return closeness * (1.0 + lethality / Math.max(1.0, herbivore.getGene("maxHealth")))
                + herbivore.rememberedDanger * herbivore.getGene("predatorMemoryWeight")
                - escapeConfidence * 0.20;
    }

    public static double predatorPreyScore(Animal predator, Animal prey, double distance, double detectionRange, int packAllies) {
        double sizeScore = preferenceScore(predator, prey.getGene("bodySize"), predator.getGene("preySizePreference"), predator.getGene("preySizePickiness"));
        double fatScore = preferenceScore(predator, prey.getGene("bodyFatRatio"), predator.getGene("preyFatPreference"), predator.getGene("preyFatPickiness"));
        double weaknessScore = (1.0 - prey.healthRatio()) * predator.getGene("preyWeaknessPreference");
        double youthScore = (1.0 - prey.safeRatio(prey.getAgeTicks(), prey.getGene("maxAgeTicks"))) * predator.getGene("preyYouthPreference");
        double distanceScore = 1.0 - clamp(distance / Math.max(1.0, detectionRange), 0.0, 1.0);
        double distancePenalty = distance * predator.getGene("preyDistanceAversion") * 0.025;
        double packBonus = Math.min(1.0, packAllies / 3.0) * predator.getGene("packHuntingDrive") * 0.50;
        double neophobiaPenalty = preyNovelty(predator, prey) * predator.getGene("neophobia") * 0.40;
        double curiosityBonus = preyNovelty(predator, prey) * predator.getGene("curiosity") * 0.18;
        double riskPenalty = preyCounterDanger(prey) * (1.0 - predator.getGene("riskTolerance")) * 0.25;
        return sizeScore * 1.20
                + fatScore * 1.55
                + weaknessScore * 1.45
                + youthScore
                + distanceScore
                + packBonus
                + curiosityBonus
                + predator.confidence * predator.getGene("learningRate") * 0.30
                - distancePenalty
                - neophobiaPenalty
                - riskPenalty;
    }

    public static double preyNovelty(Animal predator, Animal prey) {
        double sizeNovelty = Math.abs(prey.getGene("bodySize") - predator.getGene("preySizePreference"))
                / Math.max(1.0, predator.getGene("preySizePreference"));
        double fatNovelty = Math.abs(prey.getGene("bodyFatRatio") - predator.getGene("preyFatPreference"));
        double motionNovelty = Math.abs(prey.getGene("maxSpeed") - predator.getGene("maxSpeed")) / Math.max(1.0, predator.getGene("maxSpeed"));
        double defenseNovelty = preyCounterDanger(prey) / Math.max(1.0, predator.getGene("attackDamage") + predator.getGene("biteForce"));
        return clamp((sizeNovelty + fatNovelty + motionNovelty + defenseNovelty) / 4.0, 0.0, 1.0);
    }

    public static double preyCounterDanger(Animal prey) {
        return prey.getGene("defense")
                + prey.getGene("hideThickness") * 0.45
                + prey.getGene("boneDensity") * 0.22
                + prey.getGene("evasion") * 4.0
                + prey.getGene("maxSpeed") * 0.30
                + prey.getGene("turningAgility") * 0.30;
    }

    public static double attackAccuracy(Animal predator, Animal prey, double distance, int packAllies) {
        double sensoryAim = predator.getGene("attackAccuracy")
                + predator.getGene("depthPerception") * 0.10
                + predator.getGene("motionSensitivity") * 0.05
                + predator.getGene("focusStability") * 0.07
                + predator.getGene("scentDiscrimination") * 0.02;
        double strike = predator.getGene("strikeSpeed") * 0.035 + predator.getGene("acceleration") * 0.025;
        double preyEscape = prey.getGene("evasion")
                + prey.getGene("turningAgility") * 0.05
                + prey.getGene("escapeZigzag") * 0.12
                + prey.getGene("motionSensitivity") * 0.04;
        double fearNoise = predator.frustration * predator.getGene("impulsivity") * 0.10;
        double patienceAim = predator.getGene("patience") * predator.getGene("strikePatience") * 0.08;
        double packAngle = packAllies * predator.getGene("packHuntingDrive") * 0.035;
        double rangePenalty = Math.max(0.0, distance - predator.getGene("attackRange")) * 0.18;
        return clamp(sensoryAim + strike + patienceAim + packAngle - preyEscape - fearNoise - rangePenalty, 0.03, 0.98);
    }

    public static double attackDamage(Animal predator, Animal prey, int packAllies) {
        double base = predator.getGene("attackDamage")
                + predator.getGene("biteForce") * 3.5
                + predator.getGene("clawSharpness") * 2.0
                + predator.getGene("grappleStrength") * 1.4
                + predator.getGene("bodySize") * predator.getGene("muscleDensity") * 1.3;
        double dominance = 1.0 + predator.getGene("dominance") * 0.10 - prey.getGene("dominance") * 0.04;
        double pack = 1.0 + Math.min(3, packAllies) * predator.getGene("packHuntingDrive") * 0.08;
        double preyArmor = prey.getGene("defense") * 0.40 + prey.getGene("hideThickness") * 0.45 + prey.getGene("boneDensity") * 0.25;
        double painEffect = 1.0 - prey.getGene("painTolerance") * 0.10;
        return Math.max(1.0, (base * dominance * pack - preyArmor) * clamp(painEffect, 0.40, 1.20));
    }

    public static boolean criticalHit(Animal predator) {
        double chance = predator.getGene("criticalHitChance")
                * (1.0 + predator.getGene("strikeSpeed") * 0.05 + predator.getGene("focusStability") * 0.10)
                * (1.0 - predator.frustration * 0.25);
        return Animal.RANDOM.nextDouble() < clamp(chance, 0.0, 0.95);
    }

    public static double criticalMultiplier(Animal predator) {
        return predator.getGene("criticalHitMultiplier")
                * clamp(1.0 + predator.getGene("biteForce") * 0.025 + predator.getGene("clawSharpness") * 0.020, 1.0, 3.0);
    }

    public static double attackEnergyCost(Animal predator) {
        double explosion = 1.0 + predator.getGene("acceleration") * 0.10 + predator.getGene("strikeSpeed") * 0.06;
        double staminaBuffer = 1.0 - predator.getGene("stamina") * 0.035;
        double bodyLoad = 1.0 + predator.getGene("bodySize") * 0.08 + predator.getGene("muscleDensity") * 0.04;
        return predator.getGene("attackEnergyCost") * clamp(explosion * staminaBuffer * bodyLoad, 0.10, 5.0);
    }

    public static double attackCalorieCost(Animal predator) {
        double metabolism = 1.0 + predator.getGene("metabolismRate") * 0.12 + predator.getGene("aggression") * 0.08;
        double efficiency = 1.0 - predator.getGene("energyToCalorieEfficiency") * 0.035;
        return predator.getGene("attackCalorieCost") * clamp(metabolism * efficiency, 0.10, 5.0);
    }

    public static double chaseDesire(Animal predator, Animal prey, double distance, int packAllies) {
        double hunger = (1.0 - predator.caloriesRatio()) * predator.getGene("hungerUrgency");
        double aggression = predator.getGene("aggression") * 0.55 + predator.getGene("territoriality") * 0.12;
        double persistence = predator.getGene("chasePersistence") + predator.getGene("stamina") * 0.05;
        double patience = predator.getGene("patience") * 0.12 - predator.getGene("impulsivity") * 0.08;
        double pack = packAllies * predator.getGene("packHuntingDrive") * 0.07;
        double distanceCost = distance * predator.getGene("preyDistanceAversion") * 0.025;
        return clamp(hunger + aggression + persistence + patience + pack - distanceCost - predator.frustration * 0.10, 0.0, 2.0);
    }

    public static boolean shouldStalk(Animal predator, double distance) {
        double stalkingBand = predator.getGene("stalkingDistance") * (1.0 + predator.getGene("ambushPreference") * 0.50);
        double ambushMind = predator.getGene("ambushPreference")
                * predator.getGene("patience")
                * predator.getGene("strikePatience")
                * (1.0 - predator.getGene("impulsivity"));
        double neophobiaWait = predator.getGene("neophobia") * 0.10;
        return distance <= stalkingBand && Animal.RANDOM.nextDouble() < clamp(ambushMind + neophobiaWait, 0.0, 0.95);
    }

    public static double scavengingChance(Animal predator, WorldModel world) {
        double hunger = 1.0 - predator.caloriesRatio();
        double caution = predator.getGene("neophobia") * 0.20 + (1.0 - predator.getGene("riskTolerance")) * 0.15;
        double smell = predator.getGene("smellRange") * predator.getGene("scentDiscrimination") * 0.015;
        double competition = nearbyAnimals(predator, world, predator.getGene("killSecurityDistance"), true) * 0.04;
        return clamp(predator.getGene("scavengingPreference") * hunger + smell - caution - competition, 0.0, 0.95);
    }

    public static double carrionRange(Animal predator) {
        return predator.getGene("carrionDetectionRange")
                + predator.getGene("smellRange") * (0.35 + predator.getGene("scentDiscrimination") * 0.35)
                + predator.getGene("memorySpanTicks") * 0.003;
    }

    public static double carrionBiteSize(Animal predator) {
        double hunger = 1.0 + predator.getGene("hungerUrgency") * (1.0 - predator.caloriesRatio());
        double security = 1.0 - predator.getGene("killSecurityDistance") * 0.015;
        double neophobia = 1.0 - predator.getGene("neophobia") * 0.12;
        return predator.getGene("plantBiteSize") * 1.5 * clamp(hunger * security * neophobia, 0.10, 5.0);
    }

    public static double explorationChance(Animal animal, WorldModel world) {
        double terrainCuriosity = animal.getGene("explorationDrive") + animal.getGene("curiosity") * 0.35;
        double noveltyFear = animal.getGene("neophobia") * (0.20 + animal.rememberedDanger * 0.50);
        double territorialPatrol = animal.getGene("territoriality") * 0.18;
        double wander = animal.getGene("wanderNoise") * 0.50;
        double focus = animal.getGene("focusStability") * 0.12;
        double cover = nearbyRockOrWaterCover(animal, world) * animal.getGene("hidingTendency") * 0.15;
        return clamp(terrainCuriosity + territorialPatrol + wander + cover - noveltyFear - focus, 0.0, 0.95);
    }

    public static double restDesire(Animal animal) {
        double fatigue = (1.0 - animal.energyRatio()) * (1.0 + animal.getGene("restDrive") + animal.getGene("energyRestThreshold"));
        double injury = (1.0 - animal.healthRatio()) * (animal.getGene("painAversion") + animal.getGene("stressSensitivity"));
        double patience = animal.getGene("patience") * 0.20;
        double impulsivePenalty = animal.getGene("impulsivity") * 0.20;
        return clamp(fatigue + injury + patience - impulsivePenalty, 0.0, 2.0);
    }

    public static double waterSeekingDrive(Animal animal) {
        double thirst = (1.0 - animal.hydrationRatio()) * animal.getGene("thirstUrgency");
        double threshold = animal.getGene("waterSeekThreshold");
        double aversion = animal.getGene("waterAversion") * 0.18;
        double dangerDelay = animal.rememberedDanger * animal.getGene("riskTolerance") * 0.12;
        return clamp(thirst + threshold - aversion - dangerDelay, 0.0, 2.0);
    }

    public static double foodSeekingDrive(Animal animal) {
        double hunger = (1.0 - animal.caloriesRatio()) * animal.getGene("hungerUrgency");
        double threshold = animal.getGene("foodSeekThreshold");
        double memory = animal.rememberedFood * animal.getGene("learningRate") * 0.12;
        double impatience = animal.getGene("impulsivity") * 0.10;
        return clamp(hunger + threshold + memory + impatience, 0.0, 2.0);
    }

    public static double healthRetreatDrive(Animal animal) {
        double injury = 1.0 - animal.healthRatio();
        double threshold = animal.getGene("healthRetreatThreshold");
        double pain = animal.getGene("painAversion") * 0.30;
        double risk = 1.0 - animal.getGene("riskTolerance") * 0.20;
        return clamp(injury + threshold + pain * risk, 0.0, 2.0);
    }

    public static double nearbyRockOrWaterCover(Animal animal, WorldModel world) {
        int cover = 0;
        int checked = 0;

        for (int row = animal.getRow() - 1; row <= animal.getRow() + 1; row++) {
            for (int col = animal.getCol() - 1; col <= animal.getCol() + 1; col++) {
                TileType type = world.getTileType(row, col);

                if (type != null) {
                    checked++;
                    if (type == TileType.ROCK || type == TileType.WATER) {
                        cover++;
                    }
                }
            }
        }

        if (checked == 0) {
            return 0.0;
        }

        return cover / (double) checked;
    }

    public static int nearbyAnimals(Animal animal, WorldModel world, double range, boolean anyType) {
        int count = 0;

        for (Animal other : world.getAnimalsSnapshot()) {
            if (other != animal && other.isAlive() && animal.distanceTo(other) <= range) {
                if (anyType || animal.getAnimalType().equals(other.getAnimalType())) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int nearbyAllies(Animal animal, WorldModel world, double range) {
        return nearbyAnimals(animal, world, range, false);
    }

    public static double socialStress(Animal animal, WorldModel world) {
        int allies = nearbyAllies(animal, world, Math.max(1.0, animal.getGene("herdDistancePreference")));
        double isolation = allies == 0 ? animal.getGene("isolationStress") * (1.0 - animal.getGene("territoriality") * 0.25) : 0.0;
        double crowding = Math.max(0, allies - animal.getGene("herdDistancePreference")) * animal.getGene("dominance") * 0.025;
        double socialComfort = allies > 0 ? animal.getGene("sociality") * 0.08 + animal.getGene("submissiveness") * 0.03 : 0.0;
        return clamp(isolation + crowding - socialComfort, -0.25, 1.0);
    }

    public static double preferenceScore(Animal animal, double actual, double preferred, double pickiness) {
        double denominator = Math.max(0.0001, Math.abs(preferred) + 1.0);
        double difference = Math.abs(actual - preferred) / denominator;
        double focus = 0.75 + animal.getGene("focusStability") * 0.35;
        double neophobia = 1.0 + animal.getGene("neophobia") * 0.20;
        return clamp(1.0 - difference * pickiness * focus * neophobia, 0.0, 1.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
