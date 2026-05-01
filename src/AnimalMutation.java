import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class AnimalMutation {
    private static final Random RANDOM = new Random();
    private static final double DIFFERENCE_THRESHOLD = 0.0001;
    private static final int MAX_SUMMARY_ITEMS = 12;

    private AnimalMutation() {
    }

    public static Map<String, Double> createFounderGenes(Map<String, Double> baseGenes, Map<String, Double> speciesAdjustments) {
        LinkedHashMap<String, Double> founderGenes = new LinkedHashMap<>();
        double variationStrength = getOrDefault(baseGenes, "founderVariationStrength", 0.08);

        for (AnimalGene gene : AnimalGene.values()) {
            String key = gene.getKey();
            double value = getOrDefault(baseGenes, key, gene.getDefaultValue());

            if (speciesAdjustments != null && speciesAdjustments.containsKey(key)) {
                value = speciesAdjustments.get(key);
            }

            value *= 1.0 + randomChange(variationStrength);
            founderGenes.put(key, gene.cleanValue(value));
        }

        return founderGenes;
    }

    public static int countDifferences(Map<String, Double> originalGenes, Map<String, Double> newGenes) {
        int count = 0;

        for (String geneName : newGenes.keySet()) {
            if (originalGenes.containsKey(geneName)) {
                double oldValue = originalGenes.get(geneName);
                double newValue = newGenes.get(geneName);

                if (Math.abs(oldValue - newValue) > DIFFERENCE_THRESHOLD) {
                    count++;
                }
            }
        }

        return count;
    }

    public static String summarizeDifferences(Map<String, Double> originalGenes, Map<String, Double> newGenes) {
        StringBuilder summary = new StringBuilder();
        int shown = 0;

        for (String geneName : newGenes.keySet()) {
            if (originalGenes.containsKey(geneName)) {
                double oldValue = originalGenes.get(geneName);
                double newValue = newGenes.get(geneName);

                if (Math.abs(oldValue - newValue) > DIFFERENCE_THRESHOLD && shown < MAX_SUMMARY_ITEMS) {
                    summary.append(geneName)
                            .append(": ")
                            .append(format(oldValue))
                            .append(" -> ")
                            .append(format(newValue))
                            .append("; ");
                    shown++;
                }
            }
        }

        if (summary.length() == 0) {
            return "No visible genetic change";
        }

        return summary.toString();
    }

    private static double getOrDefault(Map<String, Double> genes, String key, double fallback) {
        if (genes == null || !genes.containsKey(key)) {
            return fallback;
        }

        return genes.get(key);
    }

    private static double randomChange(double strength) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * strength;
    }

    private static String format(double value) {
        return String.format("%.4f", value);
    }
}
