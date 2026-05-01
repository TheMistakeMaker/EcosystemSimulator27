public class Tile {
    private TileType terrain;
    private Plant plant;
    private Animal animal;
    private double corpseCalories;

    public Tile(TileType terrain) {
        setTerrain(terrain);
        this.plant = null;
        this.animal = null;
        this.corpseCalories = 0;
    }

    public TileType getTerrain() {
        return terrain;
    }

    public void setTerrain(TileType terrain) {
        if (terrain == null) {
            throw new IllegalArgumentException("Terrain cannot be null");
        }

        this.terrain = terrain;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public void clearPlant() {
        plant = null;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public void clearAnimal() {
        animal = null;
    }

    public double getCorpseCalories() {
        return corpseCalories;
    }

    public void addCorpseCalories(double calories) {
        if (calories <= 0) {
            return;
        }

        corpseCalories += calories;
    }

    public void clearCorpseCalories() {
        corpseCalories = 0;
    }

    public double eatCorpseCalories(double requestedCalories) {
        if (requestedCalories <= 0 || corpseCalories <= 0) {
            return 0;
        }

        double eaten = Math.min(requestedCalories, corpseCalories);
        corpseCalories -= eaten;

        return eaten;
    }

    public boolean hasLivingPlant() {
        return plant != null && plant.isAlive();
    }

    public boolean hasLivingAnimal() {
        return animal != null && animal.isAlive();
    }

    public boolean hasCorpse() {
        return corpseCalories > 0;
    }

    public boolean isGrass() {
        return terrain == TileType.GRASS;
    }

    public boolean isWater() {
        return terrain == TileType.WATER;
    }

    public boolean isRock() {
        return terrain == TileType.ROCK;
    }

    public boolean isOpenForPlant() {
        return isGrass() && plant == null && animal == null;
    }

    public boolean isOpenForAnimal(Animal animalTryingToEnter) {
        if (!isGrass()) {
            return false;
        }

        return animal == null || animal == animalTryingToEnter;
    }
}