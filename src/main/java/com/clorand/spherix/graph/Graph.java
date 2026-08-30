package com.clorand.spherix.graph;

import java.util.ArrayList;
import java.util.List;

import java.util.*;

class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<Integer, Vertex> vertexMap;

    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.vertexMap = new HashMap<>();
    }

    // Smart constructor to build a graph from a string
    public static Graph fromString(String graphStr) {
        Graph graph = new Graph();
        List<int[]> edgePairs = parseEdgePairs(graphStr);

        for (int[] pair : edgePairs) {
            int sourceId = pair[0];
            int targetId = pair[1];

            Vertex source = graph.getOrCreateVertex(sourceId);
            Vertex target = graph.getOrCreateVertex(targetId);

            graph.addEdge(new Edge(source, target));
        }
        return graph;
    }

    // Helper method to parse the string into a list of edge pairs
    private static List<int[]> parseEdgePairs(String graphStr) {
        List<int[]> edgePairs = new ArrayList<>();
        // Remove all parentheses and split by ", "
        String[] pairs = graphStr.replaceAll("[()]", "").split(",\\s*");
        for (int i = 0; i < pairs.length; i += 2) {
            int sourceId = Integer.parseInt(pairs[i].trim());
            int targetId = Integer.parseInt(pairs[i + 1].trim());
            edgePairs.add(new int[]{sourceId, targetId});
        }
        return edgePairs;
    }

    // Helper method to get or create a vertex
    private Vertex getOrCreateVertex(int id) {
        if (!vertexMap.containsKey(id)) {
            Vertex vertex = new Vertex(id);
            vertexMap.put(id, vertex);
            vertices.add(vertex);
        }
        return vertexMap.get(id);
    }

    // Add a vertex to the graph
    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    // Add an edge to the graph
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    // Get all vertices
    public List<Vertex> getVertices() {
        return vertices;
    }

    // Get all edges
    public List<Edge> getEdges() {
        return edges;
    }

    // Override toString() to print all edges
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph Edges:\n");
        for (Edge edge : edges) {
            sb.append("Edge: ")
              .append(edge.getSource().getId())
              .append(" -> ")
              .append(edge.getTarget().getId())
              .append("\n");
        }
        return sb.toString();
    }
    
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
            adjacencyMatrix[targetIndex][sourceIndex] = 1; // Add reverse edge for undirected graph
        }

        return adjacencyMatrix;
    }
    
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
    
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
}