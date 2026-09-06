package com.clorand.spherix.graph;

public class Vertex {
    private int id;
    private double x;
    private double y;

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
}