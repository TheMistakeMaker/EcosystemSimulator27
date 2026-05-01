import java.util.LinkedHashMap;
import java.util.Map;

public class Herbivore extends Animal {
    private Animal lastThreat;
    private Plant lastFoodTarget;
    private int calmTicks;
    private int panicTicks;
    private java.util.List<Animal> lastWorldSnapshot;
    private WorldModel lastKnownWorld;

    public Herbivore(int row, int col, Map<String, Double> customGenes, String speciesName, int generation) {
        super(row, col, customGenes, speciesName, generation);
        this.lastThreat = null;
        this.lastFoodTarget = null;
        this.calmTicks = 0;
        this.panicTicks = 0;
        this.lastWorldSnapshot = new java.util.ArrayList<>();
        this.lastKnownWorld = null;
    }

    @Override
    protected void decideAction(WorldModel world) {
        lastKnownWorld = world;
        lastWorldSnapshot = world.getAnimalsSnapshot();
        Animal threat = findMostRelevantThreat(world);
        updateEmotionalState(threat);

        if (shouldFreeze(threat)) {
            rest();
            return;
        }

        if (shouldFlee(threat, world)) {
            panicTicks++;
            moveAwayWithSpeed(threat.getRow(), threat.getCol(), world);
            return;
        }

        if (AnimalGeneEffects.waterSeekingDrive(this) > 0.85) {
            if (seekWater(world)) {
                return;
            }
        }

        if (AnimalGeneEffects.restDesire(this) > 0.85 || AnimalGeneEffects.healthRetreatDrive(this) > 0.95) {
            rest();
            return;
        }

        if (isReproductivelyReady() && RANDOM.nextDouble() < getGene("reproductionDrive")) {
            Animal mate = world.findNearestMate(this, AnimalGeneEffects.detectionRange(this, "social"));

            if (mate != null) {
                if (distanceTo(mate) <= 1.5) {
                    reproduceWith(mate, world);
                } else {
                    moveTowardWithSpeed(mate.getRow(), mate.getCol(), world);
                }
                return;
            }
        }

        if (AnimalGeneEffects.foodSeekingDrive(this) > 0.80 || RANDOM.nextDouble() < getGene("grazingPatience")) {
            if (graze(world)) {
                return;
            }
        }

        if (shouldRegroup(world)) {
            Animal herdMate = findNearestHerdMate(world);
            if (herdMate != null) {
                moveTowardWithSpeed(herdMate.getRow(), herdMate.getCol(), world);
                return;
            }
        }

        exploratoryMove(world);
    }

    @Override
    public boolean canEatAnimal(Animal other) {
        return false;
    }

    @Override
    protected Animal createChild(int row, int col, Map<String, Double> childGenes, String childSpeciesName, int childGeneration) {
        Herbivore child = new Herbivore(row, col, childGenes, childSpeciesName, childGeneration);
        child.setVariationRecord(getGenes(), "Inherited herbivore recombination");
        return child;
    }

    @Override
    public String getAnimalType() {
        return "Herbivore";
    }

    @Override
    public Map<String, String> getInspectionData() {
        Map<String, String> data = new LinkedHashMap<>(super.getInspectionData());
        data.put("Behavior State", panicTicks > calmTicks ? "Vigilant / flight-biased" : "Grazing / exploratory");
        data.put("Panic Ticks", String.valueOf(panicTicks));
        data.put("Calm Ticks", String.valueOf(calmTicks));
        data.put("Last Threat ID", lastThreat == null ? "none" : String.valueOf(lastThreat.getId()));
        data.put("Last Food Target ID", lastFoodTarget == null ? "none" : String.valueOf(lastFoodTarget.getId()));
        data.put("Predator Distance Preference", format(getGene("preferredPredatorDistance")));
        data.put("Leaf Area Preference", format(getGene("leafAreaPreference")));
        data.put("Plant Calorie Preference", format(getGene("plantCaloriePreference")));
        return data;
    }

    private void updateEmotionalState(Animal threat) {
        if (threat == null) {
            calmTicks++;
            rememberedDanger = clamp(rememberedDanger - getGene("learningRate") * 0.006, 0.0, 1.0);
            return;
        }

        lastThreat = threat;
        double distanceFactor = 1.0 - clamp(distanceTo(threat) / Math.max(1.0, getGene("preferredPredatorDistance")), 0.0, 1.0);
        rememberedDanger = clamp(
                rememberedDanger + distanceFactor * getGene("predatorMemoryWeight") * getGene("fearfulness"),
                0.0,
                1.0
        );
        confidence = clamp(confidence - distanceFactor * getGene("stressSensitivity") * 0.05, 0.0, 1.0);
    }

    private boolean shouldFreeze(Animal threat) {
        if (threat == null || lastKnownWorld == null) {
            return false;
        }

        return RANDOM.nextDouble() < AnimalGeneEffects.freezeChance(this, threat, lastKnownWorld);
    }


