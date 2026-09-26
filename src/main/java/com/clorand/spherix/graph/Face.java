package com.clorand.spherix.graph;

import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;

public class Face {
    private List<Vertex> vertices;  // List of Vertex objects that form the face
    private List<Edge> edges;        // List of edges that form the boundary of the face
    private Point2D.Double centerOfMass; // Center of mass (centroid) of the face

    public Face(List<Vertex> vertices, List<Edge> edges) {
        this.vertices = new ArrayList<>(vertices);
        this.edges = new ArrayList<>(edges);
        this.centerOfMass = computeCenterOfMass(); // Compute center of mass during initialization
    }

    // Compute the center of mass (centroid) of the face
    private Point2D.Double computeCenterOfMass() {
        double sumX = 0.0;
        double sumY = 0.0;
        int n = vertices.size();

        for (Vertex vertex : vertices) {
            sumX += vertex.getX();
            sumY += vertex.getY();
        }

        return new Point2D.Double(sumX / n, sumY / n);
    }

    // Get the center of mass of the face
    public Point2D.Double getCenterOfMass() {
        return centerOfMass;
    }

    // Get the vertices of the face
    public List<Vertex> getVertices() {
        return new ArrayList<>(vertices);
    }

    // Get the edges of the face
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    // Check if the face contains a specific vertex
    public boolean containsVertex(Vertex vertex) {
        return vertices.contains(vertex);
    }

    // Compute the area of the face using the shoelace formula
    public double getArea() {
        double area = 0.0;
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            Vertex p1 = vertices.get(i);
            Vertex p2 = vertices.get(j);
            area += (p1.getX() * p2.getY()) - (p2.getX() * p1.getY());
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
        for (Edge faceEdge : edges) {
            if (faceEdge.equals(edge)) {
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

	public int size() {
		return vertices.size();
	}
}