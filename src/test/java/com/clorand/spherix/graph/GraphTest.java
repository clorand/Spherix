package com.clorand.spherix.graph;

import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import com.clorand.spherix.graph.Graph.BoundaryDirection;
import com.clorand.spherix.graph.Graph.ExtendDirection;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Collectors;

public class GraphTest {

    // Helper method to create the sample graph
    private Graph createSampleGraph() {
        String graphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";
        Map<Integer, double[]> coordinates = new HashMap<>();
        coordinates.put(0, new double[]{5.0, 0.0});
        coordinates.put(1, new double[]{0.0, 5.0});
        coordinates.put(2, new double[]{4.0, 4.0});
        coordinates.put(3, new double[]{7.0, 7.0});
        coordinates.put(4, new double[]{11.0, 6.0});
        coordinates.put(5, new double[]{6.0, 11.0});
        return Graph.fromString(graphStr, coordinates);
    }

    private Graph createDualGraph() {
    	String graphStr = "(0, 1), (0, 2), (1, 3), (1, 2), (2, 3), (2, 4), (3, 5), (3, 4), (4, 5)";
        Map<Integer, double[]> coordinates = new HashMap<>();
        coordinates.put(0, new double[]{ 0.0, 5.0});
        coordinates.put(1, new double[]{25.0,10.0});
        coordinates.put(2, new double[]{30.0, 0.0});
        coordinates.put(3, new double[]{45.0,25.0});
        coordinates.put(4, new double[]{50.0,15.0});
        coordinates.put(5, new double[]{75.0,20.0});
        return Graph.fromString(graphStr, coordinates);
    }
    
    @Test
    public void testFaceDetection() {
        Graph graph = createSampleGraph();

        // Find the faces
        List<Face> detectedFaces = graph.findFaces();

        // Print detected faces for debugging
        System.out.println("Detected Faces:");
        for (Face face : detectedFaces) {
            System.out.println(face);
        }

        // Verify the number of faces
        assertEquals(5, detectedFaces.size(), "The number of detected faces should be 5.");

        // Verify the outer face (largest face)
        boolean outerFaceFound = false;
        for (Face face : detectedFaces) {
            if (face.size() == 4) { // The outer face should have 4 vertices for this graph
                outerFaceFound = true;
                break;
            }
        }
        assertTrue(outerFaceFound, "There should be an outer face with 6 vertices.");
    }

    @Test
    public void testExtendedFaceDetection() {
        Graph graph = createSampleGraph();
        graph.extend(Graph.ExtendDirection.HORIZONTAL);

        // Find the faces
        List<Face> detectedFaces = graph.findFaces();

        // Print detected faces for debugging
        System.out.println("Detected Faces:");
        for (Face face : detectedFaces) {
            System.out.println(face);
        }

        // Verify the number of faces
        assertEquals(7, detectedFaces.size(), "The number of detected faces should be 5.");

        // Verify the outer face (largest face)
        boolean outerFaceFound = false;
        for (Face face : detectedFaces) {
            if (face.size() == 4) { // The outer face should have 4 vertices for this graph
                outerFaceFound = true;
                break;
            }
        }
        assertTrue(outerFaceFound, "There should be an outer face with 6 vertices.");
        GraphVisualizer.visualize(graph);
    }
    
    @Test
    public void testLeftmostVertex() {
        Graph graph = createSampleGraph();
        Vertex leftmost = graph.getBoundaryVertex(BoundaryDirection.LEFT);
        assertNotNull(leftmost, "The graph should have a leftmost vertex.");
        assertEquals(1, leftmost.getId(), "The leftmost vertex should be vertex 1.");
        assertEquals(0.0, leftmost.getX(), 0.001, "The x-coordinate of the leftmost vertex should be 0.0.");
    }

    @Test
    public void testRightmostVertex() {
        Graph graph = createSampleGraph();
        Vertex rightmost = graph.getBoundaryVertex(BoundaryDirection.RIGHT);
        assertNotNull(rightmost, "The graph should have a rightmost vertex.");
        assertEquals(4, rightmost.getId(), "The rightmost vertex should be vertex 4.");
        assertEquals(11.0, rightmost.getX(), 0.001, "The x-coordinate of the rightmost vertex should be 11.0.");
    }

