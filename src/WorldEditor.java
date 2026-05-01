public class WorldEditor {
    private ToolType selectedTool;

    public WorldEditor() {
        this.selectedTool = ToolType.GRASS;
    }

    public void useSelectedTool(WorldModel model, int row, int col) {
        switch (selectedTool) {
            case GRASS:
                model.setTile(row, col, TileType.GRASS);
                break;
            case WATER:
                model.setTile(row, col, TileType.WATER);
                break;
            case ROCK:
                model.setTile(row, col, TileType.ROCK);
                break;
            case PLANT:
                model.addDefaultPlant(row, col);
                break;
            case ERASE_PLANT:
                model.removePlantAt(row, col);
                break;
            default:
                break;
        }
    }

    public ToolType getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(ToolType selectedTool) {
        if (selectedTool != null) {
            this.selectedTool = selectedTool;
        }
    }
}