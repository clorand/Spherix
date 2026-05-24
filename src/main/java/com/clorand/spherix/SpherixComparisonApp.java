package com.clorand.spherix;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.AmbientLight;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.clorand.spherix.view.ConfigurationComparisonView;


public class SpherixComparisonApp extends Application {

	@Override
	public void start(Stage primaryStage) {
	    ConfigurationComparisonView view = new ConfigurationComparisonView(1056L, 1058L);

	    // Use a single Scene with depth buffer and anti-aliasing
	    Scene scene = new Scene(view, 1024, 768, true, SceneAntialiasing.BALANCED);
	    scene.setFill(Color.LIGHTGRAY);

	    // Set up the camera
	    PerspectiveCamera camera = new PerspectiveCamera(true);
	    camera.setNearClip(0.1);
	    camera.setFarClip(1000);  // Reduce far clip to match your scene scale
	    camera.setTranslateZ(-10);  // Move camera closer (adjust as needed)
	    scene.setCamera(camera);

	    // Add ambient light
	    AmbientLight ambientLight = new AmbientLight(Color.WHITE);
	    view.getChildren().add(ambientLight);

	    // Add a point light
	    PointLight pointLight = new PointLight(Color.WHITE);
	    pointLight.setTranslateX(0);
	    pointLight.setTranslateY(0);
	    pointLight.setTranslateZ(-20);
	    view.getChildren().add(pointLight);

	    primaryStage.setTitle("Spherix: Comparing dbkey=642 vs. dbkey=643");
	    primaryStage.setScene(scene);
	    primaryStage.show();

	    // Request focus on the view after the stage is shown
	    Platform.runLater(() -> view.requestFocus());
	}

    public static void main(String[] args) {
        launch(args);
    }
}