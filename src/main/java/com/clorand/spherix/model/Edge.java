package com.clorand.spherix.model;

public class Edge {
    private final int vertex1;
    private final int vertex2;
    private final double length;

    public Edge(int vertex1, int vertex2, double length) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.length = length;
    }

    public int getVertex1() { return vertex1; }
    public int getVertex2() { return vertex2; }
    public double getLength() { return length; }

    @Override
    public String toString() {
        return String.format("Edge(%d-%d, length=%.4f)", vertex1, vertex2, length);
    }


}