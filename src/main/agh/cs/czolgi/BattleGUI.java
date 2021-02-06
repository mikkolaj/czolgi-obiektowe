package agh.cs.czolgi;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javafx.scene.input.KeyEvent;

public class BattleGUI {
    private final InfoPanel infoPanel;
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int STATS_WIDTH = 400;
    private final Tank player;
    private final BattleEngine battleEngine;

    public BattleGUI(Stage primaryStage, BattleEngine battleEngine, Tank player,
                     BattleField map, int width, int height) {
        Pane root = new Pane();
        this.player = player;
        this.battleEngine = battleEngine;

        int tileSize = this.calculateTileSize(width, height);

        this.infoPanel = new InfoPanel(battleEngine, player, STATS_WIDTH,
                Math.max(height * tileSize + 1, 370), width * tileSize + 1, 0);
        MapVisualizer mapVisualizer = new MapVisualizer(map, width, height, tileSize);

        root.getChildren().addAll(mapVisualizer, this.infoPanel);
        primaryStage.setScene(new Scene(root, width * tileSize + STATS_WIDTH, Math.max(height * tileSize, 370)));
        this.addKeyListener(primaryStage, battleEngine);

        primaryStage.setTitle("Simulation");
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void addKeyListener(Stage primaryStage, BattleEngine battleEngine) {
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
            switch (key.getCode()) {
                case W -> battleEngine.movePlayer(MapDirection.NORTH);
                case S -> battleEngine.movePlayer(MapDirection.SOUTH);
                case A -> battleEngine.movePlayer(MapDirection.WEST);
                case D -> battleEngine.movePlayer(MapDirection.EAST);
                case NUMPAD1 -> battleEngine.shoot(MapDirection.SOUTH_WEST);
                case NUMPAD2 -> battleEngine.shoot(MapDirection.SOUTH);
                case NUMPAD3 -> battleEngine.shoot(MapDirection.SOUTH_EAST);
                case NUMPAD4 -> battleEngine.shoot(MapDirection.WEST);
                case NUMPAD6 -> battleEngine.shoot(MapDirection.EAST);
                case NUMPAD7 -> battleEngine.shoot(MapDirection.NORTH_WEST);
                case NUMPAD8 -> battleEngine.shoot(MapDirection.NORTH);
                case NUMPAD9 -> battleEngine.shoot(MapDirection.NORTH_EAST);
                case Z -> {
                    player.usePowerup(Powerups.PLAYER_MOVES);
                    this.updatePowerups();
                }
                case X -> {
                    player.usePowerup(Powerups.BULLET_RANGE);
                    this.updatePowerups();
                }
                case C -> {
                    player.usePowerup(Powerups.PIERCING_BULLETS);
                    this.updatePowerups();
                }
                case V -> {
                    player.usePowerup(Powerups.BOUNCING_BULLETS);
                    this.updatePowerups();
                }
                case B -> {
                    player.usePowerup(Powerups.IMMORTALITY);
                    this.updatePowerups();
                }
            }
        });
    }

    private int calculateTileSize(int width, int height) {
        return Math.min((WINDOW_WIDTH - STATS_WIDTH) / width, (WINDOW_HEIGHT) / height);
    }

    public void showGameOverAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText(null);
        alert.setContentText("You died.\nYour score is: " + this.battleEngine.getScore());

        alert.showAndWait();
    }

    public void updatePowerups() {
        this.infoPanel.updatePowerups();
    }

    public void updateStats() {
        this.infoPanel.updateStats();
    }
}