    private boolean shouldFlee(Animal threat, WorldModel world) {
        if (threat == null) {
            return false;
        }

        int allies = AnimalGeneEffects.nearbyAllies(this, world, Math.max(1.0, getGene("herdDistancePreference")));
        return AnimalGeneEffects.flightPressure(this, threat, world, allies) > getGene("riskTolerance") * 0.75;
    }


    private boolean seekWater(WorldModel world) {
        int[] water = world.findNearestWater(row, col, AnimalGeneEffects.detectionRange(this, "water"));

        if (water == null) {
            return false;
        }

        if (distanceTo(water[0], water[1]) <= 1.5) {
            drink(AnimalGeneEffects.drinkAmount(this));
            return true;
        }

        return moveTowardWithSpeed(water[0], water[1], world);
    }

    private boolean graze(WorldModel world) {
        Plant plant = findBestPlant(world);

        if (plant == null) {
            frustration = clamp(frustration + 0.04, 0.0, 1.0);
            return false;
        }

        lastFoodTarget = plant;

        if (plant.getRow() == row && plant.getCol() == col) {
            double requestedCalories = AnimalGeneEffects.herbivoreBiteSize(this, plant);
            double rawCalories = world.eatPlantAt(row, col, requestedCalories, this);
            double gained = AnimalGeneEffects.digestedPlantCalories(this, plant, rawCalories);
            gainCalories(gained);
            energy = Math.min(getGene("maxEnergy"), energy + gained * getGene("energyToCalorieEfficiency"));
            return true;
        }

        return moveTowardWithSpeed(plant.getRow(), plant.getCol(), world);
    }

    private Plant findBestPlant(WorldModel world) {
        Plant best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double vision = AnimalGeneEffects.detectionRange(this, "food");

        for (Plant plant : world.getPlantsSnapshot()) {
            if (!plant.isAlive()) {
                continue;
            }

            double distance = distanceTo(plant.getRow(), plant.getCol());

            if (distance > vision) {
                continue;
            }

            double score = scorePlant(plant, distance);

            if (score > bestScore) {
                bestScore = score;
                best = plant;
            }
        }

        return best;
    }

    private double scorePlant(Plant plant, double distance) {
        int nearbyHerbivores = countNearbyHerbivores(plant.getRow(), plant.getCol(), 3);
        return AnimalGeneEffects.herbivorePlantScore(this, plant, distance, nearbyHerbivores);
    }

    private int countNearbyHerbivores(int centerRow, int centerCol, int range) {
        int count = 0;

        for (Animal animal : rememberedWorldAnimals()) {
            if (animal instanceof Herbivore && animal.isAlive()) {
                int rowDifference = animal.getRow() - centerRow;
                int colDifference = animal.getCol() - centerCol;

                if (rowDifference * rowDifference + colDifference * colDifference <= range * range) {
                    count++;
                }
            }
        }

        return count;
    }

    private java.util.List<Animal> rememberedWorldAnimals() {
        return lastWorldSnapshot;
    }


    private Animal findMostRelevantThreat(WorldModel world) {
        Animal bestThreat = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double range = AnimalGeneEffects.detectionRange(this, "threat");

        for (Animal animal : world.getAnimalsSnapshot()) {
            if (animal == this || !animal.isAlive() || !animal.canEatAnimal(this)) {
                continue;
            }

            double distance = distanceTo(animal);

            if (distance > range) {
                continue;
            }

            double score = AnimalGeneEffects.predatorThreatScore(this, animal, distance);

            if (score > bestScore) {
                bestScore = score;
                bestThreat = animal;
            }
        }

        return bestThreat;
    }

    private double localHerdSafety(WorldModel world, Animal threat) {
        if (threat == null) {
            return 0;
        }

        return AnimalGeneEffects.herdSafety(this, threat, AnimalGeneEffects.nearbyAllies(this, world, Math.max(1.0, getGene("herdDistancePreference"))));
    }


    private boolean shouldRegroup(WorldModel world) {
        if (getGene("herdingDrive") <= 0.05 || rememberedDanger > 0.80) {
            return false;
        }

        Animal herdMate = findNearestHerdMate(world);
        if (herdMate == null) {
            return false;
        }

        double distance = distanceTo(herdMate);
        return distance > getGene("herdDistancePreference") && RANDOM.nextDouble() < getGene("sociality") * getGene("herdingDrive") + AnimalGeneEffects.socialStress(this, world);
    }

    private Animal findNearestHerdMate(WorldModel world) {
        Animal nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Animal animal : world.getAnimalsSnapshot()) {
            if (animal == this || !animal.isAlive() || !getAnimalType().equals(animal.getAnimalType())) {
                continue;
            }

            double distance = distanceTo(animal);

            if (distance <= AnimalGeneEffects.detectionRange(this, "social") && distance < nearestDistance) {
                nearestDistance = distance;
                nearest = animal;
            }
        }

        return nearest;
    }

    private void exploratoryMove(WorldModel world) {
        if (RANDOM.nextDouble() < AnimalGeneEffects.explorationChance(this, world)) {
            moveRandomly(world);
        } else {
            rest();
        }
    }
}
