package com.clorand.spherix.graph;

import java.util.*;
import java.awt.geom.Point2D;

public class GraphEmbedding {
    private Graph graph;
    private Map<Integer, Point2D.Double> vertexCoordinates;
    private List<List<Integer>> faces;

    public GraphEmbedding(Graph graph, Map<Integer, Point2D.Double> vertexCoordinates) {
        this.graph = graph;
        this.vertexCoordinates = vertexCoordinates;
        this.faces = new ArrayList<>();
    }

    // Compute the cyclic order of neighbors around each vertex
    public Map<Integer, List<Integer>> computeCyclicOrder() {
        Map<Integer, List<Integer>> cyclicOrder = new HashMap<>();

        for (Vertex vertex : graph.getVertices()) {
            int v = vertex.getId();
            List<Integer> neighbors = new ArrayList<>();

            // Collect all neighbors of vertex v
            for (Edge edge : graph.getEdges()) {
                if (edge.getSource().getId() == v) {
                    neighbors.add(edge.getTarget().getId());
                } else if (edge.getTarget().getId() == v) {
                    neighbors.add(edge.getSource().getId());
                }
            }

            // Sort neighbors by polar angle around vertex v
            Point2D.Double vCoord = vertexCoordinates.get(v);
            neighbors.sort((u1, u2) -> {
                Point2D.Double u1Coord = vertexCoordinates.get(u1);
                Point2D.Double u2Coord = vertexCoordinates.get(u2);
                double angle1 = Math.atan2(u1Coord.y - vCoord.y, u1Coord.x - vCoord.x);
                double angle2 = Math.atan2(u2Coord.y - vCoord.y, u2Coord.x - vCoord.x);
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
        for (Edge edge : graph.getEdges()) {
            int u = edge.getSource().getId();
            int v = edge.getTarget().getId();
            directedEdges.add(u + "," + v);
            directedEdges.add(v + "," + u);
        }

        Set<String> visited = new HashSet<>();
        faces.clear();

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

    // Get the detected faces
    public List<List<Integer>> getFaces() {
        if (faces.isEmpty()) {
            findFaces();
        }
        return faces;
    }

    // Print the detected faces
    public void printFaces() {
        System.out.println("Detected Faces:");
        for (int i = 0; i < faces.size(); i++) {
            System.out.println("  Face " + (i + 1) + ": " + faces.get(i));
        }
    }
}