package agh.cs.czolgi;

import javafx.scene.layout.StackPane;

public class MapVisualizer extends StackPane implements IFieldChangeObserver {
    private final int tileSize;
    private final int mapWidth;
    private final int mapHeight;
    private final MapTile[][] grid;
    private final BattleField map;

    private void createContent() {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                MapTile tile = new MapTile(x, this.mapHeight - 1 - y, this.tileSize);

                this.grid[y][x] = tile;
                this.getChildren().add(tile);
            }
        }
    }

    public MapVisualizer(BattleField map, int width, int height, int tileSize) {
        this.grid = new MapTile[height][width];
        this.mapHeight = height;
        this.mapWidth = width;
        this.map = map;
        this.map.addObserver(this);
        this.tileSize = tileSize;
        this.createContent();
    }

    public void fieldChanged(Vector2d position) {
        grid[position.y][position.x].setCorrectBackground(this.map.objectAt(position));
    }
}
