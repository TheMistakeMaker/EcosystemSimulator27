import java.util.LinkedHashMap;
import java.util.Map;

public class AnimalFactory {
    private AnimalFactory() {
    }

    public static Herbivore createRandomFounderHerbivore(int row, int col) {
        Map<String, Double> base = herbivoreDefaults();
        Map<String, Double> genes = AnimalMutation.createFounderGenes(base, null);
        Herbivore herbivore = new Herbivore(row, col, genes, "Herbivore", 0);
        herbivore.setFounderGeneBaseline(base);
        herbivore.setVariationRecord(base, "Founder herbivore variation");
        return herbivore;
    }

    public static Predator createRandomFounderPredator(int row, int col) {
        Map<String, Double> base = predatorDefaults();
        Map<String, Double> genes = AnimalMutation.createFounderGenes(base, null);
        Predator predator = new Predator(row, col, genes, "Predator", 0);
        predator.setFounderGeneBaseline(base);
        predator.setVariationRecord(base, "Founder predator variation");
        return predator;
    }

    private static Map<String, Double> herbivoreDefaults() {
        Map<String, Double> g = Genome.createDefaultAnimalGenome().toMap();
        g.put("size", 0.90);
        g.put("speed", 1.22);
        g.put("sense", 1.28);
        g.put("stamina", 1.15);
        g.put("metabolism", 0.82);
        g.put("armor", 0.42);
        g.put("attack", 0.10);
        g.put("aggression", 0.16);
        g.put("fear", 0.68);
        g.put("social", 0.70);
        g.put("fertility", 0.88);
        g.put("parentalCare", 0.52);
        g.put("litterSize", 1.25);
        g.put("camouflage", 0.55);
        g.put("digestion", 1.12);
        g.put("waterRetention", 1.05);
        return g;
    }

    private static Map<String, Double> predatorDefaults() {
        Map<String, Double> g = new LinkedHashMap<>(Genome.createDefaultAnimalGenome().toMap());
        g.put("size", 1.28);
        g.put("speed", 1.32);
        g.put("sense", 1.78);
        g.put("stamina", 1.24);
        g.put("metabolism", 0.70);
        g.put("armor", 0.55);
        g.put("attack", 1.10);
        g.put("aggression", 0.60);
        g.put("fear", 0.16);
        g.put("social", 0.46);
        g.put("fertility", 1.0);
        g.put("parentalCare", 0.62);
        g.put("litterSize", 2.0);
        g.put("camouflage", 0.62);
        g.put("digestion", 1.18);
        g.put("waterRetention", 1.00);
        return g;
    }
}
