package com.clorand.spherix;

import javafx.application.Application;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.clorand.spherix.view.ConfigurationComparisonView;

public class SpherixComparisonApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ConfigurationComparisonView view = new ConfigurationComparisonView(642L, 643L);

        Scene scene = new Scene(view, 1024, 768, true);
        scene.setFill(Color.LIGHTGRAY);

        // Forward key events from the Scene to the view
        scene.setOnKeyPressed(view::handleKeyPress);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000);
        camera.setTranslateZ(-10);
        scene.setCamera(camera);

        primaryStage.setTitle("Spherix: Comparing dbkey=642 vs. dbkey=643");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}