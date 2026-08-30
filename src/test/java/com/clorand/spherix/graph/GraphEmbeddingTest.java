package com.clorand.spherix.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.awt.geom.Point2D;

public class GraphEmbeddingTest {

    @Test
    public void testFaceDetection() {
        // Create the graph
    	String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Graph graph = Graph.fromString(graphStr);
        // Define vertex coordinates
        Map<Integer, Point2D.Double> vertexCoordinates = new HashMap<>();
        vertexCoordinates.put(0, new Point2D.Double(5.0, 11.0));
        vertexCoordinates.put(1, new Point2D.Double(0.0, 6.0));
        vertexCoordinates.put(2, new Point2D.Double(4.0, 7.0));
        vertexCoordinates.put(3, new Point2D.Double(7.0, 4.0));
        vertexCoordinates.put(4, new Point2D.Double(11.0, 5.0));
        vertexCoordinates.put(5, new Point2D.Double(6.0, 0.0));

        // Create the graph embedding
        GraphEmbedding embedding = new GraphEmbedding(graph, vertexCoordinates);

        // Find the faces
        List<List<Integer>> detectedFaces = embedding.findFaces();

        // Print detected faces for debugging
        System.out.println("Detected Faces:");
        for (List<Integer> face : detectedFaces) {
            System.out.println(face);
        }

        // Expected faces
        List<List<Integer>> expectedFaces = new ArrayList<>();
        expectedFaces.add(Arrays.asList(0, 1, 2));               // Face 1
        expectedFaces.add(Arrays.asList(0, 2, 3, 4));            // Face 2
        expectedFaces.add(Arrays.asList(1, 2, 3, 5));            // Face 3
        expectedFaces.add(Arrays.asList(3, 5, 4));               // Face 4
        expectedFaces.add(Arrays.asList(0, 1, 5, 4));      // Outer face

        // Verify the number of faces
        assertEquals(expectedFaces.size(), detectedFaces.size(),
            "The number of detected faces should match the expected number of faces.");

        // Verify each detected face matches an expected face
        for (List<Integer> expectedFace : expectedFaces) {
            boolean found = false;
            for (List<Integer> detectedFace : detectedFaces) {
                if (detectedFace.size() == expectedFace.size() &&
                    detectedFace.containsAll(expectedFace)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "The detected faces should include the expected face: " + expectedFace);
        }
    }
}