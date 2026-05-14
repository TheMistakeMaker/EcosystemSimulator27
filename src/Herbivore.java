import java.util.LinkedHashMap;
import java.util.Map;

public class Herbivore extends Animal {
    private Animal lastThreat;
    private Plant lastFood;
    private int panicTicks;

    public Herbivore(int row, int col, Map<String, Double> customGenes, String speciesName, int generation) {
        super(row, col, customGenes, speciesName, generation);
    }

    @Override
    protected void decideAction(WorldModel world) {
        Animal threat = findThreat(world);
        if (threat != null && shouldFlee(threat, world)) {
            lastThreat = threat;
            panicTicks++;
            moveAwayWithSpeed(threat.getRow(), threat.getCol(), world);
            return;
        }
        if (hydrationRatio() < 0.60 && seekWater(world)) return;
        if (energyRatio() < 0.25 || healthRatio() < 0.45) { rest(); return; }
        if (tryReproduce(world)) return;
        if (caloriesRatio() < 0.62 && graze(world)) return;
        if (regroup(world)) return;
        if (RANDOM.nextDouble() < 0.35 + getGene("sense") * 0.04) moveRandomly(world); else rest();
    }

    @Override
    public boolean canEatAnimal(Animal other) {
        return false;
    }

    @Override
    protected Animal createChild(int row, int col, Map<String, Double> childGenes, String childSpeciesName, int childGeneration) {
        Herbivore child = new Herbivore(row, col, childGenes, childSpeciesName, childGeneration);
        child.setVariationRecord(getGenes(), "Inherited herbivore genes");
        return child;
    }

    @Override
    public String getAnimalType() {
        return "Herbivore";
    }

    @Override
    public Map<String, String> getInspectionData() {
        Map<String, String> data = new LinkedHashMap<>(super.getInspectionData());
        data.put("State", lastThreat == null ? "Grazing / searching" : "Threat-aware");
        data.put("Panic Ticks", String.valueOf(panicTicks));
        data.put("Last Threat", lastThreat == null ? "none" : String.valueOf(lastThreat.getId()));
        data.put("Last Food", lastFood == null ? "none" : String.valueOf(lastFood.getId()));
        return data;
    }

    private Animal findThreat(WorldModel world) {
        Animal best = null;
        double bestScore = 0;
        double range = AnimalGeneEffects.detectionRange(this, "threat");
        for (Animal animal : world.getAnimalsSnapshot()) {
            if (!animal.isAlive() || !animal.canEatAnimal(this)) continue;
            double distance = distanceTo(animal);
            if (distance > range) continue;
            double score = animal.getGene("speed") + animal.getGene("attack") + animal.getGene("aggression")
                    - animal.getGene("camouflage") * 0.35 - getGene("camouflage") * 0.20 - distance * 0.12;
            if (score > bestScore) {
                bestScore = score;
                best = animal;
            }
        }
        return best;
    }

    private boolean shouldFlee(Animal threat, WorldModel world) {
        double distance = distanceTo(threat);
        int allies = countNearbyHerbivores(world, 4);
        double pressure = 2.4 + getGene("fear") * 5.0 - getGene("armor") * 0.65 - getGene("camouflage") * 0.45
                - allies * getGene("social") * 0.20;
        rememberedDanger = clamp(rememberedDanger + Math.max(0, pressure - distance) * 0.035, 0, 1);
        return distance < pressure || rememberedDanger > 0.65;
    }

    private boolean seekWater(WorldModel world) {
        int[] water = world.findNearestWater(row, col, AnimalGeneEffects.detectionRange(this, "water"));
        if (water == null) return false;
        if (distanceTo(water[0], water[1]) <= 1.5) {
            drink(AnimalGeneEffects.drinkAmount(this));
            return true;
        }
        return moveTowardWithSpeed(water[0], water[1], world);
    }

