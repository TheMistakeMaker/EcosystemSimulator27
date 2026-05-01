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

        this.calories = getGene("maxCalories") * getGene("startingCaloriesRatio");
        this.hydration = getGene("maxHydration") * getGene("startingHydrationRatio");
        this.energy = getGene("maxEnergy") * getGene("startingEnergyRatio");
        this.health = getGene("maxHealth");
    }

    public void update(WorldModel world) {
        if (!alive) {
            return;
        }

        ageTicks++;
        decrementReproductionCooldown();
        applyPassiveBiology();

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

    protected void applyPassiveBiology() {
        calories = Math.max(0, calories - getGene("metabolismRate"));
        hydration = Math.max(0, hydration - getGene("hydrationLossRate"));

        if (calories <= 0) {
            health -= getGene("starvationDamage");
        }

        if (hydration <= 0) {
            health -= getGene("dehydrationDamage");
        }

        if (ageTicks > getGene("maxAgeTicks")) {
            health -= getGene("oldAgeDamage");
        }

        if (calories > getGene("maxCalories") * 0.25 && hydration > getGene("maxHydration") * 0.25) {
            energy = Math.min(getGene("maxEnergy"), energy + getGene("energyRecoveryRate"));
        }

        if (health <= 0) {
            alive = false;
        }
    }

    public boolean moveTo(int newRow, int newCol, WorldModel world) {
        if (!alive || world == null || !world.canAnimalEnter(this, newRow, newCol)) {
            return false;
        }

        double terrainCost = world.getMovementCost(newRow, newCol);
        double energyCost = getGene("movementEnergyCost") * terrainCost;
        double calorieCost = getGene("movementCalorieCost") * terrainCost;

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
    }

    public void takeDamage(double damage, WorldModel world) {
        if (!alive || damage <= 0) {
            return;
        }

        health -= damage;

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
            world.addCorpse(row, col, Math.max(0, calories) * getGene("edibleCaloriesRatio"));
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

        return geneticDistanceTo(mate) <= getGene("maxMatingGeneticDistance");
    }

    public boolean isReproductivelyReady() {
        return alive
                && ageTicks >= getGene("reproductionAgeTicks")
                && reproductionCooldown <= 0
                && calories >= getGene("reproductionCaloriesRequired")
                && hydration >= getGene("reproductionHydrationRequired")
                && energy >= getGene("reproductionEnergyRequired")
                && health >= getGene("reproductionHealthRequired");
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
        calories = Math.max(0, calories - getGene("reproductionCaloriesCost"));
        energy = Math.max(0, energy - getGene("reproductionEnergyCost"));
        reproductionCooldown = (int) Math.round(getGene("reproductionCooldownTicks"));
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

        for (String geneName : genes.keySet()) {
            data.put("Gene: " + geneName, format(genes.get(geneName)));
        }

        return data;
    }

    public double distanceTo(Animal other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }

        int rowDifference = row - other.row;
        int colDifference = col - other.col;

        return Math.sqrt(rowDifference * rowDifference + colDifference * colDifference);
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