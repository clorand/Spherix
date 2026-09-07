package com.clorand.spherix.graph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Map;

public class GraphVisualizer extends Application {
    private static Graph graph;
    private static Map<Integer, double[]> coordinates;

    public GraphVisualizer() {
        // Empty constructor
    }

    public static void visualize(Graph graph) {
        GraphVisualizer.graph = graph;
        GraphVisualizer.coordinates = null;
        launch();
    }

    public static void visualize(Graph graph, Map<Integer, double[]> coordinates) {
        GraphVisualizer.graph = graph;
        GraphVisualizer.coordinates = coordinates;
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setPrefSize(1000, 800); // Larger window size

        // Find the min and max coordinates to center the graph
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Vertex vertex : graph.getVertices()) {
            double x, y;
            if (coordinates != null) {
                double[] coord = coordinates.get(vertex.getId());
                x = coord[0];
                y = coord[1];
            } else {
                x = vertex.getX();
                y = vertex.getY();
            }

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Calculate the range of coordinates
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        // Scale factor to make the graph bigger
        double scale = 50.0; // Adjust this value to control the size of the graph

        // Centering offsets
        double offsetX = 500 - (minX + maxX) * scale / 2;
        double offsetY = 400 - (minY + maxY) * scale / 2;

        // Draw edges
        for (Edge edge : graph.getEdges()) {
            Vertex source = edge.getSource();
            Vertex target = edge.getTarget();

            double sourceX, sourceY, targetX, targetY;

            if (coordinates != null) {
                double[] sourceCoord = coordinates.get(source.getId());
                double[] targetCoord = coordinates.get(target.getId());
                sourceX = sourceCoord[0] * scale + offsetX;
                sourceY = sourceCoord[1] * scale + offsetY;
                targetX = targetCoord[0] * scale + offsetX;
                targetY = targetCoord[1] * scale + offsetY;
            } else {
                sourceX = source.getX() * scale + offsetX;
                sourceY = source.getY() * scale + offsetY;
                targetX = target.getX() * scale + offsetX;
                targetY = target.getY() * scale + offsetY;
            }

            Line line = new Line(sourceX, sourceY, targetX, targetY);
            line.setStroke(Color.BLACK);
            root.getChildren().add(line);
        }

        // Draw vertices
        for (Vertex vertex : graph.getVertices()) {
            double x, y;
            if (coordinates != null) {
                double[] coord = coordinates.get(vertex.getId());
                x = coord[0] * scale + offsetX;
                y = coord[1] * scale + offsetY;
            } else {
                x = vertex.getX() * scale + offsetX;
                y = vertex.getY() * scale + offsetY;
            }

            Circle circle = new Circle(x, y, 15, Color.BLUE); // Larger vertex circles
            root.getChildren().add(circle);

            Text text = new Text(x - 5, y - 20, String.valueOf(vertex.getId())); // Adjusted text position
            text.setStyle("-fx-font-size: 14px;"); // Larger font size
            root.getChildren().add(text);
        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("Graph Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}