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
            genes.put(gene, cleanGeneValue(gene, genes.containsKey(gene) ? genes.get(gene) : gene.getDefaultValue()));
        }
    }

    public static double cleanGeneValue(PlantGene gene, double value) {
        return gene.cleanValue(value);
    }
}
