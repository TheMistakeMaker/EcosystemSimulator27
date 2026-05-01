import javax.swing.JPanel;

import java.awt.BorderLayout;

public class World extends JPanel {
    private static final long serialVersionUID = 1L;

    private final WorldModel model;
    private final StatsPanel statsPanel;
    private final WorldPanel worldPanel;
    private final ToolbarPanel toolbarPanel;

    public World(int rows, int cols, int tileSize) {
        this.model = new WorldModel(rows, cols);
        this.statsPanel = new StatsPanel(model);
        this.worldPanel = new WorldPanel(model, tileSize);
        this.toolbarPanel = new ToolbarPanel(model);

        setLayout(new BorderLayout());

        add(statsPanel, BorderLayout.NORTH);
        add(worldPanel, BorderLayout.CENTER);
        add(toolbarPanel, BorderLayout.SOUTH);

        model.addChangeListener(() -> statsPanel.updateStats(model));
    }
}