import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public abstract class Animal {
    protected static final Random rand = new Random();
    private static int nextId = 1;

    // Ancestry
    protected final int id;
    protected final int generation;
    protected int parentOneId;
    protected int parentTwoId;
    protected String speciesName;

    // Position
    protected int row;
    protected int col;

    // Biological state
    protected Sex sex;
    protected boolean alive;
    protected int ageTicks;
    protected int reproductionCooldown;

    protected double calories;
    protected double hydration;
    protected double energy;
    protected double health;

    // Genetic information
    protected Map<String, Double> genes;

    public Animal(int row, int col, Map<String, Double> genes, String speciesName, int generation) {
        this.id = nextId++;
        this.row = row;
        this.col = col;
        this.genes = new HashMap<>();

        addDefaultGenes();
        this.genes.putAll(genes);

        this.speciesName = speciesName;
        this.generation = generation;
        this.parentOneId = -1;
        this.parentTwoId = -1;

        this.sex = rand.nextBoolean() ? Sex.MALE : Sex.FEMALE;
        this.alive = true;
        this.ageTicks = 0;
        this.reproductionCooldown = 0;

        this.calories = getGene("maxCalories") * getGene("startingCaloriesRatio");
        this.hydration = getGene("maxHydration") * getGene("startingHydrationRatio");
        this.energy = getGene("maxEnergy") * getGene("startingEnergyRatio");
        this.health = getGene("maxHealth");
    }


    //Main method called once per simulation time increment.
    public void update(World world) {
        if (!alive) {
            return;
        }

        ageTicks++;

        if (reproductionCooldown > 0) {
            reproductionCooldown--;
        }

        applyPassiveBiology();

        if (!alive) {
            die(world);
            return;
        }

        decideAction(world);
    }

    /*
     * Subclasses decide their own behavior.
     * Example:
     * - A herbivore may search for plants.
     * - A carnivore may search for prey.
     * - An omnivore may choose between plants and animals.
     */
    protected abstract void decideAction(World world);

    /*
     * Subclasses decide what they can eat.
     */
    public abstract boolean canEatAnimal(Animal other);

    /*
     * Subclasses create their own child type.
     */
    protected abstract Animal createChild(
            int row,
            int col,
            Map<String, Double> childGenes,
            String childSpeciesName,
            int childGeneration
    );

    /*
     * Returns a broad animal type, such as "Herbivore", "Predator", or "Omnivore".
     */
    public abstract String getAnimalType();

    /*
     * Passive survival mechanics.
     */
    protected void applyPassiveBiology() {
        calories -= getGene("metabolismRate");
        hydration -= getGene("hydrationLossRate");

        if (calories < 0) {
            calories = 0;
        }

        if (hydration < 0) {
            hydration = 0;
        }

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
            energy += getGene("energyRecoveryRate");
        }

        if (energy > getGene("maxEnergy")) {
            energy = getGene("maxEnergy");
        }

        if (health > 0
                && health < getGene("maxHealth")
                && calories > getGene("maxCalories") * 0.50
                && hydration > getGene("maxHydration") * 0.50) {
            health += getGene("healthRecoveryRate");

            if (health > getGene("maxHealth")) {
                health = getGene("maxHealth");
            }
        }

        if (health <= 0) {
            alive = false;
        }
    }

    // Moves to a new tile
    public boolean moveTo(int newRow, int newCol, World world) {
        if (!alive) {
            return false;
        }

        if (!world.isValidTile(newRow, newCol)) {
            return false;
        }

        if (!world.canAnimalEnter(this, newRow, newCol)) {
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

    /*
     * Simple tile-based movement toward a target.
     * More advanced pathfinding can be added later in World.
     */
    public boolean moveToward(int targetRow, int targetCol, World world) {
        int rowChange = Integer.compare(targetRow, row);
        int colChange = Integer.compare(targetCol, col);

        if (Math.abs(targetRow - row) >= Math.abs(targetCol - col)) {
            if (moveTo(row + rowChange, col, world)) {
                return true;
            }

            return moveTo(row, col + colChange, world);
        } else {
            if (moveTo(row, col + colChange, world)) {
                return true;
            }

            return moveTo(row + rowChange, col, world);
        }
    }

    public boolean moveRandomly(World world) {
        int[][] directions = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        int start = rand.nextInt(directions.length);

        for (int i = 0; i < directions.length; i++) {
            int index = (start + i) % directions.length;
            int newRow = row + directions[index][0];
            int newCol = col + directions[index][1];

            if (moveTo(newRow, newCol, world)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Drinking from water or another hydration source.
     */
    public void drink(double amount) {
        if (!alive) {
            return;
        }

        hydration += amount;

        if (hydration > getGene("maxHydration")) {
            hydration = getGene("maxHydration");
        }
    }

    /*
     * Eating from any source: plants, prey, corpse, etc.
     */
    public void gainCalories(double amount) {
        if (!alive) {
            return;
        }

        calories += amount;

        if (calories > getGene("maxCalories")) {
            calories = getGene("maxCalories");
        }
    }

    /*
     * Hunting / attacking another animal.
     */
    public boolean attack(Animal target, World world) {
        if (!alive || target == null || !target.isAlive()) {
            return false;
        }

        if (!canEatAnimal(target)) {
            return false;
        }

        if (distanceTo(target) > getGene("attackRange")) {
            return false;
        }

        double energyCost = getGene("attackEnergyCost");
        double calorieCost = getGene("attackCalorieCost");

        if (energy < energyCost || calories < calorieCost) {
            return false;
        }

        energy -= energyCost;
        calories -= calorieCost;

        double hitChance = getGene("attackAccuracy") - target.getGene("evasion");
        hitChance = clamp(hitChance, 0.05, 0.95);

        if (rand.nextDouble() > hitChance) {
            return false;
        }

        double damage = getGene("attackDamage") - target.getGene("defense");
        damage = Math.max(1, damage);

        target.takeDamage(damage, world);

        if (!target.isAlive()) {
            double caloriesGained = target.getEdibleCalories();
            gainCalories(caloriesGained);
            world.recordSuccessfulHunt(this, target);
        }

        return true;
    }

    public void takeDamage(double damage, World world) {
        if (!alive) {
            return;
        }

        health -= damage;

        if (health <= 0) {
            alive = false;
            die(world);
        }
    }

    public void die(World world) {
        if (!alive) {
            world.removeAnimal(this);
            world.addCorpse(row, col, getEdibleCalories());
            return;
        }

        alive = false;
        world.removeAnimal(this);
        world.addCorpse(row, col, getEdibleCalories());
    }

    /*
     * Individual reproduction readiness.
     */
    public boolean isReproductivelyReady() {
        return alive
                && ageTicks >= getGene("reproductionAgeTicks")
                && reproductionCooldown <= 0
                && calories >= getGene("reproductionCaloriesRequired")
                && hydration >= getGene("reproductionHydrationRequired")
                && energy >= getGene("reproductionEnergyRequired")
                && health >= getGene("reproductionHealthRequired");
    }

    /*
     * Sexual compatibility check.
     */
    public boolean canReproduceWith(Animal mate) {
        if (mate == null) {
            return false;
        }

        if (!this.isReproductivelyReady() || !mate.isReproductivelyReady()) {
            return false;
        }

        if (this.sex == mate.sex) {
            return false;
        }

        double geneticDistance = geneticDistanceTo(mate);

        return geneticDistance <= getGene("maxMatingGeneticDistance");
    }

    /*
     * Creates a child with mixed genes from two parents.
     */
    public Animal reproduceWith(Animal mate, World world) {
        if (!canReproduceWith(mate)) {
            return null;
        }

        int[] openTile = world.findOpenAdjacentTile(row, col);

        if (openTile == null) {
            return null;
        }

        Map<String, Double> childGenes = createMixedMutatedGenes(mate);
        String childSpeciesName = world.determineSpeciesName(this, mate, childGenes);

        calories -= getGene("reproductionCaloriesCost");
        energy -= getGene("reproductionEnergyCost");

        mate.calories -= mate.getGene("reproductionCaloriesCost");
        mate.energy -= mate.getGene("reproductionEnergyCost");

        reproductionCooldown = (int) getGene("reproductionCooldownTicks");
        mate.reproductionCooldown = (int) mate.getGene("reproductionCooldownTicks");

        Animal child = createChild(
                openTile[0],
                openTile[1],
                childGenes,
                childSpeciesName,
                Math.max(this.generation, mate.generation) + 1
        );

        child.parentOneId = this.id;
        child.parentTwoId = mate.id;

        world.addAnimal(child);

        return child;
    }

    /*
     * Gene inheritance with mutation.
     */
    protected Map<String, Double> createMixedMutatedGenes(Animal mate) {
        Map<String, Double> childGenes = new HashMap<>();

        for (String geneName : genes.keySet()) {
            double parentOneValue = this.getGene(geneName);
            double parentTwoValue = mate.getGene(geneName);

            double childValue;

            if (rand.nextBoolean()) {
                childValue = parentOneValue;
            } else {
                childValue = parentTwoValue;
            }

            double mutationRate = averageGene(this, mate, "mutationRate");
            double mutationStrength = averageGene(this, mate, "mutationStrength");

            if (rand.nextDouble() < mutationRate) {
                double mutationMultiplier = 1.0 + ((rand.nextDouble() * 2.0 - 1.0) * mutationStrength);
                childValue *= mutationMultiplier;
            }

            childValue = cleanGeneValue(geneName, childValue);
            childGenes.put(geneName, childValue);
        }

        return childGenes;
    }

    protected double averageGene(Animal a, Animal b, String geneName) {
        return (a.getGene(geneName) + b.getGene(geneName)) / 2.0;
    }

    /*
     * A rough measure of biological difference.
     * World can use this for species grouping.
     */
    public double geneticDistanceTo(Animal other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }

        double totalDifference = 0;
        int comparedGenes = 0;

        for (String geneName : genes.keySet()) {
            if (other.genes.containsKey(geneName)) {
                double a = this.getGene(geneName);
                double b = other.getGene(geneName);

                double average = (Math.abs(a) + Math.abs(b)) / 2.0;

                if (average > 0) {
                    totalDifference += Math.abs(a - b) / average;
                    comparedGenes++;
                }
            }
        }

        if (comparedGenes == 0) {
            return Double.MAX_VALUE;
        }

        return totalDifference / comparedGenes;
    }

    /*
     * Information for the click-inspection panel.
     */
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

    protected double getEdibleCalories() {
        return Math.max(0, calories * getGene("edibleCaloriesRatio"));
    }

    protected double cleanGeneValue(String geneName, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = 1.0;
        }

        value = Math.max(0.001, value);

        if (geneName.equals("startingCaloriesRatio")
                || geneName.equals("startingHydrationRatio")
                || geneName.equals("startingEnergyRatio")
                || geneName.equals("mutationRate")
                || geneName.equals("attackAccuracy")
                || geneName.equals("evasion")
                || geneName.equals("edibleCaloriesRatio")) {
            value = clamp(value, 0.0, 1.0);
        }

        return value;
    }

    protected double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    protected String format(double value) {
        return String.format("%.2f", value);
    }

    public double distanceTo(Animal other) {
        return distanceTo(other.row, other.col);
    }

    public double distanceTo(int otherRow, int otherCol) {
        int rowDifference = row - otherRow;
        int colDifference = col - otherCol;

        return Math.sqrt(rowDifference * rowDifference + colDifference * colDifference);
    }

    public double getGene(String geneName) {
        if (!genes.containsKey(geneName)) {
            throw new IllegalArgumentException("Missing gene: " + geneName);
        }

        return genes.get(geneName);
    }

    /*
     * Default values. Subclasses or constructors can override any of these.
     */
    private void addDefaultGenes() {
        genes.put("maxHealth", 100.0);
        genes.put("maxCalories", 100.0);
        genes.put("maxHydration", 100.0);
        genes.put("maxEnergy", 100.0);

        genes.put("startingCaloriesRatio", 0.75);
        genes.put("startingHydrationRatio", 0.75);
        genes.put("startingEnergyRatio", 0.75);

        genes.put("metabolismRate", 0.35);
        genes.put("hydrationLossRate", 0.25);
        genes.put("energyRecoveryRate", 1.2);
        genes.put("healthRecoveryRate", 0.05);

        genes.put("starvationDamage", 1.5);
        genes.put("dehydrationDamage", 2.0);
        genes.put("oldAgeDamage", 0.5);
        genes.put("maxAgeTicks", 2500.0);

        genes.put("visionRange", 8.0);

        genes.put("movementEnergyCost", 1.0);
        genes.put("movementCalorieCost", 0.25);

        genes.put("attackRange", 1.0);
        genes.put("attackDamage", 20.0);
        genes.put("attackAccuracy", 0.75);
        genes.put("attackEnergyCost", 5.0);
        genes.put("attackCalorieCost", 2.0);
        genes.put("defense", 1.0);
        genes.put("evasion", 0.10);

        genes.put("edibleCaloriesRatio", 0.65);

        genes.put("reproductionAgeTicks", 200.0);
        genes.put("reproductionCooldownTicks", 250.0);
        genes.put("reproductionCaloriesRequired", 70.0);
        genes.put("reproductionHydrationRequired", 60.0);
        genes.put("reproductionEnergyRequired", 60.0);
        genes.put("reproductionHealthRequired", 70.0);
        genes.put("reproductionCaloriesCost", 35.0);
        genes.put("reproductionEnergyCost", 35.0);

        genes.put("mutationRate", 0.08);
        genes.put("mutationStrength", 0.15);
        genes.put("maxMatingGeneticDistance", 0.40);
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

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
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

    public Map<String, Double> getGenes() {
        return new HashMap<>(genes);
    }
}