package com.clorand.spherix.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VertexTest {

    @Test
    public void testEqualsWithSameCoordinates() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 1.0, 2.0);
        assertTrue(v1.equals(v2), "Vertices with same coordinates should be equal");
    }

    @Test
    public void testEqualsWithDifferentCoordinates() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 3.0, 4.0);
        assertFalse(v1.equals(v2), "Vertices with different coordinates should not be equal");
    }

    @Test
    public void testEqualsWithEpsilon() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 1.0 + 1e-10, 2.0 + 1e-10);
        assertTrue(v1.equals(v2), "Vertices with coordinates within epsilon should be equal");
    }

    @Test
    public void testHashCodeConsistency() {
        Vertex v1 = new Vertex(1, 1.0, 2.0);
        Vertex v2 = new Vertex(2, 1.0, 2.0);
        assertEquals(v1.hashCode(), v2.hashCode(), "Equal vertices should have equal hash codes");
    }
}