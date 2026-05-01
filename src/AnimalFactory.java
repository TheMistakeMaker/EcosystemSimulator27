import java.util.LinkedHashMap;
import java.util.Map;

public class AnimalFactory {
    private AnimalFactory() {
    }

    public static Herbivore createRandomFounderHerbivore(int row, int col) {
        Map<String, Double> baseGenes = Genome.createDefaultAnimalGenome().toMap();
        Map<String, Double> adjustedBase = withHerbivoreDefaults(baseGenes);
        Map<String, Double> founderGenes = AnimalMutation.createFounderGenes(adjustedBase, null);

        Herbivore herbivore = new Herbivore(row, col, founderGenes, "Herbivore", 0);
        herbivore.setVariationRecord(adjustedBase, "Founder herbivore variation");
        return herbivore;
    }

    public static Predator createRandomFounderPredator(int row, int col) {
        Map<String, Double> baseGenes = Genome.createDefaultAnimalGenome().toMap();
        Map<String, Double> adjustedBase = withPredatorDefaults(baseGenes);
        Map<String, Double> founderGenes = AnimalMutation.createFounderGenes(adjustedBase, null);

        Predator predator = new Predator(row, col, founderGenes, "Predator", 0);
        predator.setVariationRecord(adjustedBase, "Founder predator variation");
        return predator;
    }

    private static Map<String, Double> withHerbivoreDefaults(Map<String, Double> base) {
        Map<String, Double> genes = new LinkedHashMap<>(base);

        genes.put("maxCalories", 124.0);
        genes.put("maxHydration", 110.0);
        genes.put("maxEnergy", 110.0);
        genes.put("startingCaloriesRatio", 0.76);
        genes.put("startingHydrationRatio", 0.86);
        genes.put("startingEnergyRatio", 0.84);
        genes.put("metabolismRate", 0.27);
        genes.put("hydrationLossRate", 0.20);
        genes.put("bodySize", 0.90);
        genes.put("bodyFatRatio", 0.20);
        genes.put("maxSpeed", 1.22);
        genes.put("turningAgility", 1.28);
        genes.put("visionRange", 9.5);
        genes.put("hearingRange", 6.5);
        genes.put("fearfulness", 0.64);
        genes.put("riskTolerance", 0.34);
        genes.put("aggression", 0.18);
        genes.put("sociality", 0.70);
        genes.put("herdingDrive", 0.74);
        genes.put("preferredPredatorDistance", 8.0);
        genes.put("panicDistance", 3.5);
        genes.put("flightInitiationDistance", 6.0);
        genes.put("plantBiteSize", 18.0);
        genes.put("plantDigestionEfficiency", 1.00);
        genes.put("plantToxicityAversion", 0.95);
        genes.put("attackDamage", 5.0);
        genes.put("defense", 2.1);
        genes.put("evasion", 0.32);
        genes.put("reproductionDrive", 0.44);
        genes.put("reproductionAgeTicks", 100.0);
        genes.put("reproductionCooldownTicks", 240.0);
        genes.put("reproductionCaloriesRequired", 76.0);
        genes.put("reproductionEnergyRequired", 62.0);
        genes.put("reproductionCaloriesCost", 36.0);
        genes.put("reproductionEnergyCost", 32.0);
        genes.put("maxMatingGeneticDistance", 0.32);

        return genes;
    }

    private static Map<String, Double> withPredatorDefaults(Map<String, Double> base) {
        Map<String, Double> genes = new LinkedHashMap<>(base);

        genes.put("maxCalories", 160.0);
        genes.put("maxHydration", 140.0);
        genes.put("maxEnergy", 140.0);
        genes.put("startingCaloriesRatio", 0.96);
        genes.put("startingHydrationRatio", 0.98);
        genes.put("startingEnergyRatio", 0.98);
        genes.put("metabolismRate", 0.08);
        genes.put("hydrationLossRate", 0.05);
        genes.put("bodySize", 1.10);
        genes.put("bodyFatRatio", 0.32);
        genes.put("muscleDensity", 1.24);
        genes.put("maxSpeed", 1.18);
        genes.put("acceleration", 1.22);
        genes.put("visionRange", 11.0);
        genes.put("smellRange", 11.5);
        genes.put("aggression", 0.44);
        genes.put("fearfulness", 0.25);
        genes.put("riskTolerance", 0.50);
        genes.put("patience", 0.70);
        genes.put("attackDamage", 15.0);
        genes.put("attackAccuracy", 0.50);
        genes.put("biteForce", 1.18);
        genes.put("clawSharpness", 1.12);
        genes.put("strikeSpeed", 1.12);
        genes.put("defense", 1.25);
        genes.put("preySizePreference", 0.95);
        genes.put("preyFatPreference", 0.24);
        genes.put("preyWeaknessPreference", 0.88);
        genes.put("chasePersistence", 0.42);
        genes.put("ambushPreference", 0.48);
        genes.put("scavengingPreference", 0.66);
        genes.put("attackEnergyCost", 2.7);
        genes.put("attackCalorieCost", 0.9);
        genes.put("packHuntingDrive", 0.12);
        genes.put("plantDigestionEfficiency", 0.05);
        genes.put("reproductionDrive", 0.20);
        genes.put("reproductionAgeTicks", 160.0);
        genes.put("reproductionCooldownTicks", 540.0);
        genes.put("reproductionCaloriesRequired", 96.0);
        genes.put("reproductionEnergyRequired", 74.0);
        genes.put("reproductionCaloriesCost", 45.0);
        genes.put("reproductionEnergyCost", 38.0);
        genes.put("foodSeekThreshold", 0.44);
        genes.put("plantBiteSize", 22.0);
        genes.put("maxAgeTicks", 4200.0);
        genes.put("maxMatingGeneticDistance", 0.30);

        return genes;
    }
}
