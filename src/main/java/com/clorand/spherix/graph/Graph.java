package com.clorand.spherix.graph;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<Integer, Vertex> vertexMap; // For quick lookup by ID

    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.vertexMap = new HashMap<>();
    }

    // Add a vertex to the graph
    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
        vertexMap.put(vertex.getId(), vertex);
    }

    // Add an edge to the graph
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    // Get all vertices
    public List<Vertex> getVertices() {
        return new ArrayList<>(vertices);
    }

    // Get all edges
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    // Get a vertex by its ID
    public Vertex getVertexById(int id) {
        return vertexMap.get(id);
    }

    // Helper method to parse edge pairs from a string
    private static List<int[]> parseEdgePairs(String graphStr) {
        List<int[]> edgePairs = new ArrayList<>();
        String[] pairs = graphStr.replaceAll("[()]", "").split(",\\s*");
        for (int i = 0; i < pairs.length; i += 2) {
            int sourceId = Integer.parseInt(pairs[i].trim());
            int targetId = Integer.parseInt(pairs[i + 1].trim());
            edgePairs.add(new int[]{sourceId, targetId});
        }
        return edgePairs;
    }

    // Build a graph from a string (without coordinates)
    public static Graph fromString(String graphStr) {
        Graph graph = new Graph();
        List<int[]> edgePairs = parseEdgePairs(graphStr);

        // Add vertices with default coordinates (0, 0)
        Set<Integer> vertexIds = new HashSet<>();
        for (int[] pair : edgePairs) {
            vertexIds.add(pair[0]);
            vertexIds.add(pair[1]);
        }
        for (int id : vertexIds) {
            graph.addVertex(new Vertex(id));
        }

        // Add edges
        for (int[] pair : edgePairs) {
            int sourceId = pair[0];
            int targetId = pair[1];
            Vertex source = graph.getVertexById(sourceId);
            Vertex target = graph.getVertexById(targetId);
            graph.addEdge(new Edge(source, target));
        }
        return graph;
    }

    // Build a graph from a string with coordinates
    public static Graph fromString(String graphStr, Map<Integer, double[]> coordinates) {
        Graph graph = new Graph();
        List<int[]> edgePairs = parseEdgePairs(graphStr);

        // Add vertices with coordinates
        for (int id : coordinates.keySet()) {
            double[] coord = coordinates.get(id);
            graph.addVertex(new Vertex(id, coord[0], coord[1]));
        }

        // Add edges
        for (int[] pair : edgePairs) {
            int sourceId = pair[0];
            int targetId = pair[1];
            Vertex source = graph.getVertexById(sourceId);
            Vertex target = graph.getVertexById(targetId);
            graph.addEdge(new Edge(source, target));
        }
        return graph;
    }

    // Compute the adjacency matrix of the graph
    public int[][] getAdjacencyMatrix() {
        int n = vertices.size();
        int[][] adjacencyMatrix = new int[n][n];

        // Initialize all entries to 0
        for (int i = 0; i < n; i++) {
            Arrays.fill(adjacencyMatrix[i], 0);
        }

        // Populate the adjacency matrix for undirected graph
        for (Edge edge : edges) {
            int sourceIndex = edge.getSource().getId();
            int targetIndex = edge.getTarget().getId();
            adjacencyMatrix[sourceIndex][targetIndex] = 1;
            adjacencyMatrix[targetIndex][sourceIndex] = 1; // Undirected graph
        }

        return adjacencyMatrix;
    }

    // Compute the Laplacian matrix of the graph
    public int[][] getLaplacianMatrix() {
        int n = vertices.size();
        int[][] adjacencyMatrix = getAdjacencyMatrix();
        int[][] laplacianMatrix = new int[n][n];

        // Compute the degree of each vertex
        int[] degrees = new int[n];
        for (Edge edge : edges) {
            degrees[edge.getSource().getId()]++;
            degrees[edge.getTarget().getId()]++;
        }

        // Populate the Laplacian matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    laplacianMatrix[i][j] = degrees[i] - adjacencyMatrix[i][j];
                } else {
                    laplacianMatrix[i][j] = -adjacencyMatrix[i][j];
                }
            }
        }

        return laplacianMatrix;
    }

 // Helper method to print a matrix
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
    
    // Compute the cyclic order of neighbors around each vertex
    public Map<Integer, List<Integer>> computeCyclicOrder() {
        Map<Integer, List<Integer>> cyclicOrder = new HashMap<>();

        for (Vertex vertex : vertices) {
            int v = vertex.getId();
            List<Integer> neighbors = new ArrayList<>();

            // Collect all neighbors of vertex v
            for (Edge edge : edges) {
                if (edge.getSource().getId() == v) {
                    neighbors.add(edge.getTarget().getId());
                } else if (edge.getTarget().getId() == v) {
                    neighbors.add(edge.getSource().getId());
                }
            }

            // Sort neighbors by polar angle around vertex v
            neighbors.sort((u1, u2) -> {
                Vertex v1 = getVertexById(u1);
                Vertex v2 = getVertexById(u2);
                double angle1 = Math.atan2(v1.getY() - vertex.getY(), v1.getX() - vertex.getX());
                double angle2 = Math.atan2(v2.getY() - vertex.getY(), v2.getX() - vertex.getX());
                return Double.compare(angle2, angle1); // Sort in clockwise order
            });

            cyclicOrder.put(v, neighbors);
        }

        return cyclicOrder;
    }

    // Find all faces in the planar embedding
    public List<List<Integer>> findFaces() {
        Map<Integer, List<Integer>> cyclicOrder = computeCyclicOrder();

        // Create a set of directed edges
        Set<String> directedEdges = new HashSet<>();
        for (Edge edge : edges) {
            int u = edge.getSource().getId();
            int v = edge.getTarget().getId();
            directedEdges.add(u + "," + v);
            directedEdges.add(v + "," + u);
        }

        Set<String> visited = new HashSet<>();
        List<List<Integer>> faces = new ArrayList<>();

        for (String startEdge : directedEdges) {
            if (visited.contains(startEdge)) {
                continue;
            }

            List<Integer> face = new ArrayList<>();
            String currentEdge = startEdge;
            String[] edgeParts = currentEdge.split(",");
            int u = Integer.parseInt(edgeParts[0]);
            int v = Integer.parseInt(edgeParts[1]);

            // Start traversing the face
            while (!visited.contains(currentEdge)) {
                visited.add(currentEdge);
                face.add(u);

                // Get the cyclic order of neighbors around v
                List<Integer> neighbors = cyclicOrder.get(v);
                if (neighbors == null || neighbors.isEmpty()) {
                    break; // No neighbors to traverse
                }

                // Find the index of u in the neighbors list
                int index = neighbors.indexOf(u);
                if (index == -1) {
                    break; // u is not a neighbor of v (should not happen for valid graphs)
                }

                // Take the previous edge in cyclic order (left-hand rule)
                int nextNeighbor = neighbors.get((index - 1 + neighbors.size()) % neighbors.size());

                // Move to the next directed edge
                currentEdge = v + "," + nextNeighbor;
                edgeParts = currentEdge.split(",");
                u = Integer.parseInt(edgeParts[0]);
                v = Integer.parseInt(edgeParts[1]);
            }

            // Remove consecutive duplicates (but preserve the start/end vertex)
            List<Integer> uniqueFace = new ArrayList<>();
            for (int vertex : face) {
                if (uniqueFace.isEmpty() || vertex != uniqueFace.get(uniqueFace.size() - 1)) {
                    uniqueFace.add(vertex);
                }
            }

            // Only add the face if it has at least 3 unique vertices
            if (uniqueFace.size() >= 3) {
                faces.add(uniqueFace);
            }
        }

        return faces;
    }
    
 // Get the leftmost vertex (smallest x-coordinate)
    public Vertex getLeftmostVertex() {
        if (vertices.isEmpty()) {
            return null;
        }
        return vertices.stream()
                .min(Comparator.comparingDouble(Vertex::getX))
                .orElse(null);
    }

    // Get the rightmost vertex (largest x-coordinate)
    public Vertex getRightmostVertex() {
        if (vertices.isEmpty()) {
            return null;
        }
        return vertices.stream()
                .max(Comparator.comparingDouble(Vertex::getX))
                .orElse(null);
    }

    // Get the northmost vertex (largest y-coordinate)
    public Vertex getNorthmostVertex() {
        if (vertices.isEmpty()) {
            return null;
        }
        return vertices.stream()
                .max(Comparator.comparingDouble(Vertex::getY))
                .orElse(null);
    }

    // Get the southmost vertex (smallest y-coordinate)
    public Vertex getSouthmostVertex() {
        if (vertices.isEmpty()) {
            return null;
        }
        return vertices.stream()
                .min(Comparator.comparingDouble(Vertex::getY))
                .orElse(null);
    }
    
}