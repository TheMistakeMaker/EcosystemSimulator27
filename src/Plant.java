import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Plant {
    private static int nextId = 1;

    private final int id;
    private final int generation;
    private final EnumMap<PlantGene, Double> genes;

    private int parentId;
    private int row;
    private int col;

    private String speciesName;
    private SpreadMethod spreadMethod;

    private boolean alive;
    private int ageTicks;
    private int reproductionCooldown;

    private double health;
    private double calories;
    private double storedWater;
    private double height;

    private int variationCount;
    private String variationSummary;

    public Plant(
            int row,
            int col,
            Map<PlantGene, Double> customGenes,
            String speciesName,
            int generation,
            SpreadMethod spreadMethod
    ) {
        this.id = nextId++;
        this.row = row;
        this.col = col;
        this.speciesName = speciesName;
        this.generation = generation;
        this.spreadMethod = spreadMethod == null ? SpreadMethod.LOCAL_SEEDS : spreadMethod;
        this.parentId = -1;

        this.genes = PlantGeneDefaults.createDefaultGenes();

        if (customGenes != null) {
            genes.putAll(customGenes);
        }

        PlantGeneDefaults.cleanGenes(genes);

        this.alive = true;
        this.ageTicks = 0;
        this.reproductionCooldown = 0;

        this.health = getGene(PlantGene.MAX_HEALTH);
        this.calories = getGene(PlantGene.STARTING_CALORIES);
        this.storedWater = getGene(PlantGene.MAX_WATER_STORAGE) * getGene(PlantGene.STARTING_WATER_RATIO);
        this.height = getGene(PlantGene.STARTING_HEIGHT);

        this.variationCount = 0;
        this.variationSummary = "No recorded variation";
    }

    public void update(WorldModel world) {
        if (!alive) {
            return;
        }

        ageTicks++;
        decrementReproductionCooldown();

        absorbWater(world);
        photosynthesize(world);
        payMaintenanceCosts();
        handleCrowding(world);
        handleAging();

        if (health <= 0) {
            die(world);
            return;
        }

        chooseAction(world);

        if (health <= 0) {
            die(world);
        }
    }

    private void decrementReproductionCooldown() {
        if (reproductionCooldown > 0) {
            reproductionCooldown--;
        }
    }

    private void chooseAction(WorldModel world) {
        double waterRatio = waterRatio();
        double calorieRatio = calorieRatio();
        double healthRatio = healthRatio();
        double heightRatio = heightRatio();

        boolean criticallyStressed = waterRatio < 0.15
                || calorieRatio < 0.10
                || healthRatio < 0.35;

        boolean immature = ageTicks < getGene(PlantGene.REPRODUCTION_AGE_TICKS)
                || height < getGene(PlantGene.SEED_HEIGHT_REQUIRED);

        if (criticallyStressed) {
            idleUnderStress();
            return;
        }

        if (immature) {
            grow();
            return;
        }

        double stress = (1.0 - waterRatio) * 0.45
                + (1.0 - calorieRatio) * 0.30
                + (1.0 - healthRatio) * 0.25;

        double growthScore = calculateGrowthScore();
        double reproductionScore = calculateReproductionScore();
        double survivalScore = getGene(PlantGene.SURVIVAL_PRIORITY) * stress;
        double repairScore = getGene(PlantGene.REPAIR_PRIORITY) * (1.0 - healthRatio);

        if (canReproduce() && reproductionScore >= growthScore) {
            spreadSeeds(world);
        } else if (repairScore > growthScore && repairScore > reproductionScore) {
            repairDamage();
        } else if (survivalScore > growthScore && survivalScore > reproductionScore) {
            idleUnderStress();
        } else {
            grow();
        }
    }

    private void absorbWater(WorldModel world) {
        double absorbedWater;

        if (hasWaterInRootRange(world)) {
            absorbedWater = getGene(PlantGene.WATER_ABSORPTION_RATE) * getGene(PlantGene.ROOT_DEPTH);
        } else {
            absorbedWater = getGene(PlantGene.WATER_ABSORPTION_RATE) * getGene(PlantGene.DRY_SOIL_WATER_FACTOR);
        }

        storedWater = Math.min(getGene(PlantGene.MAX_WATER_STORAGE), storedWater + absorbedWater);
    }

    private boolean hasWaterInRootRange(WorldModel world) {
        int searchRange = (int) Math.round(getGene(PlantGene.ROOT_DEPTH));

        for (int r = row - searchRange; r <= row + searchRange; r++) {
            for (int c = col - searchRange; c <= col + searchRange; c++) {
                if (world.isValidTile(r, c) && world.getTileType(r, c) == TileType.WATER) {
                    return true;
                }
            }
        }

        return false;
    }

    private void photosynthesize(WorldModel world) {
        if (storedWater <= 0) {
            health -= getGene(PlantGene.DEHYDRATION_DAMAGE);
            return;
        }

        double sun = world.getSunValue();
        int nearbyPlants = world.countPlantsNear(row, col, 1);

        double shadePenalty = nearbyPlants * getGene(PlantGene.LIGHT_COMPETITION_PENALTY);
        double heightAdvantage = 0.50 + height / getGene(PlantGene.MAX_HEIGHT);
        double localLight = clamp(sun * heightAdvantage - shadePenalty, 0.0, 1.5);

        double leafArea = getGene(PlantGene.LEAF_AREA);
        double efficiency = getGene(PlantGene.PHOTOSYNTHESIS_EFFICIENCY);
        double heightFactor = Math.sqrt(height + 0.20);

        double caloriesMade = getGene(PlantGene.PHOTOSYNTHESIS_RATE)
                * localLight
                * leafArea
                * efficiency
                * heightFactor;

        double waterCost = getGene(PlantGene.PHOTOSYNTHESIS_WATER_COST)
                * leafArea
                * efficiency
                * heightFactor;

        calories = Math.min(getGene(PlantGene.MAX_CALORIES), calories + caloriesMade);
        storedWater = Math.max(0, storedWater - waterCost);
    }

    private void payMaintenanceCosts() {
        double totalCost = calculateMaintenanceCost();

        calories -= totalCost;

        if (calories <= 0) {
            calories = 0;
            health -= getGene(PlantGene.STARVATION_DAMAGE);
        }
    }

    private double calculateMaintenanceCost() {
        double efficiency = getGene(PlantGene.PHOTOSYNTHESIS_EFFICIENCY);

        return getGene(PlantGene.BASE_METABOLISM)
                + height * getGene(PlantGene.HEIGHT_MAINTENANCE_COST)
                + getGene(PlantGene.LEAF_AREA) * getGene(PlantGene.LEAF_MAINTENANCE_COST)
                + getGene(PlantGene.ROOT_DEPTH) * getGene(PlantGene.ROOT_MAINTENANCE_COST)
                + getGene(PlantGene.TOXICITY) * getGene(PlantGene.TOXICITY_MAINTENANCE_COST)
                + efficiency * efficiency * getGene(PlantGene.EFFICIENCY_MAINTENANCE_COST);
    }

    private void grow() {
        if (height >= getGene(PlantGene.MAX_HEIGHT)) {
            return;
        }

        double calorieCost = getGene(PlantGene.GROWTH_CALORIE_COST);
        double waterCost = getGene(PlantGene.GROWTH_WATER_COST);

        if (calories >= calorieCost && storedWater >= waterCost) {
            calories -= calorieCost;
            storedWater -= waterCost;
            height = Math.min(getGene(PlantGene.MAX_HEIGHT), height + getGene(PlantGene.GROWTH_RATE));
        }
    }

    private void idleUnderStress() {
        // The plant has already paid maintenance this tick. This intentionally performs no extra action.
    }

    private void repairDamage() {
        if (health >= getGene(PlantGene.MAX_HEALTH)) {
            return;
        }

        double calorieCost = getGene(PlantGene.REPAIR_CALORIE_COST);
        double waterCost = getGene(PlantGene.REPAIR_WATER_COST);

        if (calories >= calorieCost && storedWater >= waterCost) {
            calories -= calorieCost;
            storedWater -= waterCost;
            health = Math.min(getGene(PlantGene.MAX_HEALTH), health + getGene(PlantGene.REPAIR_RATE));
        }
    }

    private void handleCrowding(WorldModel world) {
        int nearbyPlants = world.countPlantsNear(row, col, 1);

        if (nearbyPlants > getGene(PlantGene.CROWDING_TOLERANCE)) {
            health -= (nearbyPlants - getGene(PlantGene.CROWDING_TOLERANCE))
                    * getGene(PlantGene.CROWDING_DAMAGE);
        }
    }

    private void handleAging() {
        if (ageTicks > getGene(PlantGene.MAX_AGE_TICKS)) {
            health -= getGene(PlantGene.OLD_AGE_DAMAGE);
        }
    }

    public boolean canReproduce() {
        return alive
                && ageTicks >= getGene(PlantGene.REPRODUCTION_AGE_TICKS)
                && reproductionCooldown <= 0
                && calories >= getGene(PlantGene.SEED_CALORIES_REQUIRED)
                && storedWater >= getGene(PlantGene.SEED_WATER_REQUIRED)
                && health >= getGene(PlantGene.SEED_HEALTH_REQUIRED)
                && height >= getGene(PlantGene.SEED_HEIGHT_REQUIRED);
    }

    public void spreadSeeds(WorldModel world) {
        int seedCount = (int) Math.round(getGene(PlantGene.SEED_COUNT));

        for (int i = 0; i < seedCount; i++) {
            int[] target = findSeedTarget(world);

            if (target != null) {
                Plant child = createChildPlant(target);
                world.addPlant(child);
            }
        }

        calories = Math.max(0, calories - seedCount * getGene(PlantGene.SEED_CALORIE_COST));
        storedWater = Math.max(0, storedWater - seedCount * getGene(PlantGene.SEED_WATER_COST));
        reproductionCooldown = (int) Math.round(getGene(PlantGene.REPRODUCTION_COOLDOWN_TICKS));
    }

    private Plant createChildPlant(int[] target) {
        EnumMap<PlantGene, Double> childGenes = PlantMutation.createChildGenes(genes);
        SpreadMethod childSpreadMethod = PlantMutation.possiblyMutateSpreadMethod(
                spreadMethod,
                getGene(PlantGene.SPREAD_METHOD_MUTATION_RATE)
        );

        Plant child = new Plant(
                target[0],
                target[1],
                childGenes,
                speciesName,
                generation + 1,
                childSpreadMethod
        );

        child.parentId = id;
        child.setVariationRecord(genes, spreadMethod, "Inherited mutation");

        return child;
    }

    private int[] findSeedTarget(WorldModel world) {
        int spreadDistance = (int) Math.round(getGene(PlantGene.SEED_SPREAD_DISTANCE));
        return spreadMethod.findTarget(world, row, col, spreadDistance);
    }

    public double beEaten(double requestedCalories, Animal eater, WorldModel world) {
        if (!alive || requestedCalories <= 0) {
            return 0;
        }

        double eatenCalories = Math.min(requestedCalories, calories);
        calories -= eatenCalories;

        double actualCalories = eatenCalories * getGene(PlantGene.NUTRITION_MULTIPLIER);

        if (eater != null && getGene(PlantGene.TOXICITY) > 0 && world != null) {
            eater.takeDamage(getGene(PlantGene.TOXICITY), world);
        }

        health -= eatenCalories * getGene(PlantGene.EATING_DAMAGE_MULTIPLIER);

        if (calories <= 0 || health <= 0) {
            alive = false;
        }

        return actualCalories;
    }

    void setVariationRecord(Map<PlantGene, Double> originalGenes, SpreadMethod originalMethod, String label) {
        variationCount = PlantMutation.countDifferences(originalGenes, genes, originalMethod, spreadMethod);
        variationSummary = label + ": "
                + PlantMutation.summarizeDifferences(originalGenes, genes, originalMethod, spreadMethod);
    }

    private void die(WorldModel world) {
        alive = false;

        if (world != null) {
            world.removePlant(this);
        }
    }

    public Map<String, String> getInspectionData() {
        Map<String, String> data = new LinkedHashMap<>();

        data.put("ID", String.valueOf(id));
        data.put("Species", speciesName);
        data.put("Generation", String.valueOf(generation));
        data.put("Parent ID", String.valueOf(parentId));
        data.put("Position", "(" + row + ", " + col + ")");
        data.put("Alive", String.valueOf(alive));
        data.put("Age", String.valueOf(ageTicks));
        data.put("Spread Method", spreadMethod.toString());
        data.put("Health", format(health) + " / " + format(getGene(PlantGene.MAX_HEALTH)));
        data.put("Calories", format(calories) + " / " + format(getGene(PlantGene.MAX_CALORIES)));
        data.put("Stored Water", format(storedWater) + " / " + format(getGene(PlantGene.MAX_WATER_STORAGE)));
        data.put("Height", format(height) + " / " + format(getGene(PlantGene.MAX_HEIGHT)));
        data.put("Reproduction Ready", String.valueOf(canReproduce()));
        data.put("Growth Score", format(calculateGrowthScore()));
        data.put("Reproduction Score", format(calculateReproductionScore()));
        data.put("Tradeoff Burden", format(calculateTradeoffBurden()));
        data.put("Variation Count", String.valueOf(variationCount));
        data.put("Variation Summary", variationSummary);

        for (PlantGene gene : PlantGene.values()) {
            data.put("Gene: " + gene, format(genes.get(gene)));
        }

        return data;
    }

    private double calculateGrowthScore() {
        return getGene(PlantGene.GROWTH_PRIORITY)
                * (1.0 - heightRatio())
                * calorieRatio()
                * waterRatio();
    }

    private double calculateReproductionScore() {
        return getGene(PlantGene.REPRODUCTION_PRIORITY)
                * heightRatio()
                * calorieRatio()
                * waterRatio()
                * healthRatio();
    }

    private double calculateTradeoffBurden() {
        double efficiency = getGene(PlantGene.PHOTOSYNTHESIS_EFFICIENCY);

        return efficiency * efficiency * getGene(PlantGene.EFFICIENCY_MAINTENANCE_COST)
                + getGene(PlantGene.LEAF_AREA) * getGene(PlantGene.LEAF_MAINTENANCE_COST)
                + getGene(PlantGene.ROOT_DEPTH) * getGene(PlantGene.ROOT_MAINTENANCE_COST)
                + getGene(PlantGene.TOXICITY) * getGene(PlantGene.TOXICITY_MAINTENANCE_COST);
    }

    private double waterRatio() {
        return safeRatio(storedWater, getGene(PlantGene.MAX_WATER_STORAGE));
    }

    private double calorieRatio() {
        return safeRatio(calories, getGene(PlantGene.MAX_CALORIES));
    }

    private double healthRatio() {
        return safeRatio(health, getGene(PlantGene.MAX_HEALTH));
    }

    private double heightRatio() {
        return safeRatio(height, getGene(PlantGene.MAX_HEIGHT));
    }

    private double safeRatio(double value, double maximum) {
        if (maximum <= 0) {
            return 0;
        }

        return clamp(value / maximum, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String format(double value) {
        return String.format("%.4f", value);
    }

    public int getId() {
        return id;
    }

    public int getGeneration() {
        return generation;
    }

    public int getParentId() {
        return parentId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public SpreadMethod getSpreadMethod() {
        return spreadMethod;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getAgeTicks() {
        return ageTicks;
    }

    public int getReproductionCooldown() {
        return reproductionCooldown;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public double getHealth() {
        return health;
    }

    public double getCalories() {
        return calories;
    }

    public double getStoredWater() {
        return storedWater;
    }

    public double getHeight() {
        return height;
    }

    public double getGene(PlantGene gene) {
        if (!genes.containsKey(gene)) {
            throw new IllegalArgumentException("Missing plant gene: " + gene);
        }

        return genes.get(gene);
    }

    public Map<PlantGene, Double> getGenes() {
        return new EnumMap<>(genes);
    }

    public enum SpreadMethod {
        LOCAL_SEEDS {
            public int[] findTarget(WorldModel world, int row, int col, int distance) {
                return world.findOpenPlantTileNear(row, col, distance);
            }
        },

        WIND_SEEDS {
            public int[] findTarget(WorldModel world, int row, int col, int distance) {
                return world.findOpenPlantTileNear(row, col, distance * 2);
            }
        },

        WATER_SEEDS {
            public int[] findTarget(WorldModel world, int row, int col, int distance) {
                int[] target = world.findOpenPlantTileNearWater(row, col, distance * 2);

                if (target != null) {
                    return target;
                }

                return world.findOpenPlantTileNear(row, col, distance);
            }
        };

        public abstract int[] findTarget(WorldModel world, int row, int col, int distance);
    }
}