import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class ToolbarPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int SIMULATION_DELAY_MS = 20;
    private static final double SUN_STEP = 0.1;

    private final WorldModel model;
    private final Timer simulationTimer;

    private boolean simulationRunning;

    public ToolbarPanel(WorldModel model) {
        this.model = model;
        this.simulationTimer = new Timer(SIMULATION_DELAY_MS, e -> model.updateWorld());
        this.simulationRunning = false;

        createButtons();
    }

    private void createButtons() {
        ButtonGroup toolGroup = new ButtonGroup();

        addToolButton(toolGroup, "Grass", ToolType.GRASS, true);
        addToolButton(toolGroup, "Water", ToolType.WATER, false);
        addToolButton(toolGroup, "Rock", ToolType.ROCK, false);
        addToolButton(toolGroup, "Plant", ToolType.PLANT, false);
        addToolButton(toolGroup, "Herbivore", ToolType.HERBIVORE, false);
        addToolButton(toolGroup, "Predator", ToolType.PREDATOR, false);
        addToolButton(toolGroup, "Erase Plant", ToolType.ERASE_PLANT, false);
        addToolButton(toolGroup, "Erase Animal", ToolType.ERASE_ANIMAL, false);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(e -> model.updateWorld());

        JButton startPauseButton = new JButton("Start");
        startPauseButton.addActionListener(e -> toggleSimulation(startPauseButton));

        JButton lessSunButton = new JButton("Less Sun");
        lessSunButton.addActionListener(e -> model.setSunValue(model.getSunValue() - SUN_STEP));

        JButton moreSunButton = new JButton("More Sun");
        moreSunButton.addActionListener(e -> model.setSunValue(model.getSunValue() + SUN_STEP));

        add(stepButton);
        add(startPauseButton);
        add(lessSunButton);
        add(moreSunButton);
    }

    private void addToolButton(ButtonGroup group, String label, ToolType tool, boolean selected) {
        JToggleButton button = new JToggleButton(label);

        button.setSelected(selected);
        button.addActionListener(e -> model.setSelectedTool(tool));

        group.add(button);
        add(button);
    }

    private void toggleSimulation(JButton startPauseButton) {
        simulationRunning = !simulationRunning;

        if (simulationRunning) {
            simulationTimer.start();
            startPauseButton.setText("Pause");
        } else {
            simulationTimer.stop();
            startPauseButton.setText("Start");
        }
    }
}
