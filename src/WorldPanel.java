import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WorldPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Color ANIMAL_COLOR = Color.WHITE;
    private static final Color HERBIVORE_COLOR = new Color(230, 230, 230);
    private static final Color PREDATOR_COLOR = new Color(180, 30, 30);
    private static final Color PLANT_COLOR = new Color(0, 90, 0);
    private static final Color CORPSE_COLOR = new Color(120, 70, 35);
    private static final Color GRASS_COLOR = Color.GREEN;
    private static final Color WATER_COLOR = Color.BLUE;
    private static final Color ROCK_COLOR = Color.GRAY;

    private final WorldModel model;
    private final int tileSize;
    private final JPanel[][] tilePanels;

    public WorldPanel(WorldModel model, int tileSize) {
        this.model = model;
        this.tileSize = tileSize;
        this.tilePanels = new JPanel[model.getRows()][model.getCols()];

        setLayout(new GridLayout(model.getRows(), model.getCols()));

        createTilePanels();
        refreshAllTiles();

        model.addChangeListener(this::repaint);
        model.addTileChangeListener(this::refreshTile);
    }

    private void createTilePanels() {
        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                JPanel tilePanel = createTilePanel(row, col);

                tilePanels[row][col] = tilePanel;
                add(tilePanel);
            }
        }
    }

    private JPanel createTilePanel(int row, int col) {
        JPanel tilePanel = new JPanel();

        tilePanel.setPreferredSize(new Dimension(tileSize, tileSize));
        tilePanel.setOpaque(true);

        tilePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    InspectorDialog.show(WorldPanel.this, model, row, col);
                } else {
                    model.useSelectedTool(row, col);
                }
            }
        });

        return tilePanel;
    }

    public void refreshAllTiles() {
        for (int row = 0; row < model.getRows(); row++) {
            for (int col = 0; col < model.getCols(); col++) {
                refreshTile(row, col);
            }
        }
    }

    private void refreshTile(int row, int col) {
        if (row < 0 || row >= tilePanels.length || col < 0 || col >= tilePanels[row].length) {
            return;
        }

        tilePanels[row][col].setBackground(getTileColor(row, col));
    }

    private Color getTileColor(int row, int col) {
        Tile tile = model.getTileObject(row, col);

        if (tile == null) {
            return GRASS_COLOR;
        }

        if (tile.hasLivingAnimal()) {
            Animal animal = tile.getAnimal();

            if (animal instanceof Predator) {
                return PREDATOR_COLOR;
            }

            if (animal instanceof Herbivore) {
                return HERBIVORE_COLOR;
            }

            return ANIMAL_COLOR;
        }

        if (tile.hasLivingPlant()) {
            return PLANT_COLOR;
        }

        if (tile.hasCorpse()) {
            return CORPSE_COLOR;
        }

        switch (tile.getTerrain()) {
            case GRASS:
                return GRASS_COLOR;
            case WATER:
                return WATER_COLOR;
            case ROCK:
                return ROCK_COLOR;
            default:
                return GRASS_COLOR;
        }
    }
}