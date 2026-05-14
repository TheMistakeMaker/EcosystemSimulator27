import java.util.ArrayList;
import java.util.List;

public class OrganismStore {
    private final List<Plant> plants;
    private final List<Animal> animals;

    public OrganismStore() {
        this.plants = new ArrayList<>();
        this.animals = new ArrayList<>();
    }

    public boolean addPlant(Plant plant) {
        if (plant == null || plants.contains(plant)) {
            return false;
        }

        return plants.add(plant);
    }

    public boolean removePlant(Plant plant) {
        if (plant == null) {
            return false;
        }

        return plants.remove(plant);
    }

    public boolean addAnimal(Animal animal) {
        if (animal == null || animals.contains(animal)) {
            return false;
        }

        return animals.add(animal);
    }

    public boolean removeAnimal(Animal animal) {
        if (animal == null) {
            return false;
        }

        return animals.remove(animal);
    }

    public List<Plant> getPlantsSnapshot() {
        return new ArrayList<>(plants);
    }

    public List<Animal> getAnimalsSnapshot() {
        return new ArrayList<>(animals);
    }

    public int countLivingAnimals() {
        int count = 0;

        for (Animal animal : animals) {
            if (animal.isAlive()) {
                count++;
            }
        }

        return count;
    }

    public Animal findNearestMate(Animal searcher, double visionRange) {
        Animal best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Animal animal : animals) {
            if (animal != searcher && animal.isAlive() && searcher.canReproduceWith(animal)) {
                double distance = searcher.distanceTo(animal);

                if (distance <= visionRange) {
                    double score = AnimalGeneEffects.mateScore(searcher, animal) - distance * 0.03;

                    if (score > bestScore) {
                        bestScore = score;
                        best = animal;
                    }
                }
            }
        }

        return best;
    }

    public Animal findNearestPrey(Animal hunter, double visionRange) {
        Animal closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Animal animal : animals) {
            if (animal != hunter && animal.isAlive() && hunter.canEatAnimal(animal)) {
                double distance = hunter.distanceTo(animal);

                if (distance <= visionRange && distance < closestDistance) {
                    closestDistance = distance;
                    closest = animal;
                }
            }
        }

        return closest;
    }
}