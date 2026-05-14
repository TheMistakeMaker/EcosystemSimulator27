import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChangeNotifier {
    private final List<Runnable> changeListeners;
    private final List<TileChangeListener> tileChangeListeners;
    private final Set<String> changedTileKeys;

    private boolean batching;
    private boolean fullChangePending;

    public ChangeNotifier() {
        this.changeListeners = new ArrayList<>();
        this.tileChangeListeners = new ArrayList<>();
        this.changedTileKeys = new LinkedHashSet<>();
        this.batching = false;
        this.fullChangePending = false;
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void addTileChangeListener(TileChangeListener listener) {
        if (listener != null) {
            tileChangeListeners.add(listener);
        }
    }

    public void beginBatch() {
        batching = true;
    }

    public void endBatch() {
        batching = false;

        if (fullChangePending || !changedTileKeys.isEmpty()) {
            flushChanges();
        }
    }

    public void markFullChange() {
        fullChangePending = true;

        if (!batching) {
            flushChanges();
        }
    }

    public void markTileChanged(int row, int col) {
        changedTileKeys.add(createTileKey(row, col));

        if (!batching) {
            flushChanges();
        }
    }

    public void markTilesChanged(int firstRow, int firstCol, int secondRow, int secondCol) {
        markTileChanged(firstRow, firstCol);
        markTileChanged(secondRow, secondCol);
    }

    private void flushChanges() {
        List<String> tileKeys = new ArrayList<>(changedTileKeys);
        changedTileKeys.clear();
        fullChangePending = false;

        for (Runnable listener : changeListeners) {
            listener.run();
        }

        for (String key : tileKeys) {
            int separatorIndex = key.indexOf(',');
            int row = Integer.parseInt(key.substring(0, separatorIndex));
            int col = Integer.parseInt(key.substring(separatorIndex + 1));

            for (TileChangeListener listener : tileChangeListeners) {
                listener.tileChanged(row, col);
            }
        }
    }

    private String createTileKey(int row, int col) {
        return row + "," + col;
    }
}