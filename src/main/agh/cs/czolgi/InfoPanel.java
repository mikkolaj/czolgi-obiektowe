package agh.cs.czolgi;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class InfoPanel extends StackPane {
    private final BattleEngine battleEngine;
    private final GridPane currentPowerups;
    private final Tank player;
    private final Text stats;

    public InfoPanel(BattleEngine battleEngine, Tank player, int width, int height,
                     int baseX, int baseY) {
        this.battleEngine = battleEngine;
        this.player = player;
        this.setPrefSize(width, height);

        Text movement = new Text();
        movement.setText("""
                Movement:
                W - go up
                S - go down
                A - go left
                D - go right
                """
        );
        movement.setTranslateY(80 - height / 2);
        movement.setTranslateX(-width / 4);

        Text stats = new Text("Current health: 1\n" +
                "Score: 0");
        stats.setTranslateY(140 - height / 2);
        stats.setTranslateX(-width / 4 + 12);
        this.stats = stats;

        Text shooting = new Text();
        shooting.setText("Shot direction:\n" +
                "1-4, 6-9 (Numpad)\n" +
                "\n" +
                "Powerup durations:\n" +
                "-Increased move count: " + Powerups.PLAYER_MOVES.getDuration() + "\n" +
                "-Increased bullet range: " + Powerups.BULLET_RANGE.getDuration() + "\n" +
                "-Piercing bullets: " + Powerups.PIERCING_BULLETS.getDuration() + "\n" +
                "-Bouncing bullets: " + Powerups.BOUNCING_BULLETS.getDuration() + "\n" +
                "-IMMORTALITY: " + Powerups.IMMORTALITY.getDuration() + "\n"
        );
        shooting.setTranslateY(100 - height / 2);
        shooting.setTranslateX(width / 4);

        GridPane currentPowerups = new GridPane();
        this.currentPowerups = currentPowerups;

        this.createRow(currentPowerups, 0, "Powerups:  ", "Key:  ", "Amount:  ", "Remaining:");
        this.createRow(currentPowerups, 1, "Increased move count  ", "[Z]", "0", "-");
        this.createRow(currentPowerups, 2, "Increased bullet range  ", "[X]", "0", "-");
        this.createRow(currentPowerups, 3, "Piercing bullets  ", "[C]", "0", "-");
        this.createRow(currentPowerups, 4, "Bouncing bullets  ", "[V]", "0", "-");
        this.createRow(currentPowerups, 5, "IMMORTALITY  ", "[B]", "0", "-");

        currentPowerups.setTranslateY(190);
        currentPowerups.setTranslateX(68);

        this.getChildren().addAll(movement, stats, shooting, currentPowerups);
        this.setBackground(new Background(new BackgroundFill(Color.LAVENDER, null, null)));

        this.setTranslateX(baseX);
        this.setTranslateY(baseY);
    }

    private void createRow(GridPane gridPane, int rowNumber, String text1, String text2, String text3, String text4) {
        Text textBox1 = new Text(text1);
        Text textBox2 = new Text(text2);
        Text textBox3 = new Text(text3);
        Text textBox4 = new Text(text4);

        gridPane.add(textBox1, 0, rowNumber, 1, 1);
        gridPane.add(textBox2, 1, rowNumber, 1, 1);
        gridPane.add(textBox3, 2, rowNumber, 1, 1);
        gridPane.add(textBox4, 3, rowNumber, 1, 1);
    }

    public void updatePowerups() {
        this.updatePowerup(Powerups.PLAYER_MOVES, 1);
        this.updatePowerup(Powerups.BULLET_RANGE, 2);
        this.updatePowerup(Powerups.PIERCING_BULLETS, 3);
        this.updatePowerup(Powerups.BOUNCING_BULLETS, 4);
        this.updatePowerup(Powerups.IMMORTALITY, 5);
    }

    public void updateStats() {
        this.stats.setText("Current health: " + this.player.getHealth() + "\n" +
                "Score: " + this.battleEngine.getScore());
    }

    private void updatePowerup(Powerups powerup, int row) {
        ((Text) this.currentPowerups.getChildren().get(row * 4 + 2))
                .setText(Integer.toString(this.player.getPowerupAmount(powerup)));
        int duration = this.player.getRemainingPowerupDuration(powerup);
        if (duration > 0) {
            ((Text) this.currentPowerups.getChildren().get(row * 4 + 3))
                    .setText(duration + " rounds");
        } else {
            ((Text) this.currentPowerups.getChildren().get(row * 4 + 3))
                    .setText("-");
        }
    }

}
