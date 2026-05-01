import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Genome {
    private static final double UNKNOWN_GENE_MINIMUM = 0.001;

    private final LinkedHashMap<String, Double> values;

    public Genome() {
        this.values = new LinkedHashMap<>();
    }

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
            for (String geneName : customGenes.keySet()) {
                genome.put(geneName, customGenes.get(geneName));
            }
        }

        return genome;
    }

    public static Genome createMixedMutatedGenome(Genome first, Genome second, Random random) {
        Genome child = new Genome();

        double mutationRate = (first.get("mutationRate") + second.get("mutationRate")) / 2.0;
        double mutationStrength = (first.get("mutationStrength") + second.get("mutationStrength")) / 2.0;

        for (String geneName : first.values.keySet()) {
            double value = random.nextBoolean() ? first.get(geneName) : second.get(geneName);

            if (random.nextDouble() < mutationRate) {
                value *= 1.0 + ((random.nextDouble() * 2.0 - 1.0) * mutationStrength);
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

        double totalDifference = 0;
        int comparedGenes = 0;

        for (String geneName : values.keySet()) {
            if (other.contains(geneName)) {
                double first = get(geneName);
                double second = other.get(geneName);
                double average = (Math.abs(first) + Math.abs(second)) / 2.0;

                if (average > 0) {
                    totalDifference += Math.abs(first - second) / average;
                    comparedGenes++;
                }
            }
        }

        if (comparedGenes == 0) {
            return Double.MAX_VALUE;
        }

        return totalDifference / comparedGenes;
    }

    public Map<String, Double> toMap() {
        return new LinkedHashMap<>(values);
    }

    Map<String, Double> backingMap() {
        return values;
    }

    private double cleanValue(String geneName, double value) {
        AnimalGene knownGene = AnimalGene.findByKey(geneName);

        if (knownGene != null) {
            return knownGene.cleanValue(value);
        }

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            value = UNKNOWN_GENE_MINIMUM;
        }

        return Math.max(UNKNOWN_GENE_MINIMUM, value);
    }
}