package com.clorand.spherix.graph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class GraphTest {

    @Test
    void testFromStringConstructor() {
        // Input string representing the graph
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";

        // Create the graph using the smart constructor
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
        System.out.println(graph);
    }
    
    @Test
    void testAdjacencyMatrix() {
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Graph graph = Graph.fromString(graphStr);

        int[][] adjacencyMatrix = graph.getAdjacencyMatrix();


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

        Graph.printMatrix(laplacianMatrix);
        // Expected Laplacian matrix for the given graph
        int[][] expectedLaplacianMatrix = {
            {3, -1, -1, 0, -1, 0},  // Vertex 0
            {-1, 3, -1, 0, 0, -1},   // Vertex 1
            {-1, -1, 3, -1, 0, 0},  // Vertex 2
            {0, 0, -1, 3, -1, -1}, // Vertex 3
            {-1, 0, 0, -1, 3, -1}, // Vertex 4
            {0, -1, 0, -1, -1, 3}  // Vertex 5
        };

        // Check if the Laplacian matrix matches the expected matrix
        assertTrue(Arrays.deepEquals(laplacianMatrix, expectedLaplacianMatrix),
            "Laplacian matrix does not match expected values.");
    }
    
    @Test
    public void testDualGraphLaplacianMatrix() {
        // Define the dual graph as a string
        // Edges: (E, A), (E, B), (E, C), (E, D), (A, B), (A, D), (B, D), (B, C), (C, D)
        String dualGraphStr = "(0, 1), (0, 2), (0, 3), (0, 4), (1, 2), (1, 4), (2, 4), (2, 3), (3, 4)";

        // Create the dual graph using the fromString method
        Graph dualGraph = Graph.fromString(dualGraphStr);

        // Compute the Laplacian matrix
        int[][] laplacianMatrix = dualGraph.getLaplacianMatrix();

        Graph.printMatrix(laplacianMatrix);
        
        // Print the Laplacian matrix
        System.out.println("Laplacian Matrix of the Dual Graph:");
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
        assertArrayEquals(expectedLaplacianMatrix, laplacianMatrix);
    }
}