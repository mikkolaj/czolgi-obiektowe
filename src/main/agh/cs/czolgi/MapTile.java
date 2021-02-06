package agh.cs.czolgi;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MapTile extends StackPane {
    private final Rectangle border;

    public MapTile(int x, int y, int tileSize) {
        this.border = new Rectangle(tileSize, tileSize);

        this.border.setStroke(Color.LIGHTGRAY);
        this.border.setFill(Color.LAVENDER);
        this.getChildren().add(border);
        this.setBackground(new Background(new BackgroundFill(Color.LAVENDER, null, null)));

        setTranslateX(x * tileSize);
        setTranslateY(y * tileSize);
    }

    public void setCorrectBackground(Object mapObject) {
        if (mapObject == null) {
            this.border.setFill(Color.LAVENDER);
        } else if (mapObject instanceof Tank) {
            Tank tank = (Tank) mapObject;
            if (tank.isPlayer()) {
                this.border.setFill(Color.GREEN);
            } else {
                this.border.setFill(Color.RED);
            }
        } else if (mapObject instanceof Bullet) {
            this.border.setFill(Color.ORANGE);
        } else if (mapObject instanceof Powerups) {
            Powerups powerup = (Powerups) mapObject;
            switch (powerup) {
                case PLAYER_MOVES -> this.border.setFill(Color.LIGHTGREEN);
                case BULLET_RANGE -> this.border.setFill(Color.LIGHTBLUE);
                case PIERCING_BULLETS -> this.border.setFill(Color.LIGHTCORAL);
                case BOUNCING_BULLETS -> this.border.setFill(Color.LIGHTPINK);
                case IMMORTALITY -> this.border.setFill(Color.LIGHTGOLDENRODYELLOW);
                case HEALTH_BOOST -> this.border.setFill(Color.INDIANRED);
            }
        } else {
            this.border.setFill(Color.GREY);
        }
    }
}

