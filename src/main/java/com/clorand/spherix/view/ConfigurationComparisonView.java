package com.clorand.spherix.view;

import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import main.Vec3;
import com.clorand.spherix.model.Configuration;
import java.util.List;

public class ConfigurationComparisonView extends Pane {
    private final Configuration referenceConfig;
    private final Configuration comparisonConfig;
    private final Pane referencePane;
    private final Pane comparisonPane;
    private final Rotate rotationX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotationY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotationZ = new Rotate(0, Rotate.Z_AXIS);

    public ConfigurationComparisonView(Long referenceDbKey, Long comparisonDbKey) {
        this.referenceConfig = new Configuration(referenceDbKey);
        this.comparisonConfig = new Configuration(comparisonDbKey);

        this.referencePane = createConfigurationPane(referenceConfig.getPoints(), Color.BLUE);
        this.comparisonPane = createConfigurationPane(comparisonConfig.getPoints(), Color.RED);
        this.comparisonPane.getTransforms().addAll(rotationX, rotationY, rotationZ);

        this.getChildren().addAll(referencePane, comparisonPane);

        // Ensure the Pane can receive focus
        this.setFocusTraversable(true);
        this.setOnKeyPressed(this::handleKeyPress);
    }


    private Pane createConfigurationPane(List<Vec3> points, Color color) {
        Pane pane = new Pane();
        PhongMaterial material = new PhongMaterial(color);
        for (Vec3 point : points) {
            Sphere sphere = new Sphere(0.05); // Radius = 0.05
            sphere.setMaterial(material);
            sphere.setTranslateX(point.x);
            sphere.setTranslateY(point.y);
            sphere.setTranslateZ(point.z);
            pane.getChildren().add(sphere);
        }
        return pane;
    }

    public void handleKeyPress(KeyEvent event) {
        double rotationAngle = 5.0;
        switch (event.getCode()) {
            case UP ->    rotationX.setAngle(rotationX.getAngle() - rotationAngle);
            case DOWN ->  rotationX.setAngle(rotationX.getAngle() + rotationAngle);
            case LEFT ->  rotationY.setAngle(rotationY.getAngle() - rotationAngle);
            case RIGHT -> rotationY.setAngle(rotationY.getAngle() + rotationAngle);
            case Q ->     rotationZ.setAngle(rotationZ.getAngle() - rotationAngle);
            case W ->     rotationZ.setAngle(rotationZ.getAngle() + rotationAngle);
            case R ->     resetRotation();
            case I ->     checkIsometry();
        }
    }

    private void resetRotation() {
        rotationX.setAngle(0);
        rotationY.setAngle(0);
        rotationZ.setAngle(0);
    }

    private void checkIsometry() {
        boolean isIsometric = referenceConfig.isIsometricTo(comparisonConfig, 0.0001);
        System.out.println("Are configurations isometric? " + isIsometric);
    }
}