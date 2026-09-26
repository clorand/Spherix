package com.clorand.spherix.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {

    @Test
    public void testEqualsWithSameVertices() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 3.0, 4.0);
        Edge e1 = new Edge(v1, v2);
        Edge e2 = new Edge(v2, v1);
        assertTrue(e1.equals(e2), "Edges with same vertices in reverse order should be equal");
    }

    @Test
    public void testEqualsWithDifferentVertices() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 3.0, 4.0);
        Vertex v3 = new Vertex(3, 5.0, 6.0);
        Edge e1 = new Edge(v1, v2);
        Edge e2 = new Edge(v1, v3);
        assertFalse(e1.equals(e2), "Edges with different vertices should not be equal");
    }

    @Test
    public void testHashCodeConsistency() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 3.0, 4.0);
        Edge e1 = new Edge(v1, v2);
        Edge e2 = new Edge(v2, v1);
        assertEquals(e1.hashCode(), e2.hashCode(), "Equal edges should have equal hash codes");
    }
}
