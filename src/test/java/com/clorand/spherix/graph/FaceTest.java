package com.clorand.spherix.graph;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FaceTest {

    @Test
    public void testContainsVertex() {
        Vertex v1 = new Vertex(1, 0.0, 0.0);
        Vertex v2 = new Vertex(2, 1.0, 0.0);
        Vertex v3 = new Vertex(3, 0.0, 1.0);
        List<Vertex> vertices = List.of(v1, v2, v3);
        List<Edge> edges = List.of(new Edge(v1, v2), new Edge(v2, v3), new Edge(v3, v1));
        Face face = new Face(vertices, edges);

        assertTrue(face.containsVertex(v1), "Face should contain vertex v1");
        assertTrue(face.containsVertex(v2), "Face should contain vertex v2");
        assertFalse(face.containsVertex(new Vertex(4, 2.0, 2.0)), "Face should not contain unrelated vertex");
    }

    @Test
    public void testContainsEdge() {
        Vertex v1 = new Vertex(1, 0.0, 0.0);
        Vertex v2 = new Vertex(2, 1.0, 0.0);
        Vertex v3 = new Vertex(3, 0.0, 1.0);
        List<Vertex> vertices = List.of(v1, v2, v3);
        List<Edge> edges = List.of(new Edge(v1, v2), new Edge(v2, v3), new Edge(v3, v1));
        Face face = new Face(vertices, edges);

        assertTrue(face.containsEdge(new Edge(v1, v2)), "Face should contain edge (v1, v2)");
        assertTrue(face.containsEdge(new Edge(v2, v1)), "Face should contain edge (v2, v1)");
        assertTrue(face.containsEdge(new Edge(v1, v3)), "Face should not contain edge (v1, v3)");
    }

    @Test
    public void testCenterOfMass() {
        Vertex v1 = new Vertex(1, 0.0, 0.0);
        Vertex v2 = new Vertex(2, 2.0, 0.0);
        Vertex v3 = new Vertex(3, 0.0, 2.0);
        List<Vertex> vertices = List.of(v1, v2, v3);
        List<Edge> edges = List.of(new Edge(v1, v2), new Edge(v2, v3), new Edge(v3, v1));
        Face face = new Face(vertices, edges);

        double expectedX = (0.0 + 2.0 + 0.0) / 3.0;
        double expectedY = (0.0 + 0.0 + 2.0) / 3.0;
        assertEquals(expectedX, face.getCenterOfMass().x, 1e-9, "Center of mass x-coordinate should match");
        assertEquals(expectedY, face.getCenterOfMass().y, 1e-9, "Center of mass y-coordinate should match");
    }
}