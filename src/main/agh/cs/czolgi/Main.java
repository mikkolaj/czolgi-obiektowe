package agh.cs.czolgi;

import javafx.application.Application;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        JSONReader jsonReader = new JSONReader("./data/parameters.json");
        JSONObject jsonObject = jsonReader.read();
        runSimulation(primaryStage, jsonObject);
    }

    private void runSimulation(Stage primaryStage, JSONObject jsonObject) {
        BattleField map = new BattleField(
                Math.toIntExact((long) jsonObject.get("width")),
                Math.toIntExact((long) jsonObject.get("height")),
                Math.toIntExact((long) jsonObject.get("minEnemyDistance"))
        );

        BattleEngine engine = new BattleEngine(primaryStage, map,
                (boolean) jsonObject.get("placeObstacles"),
                (boolean) jsonObject.get("placePowerups"),
                (boolean) jsonObject.get("enemiesSpawnWithPowerups"),
                (boolean) jsonObject.get("enemiesCollectPowerups")
        );

        engine.run();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
