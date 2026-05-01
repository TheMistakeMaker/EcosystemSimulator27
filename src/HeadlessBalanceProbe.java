import java.util.List;

public class HeadlessBalanceProbe {
    private static final int DEFAULT_RUNS = 8;
    private static final int DEFAULT_TICKS = 1200;
    private static final int DEFAULT_INTERVAL = 100;

    public static void main(String[] args) {
        int runs = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_RUNS;
        int ticks = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_TICKS;
        int interval = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_INTERVAL;

        for (int run = 1; run <= runs; run++) {
            WorldModel model = new WorldModel(100, 200);
            BalancedWorldSeeder.seed(model);

            for (int tick = 0; tick <= ticks; tick++) {
                if (tick % interval == 0 || tick == ticks) {
                    printStats(run, model);
                }

                if (tick < ticks) {
                    model.updateWorld();
                }
            }

            System.out.println("END_RUN," + run);
        }
    }

    private static void printStats(int run, WorldModel model) {
        List<Plant> plants = model.getPlantsSnapshot();
        List<Animal> animals = model.getAnimalsSnapshot();

        int herbivores = model.countLivingAnimalsByType("Herbivore");
        int predators = model.countLivingAnimalsByType("Predator");

        double plantCalories = 0;
        double plantHeight = 0;
        int maxPlantGeneration = 0;
        int maxAnimalGeneration = 0;

        for (Plant plant : plants) {
            plantCalories += plant.getCalories();
            plantHeight += plant.getHeight();
            maxPlantGeneration = Math.max(maxPlantGeneration, plant.getGeneration());
        }

        for (Animal animal : animals) {
            if (animal.isAlive()) {
                maxAnimalGeneration = Math.max(maxAnimalGeneration, animal.getGeneration());
            }
        }

        System.out.printf(
                "RUN,%d,TICK,%d,PLANTS,%d,HERBIVORES,%d,PREDATORS,%d,AVG_PLANT_CALORIES,%.2f,AVG_PLANT_HEIGHT,%.2f,MAX_PLANT_GEN,%d,MAX_ANIMAL_GEN,%d%n",
                run,
                model.getTickCount(),
                plants.size(),
                herbivores,
                predators,
                plants.isEmpty() ? 0.0 : plantCalories / plants.size(),
                plants.isEmpty() ? 0.0 : plantHeight / plants.size(),
                maxPlantGeneration,
                maxAnimalGeneration
        );
    }
}
