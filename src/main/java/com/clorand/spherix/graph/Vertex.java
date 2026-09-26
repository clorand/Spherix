package com.clorand.spherix.graph;

public class Vertex {
    private int id;
    private double x;
    private double y;
    private static final double EPSILON = 1e-9; // Tolerance for floating-point comparison

    public Vertex(int id) {
        this(id, 0.0, 0.0); // Default coordinates (0, 0)
    }

    public Vertex(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vertex " + id + " (" + x + ", " + y + ")";
    }

    // Override equals to compare vertices by coordinates (with epsilon)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertex vertex = (Vertex) obj;
        return Math.abs(this.x - vertex.x) < EPSILON &&
               Math.abs(this.y - vertex.y) < EPSILON;
    }

    // Override hashCode to maintain consistency with equals
    @Override
    public int hashCode() {
        // Use a hash based on rounded coordinates to respect epsilon equality
        int xHash = Double.hashCode(Math.round(x / EPSILON) * EPSILON);
        int yHash = Double.hashCode(Math.round(y / EPSILON) * EPSILON);
        return xHash + 31 * yHash;
    }
}