    private boolean tryReproduce(WorldModel world) {
        int herbivores = world.countLivingAnimalsByType("Herbivore");
        int plants = world.getPlantsSnapshot().size();
        int herbivoreLimit = Math.max(90, (int) Math.round(world.getRows() * world.getCols() * 0.018));
        if (herbivores > herbivoreLimit || plants < herbivores * 4) return false;
        if (!isReproductivelyReady() || RANDOM.nextDouble() > getGene("fertility") * 0.34) return false;
        if (countNearbyHerbivores(world, 3) > 4 || countNearbyPlants(world, 5) < 3) return false;
        Animal mate = world.findNearestMate(this, Math.max(16.0, AnimalGeneEffects.detectionRange(this, "social")));
        if (mate == null) return false;
        if (distanceTo(mate) <= 5.0) reproduceWith(mate, world); else moveTowardWithSpeed(mate.getRow(), mate.getCol(), world);
        return true;
    }

    private boolean graze(WorldModel world) {
        Plant plant = findBestPlant(world);
        if (plant == null) {
            frustration = clamp(frustration + 0.04, 0, 1);
            return false;
        }
        lastFood = plant;
        if (distanceTo(plant.getRow(), plant.getCol()) <= 1.5) {
            double bite = 8 + getGene("size") * 5.5 + getGene("digestion") * 3.5;
            double eaten = world.eatPlantAt(plant.getRow(), plant.getCol(), bite, this);
            gainCalories(eaten * (0.72 + getGene("digestion") * 0.22));
            drink(eaten * (0.16 + getGene("waterRetention") * 0.06));
            return true;
        }
        return moveTowardWithSpeed(plant.getRow(), plant.getCol(), world);
    }

    private Plant findBestPlant(WorldModel world) {
        Plant best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double range = AnimalGeneEffects.detectionRange(this, "food");
        for (Plant plant : world.getPlantsSnapshot()) {
            if (!plant.isAlive()) continue;
            double distance = distanceTo(plant.getRow(), plant.getCol());
            if (distance > range) continue;
            double toxinPenalty = plant.getGene(PlantGene.TOXICITY) * (0.70 + getGene("fear") * 0.55);
            double defensePenalty = plant.getGene(PlantGene.THORNS) * 0.75 + plant.getGene(PlantGene.WOODINESS) * 0.22;
            double leafReward = plant.getGene(PlantGene.LEAF_SIZE) * 1.45;
            double reward = plant.getCalories() * 0.035 + plant.getHeight() * 0.55 + leafReward;
            double score = reward - toxinPenalty - defensePenalty - distance * 0.22;
            if (score > bestScore) {
                bestScore = score;
                best = plant;
            }
        }
        return best;
    }

    private boolean regroup(WorldModel world) {
        if (getGene("social") < 0.45 || rememberedDanger > 0.75) return false;
        Animal nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Animal animal : world.getAnimalsSnapshot()) {
            if (animal != this && animal instanceof Herbivore && animal.isAlive()) {
                double d = distanceTo(animal);
                if (d < nearestDistance) {
                    nearestDistance = d;
                    nearest = animal;
                }
            }
        }
        if (nearest != null && nearestDistance > 5.0 - getGene("social") && nearestDistance < AnimalGeneEffects.detectionRange(this, "social")) {
            return moveTowardWithSpeed(nearest.getRow(), nearest.getCol(), world);
        }
        return false;
    }

    private int countNearbyPlants(WorldModel world, int range) {
        int count = 0;
        for (Plant plant : world.getPlantsSnapshot()) {
            if (plant.isAlive() && distanceTo(plant.getRow(), plant.getCol()) <= range) {
                count++;
            }
        }
        return count;
    }

    private int countNearbyHerbivores(WorldModel world, int range) {
        int count = 0;
        for (Animal animal : world.getAnimalsSnapshot()) {
            if (animal != this && animal instanceof Herbivore && animal.isAlive() && distanceTo(animal) <= range) {
                count++;
            }
        }
        return count;
    }
}
