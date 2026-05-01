import java.util.EnumMap;

public class PlantFactory {
    private PlantFactory() {
    }

    public static Plant createRandomFounderPlant(int row, int col) {
        EnumMap<PlantGene, Double> baseGenes = PlantGeneDefaults.createDefaultGenes();
        EnumMap<PlantGene, Double> founderGenes = PlantMutation.createFounderGenes(baseGenes);

        Plant.SpreadMethod baseMethod = Plant.SpreadMethod.LOCAL_SEEDS;
        Plant.SpreadMethod founderMethod = PlantMutation.possiblyVaryFounderSpreadMethod(baseMethod);

        Plant plant = new Plant(
                row,
                col,
                founderGenes,
                "Plant",
                0,
                founderMethod
        );

        plant.setVariationRecord(baseGenes, baseMethod, "Founder variation");

        return plant;
    }
}