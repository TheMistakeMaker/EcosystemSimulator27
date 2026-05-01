import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public abstract class Animal {
    protected static final Random RANDOM = new Random();

    private static final int[][] CARDINAL_DIRECTIONS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    private static int nextId = 1;

    public enum Sex {
        MALE,
        FEMALE
    }

    protected final int id;
    protected final int generation;

    protected int parentOneId;
    protected int parentTwoId;

    protected int row;
    protected int col;

    protected String speciesName;
    protected Sex sex;
    protected boolean alive;

    protected int ageTicks;
    protected int reproductionCooldown;

    protected double calories;
    protected double hydration;
    protected double energy;
    protected double health;

    protected int variationCount;
    protected String variationSummary;

    protected double rememberedDanger;
    protected double rememberedFood;
    protected double frustration;
    protected double confidence;

    protected final Genome genome;
    protected final Map<String, Double> genes;

    public Animal(int row, int col, Map<String, Double> customGenes, String speciesName, int generation) {
        this.id = nextId++;
        this.row = row;
        this.col = col;
        this.speciesName = speciesName;
        this.generation = generation;

        this.parentOneId = -1;
        this.parentTwoId = -1;

        this.sex = RANDOM.nextBoolean() ? Sex.MALE : Sex.FEMALE;
        this.alive = true;

        this.ageTicks = 0;
        this.reproductionCooldown = 0;

        this.genome = Genome.fromAnimalGenes(customGenes);
        this.genes = genome.backingMap();

        this.calories = AnimalGeneEffects.startingCalories(this);
        this.hydration = AnimalGeneEffects.startingHydration(this);
        this.energy = AnimalGeneEffects.startingEnergy(this);
        this.health = getGene("maxHealth");

        this.variationCount = 0;
        this.variationSummary = "No recorded variation";

        this.rememberedDanger = 0;
        this.rememberedFood = 0;
        this.frustration = 0;
        this.confidence = 0.50;
    }

    public void update(WorldModel world) {
        if (!alive) {
            return;
        }

        ageTicks++;
        decrementReproductionCooldown();
        applyPassiveBiology(world);

        if (!alive) {
            die(world);
            return;
        }

        decideAction(world);
    }

    protected abstract void decideAction(WorldModel world);

    public abstract boolean canEatAnimal(Animal other);

    protected abstract Animal createChild(
            int row,
            int col,
            Map<String, Double> childGenes,
            String childSpeciesName,
            int childGeneration
    );

    public abstract String getAnimalType();

    private void decrementReproductionCooldown() {
        if (reproductionCooldown > 0) {
            reproductionCooldown--;
        }
    }

    protected void applyPassiveBiology(WorldModel world) {
        calories = Math.max(0, calories - AnimalGeneEffects.passiveCaloriesBurn(this));
        hydration = Math.max(0, hydration - AnimalGeneEffects.passiveHydrationLoss(this));

        if (calories <= 0) {
            health -= AnimalGeneEffects.starvationDamage(this);
        }

        if (hydration <= 0) {
            health -= AnimalGeneEffects.dehydrationDamage(this);
        }

        if (ageTicks > getGene("maxAgeTicks")) {
            health -= AnimalGeneEffects.oldAgeDamage(this);
        }

        if (calories > getGene("maxCalories") * 0.25 && hydration > getGene("maxHydration") * 0.25) {
            energy = Math.min(getGene("maxEnergy"), energy + AnimalGeneEffects.energyRecovery(this));
        }

        if (energyRatio() > 0.70 && caloriesRatio() > 0.35 && hydrationRatio() > 0.35) {
            health = Math.min(getGene("maxHealth"), health + AnimalGeneEffects.healingRate(this));
        }

        decayMentalState();

        if (world != null) {
            double socialStress = AnimalGeneEffects.socialStress(this, world);
            rememberedDanger = clamp(rememberedDanger + Math.max(0.0, socialStress) * getGene("stressSensitivity") * 0.015, 0.0, 1.0);
            confidence = clamp(confidence - Math.max(0.0, socialStress) * 0.010 + Math.max(0.0, -socialStress) * 0.006, 0.0, 1.0);
        }

        if (health <= 0) {
            alive = false;
        }
    }

    public boolean moveTo(int newRow, int newCol, WorldModel world) {
        if (!alive || world == null || !world.canAnimalEnter(this, newRow, newCol)) {
            return false;
        }

        double energyCost = AnimalGeneEffects.movementEnergyCost(this, world, newRow, newCol);
        double calorieCost = AnimalGeneEffects.movementCalorieCost(this, world, newRow, newCol);

        if (energy < energyCost || calories < calorieCost) {
            return false;
        }

        int oldRow = row;
        int oldCol = col;

        energy -= energyCost;
        calories -= calorieCost;

        row = newRow;
        col = newCol;

        world.updateAnimalPosition(this, oldRow, oldCol, newRow, newCol);

        return true;
    }

    public boolean moveToward(int targetRow, int targetCol, WorldModel world) {
        int rowChange = Integer.compare(targetRow, row);
        int colChange = Integer.compare(targetCol, col);

        if (rowChange == 0 && colChange == 0) {
            return false;
        }

        if (Math.abs(targetRow - row) >= Math.abs(targetCol - col)) {
            if (rowChange != 0 && moveTo(row + rowChange, col, world)) {
                return true;
            }

            return colChange != 0 && moveTo(row, col + colChange, world);
        }

        if (colChange != 0 && moveTo(row, col + colChange, world)) {
            return true;
        }

        return rowChange != 0 && moveTo(row + rowChange, col, world);
    }

    public boolean moveRandomly(WorldModel world) {
        int start = RANDOM.nextInt(CARDINAL_DIRECTIONS.length);

        for (int i = 0; i < CARDINAL_DIRECTIONS.length; i++) {
            int index = (start + i) % CARDINAL_DIRECTIONS.length;
            int newRow = row + CARDINAL_DIRECTIONS[index][0];
            int newCol = col + CARDINAL_DIRECTIONS[index][1];

            if (moveTo(newRow, newCol, world)) {
                return true;
            }
        }

        return false;
    }

    public void drink(double amount) {
        if (amount <= 0) {
            return;
        }

        hydration = Math.min(getGene("maxHydration"), hydration + amount);
    }

    public void gainCalories(double amount) {
        if (amount <= 0) {
            return;
        }

        calories = Math.min(getGene("maxCalories"), calories + amount);
        rememberedFood = clamp(rememberedFood + amount / Math.max(1.0, getGene("maxCalories")), 0.0, 1.0);
    }

    protected boolean payActionCost(double energyCost, double calorieCost) {
        if (energy < energyCost || calories < calorieCost) {
            return false;
        }

        energy -= Math.max(0, energyCost);
        calories -= Math.max(0, calorieCost);
        return true;
    }

    protected void rest() {
        energy = Math.min(getGene("maxEnergy"), energy + AnimalGeneEffects.energyRecovery(this));

        if (caloriesRatio() > 0.30 && hydrationRatio() > 0.30) {
            health = Math.min(getGene("maxHealth"), health + AnimalGeneEffects.healingRate(this));
        }
    }

    public void takeDamage(double damage, WorldModel world) {
        if (!alive || damage <= 0) {
            return;
        }

        double reducedDamage = AnimalGeneEffects.defendedDamage(this, damage);
        health -= reducedDamage;
        rememberedDanger = clamp(rememberedDanger + reducedDamage / Math.max(1.0, getGene("maxHealth")), 0.0, 1.0);
        confidence = clamp(confidence - reducedDamage / Math.max(1.0, getGene("maxHealth")), 0.0, 1.0);

        if (health <= 0) {
            die(world);
        }
    }

    protected void die(WorldModel world) {
        if (!alive && world == null) {
            return;
        }

        alive = false;

        if (world != null) {
            world.removeAnimal(this);
            world.addCorpse(row, col, AnimalGeneEffects.edibleCorpseCalories(this));
        }
    }

    public boolean canReproduceWith(Animal mate) {
        if (mate == null) {
            return false;
        }

        if (!isReproductivelyReady() || !mate.isReproductivelyReady()) {
            return false;
        }

        if (sex == mate.sex) {
            return false;
        }

        if (!getAnimalType().equals(mate.getAnimalType())) {
            return false;
        }

        return AnimalGeneEffects.canMate(this, mate);
    }

    public boolean isReproductivelyReady() {
        return alive
                && ageTicks >= getGene("reproductionAgeTicks")
                && reproductionCooldown <= 0
                && calories >= AnimalGeneEffects.reproductionCaloriesRequired(this)
                && hydration >= AnimalGeneEffects.reproductionHydrationRequired(this)
                && energy >= AnimalGeneEffects.reproductionEnergyRequired(this)
                && health >= AnimalGeneEffects.reproductionHealthRequired(this);
    }

    public Animal reproduceWith(Animal mate, WorldModel world) {
        if (world == null || !canReproduceWith(mate)) {
            return null;
        }

        int[] openTile = world.findOpenAdjacentTile(row, col);

        if (openTile == null) {
            return null;
        }

        Map<String, Double> childGenes = createMixedMutatedGenes(mate);
        String childSpeciesName = world.determineSpeciesName(this, mate, childGenes);

        payReproductionCost();
        mate.payReproductionCost();

        Animal child = createChild(
                openTile[0],
                openTile[1],
                childGenes,
                childSpeciesName,
                Math.max(generation, mate.generation) + 1
        );

        child.parentOneId = id;
        child.parentTwoId = mate.id;

        world.addAnimal(child);

        return child;
    }

    private void payReproductionCost() {
        calories = Math.max(0, calories - AnimalGeneEffects.reproductionCaloriesCost(this));
        energy = Math.max(0, energy - AnimalGeneEffects.reproductionEnergyCost(this));
        reproductionCooldown = AnimalGeneEffects.reproductionCooldown(this);
    }

    protected Map<String, Double> createMixedMutatedGenes(Animal mate) {
        return Genome.createMixedMutatedGenome(genome, mate.genome, RANDOM).toMap();
    }

    public double geneticDistanceTo(Animal other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }

        return genome.distanceTo(other.genome);
    }

    public Map<String, String> getInspectionData() {
        Map<String, String> data = new LinkedHashMap<>();

        data.put("ID", String.valueOf(id));
        data.put("Animal Type", getAnimalType());
        data.put("Species", speciesName);
        data.put("Sex", sex.toString());
        data.put("Generation", String.valueOf(generation));
        data.put("Parent One ID", String.valueOf(parentOneId));
        data.put("Parent Two ID", String.valueOf(parentTwoId));
        data.put("Position", "(" + row + ", " + col + ")");
        data.put("Alive", String.valueOf(alive));
        data.put("Age", String.valueOf(ageTicks));
        data.put("Health", format(health) + " / " + format(getGene("maxHealth")));
        data.put("Calories", format(calories) + " / " + format(getGene("maxCalories")));
        data.put("Hydration", format(hydration) + " / " + format(getGene("maxHydration")));
        data.put("Energy", format(energy) + " / " + format(getGene("maxEnergy")));
        data.put("Reproduction Cooldown", String.valueOf(reproductionCooldown));
        data.put("Calories Ratio", format(caloriesRatio()));
        data.put("Hydration Ratio", format(hydrationRatio()));
        data.put("Energy Ratio", format(energyRatio()));
        data.put("Health Ratio", format(healthRatio()));
        data.put("Remembered Danger", format(rememberedDanger));
        data.put("Remembered Food", format(rememberedFood));
        data.put("Frustration", format(frustration));
        data.put("Confidence", format(confidence));
        data.put("Derived Detection Range", format(AnimalGeneEffects.detectionRange(this, "food")));
        data.put("Derived Movement Steps", String.valueOf(AnimalGeneEffects.movementSteps(this, false)));
        data.put("Derived Water Drive", format(AnimalGeneEffects.waterSeekingDrive(this)));
        data.put("Derived Food Drive", format(AnimalGeneEffects.foodSeekingDrive(this)));
        data.put("Derived Rest Drive", format(AnimalGeneEffects.restDesire(this)));
        data.put("Derived Health Retreat Drive", format(AnimalGeneEffects.healthRetreatDrive(this)));
        data.put("Variation Count", String.valueOf(variationCount));
        data.put("Variation Summary", variationSummary);

        for (String geneName : genes.keySet()) {
            data.put("Gene: " + geneName, format(genes.get(geneName)));
        }

        return data;
    }

    public double distanceTo(Animal other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }

        return distanceTo(other.row, other.col);
    }

    public double distanceTo(int otherRow, int otherCol) {
        int rowDifference = row - otherRow;
        int colDifference = col - otherCol;

        return Math.sqrt(rowDifference * rowDifference + colDifference * colDifference);
    }

    protected boolean moveAwayFrom(int threatRow, int threatCol, WorldModel world) {
        int rowChange = Integer.compare(row, threatRow);
        int colChange = Integer.compare(col, threatCol);

        if (RANDOM.nextDouble() < getGene("escapeZigzag")) {
            if (rowChange != 0 && moveTo(row + rowChange, col, world)) {
                return true;
            }

            if (colChange != 0 && moveTo(row, col + colChange, world)) {
                return true;
            }
        } else {
            if (colChange != 0 && moveTo(row, col + colChange, world)) {
                return true;
            }

            if (rowChange != 0 && moveTo(row + rowChange, col, world)) {
                return true;
            }
        }

        return moveRandomly(world);
    }

    protected boolean moveTowardWithSpeed(int targetRow, int targetCol, WorldModel world) {
        int steps = AnimalGeneEffects.movementSteps(this, false);
        boolean moved = false;

        for (int i = 0; i < steps; i++) {
            if (!moveToward(targetRow, targetCol, world)) {
                break;
            }
            moved = true;
        }

        return moved;
    }

    protected boolean moveAwayWithSpeed(int targetRow, int targetCol, WorldModel world) {
        int steps = AnimalGeneEffects.movementSteps(this, true);
        boolean moved = false;

        for (int i = 0; i < steps; i++) {
            if (!moveAwayFrom(targetRow, targetCol, world)) {
                break;
            }
            moved = true;
        }

        return moved;
    }

    protected void decayMentalState() {
        rememberedDanger = clamp(rememberedDanger * (1.0 - AnimalGeneEffects.dangerMemoryDecay(this)), 0.0, 1.0);
        rememberedFood = clamp(rememberedFood * (1.0 - AnimalGeneEffects.foodMemoryDecay(this)), 0.0, 1.0);
        frustration = clamp(frustration - getGene("frustrationRecovery") * (1.0 + getGene("focusStability")), 0.0, 1.0);
        confidence = clamp(confidence + getGene("learningRate") * (0.001 + getGene("focusStability") * 0.002), 0.0, 1.0);
    }

    protected double caloriesRatio() {
        return safeRatio(calories, getGene("maxCalories"));
    }

    protected double hydrationRatio() {
        return safeRatio(hydration, getGene("maxHydration"));
    }

    protected double energyRatio() {
        return safeRatio(energy, getGene("maxEnergy"));
    }

    protected double healthRatio() {
        return safeRatio(health, getGene("maxHealth"));
    }

    protected double safeRatio(double value, double maximum) {
        if (maximum <= 0) {
            return 0;
        }

        return clamp(value / maximum, 0.0, 1.0);
    }

    protected double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    protected double preferenceScore(double actual, double preferred, double pickiness) {
        double denominator = Math.max(0.0001, Math.abs(preferred) + 1.0);
        double difference = Math.abs(actual - preferred) / denominator;
        return clamp(1.0 - difference * pickiness, 0.0, 1.0);
    }

    void setVariationRecord(Map<String, Double> originalGenes, String label) {
        variationCount = AnimalMutation.countDifferences(originalGenes, genes);
        variationSummary = label + ": " + AnimalMutation.summarizeDifferences(originalGenes, genes);
    }

    protected String format(double value) {
        return String.format("%.4f", value);
    }

    public int getId() {
        return id;
    }

    public int getGeneration() {
        return generation;
    }

    public int getParentOneId() {
        return parentOneId;
    }

    public int getParentTwoId() {
        return parentTwoId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public Sex getSex() {
        return sex;
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

    public double getCalories() {
        return calories;
    }

    public double getHydration() {
        return hydration;
    }

    public double getEnergy() {
        return energy;
    }

    public double getHealth() {
        return health;
    }

    public double getGene(String geneName) {
        return genome.get(geneName);
    }

    public Map<String, Double> getGenes() {
        return genome.toMap();
    }
}