import java.util.EnumMap;

public class PlantFactory {
    private PlantFactory() {
    }

    public static Plant createRandomFounderPlant(int row, int col) {
        EnumMap<PlantGene, Double> base = PlantGeneDefaults.createDefaultGenes();
        EnumMap<PlantGene, Double> genes = PlantMutation.createFounderGenes(base);
        Plant plant = new Plant(row, col, genes, "Plant", 0);
        plant.setFounderGeneBaseline(base);
        plant.setVariationRecord(base, "Founder plant variation");
        return plant;
    }
}
