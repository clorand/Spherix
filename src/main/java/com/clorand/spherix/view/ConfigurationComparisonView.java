package com.clorand.spherix.view;

import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import main.Vec3;
import com.clorand.spherix.model.Configuration;
import com.clorand.spherix.model.DatabaseLoader;
import com.clorand.spherix.model.Edge;
import com.clorand.spherix.model.LineMesh;

import java.util.*;

public class ConfigurationComparisonView extends Group {
    private final Configuration referenceConfig;
    private final Configuration comparisonConfig;
    private final Group referenceGroup;
    private final Group comparisonGroup;
    private final Rotate rotationX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotationY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotationZ = new Rotate(0, Rotate.Z_AXIS);

    public ConfigurationComparisonView(Long referenceDbKey, Long comparisonDbKey) {
        this.referenceConfig = DatabaseLoader.loadConfiguration(referenceDbKey);
        this.comparisonConfig = DatabaseLoader.loadConfiguration(comparisonDbKey);

        // Create groups for reference and comparison configurations
        this.referenceGroup = createConfigurationGroup(
            referenceConfig.getPoints(),
            Color.BLUE,  // Base color for reference vertices
            referenceConfig.getDegrees(),
            referenceConfig.getContactGraph(),
            true  // Reference configuration (blue edges)
        );
        this.comparisonGroup = createConfigurationGroup(
            comparisonConfig.getPoints(),
            Color.RED,   // Base color for comparison vertices
            comparisonConfig.getDegrees(),
            comparisonConfig.getContactGraph(),
            false // Comparison configuration (red edges)
        );

        // Apply rotations to the comparison group
        this.comparisonGroup.getTransforms().addAll(rotationX, rotationY, rotationZ);

        // Add groups to the view
        this.getChildren().addAll(referenceGroup, comparisonGroup);
        this.setFocusTraversable(true);
        this.setOnKeyPressed(this::handleKeyPress);
    }

    private Group createConfigurationGroup(
        List<Vec3> points,
        Color baseColor,
        Map<Integer, Integer> degrees,
        List<Edge> contactGraph,
        boolean isReference
    ) {
        Group group = new Group();

        // Add spheres for vertices (colored by degree)
        for (int i = 0; i < points.size(); i++) {
            Vec3 point = points.get(i);

            // Main sphere for the vertex (colored by degree)
            Sphere sphere = new Sphere(0.05);
            Color vertexColor = getDegreeColor(degrees.getOrDefault(i, 0));
            PhongMaterial material = new PhongMaterial(vertexColor);
            sphere.setMaterial(material);
            sphere.setTranslateX(point.x);
            sphere.setTranslateY(point.y);
            sphere.setTranslateZ(point.z);

            group.getChildren().add(sphere);
        }

        // Add LineMesh for edges in the contact graph
        Color edgeColor = isReference ? Color.BLUE : Color.RED;
        for (Edge edge : contactGraph) {
            Vec3 p1 = points.get(edge.getVertex1());
            Vec3 p2 = points.get(edge.getVertex2());
            LineMesh line = new LineMesh(p1, p2, 0.01, edgeColor);
            group.getChildren().add(line);
        }

        return group;
    }

    /**
     * Returns a color based on the degree (1 to 5).
     * @param degree The degree of the vertex (1 to 5).
     * @return The color for the degree.
     */
    private Color getDegreeColor(int degree) {
        switch (degree) {
            case 1: return Color.BLUE;
            case 2: return Color.CYAN;
            case 3: return Color.YELLOW;
            case 4: return Color.GREEN;
            case 5: return Color.RED;
            default: return Color.WHITE; // Fallback for degree 0 or >5
        }
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