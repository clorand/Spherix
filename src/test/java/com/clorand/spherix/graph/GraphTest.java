package com.clorand.spherix.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Collectors;

public class GraphTest {

    // Helper method to create the sample graph
    private Graph createSampleGraph() {
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Map<Integer, double[]> coordinates = new HashMap<>();
        coordinates.put(0, new double[]{5.0, 11.0});
        coordinates.put(1, new double[]{0.0, 6.0});
        coordinates.put(2, new double[]{4.0, 7.0});
        coordinates.put(3, new double[]{7.0, 4.0});
        coordinates.put(4, new double[]{11.0, 5.0});
        coordinates.put(5, new double[]{6.0, 0.0});
        return Graph.fromString(graphStr, coordinates);
    }

    @Test
    public void testFaceDetection() {
        Graph graph = createSampleGraph();

        // Find the faces
        List<List<Integer>> detectedFaces = graph.findFaces();

        // Print detected faces for debugging
        System.out.println("Detected Faces:");
        for (List<Integer> face : detectedFaces) {
            System.out.println(face);
        }

        // Verify the number of faces
        assertEquals(5, detectedFaces.size(), "The number of detected faces should be 5.");

        // Verify the outer face (largest face)
        boolean outerFaceFound = false;
        for (List<Integer> face : detectedFaces) {
            if (face.size() == 4) { // The outer face should have 4 vertices for this graph
                outerFaceFound = true;
                break;
            }
        }
        assertTrue(outerFaceFound, "There should be an outer face with 6 vertices.");
    }

    @Test
    public void testLeftmostVertex() {
        Graph graph = createSampleGraph();
        Vertex leftmost = graph.getLeftmostVertex();
        assertNotNull(leftmost, "The graph should have a leftmost vertex.");
        assertEquals(1, leftmost.getId(), "The leftmost vertex should be vertex 1.");
        assertEquals(0.0, leftmost.getX(), 0.001, "The x-coordinate of the leftmost vertex should be 0.0.");
    }

    @Test
    public void testRightmostVertex() {
        Graph graph = createSampleGraph();
        Vertex rightmost = graph.getRightmostVertex();
        assertNotNull(rightmost, "The graph should have a rightmost vertex.");
        assertEquals(4, rightmost.getId(), "The rightmost vertex should be vertex 4.");
        assertEquals(11.0, rightmost.getX(), 0.001, "The x-coordinate of the rightmost vertex should be 11.0.");
    }

    @Test
    public void testNorthmostVertex() {
        Graph graph = createSampleGraph();
        Vertex northmost = graph.getNorthmostVertex();
        assertNotNull(northmost, "The graph should have a northmost vertex.");
        assertEquals(0, northmost.getId(), "The northmost vertex should be vertex 0.");
        assertEquals(11.0, northmost.getY(), 0.001, "The y-coordinate of the northmost vertex should be 11.0.");
    }

    @Test
    public void testSouthmostVertex() {
        Graph graph = createSampleGraph();
        Vertex southmost = graph.getSouthmostVertex();
        assertNotNull(southmost, "The graph should have a southmost vertex.");
        assertEquals(5, southmost.getId(), "The southmost vertex should be vertex 5.");
        assertEquals(0.0, southmost.getY(), 0.001, "The y-coordinate of the southmost vertex should be 0.0.");
    }

    @Test
    void testFromStringConstructor() {
        // Input string representing the graph
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";

        // Create the graph using the fromString method
        Graph graph = Graph.fromString(graphStr);

        // Expected vertices: 0, 1, 2, 3, 4, 5
        List<Vertex> vertices = graph.getVertices();
        assertEquals(6, vertices.size(), "Graph should have 6 vertices.");

        // Expected edges: 9 edges as per the input string
        List<Edge> edges = graph.getEdges();
        assertEquals(9, edges.size(), "Graph should have 9 edges.");

        // Verify specific edges exist
        boolean hasEdge0To1 = edges.stream()
            .anyMatch(edge -> edge.getSource().getId() == 0 && edge.getTarget().getId() == 1);
        assertTrue(hasEdge0To1, "Graph should contain edge 0 -> 1.");

        boolean hasEdge4To5 = edges.stream()
            .anyMatch(edge -> edge.getSource().getId() == 4 && edge.getTarget().getId() == 5);
        assertTrue(hasEdge4To5, "Graph should contain edge 4 -> 5.");

        // Print the graph for visual verification
        System.out.println("Graph from string constructor:");
        System.out.println("Vertices: " + vertices.stream().map(Vertex::getId).collect(Collectors.toList()));
        System.out.println("Edges: " + edges.size());
    }

