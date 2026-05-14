import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class PlantMutation {
    private static final Random RANDOM = new Random();
    private static final double FOUNDER_VARIATION = 0.12;
    private static final double DIFFERENCE_THRESHOLD = 0.0001;

    private PlantMutation() {
    }

    public static EnumMap<PlantGene, Double> createFounderGenes(Map<PlantGene, Double> baseGenes) {
        return variedCopy(baseGenes, FOUNDER_VARIATION, 1.0);
    }

    public static EnumMap<PlantGene, Double> createChildGenes(Map<PlantGene, Double> parentGenes) {
        double rate = parentGenes.get(PlantGene.MUTATION_RATE);
        double strength = parentGenes.get(PlantGene.MUTATION_STRENGTH);
        return variedCopy(parentGenes, strength, rate);
    }

    private static EnumMap<PlantGene, Double> variedCopy(Map<PlantGene, Double> source, double strength, double rate) {
        EnumMap<PlantGene, Double> copy = new EnumMap<>(PlantGene.class);
        for (PlantGene gene : PlantGene.values()) {
            double value = source.containsKey(gene) ? source.get(gene) : gene.getDefaultValue();
            if (RANDOM.nextDouble() < rate) {
                value *= 1.0 + (RANDOM.nextDouble() * 2.0 - 1.0) * strength;
            }
            copy.put(gene, PlantGeneDefaults.cleanGeneValue(gene, value));
        }
        return copy;
    }

    public static int countDifferences(Map<PlantGene, Double> originalGenes, Map<PlantGene, Double> newGenes) {
        int count = 0;
        for (PlantGene gene : PlantGene.values()) {
            if (Math.abs(originalGenes.get(gene) - newGenes.get(gene)) > DIFFERENCE_THRESHOLD) {
                count++;
            }
        }
        return count;
    }

    public static String summarizeDifferences(Map<PlantGene, Double> originalGenes, Map<PlantGene, Double> newGenes) {
        StringBuilder summary = new StringBuilder();
        int shown = 0;
        for (PlantGene gene : PlantGene.values()) {
            if (Math.abs(originalGenes.get(gene) - newGenes.get(gene)) > DIFFERENCE_THRESHOLD && shown < 8) {
                summary.append(gene).append(": ").append(format(originalGenes.get(gene))).append(" -> ").append(format(newGenes.get(gene))).append("; ");
                shown++;
            }
        }
        return summary.length() == 0 ? "No visible genetic change" : summary.toString();
    }

    private static String format(double value) {
        return String.format("%.3f", value);
    }
}
