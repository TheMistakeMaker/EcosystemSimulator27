import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        World world = new World(25, 35, 24);

        JFrame window = new JFrame("Ecosystem Simulation");
        window.add(world);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}