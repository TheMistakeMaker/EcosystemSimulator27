import java.util.Random;

public class BalancedWorldSeeder {
    private static final Random RANDOM = new Random();

    private BalancedWorldSeeder() {
    }

    public static void seed(WorldModel model) {
        addPonds(model, 6);
        scatterRocks(model, scaled(model, 0.010));
        scatterPlants(model, scaled(model, 0.040));
        scatterHerbivores(model, scaled(model, 0.0050));
        scatterPredators(model, scaled(model, 0.00030));
    }

    private static void addPonds(WorldModel model, int count) {
        for (int i = 0; i < count; i++) {
            int centerRow = RANDOM.nextInt(model.getRows());
            int centerCol = RANDOM.nextInt(model.getCols());
            int radius = 4 + RANDOM.nextInt(Math.max(3, Math.min(model.getRows(), model.getCols()) / 11));
            for (int row = centerRow - radius; row <= centerRow + radius; row++) {
                for (int col = centerCol - radius; col <= centerCol + radius; col++) {
                    if (model.isValidTile(row, col) && distance(row, col, centerRow, centerCol) <= radius * (0.72 + RANDOM.nextDouble() * 0.38)) {
                        model.setTile(row, col, TileType.WATER);
                    }
                }
            }
        }
    }

    private static void scatterRocks(WorldModel model, int count) {
        for (int i = 0; i < count; i++) {
            int[] tile = randomGrassTile(model);
            if (tile != null) model.setTile(tile[0], tile[1], TileType.ROCK);
        }
    }

    private static void scatterPlants(WorldModel model, int count) {
        for (int i = 0; i < count; i++) {
            int[] tile = biasedPlantTile(model);
            if (tile != null) model.addDefaultPlant(tile[0], tile[1]);
        }
    }

    private static void scatterHerbivores(WorldModel model, int count) {
        for (int i = 0; i < count; i++) {
            int[] tile = randomGrassTile(model);
            if (tile != null) model.addDefaultHerbivore(tile[0], tile[1]);
        }
    }

    private static void scatterPredators(WorldModel model, int count) {
        for (int i = 0; i < count; i++) {
            int[] tile = randomGrassTile(model);
            if (tile != null) model.addDefaultPredator(tile[0], tile[1]);
        }
    }

    private static int[] biasedPlantTile(WorldModel model) {
        for (int attempt = 0; attempt < 80; attempt++) {
            int[] tile = randomGrassTile(model);
            if (tile == null) return null;
            boolean nearWater = false;
            for (int row = tile[0] - 3; row <= tile[0] + 3; row++) {
                for (int col = tile[1] - 3; col <= tile[1] + 3; col++) {
                    nearWater |= model.isValidTile(row, col) && model.getTileType(row, col) == TileType.WATER;
                }
            }
            if (nearWater || RANDOM.nextDouble() < 0.38) return tile;
        }
        return randomGrassTile(model);
    }

    private static int[] randomGrassTile(WorldModel model) {
        for (int attempt = 0; attempt < 500; attempt++) {
            int row = RANDOM.nextInt(model.getRows());
            int col = RANDOM.nextInt(model.getCols());
            if (model.getTileType(row, col) == TileType.GRASS) return new int[] {row, col};
        }
        return null;
    }

    private static int scaled(WorldModel model, double density) {
        return Math.max(1, (int) Math.round(model.getRows() * model.getCols() * density));
    }

    private static double distance(int row, int col, int centerRow, int centerCol) {
        int dr = row - centerRow;
        int dc = col - centerCol;
        return Math.sqrt(dr * dr + dc * dc);
    }
}
