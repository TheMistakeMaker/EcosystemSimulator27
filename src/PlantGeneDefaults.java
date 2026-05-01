import java.util.EnumMap;
import java.util.Map;

public class PlantGeneDefaults {
    private PlantGeneDefaults() {
    }

    public static EnumMap<PlantGene, Double> createDefaultGenes() {
        EnumMap<PlantGene, Double> genes = new EnumMap<>(PlantGene.class);

        for (PlantGene gene : PlantGene.values()) {
            genes.put(gene, gene.getDefaultValue());
        }

        return genes;
    }

    public static void cleanGenes(Map<PlantGene, Double> genes) {
        for (PlantGene gene : PlantGene.values()) {
            double value = gene.getDefaultValue();

            if (genes.containsKey(gene)) {
                value = genes.get(gene);
            }

            genes.put(gene, cleanGeneValue(gene, value));
        }
    }

    public static double cleanGeneValue(PlantGene gene, double value) {
        if (gene == null) {
            throw new IllegalArgumentException("Plant gene cannot be null");
        }

        return gene.cleanValue(value);
    }
}