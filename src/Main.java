import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    private static final int WORLD_ROWS = 25;
    private static final int WORLD_COLS = 35;
    private static final int TILE_SIZE = 24;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createWindow);
    }

    private static void createWindow() {
        JFrame window = new JFrame("Ecosystem Simulation");
        World world = new World(WORLD_ROWS, WORLD_COLS, TILE_SIZE);

        window.add(world);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}