    @Test
    void testAdjacencyMatrix() {
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Graph graph = Graph.fromString(graphStr);

        int[][] adjacencyMatrix = graph.getAdjacencyMatrix();

        // Print the adjacency matrix for debugging
        System.out.println("\nAdjacency Matrix:");
        Graph.printMatrix(adjacencyMatrix);

        // Expected adjacency matrix for the given graph
        int[][] expectedAdjacencyMatrix = {
            {0, 1, 1, 0, 1, 0}, // Vertex 0
            {1, 0, 1, 0, 0, 1}, // Vertex 1
            {1, 1, 0, 1, 0, 0}, // Vertex 2
            {0, 0, 1, 0, 1, 1}, // Vertex 3
            {1, 0, 0, 1, 0, 1}, // Vertex 4
            {0, 1, 0, 1, 1, 0}  // Vertex 5
        };

        // Check if the adjacency matrix matches the expected matrix
        assertTrue(Arrays.deepEquals(adjacencyMatrix, expectedAdjacencyMatrix),
            "Adjacency matrix does not match expected values.");
    }

    @Test
    void testLaplacianMatrix() {
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Graph graph = Graph.fromString(graphStr);

        int[][] laplacianMatrix = graph.getLaplacianMatrix();

        // Print the Laplacian matrix for debugging
        System.out.println("\nLaplacian Matrix:");
        Graph.printMatrix(laplacianMatrix);

        // Expected Laplacian matrix for the given graph
        int[][] expectedLaplacianMatrix = {
            {3, -1, -1, 0, -1, 0},  // Vertex 0
            {-1, 3, -1, 0, 0, -1},  // Vertex 1
            {-1, -1, 3, -1, 0, 0},  // Vertex 2
            {0, 0, -1, 3, -1, -1},  // Vertex 3
            {-1, 0, 0, -1, 3, -1},  // Vertex 4
            {0, -1, 0, -1, -1, 3}   // Vertex 5
        };

        // Check if the Laplacian matrix matches the expected matrix
        assertTrue(Arrays.deepEquals(laplacianMatrix, expectedLaplacianMatrix),
            "Laplacian matrix does not match expected values.");
    }

    @Test
    public void testDualGraphLaplacianMatrix() {
        // Define the dual graph as a string
        String dualGraphStr = "(0, 1), (0, 2), (0, 3), (0, 4), (1, 2), (1, 4), (2, 4), (2, 3), (3, 4)";

        // Create the dual graph using the fromString method
        Graph dualGraph = Graph.fromString(dualGraphStr);

        // Compute the Laplacian matrix
        int[][] laplacianMatrix = dualGraph.getLaplacianMatrix();

        // Print the Laplacian matrix for debugging
        System.out.println("\nLaplacian Matrix of the Dual Graph:");
        Graph.printMatrix(laplacianMatrix);

        // Expected Laplacian matrix for the dual graph
        int[][] expectedLaplacianMatrix = {
            {4, -1, -1, -1, -1},  // Vertex 0 (E)
            {-1, 3, -1, 0, -1},   // Vertex 1 (A)
            {-1, -1, 4, -1, -1},  // Vertex 2 (B)
            {-1, 0, -1, 3, -1},   // Vertex 3 (C)
            {-1, -1, -1, -1, 4}   // Vertex 4 (D)
        };

        // Verify the Laplacian matrix
        assertTrue(Arrays.deepEquals(laplacianMatrix, expectedLaplacianMatrix),
            "Dual graph Laplacian matrix does not match expected values.");
    }
}