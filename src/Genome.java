import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Genome {
    private final LinkedHashMap<String, Double> values = new LinkedHashMap<>();

    public static Genome createDefaultAnimalGenome() {
        Genome genome = new Genome();
        for (AnimalGene gene : AnimalGene.values()) {
            genome.put(gene.getKey(), gene.getDefaultValue());
        }
        return genome;
    }

    public static Genome fromAnimalGenes(Map<String, Double> customGenes) {
        Genome genome = createDefaultAnimalGenome();
        if (customGenes != null) {
            for (Map.Entry<String, Double> entry : customGenes.entrySet()) {
                genome.put(entry.getKey(), entry.getValue());
            }
        }
        return genome;
    }

    public static Genome createMixedMutatedGenome(Genome first, Genome second, Random random) {
        Genome child = new Genome();
        double rate = (first.get("mutationRate") + second.get("mutationRate")) / 2.0;
        double strength = (first.get("mutationStrength") + second.get("mutationStrength")) / 2.0;

        for (String geneName : first.values.keySet()) {
            double value = random.nextBoolean() ? first.get(geneName) : second.get(geneName);
            if (random.nextDouble() < rate) {
                value *= 1.0 + (random.nextDouble() * 2.0 - 1.0) * strength;
            }
            child.put(geneName, value);
        }
        return child;
    }

    public void put(String geneName, double value) {
        if (geneName == null || geneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Gene name cannot be blank");
        }
        values.put(geneName, cleanValue(geneName, value));
    }

    public double get(String geneName) {
        if (!values.containsKey(geneName)) {
            throw new IllegalArgumentException("Missing animal gene: " + geneName);
        }
        return values.get(geneName);
    }

    public boolean contains(String geneName) {
        return values.containsKey(geneName);
    }

    public double distanceTo(Genome other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }

        double total = 0;
        int count = 0;
        for (String geneName : values.keySet()) {
            if (!other.contains(geneName)) {
                continue;
            }
            double a = get(geneName);
            double b = other.get(geneName);
            double scale = (Math.abs(a) + Math.abs(b)) / 2.0;
            if (scale > 0) {
                total += Math.abs(a - b) / scale;
                count++;
            }
        }
        return count == 0 ? Double.MAX_VALUE : total / count;
    }

    public Map<String, Double> toMap() {
        return new LinkedHashMap<>(values);
    }

    Map<String, Double> backingMap() {
        return values;
    }

    private double cleanValue(String geneName, double value) {
        AnimalGene gene = AnimalGene.findByKey(geneName);
        if (gene != null) {
            return gene.cleanValue(value);
        }
        return Math.max(0.001, Double.isFinite(value) ? value : 0.001);
    }
}
