import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;

public class InspectorDialog {
    private static final int INSPECTION_WIDTH = 800;
    private static final int INSPECTION_HEIGHT = 300;

    private InspectorDialog() {
    }

    public static void show(JPanel parent, WorldModel model, int row, int col) {
        Tile tile = model.getTileObject(row, col);

        if (tile == null) {
            return;
        }

        JTextArea textArea = new JTextArea(createInspectionText(model, tile, row, col));
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(INSPECTION_WIDTH, INSPECTION_HEIGHT));

        JOptionPane.showMessageDialog(parent, scrollPane, "Tile Inspection", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String createInspectionText(WorldModel model, Tile tile, int row, int col) {
        StringBuilder message = new StringBuilder();

        message.append("Tile: (").append(row).append(", ").append(col).append(")\n");
        message.append("Terrain: ").append(tile.getTerrain()).append("\n");
        message.append("World Sun Value: ").append(format(model.getSunValue())).append("\n");
        message.append("Tick: ").append(model.getTickCount()).append("\n");

        if (tile.getAnimal() != null) {
            message.append("\nANIMAL\n");
            message.append(formatInspectionData(tile.getAnimal().getInspectionData()));
        }

        if (tile.getPlant() != null) {
            message.append("\nPLANT\n");
            message.append(formatInspectionData(tile.getPlant().getInspectionData()));
        }

        if (tile.hasCorpse()) {
            message.append("\nCorpse Calories: ")
                    .append(format(tile.getCorpseCalories()))
                    .append("\n");
        }

        if (tile.getAnimal() == null && tile.getPlant() == null && !tile.hasCorpse()) {
            message.append("\nNo organism on this tile\n");
        }

        return message.toString();
    }

    private static String formatInspectionData(Map<String, String> data) {
        StringBuilder text = new StringBuilder();

        for (String key : data.keySet()) {
            text.append(key).append(": ").append(data.get(key)).append("\n");
        }

        return text.toString();
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }
}