    @Test
    public void testNorthmostVertex() {
        Graph graph = createSampleGraph();
        Vertex northmost = graph.getBoundaryVertex(BoundaryDirection.UP);
        assertNotNull(northmost, "The graph should have a northmost vertex.");
        assertEquals(5, northmost.getId(), "The northmost vertex should be vertex 5.");
        assertEquals(11.0, northmost.getY(), 0.001, "The y-coordinate of the northmost vertex should be 11.0.");
    }

    @Test
    public void testSouthmostVertex() {
        Graph graph = createSampleGraph();
        Vertex southmost = graph.getBoundaryVertex(BoundaryDirection.DOWN);
        assertNotNull(southmost, "The graph should have a southmost vertex.");
        assertEquals(0, southmost.getId(), "The southmost vertex should be vertex 0.");
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
    
    @Test
    public void testLeftmostFaces() {
        Graph graph = createSampleGraph();
        List<Face> leftmostFaces = graph.getBoundaryFaces(BoundaryDirection.LEFT);

        // Print the leftmost faces for debugging
        System.out.println("Leftmost Faces:");
        for (Face face : leftmostFaces) {
            System.out.println(face);
        }

        // Verify that the leftmost vertex (vertex 1) is in all leftmost faces
        Vertex leftmostVertex = graph.getBoundaryVertex(BoundaryDirection.LEFT);
        for (Face face : leftmostFaces) {
            assertTrue(face.containsVertex(leftmostVertex),
                "Leftmost vertex should be in all leftmost faces.");
        }
    }

    @Test
    public void testRightmostFaces() {
        Graph graph = createSampleGraph();
        List<Face> rightmostFaces = graph.getBoundaryFaces(BoundaryDirection.RIGHT);

        // Print the rightmost faces for debugging
        System.out.println("Rightmost Faces:");
        for (Face face : rightmostFaces) {
            System.out.println(face);
        }

        // Verify that the rightmost vertex (vertex 4) is in all rightmost faces
        Vertex rightmostVertex = graph.getBoundaryVertex(BoundaryDirection.RIGHT);
        for (Face face : rightmostFaces) {
            assertTrue(face.containsVertex(rightmostVertex),
                "Rightmost vertex should be in all rightmost faces.");
        }
    }

    @Test
    public void testNorthmostFaces() {
        Graph graph = createSampleGraph();
        List<Face> northmostFaces = graph.getBoundaryFaces(BoundaryDirection.UP);

        // Print the northmost faces for debugging
        System.out.println("Northmost Faces:");
        for (Face face : northmostFaces) {
            System.out.println(face);
        }

        // Verify that the northmost vertex (vertex 0) is in all northmost faces
        Vertex northmostVertex = graph.getBoundaryVertex(BoundaryDirection.UP);
        for (Face face : northmostFaces) {
            assertTrue(face.containsVertex(northmostVertex),
                "Northmost vertex should be in all northmost faces.");
        }
    }

    @Test
    public void testSouthmostFaces() {
        Graph graph = createSampleGraph();
        List<Face> southmostFaces = graph.getBoundaryFaces(BoundaryDirection.DOWN);

        // Print the southmost faces for debugging
        System.out.println("Southmost Faces:");
        for (Face face : southmostFaces) {
            System.out.println(face);
        }

        // Verify that the southmost vertex (vertex 5) is in all southmost faces
        Vertex southmostVertex = graph.getBoundaryVertex(BoundaryDirection.DOWN);
        for (Face face : southmostFaces) {
            assertTrue(face.containsVertex(southmostVertex),
                "Southmost vertex should be in all southmost faces.");
        }
    }
    
    @Test
    public void testStretchHorizontal() {
        // Create the sample graph
        Graph graph = createSampleGraph();

        // Expected x-coordinates before stretching
        // These values are based on the elastic stretching algorithm
        // and should match the computed positions.
        Map<Integer, Double> expectedXCoordinates = new HashMap<>();
        for (Vertex v:graph.getVertices())
        	expectedXCoordinates.put(v.getId(), v.getX()*5);

        
        // Stretch the graph horizontally between vertex 0 and vertex 5
        Vertex start = graph.getVertexById(1);
        Vertex end = graph.getVertexById(4);
        graph.stretch(ExtendDirection.HORIZONTAL, start, end);
        
        for (Vertex v : graph.getVertices())
        {
        	System.out.println("id:"+v.getId()+", "+v.getX());
        }


        // Verify the x-coordinates of the vertices
        for (Vertex vertex : graph.getVertices()) {
            int id = vertex.getId();
            double actualX = vertex.getX();
            double expectedX = expectedXCoordinates.getOrDefault(id, actualX); // Default to actual if not specified

            // Allow for small floating-point precision errors
            assertEquals(expectedX, actualX, 0.01,
                String.format("Vertex %d: Expected x-coordinate %f, but got %f", id, expectedX, actualX));
        }

        // Print the updated x-coordinates for debugging
        System.out.println("Updated x-coordinates after stretching:");
        for (Vertex vertex : graph.getVertices()) {
            System.out.printf("Vertex %d: x = %f%n", vertex.getId(), vertex.getX());
        }
    }
    
    @Test
    public void testStretchVertical() {
        // Create the sample graph
        Graph graph = createSampleGraph();

        // Expected x-coordinates before stretching
        // These values are based on the elastic stretching algorithm
        // and should match the computed positions.
        Map<Integer, Double> expectedXCoordinates = new HashMap<>();
        for (Vertex v:graph.getVertices())
        	expectedXCoordinates.put(v.getId(), v.getY()*5);

        
        // Stretch the graph horizontally between vertex 0 and vertex 5
        Vertex start = graph.getVertexById(0);
        Vertex end = graph.getVertexById(5);
        graph.stretch(ExtendDirection.VERTICAL, start, end);
        
        for (Vertex v : graph.getVertices())
        {
        	System.out.println("id:"+v.getId()+", "+v.getY());
        }


        // Verify the x-coordinates of the vertices
        for (Vertex vertex : graph.getVertices()) {
            int id = vertex.getId();
            double actualY = vertex.getY();
            double expectedX = expectedXCoordinates.getOrDefault(id, actualY); // Default to actual if not specified

            // Allow for small floating-point precision errors
            assertEquals(expectedX, actualY, 0.01,
                String.format("Vertex %d: Expected y-coordinate %f, but got %f", id, expectedX, actualY));
        }

        // Print the updated x-coordinates for debugging
        System.out.println("Updated y-coordinates after stretching:");
        for (Vertex vertex : graph.getVertices()) {
            System.out.printf("Vertex %d: y = %f%n", vertex.getId(), vertex.getY());
        }
    }
    @Test
    public void testGetDualGraph() {
        // Create the sample graph
        Graph graph = createSampleGraph();

        Graph dualGraph = graph.getDualGraph();
        
        // Print the dual graph for debugging
        System.out.println("Dual Graph Vertices:");
        for (Vertex vertex : dualGraph.getVertices()) {
            System.out.println("Vertex " + vertex.getId());
        }
             
        RealVector horizontalStretch = dualGraph.stretch(ExtendDirection.HORIZONTAL, dualGraph.getVertexById(0), dualGraph.getVertexById(5));
        RealVector verticalStretch =dualGraph.stretch(ExtendDirection.VERTICAL, dualGraph.getVertexById(2), dualGraph.getVertexById(3));
        
        dualGraph.sortEdges();
        
        System.out.println("Dual Graph String:"+dualGraph);
        
        System.out.println("\nDual Graph Edges:");
        for (Edge edge : dualGraph.getEdges()) {
            System.out.println(""+edge);
        }
        
        System.out.println("horizontalStretch:"+horizontalStretch);
        System.out.println("verticalStretch  :"+verticalStretch);
        
        Graph expectedDualGraph = createDualGraph();
        
        String dualGraphStr = dualGraph.toString();
        String expectedDualGraphStr = expectedDualGraph.toString();
        
        System.out.println("dualGraphStr:"+dualGraphStr);
        System.out.println("expectedDualGraphStr:"+expectedDualGraphStr);
        
        assertEquals(dualGraphStr, expectedDualGraphStr);   
        assertEquals(dualGraph, expectedDualGraph);
    }
    
    @Test
    public void testGraphToString() {	
        String ExpectedGraphStr = "(0, 1), (0, 2), (0, 4), (2, 1), (2, 3), (3, 4), (1, 5), (3, 5), (4, 5)";        
        Graph graph = createSampleGraph();
        String graphStr = graph.toEdgeString();
        
        System.out.println("ExpectedGraphStr:"+ExpectedGraphStr );
        System.out.println("GraphStr:"+graphStr );

        
        assertEquals(graphStr, ExpectedGraphStr);
    }
}