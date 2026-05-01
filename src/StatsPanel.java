import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.List;

public class StatsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel tickLabel;
    private final JLabel plantCountLabel;
    private final JLabel animalCountLabel;
    private final JLabel predatorCountLabel;
    private final JLabel herbivoreCountLabel;
    private final JLabel averageGenerationLabel;
    private final JLabel averagePlantCaloriesLabel;
    private final JLabel averagePlantHeightLabel;
    private final JLabel averageAnimalCaloriesLabel;
    private final JLabel sunLabel;

    public StatsPanel(WorldModel model) {
        this.tickLabel = new JLabel();
        this.plantCountLabel = new JLabel();
        this.animalCountLabel = new JLabel();
        this.predatorCountLabel = new JLabel();
        this.herbivoreCountLabel = new JLabel();
        this.averageGenerationLabel = new JLabel();
        this.averagePlantCaloriesLabel = new JLabel();
        this.averagePlantHeightLabel = new JLabel();
        this.averageAnimalCaloriesLabel = new JLabel();
        this.sunLabel = new JLabel();

        add(tickLabel);
        add(plantCountLabel);
        add(animalCountLabel);
        add(predatorCountLabel);
        add(herbivoreCountLabel);
        add(averageGenerationLabel);
        add(averagePlantCaloriesLabel);
        add(averagePlantHeightLabel);
        add(averageAnimalCaloriesLabel);
        add(sunLabel);

        updateStats(model);
    }

    public void updateStats(WorldModel model) {
        List<Plant> plants = model.getPlantsSnapshot();
        List<Animal> animals = model.getAnimalsSnapshot();
        PlantStats plantStats = calculatePlantStats(plants);
        AnimalStats animalStats = calculateAnimalStats(animals);

        tickLabel.setText("Ticks: " + model.getTickCount());
        plantCountLabel.setText("Plants: " + plants.size());
        animalCountLabel.setText("Animals: " + model.countLivingAnimals());
        predatorCountLabel.setText("Predators: " + model.countLivingAnimalsByType("Predator"));
        herbivoreCountLabel.setText("Herbivores: " + model.countLivingAnimalsByType("Herbivore"));
        averageGenerationLabel.setText("Avg Gen: " + format((plantStats.averageGeneration + animalStats.averageGeneration) / 2.0));
        averagePlantCaloriesLabel.setText("Avg Plant Calories: " + format(plantStats.averageCalories));
        averagePlantHeightLabel.setText("Avg Plant Height: " + format(plantStats.averageHeight));
        averageAnimalCaloriesLabel.setText("Avg Animal Calories: " + format(animalStats.averageCalories));
        sunLabel.setText("Sun: " + format(model.getSunValue()));
    }

    private PlantStats calculatePlantStats(List<Plant> plants) {
        if (plants.isEmpty()) {
            return new PlantStats(0, 0, 0);
        }

        double totalGeneration = 0;
        double totalCalories = 0;
        double totalHeight = 0;

        for (Plant plant : plants) {
            totalGeneration += plant.getGeneration();
            totalCalories += plant.getCalories();
            totalHeight += plant.getHeight();
        }

        return new PlantStats(
                totalGeneration / plants.size(),
                totalCalories / plants.size(),
                totalHeight / plants.size()
        );
    }

    private AnimalStats calculateAnimalStats(List<Animal> animals) {
        int living = 0;
        double totalGeneration = 0;
        double totalCalories = 0;

        for (Animal animal : animals) {
            if (animal.isAlive()) {
                living++;
                totalGeneration += animal.getGeneration();
                totalCalories += animal.getCalories();
            }
        }

        if (living == 0) {
            return new AnimalStats(0, 0);
        }

        return new AnimalStats(totalGeneration / living, totalCalories / living);
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private static class PlantStats {
        private final double averageGeneration;
        private final double averageCalories;
        private final double averageHeight;

        private PlantStats(double averageGeneration, double averageCalories, double averageHeight) {
            this.averageGeneration = averageGeneration;
            this.averageCalories = averageCalories;
            this.averageHeight = averageHeight;
        }
    }

    private static class AnimalStats {
        private final double averageGeneration;
        private final double averageCalories;

        private AnimalStats(double averageGeneration, double averageCalories) {
            this.averageGeneration = averageGeneration;
            this.averageCalories = averageCalories;
        }
    }
}
