import java.util.Random;

public class BalancedWorldSeeder {
    private static final int BASE_ROWS = 50;
    private static final int BASE_COLS = 75;
    private static final int BASE_AREA = BASE_ROWS * BASE_COLS;

    private static final int BASE_TARGET_PLANTS = 1000;
    private static final int BASE_TARGET_HERBIVORES = 200;
    private static final int BASE_TARGET_PREDATORS = 20;
    private static final int BASE_TARGET_ROCKS = 185;

    private static final Random RANDOM = new Random();

    private BalancedWorldSeeder() {
    }

    public static void seed(WorldModel model) {
        if (model == null) {
            return;
        }

        createGrassBase(model);
        createPonds(model);
        createRockBarriers(model);
        createPlantCommunities(model);
        createHerbivoreHerds(model);
        createPredators(model);
    }

    private static void createGrassBase(WorldModel model) {
        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                model.setTile(row, col, TileType.GRASS);
            }
        }
    }

    private static void createPonds(WorldModel model) {
        createIrregularPond(model, sr(model, 8), sc(model, 13), srl(model, 4), scl(model, 6));
        createIrregularPond(model, sr(model, 12), sc(model, 58), srl(model, 5), scl(model, 8));
        createIrregularPond(model, sr(model, 26), sc(model, 36), srl(model, 5), scl(model, 7));
        createIrregularPond(model, sr(model, 39), sc(model, 16), srl(model, 5), scl(model, 7));
        createIrregularPond(model, sr(model, 38), sc(model, 61), srl(model, 5), scl(model, 8));
    }

    private static void createIrregularPond(WorldModel model, int centerRow, int centerCol, int radiusRows, int radiusCols) {
        for (int row = centerRow - radiusRows - 1; row <= centerRow + radiusRows + 1; row++) {
            for (int col = centerCol - radiusCols - 1; col <= centerCol + radiusCols + 1; col++) {
                double rowPart = Math.pow((row - centerRow) / (double) radiusRows, 2);
                double colPart = Math.pow((col - centerCol) / (double) radiusCols, 2);
                double noise = RANDOM.nextDouble() * 0.35;

                if (rowPart + colPart <= 1.0 + noise) {
                    model.setTile(row, col, TileType.WATER);
                }
            }
        }
    }

    private static void createRockBarriers(WorldModel model) {
        int rocksPlaced = 0;

        rocksPlaced += createBrokenRockBand(model, sr(model, 14), sr(model, 18), sc(model, 34), sc(model, 44), 0.48);
        rocksPlaced += createBrokenRockBand(model, sr(model, 26), sr(model, 32), sc(model, 12), sc(model, 20), 0.50);
        rocksPlaced += createBrokenRockBand(model, sr(model, 38), sr(model, 43), sc(model, 32), sc(model, 40), 0.47);
        rocksPlaced += createBrokenRockBand(model, sr(model, 18), sr(model, 24), sc(model, 62), sc(model, 68), 0.45);
        rocksPlaced += createBrokenRockBand(model, sr(model, 5), sr(model, 10), sc(model, 36), sc(model, 42), 0.25);

        int targetRocks = scaledCount(model, BASE_TARGET_ROCKS);

        while (rocksPlaced < targetRocks) {
            int row = RANDOM.nextInt(model.getRows());
            int col = RANDOM.nextInt(model.getCols());

            if (model.getTileType(row, col) == TileType.GRASS && model.getPlant(row, col) == null) {
                model.setTile(row, col, TileType.ROCK);
                rocksPlaced++;
            }
        }
    }

    private static int createBrokenRockBand(WorldModel model, int minRow, int maxRow, int minCol, int maxCol, double density) {
        int count = 0;

        minRow = clampInt(minRow, 0, model.getRows() - 1);
        maxRow = clampInt(maxRow, 0, model.getRows() - 1);
        minCol = clampInt(minCol, 0, model.getCols() - 1);
        maxCol = clampInt(maxCol, 0, model.getCols() - 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                if (RANDOM.nextDouble() < density && model.getTileType(row, col) == TileType.GRASS) {
                    model.setTile(row, col, TileType.ROCK);
                    count++;
                }
            }
        }

        return count;
    }

    private static void createPlantCommunities(WorldModel model) {
        int plantsPlaced = 0;

        plantsPlaced += placePlantsInPatch(model, sr(model, 3), sr(model, 15), sc(model, 5), sc(model, 24), scaledCount(model, 70));
        plantsPlaced += placePlantsInPatch(model, sr(model, 5), sr(model, 18), sc(model, 48), sc(model, 70), scaledCount(model, 70));
        plantsPlaced += placePlantsInPatch(model, sr(model, 19), sr(model, 33), sc(model, 25), sc(model, 47), scaledCount(model, 66));
        plantsPlaced += placePlantsInPatch(model, sr(model, 32), sr(model, 47), sc(model, 6), sc(model, 27), scaledCount(model, 66));
        plantsPlaced += placePlantsInPatch(model, sr(model, 31), sr(model, 46), sc(model, 51), sc(model, 72), scaledCount(model, 66));

        plantsPlaced += placePlantsInPatch(model, sr(model, 12), sr(model, 22), sc(model, 25), sc(model, 35), scaledCount(model, 50));
        plantsPlaced += placePlantsInPatch(model, sr(model, 25), sr(model, 38), sc(model, 3), sc(model, 12), scaledCount(model, 50));
        plantsPlaced += placePlantsInPatch(model, sr(model, 16), sr(model, 28), sc(model, 58), sc(model, 72), scaledCount(model, 50));

        int targetPlants = scaledCount(model, BASE_TARGET_PLANTS);

        while (plantsPlaced < targetPlants) {
            if (placeOnePlantAnywhere(model)) {
                plantsPlaced++;
            } else {
                break;
            }
        }
    }

    private static int placePlantsInPatch(WorldModel model, int minRow, int maxRow, int minCol, int maxCol, int count) {
        int placed = 0;
        int attempts = 0;
        int maxAttempts = count * 80;

        minRow = clampInt(minRow, 0, model.getRows() - 1);
        maxRow = clampInt(maxRow, 0, model.getRows() - 1);
        minCol = clampInt(minCol, 0, model.getCols() - 1);
        maxCol = clampInt(maxCol, 0, model.getCols() - 1);

        while (placed < count && attempts < maxAttempts) {
            int row = randomBetween(minRow, maxRow);
            int col = randomBetween(minCol, maxCol);

            if (placeOnePlant(model, row, col)) {
                placed++;
            }

            attempts++;
        }

        return placed;
    }

    private static boolean placeOnePlantAnywhere(WorldModel model) {
        int attempts = 0;

        while (attempts < 1200) {
            int row = RANDOM.nextInt(model.getRows());
            int col = RANDOM.nextInt(model.getCols());

            if (placeOnePlant(model, row, col)) {
                return true;
            }

            attempts++;
        }

        return false;
    }

    private static boolean placeOnePlant(WorldModel model, int row, int col) {
        int before = model.getPlantsSnapshot().size();
        model.addDefaultPlant(row, col);
        return model.getPlantsSnapshot().size() > before;
    }

    private static void createHerbivoreHerds(WorldModel model) {
        placeAnimalCluster(model, sr(model, 12), sc(model, 24), scaledCount(model, 19), true);
        placeAnimalCluster(model, sr(model, 21), sc(model, 53), scaledCount(model, 19), true);
        placeAnimalCluster(model, sr(model, 37), sc(model, 29), scaledCount(model, 19), true);
        placeAnimalCluster(model, sr(model, 34), sc(model, 50), scaledCount(model, 19), true);

        int targetHerbivores = scaledCount(model, BASE_TARGET_HERBIVORES);

        while (model.countLivingAnimalsByType("Herbivore") < targetHerbivores) {
            placeAnimalCluster(model, RANDOM.nextInt(model.getRows()), RANDOM.nextInt(model.getCols()), 1, true);
        }
    }

    private static void createPredators(WorldModel model) {
        int targetPredators = scaledCount(model, BASE_TARGET_PREDATORS);

        placePredatorPair(model, sr(model, 8), sc(model, 31));
        placePredatorPair(model, sr(model, 26), sc(model, 52));

        int attempts = 0;

        while (model.countLivingAnimalsByType("Predator") < targetPredators && attempts < targetPredators * 100) {
            int row = RANDOM.nextInt(model.getRows());
            int col = RANDOM.nextInt(model.getCols());

            placePredatorPair(model, row, col);
            attempts++;
        }
    }

    private static void placePredatorPair(WorldModel model, int row, int col) {
        placeOneAnimalNear(model, row, col, false);
        placeOneAnimalNear(model, row + RANDOM.nextInt(7) - 3, col + RANDOM.nextInt(7) - 3, false);
    }

    private static void placeAnimalCluster(WorldModel model, int centerRow, int centerCol, int count, boolean herbivore) {
        int placed = 0;
        int radius = clusterRadius(model, count);
        int attempts = 0;

        while (placed < count && attempts < count * 160) {
            int row = centerRow + RANDOM.nextInt(radius * 2 + 1) - radius;
            int col = centerCol + RANDOM.nextInt(radius * 2 + 1) - radius;

            if (placeOneAnimalNear(model, row, col, herbivore)) {
                placed++;
            }

            attempts++;
        }
    }

    private static boolean placeOneAnimalNear(WorldModel model, int row, int col, boolean herbivore) {
        int before = model.countLivingAnimals();

        if (herbivore) {
            model.addDefaultHerbivore(row, col);
        } else {
            model.addDefaultPredator(row, col);
        }

        if (model.countLivingAnimals() > before) {
            return true;
        }

        int searchRadius = maxSearchRadius(model);

        for (int radius = 1; radius <= searchRadius; radius++) {
            for (int attempt = 0; attempt < 40; attempt++) {
                int newRow = row + RANDOM.nextInt(radius * 2 + 1) - radius;
                int newCol = col + RANDOM.nextInt(radius * 2 + 1) - radius;

                before = model.countLivingAnimals();

                if (herbivore) {
                    model.addDefaultHerbivore(newRow, newCol);
                } else {
                    model.addDefaultPredator(newRow, newCol);
                }

                if (model.countLivingAnimals() > before) {
                    return true;
                }
            }
        }

        return false;
    }

    private static int scaledCount(WorldModel model, int baseCount) {
        double scale = areaScale(model);

        double densityMultiplier = 0.45;

        return Math.max(1, (int) Math.round(baseCount * scale * densityMultiplier));
    }

    private static double areaScale(WorldModel model) {
        return (model.getRows() * model.getCols()) / (double) BASE_AREA;
    }

    private static int sr(WorldModel model, int baseRow) {
        return clampInt((int) Math.round(baseRow * model.getRows() / (double) BASE_ROWS), 0, model.getRows() - 1);
    }

    private static int sc(WorldModel model, int baseCol) {
        return clampInt((int) Math.round(baseCol * model.getCols() / (double) BASE_COLS), 0, model.getCols() - 1);
    }

    private static int srl(WorldModel model, int baseLength) {
        return Math.max(1, (int) Math.round(baseLength * model.getRows() / (double) BASE_ROWS));
    }

    private static int scl(WorldModel model, int baseLength) {
        return Math.max(1, (int) Math.round(baseLength * model.getCols() / (double) BASE_COLS));
    }

    private static int clusterRadius(WorldModel model, int count) {
        int radiusFromPopulation = (int) Math.ceil(Math.sqrt(count) * 1.25);
        int radiusFromWorld = Math.max(4, Math.min(model.getRows(), model.getCols()) / 8);

        return Math.max(4, Math.min(radiusFromPopulation, radiusFromWorld));
    }

    private static int maxSearchRadius(WorldModel model) {
        return Math.max(6, Math.min(80, Math.min(model.getRows(), model.getCols()) / 6));
    }

    private static int randomBetween(int min, int max) {
        if (max < min) {
            int temporary = min;
            min = max;
            max = temporary;
        }

        return min + RANDOM.nextInt(max - min + 1);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}