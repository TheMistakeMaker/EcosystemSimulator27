import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class AnimalMutation {
    private static final Random RANDOM = new Random();
    private static final double FOUNDER_VARIATION = 0.12;
    private static final double DIFFERENCE_THRESHOLD = 0.0001;

    private AnimalMutation() {
    }

    public static Map<String, Double> createFounderGenes(Map<String, Double> baseGenes, Map<String, Double> speciesAdjustments) {
        LinkedHashMap<String, Double> genes = new LinkedHashMap<>();
        for (AnimalGene gene : AnimalGene.values()) {
            String key = gene.getKey();
            double value = speciesAdjustments != null && speciesAdjustments.containsKey(key)
                    ? speciesAdjustments.get(key)
                    : baseGenes.getOrDefault(key, gene.getDefaultValue());
            value *= 1.0 + (RANDOM.nextDouble() * 2.0 - 1.0) * FOUNDER_VARIATION;
            genes.put(key, gene.cleanValue(value));
        }
        return genes;
    }

    public static int countDifferences(Map<String, Double> originalGenes, Map<String, Double> newGenes) {
        int count = 0;
        for (String key : newGenes.keySet()) {
            if (originalGenes.containsKey(key) && Math.abs(originalGenes.get(key) - newGenes.get(key)) > DIFFERENCE_THRESHOLD) {
                count++;
            }
        }
        return count;
    }

    public static String summarizeDifferences(Map<String, Double> originalGenes, Map<String, Double> newGenes) {
        StringBuilder summary = new StringBuilder();
        int shown = 0;
        for (String key : newGenes.keySet()) {
            if (originalGenes.containsKey(key) && Math.abs(originalGenes.get(key) - newGenes.get(key)) > DIFFERENCE_THRESHOLD && shown < 10) {
                summary.append(key).append(": ").append(format(originalGenes.get(key))).append(" -> ").append(format(newGenes.get(key))).append("; ");
                shown++;
            }
        }
        return summary.length() == 0 ? "No visible genetic change" : summary.toString();
    }

    private static String format(double value) {
        return String.format("%.3f", value);
    }
}
