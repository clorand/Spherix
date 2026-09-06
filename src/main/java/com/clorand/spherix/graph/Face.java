package com.clorand.spherix.graph;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.awt.geom.Point2D;


public class Face {
    private List<Integer> vertices;  // List of vertex IDs that form the face
    private List<Edge> edges;        // List of edges that form the boundary of the face
    private Graph graph;             // Reference to the graph this face belongs to
    private Map<Integer, Point2D.Double> vertexCoordinates; // Coordinates of the vertices

    public Face(List<Integer> vertices, Graph graph, Map<Integer, Point2D.Double> vertexCoordinates) {
        this.vertices = new ArrayList<>(vertices);
        this.graph = graph;
        this.vertexCoordinates = vertexCoordinates;
        this.edges = new ArrayList<>();
        this.initializeEdges();
    }

    // Initialize the edges of the face
    private void initializeEdges() {
        for (int i = 0; i < vertices.size(); i++) {
            int u = vertices.get(i);
            int v = vertices.get((i + 1) % vertices.size());
            edges.add(new Edge(new Vertex(u), new Vertex(v)));
        }
    }

    // Get the vertices of the face
    public List<Integer> getVertices() {
        return new ArrayList<>(vertices);
    }

    // Get the edges of the face
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    // Compute the area of the face using the shoelace formula
    public double getArea() {
        double area = 0.0;
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            Point2D.Double p1 = vertexCoordinates.get(vertices.get(i));
            Point2D.Double p2 = vertexCoordinates.get(vertices.get(j));
            area += (p1.x * p2.y) - (p2.x * p1.y);
        }
        return Math.abs(area) / 2.0;
    }

    // Check if this face is the outer face (heuristic: largest area)
    public boolean isOuterFace(List<Face> allFaces) {
        double maxArea = 0.0;
        for (Face face : allFaces) {
            double currentArea = face.getArea();
            if (currentArea > maxArea) {
                maxArea = currentArea;
            }
        }
        return Math.abs(this.getArea() - maxArea) < 1e-9; // Account for floating-point precision
    }

    // Check if this face contains a specific edge
    public boolean containsEdge(Edge edge) {
        int u = edge.getSource().getId();
        int v = edge.getTarget().getId();
        for (Edge faceEdge : edges) {
            int faceU = faceEdge.getSource().getId();
            int faceV = faceEdge.getTarget().getId();
            if ((faceU == u && faceV == v) || (faceU == v && faceV == u)) {
                return true;
            }
        }
        return false;
    }

    // Check if this face shares an edge with another face
    public boolean sharesEdgeWith(Face otherFace) {
        for (Edge edge : this.edges) {
            if (otherFace.containsEdge(edge)) {
                return true;
            }
        }
        return false;
    }

    // Override toString to print the face vertices
    @Override
    public String toString() {
        return "Face: " + vertices;
    }
}