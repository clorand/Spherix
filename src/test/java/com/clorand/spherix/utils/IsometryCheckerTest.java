package com.clorand.spherix.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import main.Vec3;
import java.util.Arrays;
import java.util.List;

class IsometryCheckerTest {

    // Test computeInertiaTensor
    @Test
    void testComputeInertiaTensor_SinglePointAtOrigin() {
        List<Vec3> points = Arrays.asList(new Vec3(0.0, 0.0, 0.0));
        double[][] tensor = IsometryChecker.computeInertiaTensor(points);
        assertArrayEquals(new double[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}}, tensor, 0.0001);
    }

    @Test
    void testComputeInertiaTensor_TwoPoints() {
        List<Vec3> points = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(-1.0, 0.0, 0.0)
        );
        double[][] tensor = IsometryChecker.computeInertiaTensor(points);
        // Expected tensor for two points on the x-axis
        double[][] expected = {
            {0, 0, 0},
            {0, 2, 0},
            {0, 0, 2}
        };
        assertArrayEquals(expected, tensor, 0.0001);
    }

    // Test canonicalForm
    @Test
    void testCanonicalForm_EmptyList() {
        List<Vec3> points = Arrays.asList();
        List<Vec3> canonical = IsometryChecker.canonicalForm(points);
        assertTrue(canonical.isEmpty());
    }

    @Test
    void testCanonicalForm_SinglePoint() {
        List<Vec3> points = Arrays.asList(new Vec3(1.0, 2.0, 3.0));
        List<Vec3> canonical = IsometryChecker.canonicalForm(points);
        Assertions.assertEquals(1, canonical.size());
        // The canonical form should be sorted and rotated
        assertEquals(new Vec3(1.0, 2.0, 3.0).normalize(), canonical.get(0), 0.0001);
    }

    @Test
    void testCanonicalForm_Tetrahedron() {
        // A regular tetrahedron (4 points)
        List<Vec3> points = Arrays.asList(
            new Vec3(1.0, 1.0, 1.0).normalize(),
            new Vec3(1.0, -1.0, -1.0).normalize(),
            new Vec3(-1.0, 1.0, -1.0).normalize(),
            new Vec3(-1.0, -1.0, 1.0).normalize()
        );
        List<Vec3> canonical = IsometryChecker.canonicalForm(points);
        Assertions.assertEquals(4, canonical.size());
        // The canonical form should be sorted
        for (int i = 0; i < canonical.size() - 1; i++) {
            Vec3 current = canonical.get(i);
            Vec3 next = canonical.get(i + 1);
            assertTrue(
                current.x <= next.x ||
                (current.x == next.x && current.y <= next.y) ||
                (current.x == next.x && current.y == next.y && current.z <= next.z)
            );
        }
    }

    // Test isIsometric
    @Test
    void testIsIsometric_IdenticalConfigurations() {
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 1.0, 0.0),
            new Vec3(0.0, 0.0, 1.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 1.0, 0.0),
            new Vec3(0.0, 0.0, 1.0)
        );
        assertTrue(IsometryChecker.isIsometric(a, b, 0.0001));
    }

    @Test
    void testIsIsometric_RotatedConfigurations() {
        // Two configurations that are rotations of each other
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 1.0, 0.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(0.0, 1.0, 0.0),
            new Vec3(-1.0, 0.0, 0.0)
        );
        assertTrue(IsometryChecker.isIsometric(a, b, 0.0001));
    }

    @Test
    void testIsIsometric_NonIsometricConfigurations() {
        // Two configurations that are not isometric
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 1.0, 0.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 2.0, 0.0)  // Different distance
        );
        assertFalse(IsometryChecker.isIsometric(a, b, 0.0001));
    }

    @Test
    void testIsIsometric_DifferentSizes() {
        List<Vec3> a = Arrays.asList(new Vec3(1.0, 0.0, 0.0));
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 0.0, 0.0),
            new Vec3(0.0, 1.0, 0.0)
        );
        assertFalse(IsometryChecker.isIsometric(a, b, 0.0001));
    }

    @Test
    void testIsIsometric_EmptyLists() {
        List<Vec3> a = Arrays.asList();
        List<Vec3> b = Arrays.asList();
        assertTrue(IsometryChecker.isIsometric(a, b, 0.0001));
    }

    // Helper method to compare Vec3 objects with a tolerance
    private void assertEquals(Vec3 expected, Vec3 actual, double tolerance) {
        assertTrue(
            Math.abs(expected.x - actual.x) < tolerance &&
            Math.abs(expected.y - actual.y) < tolerance &&
            Math.abs(expected.z - actual.z) < tolerance,
            String.format("Expected %s, but was %s", expected, actual)
        );
    }

    // Helper method to compare 2D arrays with a tolerance
    private void assertArrayEquals(double[][] expected, double[][] actual, double tolerance) {
        Assertions.assertEquals(expected.length, actual.length, "Array lengths differ");
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(expected[i].length, actual[i].length, "Sub-array lengths differ at index " + i);
            for (int j = 0; j < expected[i].length; j++) {
                assertTrue(
                    Math.abs(expected[i][j] - actual[i][j]) < tolerance,
                    String.format("Mismatch at [%d][%d]: expected %f, but was %f", i, j, expected[i][j], actual[i][j])
                );
            }
        }
    }
}