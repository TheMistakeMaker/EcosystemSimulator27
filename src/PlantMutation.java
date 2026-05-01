import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class PlantMutation {
    private static final Random RANDOM = new Random();

    private static final double FOUNDER_VARIATION_STRENGTH = 0.10;
    private static final double FOUNDER_SPREAD_METHOD_VARIATION_CHANCE = 0.12;
    private static final double DIFFERENCE_THRESHOLD = 0.0001;
    private static final int MAX_SUMMARY_ITEMS = 10;

    private PlantMutation() {
    }

    public static EnumMap<PlantGene, Double> createFounderGenes(Map<PlantGene, Double> baseGenes) {
        EnumMap<PlantGene, Double> founderGenes = new EnumMap<>(PlantGene.class);

        for (PlantGene gene : PlantGene.values()) {
            double baseValue = baseGenes.containsKey(gene) ? baseGenes.get(gene) : gene.getDefaultValue();
            double changedValue = baseValue * (1.0 + randomChange(FOUNDER_VARIATION_STRENGTH));

            founderGenes.put(gene, PlantGeneDefaults.cleanGeneValue(gene, changedValue));
        }

        return founderGenes;
    }

    public static EnumMap<PlantGene, Double> createChildGenes(Map<PlantGene, Double> parentGenes) {
        EnumMap<PlantGene, Double> childGenes = new EnumMap<>(PlantGene.class);

        double mutationRate = parentGenes.get(PlantGene.MUTATION_RATE);
        double mutationStrength = parentGenes.get(PlantGene.MUTATION_STRENGTH);

        for (PlantGene gene : PlantGene.values()) {
            double value = parentGenes.containsKey(gene) ? parentGenes.get(gene) : gene.getDefaultValue();

            if (RANDOM.nextDouble() < mutationRate) {
                value *= 1.0 + randomChange(mutationStrength);
            }

            childGenes.put(gene, PlantGeneDefaults.cleanGeneValue(gene, value));
        }

        return childGenes;
    }

    public static Plant.SpreadMethod possiblyVaryFounderSpreadMethod(Plant.SpreadMethod method) {
        if (RANDOM.nextDouble() >= FOUNDER_SPREAD_METHOD_VARIATION_CHANCE) {
            return method;
        }

        return randomSpreadMethod();
    }

    public static Plant.SpreadMethod possiblyMutateSpreadMethod(Plant.SpreadMethod method, double mutationRate) {
        if (RANDOM.nextDouble() >= mutationRate) {
            return method;
        }

        return randomSpreadMethod();
    }

    public static int countDifferences(
            Map<PlantGene, Double> originalGenes,
            Map<PlantGene, Double> newGenes,
            Plant.SpreadMethod originalMethod,
            Plant.SpreadMethod newMethod
    ) {
        int count = 0;

        for (PlantGene gene : PlantGene.values()) {
            if (originalGenes.containsKey(gene) && newGenes.containsKey(gene)) {
                double oldValue = originalGenes.get(gene);
                double newValue = newGenes.get(gene);

                if (Math.abs(oldValue - newValue) > DIFFERENCE_THRESHOLD) {
                    count++;
                }
            }
        }

        if (originalMethod != newMethod) {
            count++;
        }

        return count;
    }

    public static String summarizeDifferences(
            Map<PlantGene, Double> originalGenes,
            Map<PlantGene, Double> newGenes,
            Plant.SpreadMethod originalMethod,
            Plant.SpreadMethod newMethod
    ) {
        StringBuilder summary = new StringBuilder();
        int shown = 0;

        for (PlantGene gene : PlantGene.values()) {
            if (originalGenes.containsKey(gene) && newGenes.containsKey(gene)) {
                double oldValue = originalGenes.get(gene);
                double newValue = newGenes.get(gene);

                if (Math.abs(oldValue - newValue) > DIFFERENCE_THRESHOLD && shown < MAX_SUMMARY_ITEMS) {
                    summary.append(gene)
                            .append(": ")
                            .append(format(oldValue))
                            .append(" -> ")
                            .append(format(newValue))
                            .append("; ");
                    shown++;
                }
            }
        }

        if (originalMethod != newMethod && shown < MAX_SUMMARY_ITEMS) {
            summary.append("spreadMethod: ")
                    .append(originalMethod)
                    .append(" -> ")
                    .append(newMethod)
                    .append("; ");
        }

        if (summary.length() == 0) {
            return "No visible genetic change";
        }

        return summary.toString();
    }

    private static Plant.SpreadMethod randomSpreadMethod() {
        Plant.SpreadMethod[] methods = Plant.SpreadMethod.values();
        return methods[RANDOM.nextInt(methods.length)];
    }

    private static double randomChange(double strength) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * strength;
    }

    private static String format(double value) {
        return String.format("%.4f", value);
    }
}