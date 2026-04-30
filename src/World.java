import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World extends JPanel {
    public enum TileType {
        GRASS,
        WATER,
        ROCK
    }

    public enum ToolType {
        GRASS,
        WATER,
        ROCK,
        PLANT,
        ERASE_PLANT
    }

    private final int rows;
    private final int cols;
    private final int tileSize;

    private double sunValue;

    private final TileType[][] tiles;
    private final JPanel[][] tilePanels;

    private final Plant[][] plantGrid;
    private final Animal[][] animalGrid;
    private final double[][] corpseCalories;

    private final List<Animal> animals;

    private ToolType selectedTool;

    private final Timer simulationTimer;
    private boolean simulationRunning;

    private int nextGeneratedSpeciesNumber;

    public World(int rows, int cols, int tileSize) {
        this.rows = rows;
        this.cols = cols;
        this.tileSize = tileSize;

        this.sunValue = 1.0;

        tiles = new TileType[rows][cols];
        tilePanels = new JPanel[rows][cols];

        plantGrid = new Plant[rows][cols];
        animalGrid = new Animal[rows][cols];
        corpseCalories = new double[rows][cols];

        animals = new ArrayList<>();

        selectedTool = ToolType.GRASS;
        simulationRunning = false;
        nextGeneratedSpeciesNumber = 1;

        setLayout(new BorderLayout());

        createBlankMap();
        createTileGrid();
        createToolbar();

        simulationTimer = new Timer(20, e -> updateWorld());
    }

    private void createBlankMap() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tiles[row][col] = TileType.GRASS;
                plantGrid[row][col] = null;
                animalGrid[row][col] = null;
                corpseCalories[row][col] = 0;
            }
        }
    }

    private void createTileGrid() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(rows, cols));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                JPanel tilePanel = new JPanel();

                tilePanel.setPreferredSize(new Dimension(tileSize, tileSize));
                tilePanel.setOpaque(true);

                int clickedRow = row;
                int clickedCol = col;

                tilePanel.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            inspectTile(clickedRow, clickedCol);
                        } else {
                            useSelectedTool(clickedRow, clickedCol);
                        }
                    }
                });

                tilePanels[row][col] = tilePanel;
                updateTilePanel(row, col);

                gridPanel.add(tilePanel);
            }
        }

        add(gridPanel, BorderLayout.CENTER);
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel();

        JToggleButton grassButton = new JToggleButton("Grass");
        JToggleButton waterButton = new JToggleButton("Water");
        JToggleButton rockButton = new JToggleButton("Rock");
        JToggleButton plantButton = new JToggleButton("Plant");
        JToggleButton erasePlantButton = new JToggleButton("Erase Plant");

        grassButton.setSelected(true);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(grassButton);
        toolGroup.add(waterButton);
        toolGroup.add(rockButton);
        toolGroup.add(plantButton);
        toolGroup.add(erasePlantButton);

        grassButton.addActionListener(e -> selectedTool = ToolType.GRASS);
        waterButton.addActionListener(e -> selectedTool = ToolType.WATER);
        rockButton.addActionListener(e -> selectedTool = ToolType.ROCK);
        plantButton.addActionListener(e -> selectedTool = ToolType.PLANT);
        erasePlantButton.addActionListener(e -> selectedTool = ToolType.ERASE_PLANT);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(e -> updateWorld());

        JButton startPauseButton = new JButton("Start");
        startPauseButton.addActionListener(e -> {
            if (simulationRunning) {
                simulationTimer.stop();
                simulationRunning = false;
                startPauseButton.setText("Start");
            } else {
                simulationTimer.start();
                simulationRunning = true;
                startPauseButton.setText("Pause");
            }
        });

        JButton lessSunButton = new JButton("Less Sun");
        lessSunButton.addActionListener(e -> {
            setSunValue(sunValue - 0.1);
            refreshAllTiles();
        });

        JButton moreSunButton = new JButton("More Sun");
        moreSunButton.addActionListener(e -> {
            setSunValue(sunValue + 0.1);
            refreshAllTiles();
        });

        toolbar.add(grassButton);
        toolbar.add(waterButton);
        toolbar.add(rockButton);
        toolbar.add(plantButton);
        toolbar.add(erasePlantButton);
        toolbar.add(stepButton);
        toolbar.add(startPauseButton);
        toolbar.add(lessSunButton);
        toolbar.add(moreSunButton);

        add(toolbar, BorderLayout.SOUTH);
    }

    private void useSelectedTool(int row, int col) {
        if (selectedTool == ToolType.GRASS) {
            setTile(row, col, TileType.GRASS);
        } else if (selectedTool == ToolType.WATER) {
            setTile(row, col, TileType.WATER);
        } else if (selectedTool == ToolType.ROCK) {
            setTile(row, col, TileType.ROCK);
        } else if (selectedTool == ToolType.PLANT) {
            addDefaultPlant(row, col);
        } else if (selectedTool == ToolType.ERASE_PLANT) {
            removePlantAt(row, col);
        }
    }

    public void setTile(int row, int col, TileType type) {
        if (!isValidTile(row, col)) {
            return;
        }

        if (animalGrid[row][col] != null && type != TileType.GRASS) {
            return;
        }

        tiles[row][col] = type;

        if (type != TileType.GRASS) {
            plantGrid[row][col] = null;
            corpseCalories[row][col] = 0;
        }

        updateTilePanel(row, col);
    }

    private void updateTilePanel(int row, int col) {
        if (!isValidTile(row, col)) {
            return;
        }

        JPanel panel = tilePanels[row][col];

        if (animalGrid[row][col] != null) {
            panel.setBackground(Color.WHITE);
        } else if (plantGrid[row][col] != null && plantGrid[row][col].isAlive()) {
            panel.setBackground(new Color(0, 90, 0));
        } else if (corpseCalories[row][col] > 0) {
            panel.setBackground(new Color(120, 70, 35));
        } else if (tiles[row][col] == TileType.GRASS) {
            panel.setBackground(Color.GREEN);
        } else if (tiles[row][col] == TileType.WATER) {
            panel.setBackground(Color.BLUE);
        } else if (tiles[row][col] == TileType.ROCK) {
            panel.setBackground(Color.GRAY);
        }
    }

    private void refreshAllTiles() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                updateTilePanel(row, col);
            }
        }
    }

    public void updateWorld() {
        List<Plant> plantCopy = getAllPlants();

        for (Plant plant : plantCopy) {
            if (plant.isAlive()) {
                plant.update(this);
            }
        }

        List<Animal> animalCopy = new ArrayList<>(animals);

        for (Animal animal : animalCopy) {
            if (animal.isAlive()) {
                animal.update(this);
            }
        }

        refreshAllTiles();
    }

    public void addDefaultPlant(int row, int col) {
        Map<String, Double> genes = new HashMap<>();

        Plant plant = new Plant(
                row,
                col,
                genes,
                "Plant",
                0,
                Plant.SpreadMethod.LOCAL_SEEDS
        );

        addPlant(plant);
    }

    public void addPlant(Plant plant) {
        if (plant == null) {
            return;
        }

        int row = plant.getRow();
        int col = plant.getCol();

        if (!isValidTile(row, col)) {
            return;
        }

        if (tiles[row][col] != TileType.GRASS) {
            return;
        }

        if (plantGrid[row][col] != null) {
            return;
        }

        plantGrid[row][col] = plant;
        updateTilePanel(row, col);
    }

    public void removePlant(Plant plant) {
        if (plant == null) {
            return;
        }

        int row = plant.getRow();
        int col = plant.getCol();

        if (isValidTile(row, col) && plantGrid[row][col] == plant) {
            plantGrid[row][col] = null;
            updateTilePanel(row, col);
        }
    }

    public void removePlantAt(int row, int col) {
        if (!isValidTile(row, col)) {
            return;
        }

        plantGrid[row][col] = null;
        updateTilePanel(row, col);
    }

    public Plant getPlant(int row, int col) {
        if (!isValidTile(row, col)) {
            return null;
        }

        return plantGrid[row][col];
    }

    private List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (plantGrid[row][col] != null) {
                    plants.add(plantGrid[row][col]);
                }
            }
        }

        return plants;
    }

    public int countPlantsNear(int centerRow, int centerCol, int range) {
        int count = 0;

        for (int row = centerRow - range; row <= centerRow + range; row++) {
            for (int col = centerCol - range; col <= centerCol + range; col++) {
                if (row == centerRow && col == centerCol) {
                    continue;
                }

                if (isValidTile(row, col)
                        && plantGrid[row][col] != null
                        && plantGrid[row][col].isAlive()) {
                    count++;
                }
            }
        }

        return count;
    }

    public int[] findOpenPlantTileNear(int centerRow, int centerCol, int range) {
        List<int[]> possibleTiles = new ArrayList<>();

        for (int row = centerRow - range; row <= centerRow + range; row++) {
            for (int col = centerCol - range; col <= centerCol + range; col++) {
                if (isOpenForPlant(row, col)) {
                    possibleTiles.add(new int[] {row, col});
                }
            }
        }

        if (possibleTiles.size() == 0) {
            return null;
        }

        int index = (int) (Math.random() * possibleTiles.size());
        return possibleTiles.get(index);
    }

    public int[] findOpenPlantTileNearWater(int centerRow, int centerCol, int range) {
        List<int[]> possibleTiles = new ArrayList<>();

        for (int row = centerRow - range; row <= centerRow + range; row++) {
            for (int col = centerCol - range; col <= centerCol + range; col++) {
                if (isOpenForPlant(row, col) && hasWaterNear(row, col, 1)) {
                    possibleTiles.add(new int[] {row, col});
                }
            }
        }

        if (possibleTiles.size() == 0) {
            return null;
        }

        int index = (int) (Math.random() * possibleTiles.size());
        return possibleTiles.get(index);
    }

    private boolean isOpenForPlant(int row, int col) {
        return isValidTile(row, col)
                && tiles[row][col] == TileType.GRASS
                && plantGrid[row][col] == null;
    }

    private boolean hasWaterNear(int centerRow, int centerCol, int range) {
        for (int row = centerRow - range; row <= centerRow + range; row++) {
            for (int col = centerCol - range; col <= centerCol + range; col++) {
                if (isValidTile(row, col) && tiles[row][col] == TileType.WATER) {
                    return true;
                }
            }
        }

        return false;
    }

    public int[] findNearestPlant(int startRow, int startCol, double visionRange) {
        int maxDistance = (int) visionRange;

        int[] closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int row = startRow - maxDistance; row <= startRow + maxDistance; row++) {
            for (int col = startCol - maxDistance; col <= startCol + maxDistance; col++) {
                if (isValidTile(row, col)
                        && plantGrid[row][col] != null
                        && plantGrid[row][col].isAlive()) {
                    double distance = distance(startRow, startCol, row, col);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closest = new int[] {row, col};
                    }
                }
            }
        }

        return closest;
    }

    public double eatPlantAt(int row, int col, double requestedCalories, Animal eater) {
        if (!isValidTile(row, col)) {
            return 0;
        }

        Plant plant = plantGrid[row][col];

        if (plant == null || !plant.isAlive()) {
            return 0;
        }

        double caloriesEaten = plant.beEaten(requestedCalories, eater, this);

        if (!plant.isAlive()) {
            plantGrid[row][col] = null;
        }

        updateTilePanel(row, col);

        return caloriesEaten;
    }

    public boolean isValidTile(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public TileType getTile(int row, int col) {
        if (!isValidTile(row, col)) {
            return null;
        }

        return tiles[row][col];
    }

    public boolean canAnimalEnter(Animal animal, int row, int col) {
        if (!isValidTile(row, col)) {
            return false;
        }

        if (tiles[row][col] == TileType.WATER || tiles[row][col] == TileType.ROCK) {
            return false;
        }

        return animalGrid[row][col] == null || animalGrid[row][col] == animal;
    }

    public double getMovementCost(int row, int col) {
        if (!isValidTile(row, col)) {
            return Double.MAX_VALUE;
        }

        if (tiles[row][col] == TileType.GRASS) {
            return 1.0;
        }

        if (tiles[row][col] == TileType.WATER) {
            return 3.0;
        }

        return 5.0;
    }

    public void addAnimal(Animal animal) {
        if (animal == null) {
            return;
        }

        int row = animal.getRow();
        int col = animal.getCol();

        if (!isValidTile(row, col)) {
            return;
        }

        if (!canAnimalEnter(animal, row, col)) {
            return;
        }

        animals.add(animal);
        animalGrid[row][col] = animal;

        updateTilePanel(row, col);
    }

    public void removeAnimal(Animal animal) {
        if (animal == null) {
            return;
        }

        animals.remove(animal);

        int row = animal.getRow();
        int col = animal.getCol();

        if (isValidTile(row, col) && animalGrid[row][col] == animal) {
            animalGrid[row][col] = null;
            updateTilePanel(row, col);
        }
    }

    public void updateAnimalPosition(Animal animal, int oldRow, int oldCol, int newRow, int newCol) {
        if (isValidTile(oldRow, oldCol) && animalGrid[oldRow][oldCol] == animal) {
            animalGrid[oldRow][oldCol] = null;
            updateTilePanel(oldRow, oldCol);
        }

        if (isValidTile(newRow, newCol)) {
            animalGrid[newRow][newCol] = animal;
            updateTilePanel(newRow, newCol);
        }
    }

    public int[] findOpenAdjacentTile(int row, int col) {
        int[][] directions = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        List<int[]> openTiles = new ArrayList<>();

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            if (isValidTile(newRow, newCol)
                    && tiles[newRow][newCol] == TileType.GRASS
                    && animalGrid[newRow][newCol] == null) {
                openTiles.add(new int[] {newRow, newCol});
            }
        }

        if (openTiles.size() == 0) {
            return null;
        }

        int index = (int) (Math.random() * openTiles.size());
        return openTiles.get(index);
    }

    public int[] findNearestWater(int startRow, int startCol, double visionRange) {
        int maxDistance = (int) visionRange;

        int[] closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (int row = startRow - maxDistance; row <= startRow + maxDistance; row++) {
            for (int col = startCol - maxDistance; col <= startCol + maxDistance; col++) {
                if (isValidTile(row, col) && tiles[row][col] == TileType.WATER) {
                    double distance = distance(startRow, startCol, row, col);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closest = new int[] {row, col};
                    }
                }
            }
        }

        return closest;
    }

    public Animal findNearestMate(Animal searcher, double visionRange) {
        Animal closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Animal animal : animals) {
            if (animal != searcher && animal.isAlive() && searcher.canReproduceWith(animal)) {
                double distance = searcher.distanceTo(animal);

                if (distance <= visionRange && distance < closestDistance) {
                    closestDistance = distance;
                    closest = animal;
                }
            }
        }

        return closest;
    }

    public Animal findNearestPrey(Animal hunter, double visionRange) {
        Animal closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Animal animal : animals) {
            if (animal != hunter && animal.isAlive() && hunter.canEatAnimal(animal)) {
                double distance = hunter.distanceTo(animal);

                if (distance <= visionRange && distance < closestDistance) {
                    closestDistance = distance;
                    closest = animal;
                }
            }
        }

        return closest;
    }

    public void addCorpse(int row, int col, double calories) {
        if (!isValidTile(row, col)) {
            return;
        }

        corpseCalories[row][col] += calories;
        updateTilePanel(row, col);
    }

    public double eatCorpseAt(int row, int col, double requestedCalories) {
        if (!isValidTile(row, col)) {
            return 0;
        }

        double eaten = Math.min(requestedCalories, corpseCalories[row][col]);
        corpseCalories[row][col] -= eaten;

        if (corpseCalories[row][col] < 0) {
            corpseCalories[row][col] = 0;
        }

        updateTilePanel(row, col);

        return eaten;
    }

    public void recordSuccessfulHunt(Animal hunter, Animal prey) {
        // Later, this can track hunting statistics.
    }

    public String determineSpeciesName(Animal parentOne, Animal parentTwo, Map<String, Double> childGenes) {
        double distanceFromParentOne = geneDistance(parentOne.getGenes(), childGenes);
        double distanceFromParentTwo = geneDistance(parentTwo.getGenes(), childGenes);

        double averageDistance = (distanceFromParentOne + distanceFromParentTwo) / 2.0;

        if (averageDistance > 0.35) {
            String newName = "Species-" + nextGeneratedSpeciesNumber;
            nextGeneratedSpeciesNumber++;
            return newName;
        }

        if (parentOne.getSpeciesName().equals(parentTwo.getSpeciesName())) {
            return parentOne.getSpeciesName();
        }

        return parentOne.getSpeciesName() + "-" + parentTwo.getSpeciesName() + " Hybrid";
    }

    private double geneDistance(Map<String, Double> firstGenes, Map<String, Double> secondGenes) {
        double totalDifference = 0;
        int comparedGenes = 0;

        for (String geneName : firstGenes.keySet()) {
            if (secondGenes.containsKey(geneName)) {
                double firstValue = firstGenes.get(geneName);
                double secondValue = secondGenes.get(geneName);

                double average = (Math.abs(firstValue) + Math.abs(secondValue)) / 2.0;

                if (average > 0) {
                    totalDifference += Math.abs(firstValue - secondValue) / average;
                    comparedGenes++;
                }
            }
        }

        if (comparedGenes == 0) {
            return 0;
        }

        return totalDifference / comparedGenes;
    }

    private double distance(int rowOne, int colOne, int rowTwo, int colTwo) {
        int rowDifference = rowOne - rowTwo;
        int colDifference = colOne - colTwo;

        return Math.sqrt(rowDifference * rowDifference + colDifference * colDifference);
    }

    private void inspectTile(int row, int col) {
        String message = "";

        message += "Tile: (" + row + ", " + col + ")\n";
        message += "Terrain: " + tiles[row][col] + "\n";
        message += "World Sun Value: " + String.format("%.2f", sunValue) + "\n";

        if (animalGrid[row][col] != null) {
            message += "\nANIMAL\n";
            message += formatInspectionData(animalGrid[row][col].getInspectionData());
        }

        if (plantGrid[row][col] != null) {
            message += "\nPLANT\n";
            message += formatInspectionData(plantGrid[row][col].getInspectionData());
        }

        if (corpseCalories[row][col] > 0) {
            message += "\nCorpse Calories: " + String.format("%.2f", corpseCalories[row][col]) + "\n";
        }

        if (animalGrid[row][col] == null && plantGrid[row][col] == null && corpseCalories[row][col] <= 0) {
            message += "\nNo organism on this tile.\n";
        }

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Tile Inspection", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatInspectionData(Map<String, String> data) {
        String text = "";

        for (String key : data.keySet()) {
            text += key + ": " + data.get(key) + "\n";
        }

        return text;
    }

    public double getSunValue() {
        return sunValue;
    }

    public void setSunValue(double sunValue) {
        if (sunValue < 0) {
            sunValue = 0;
        }

        if (sunValue > 1.5) {
            sunValue = 1.5;
        }

        this.sunValue = sunValue;
    }

    public ToolType getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(ToolType selectedTool) {
        this.selectedTool = selectedTool;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTileSize() {
        return tileSize;
    }
}