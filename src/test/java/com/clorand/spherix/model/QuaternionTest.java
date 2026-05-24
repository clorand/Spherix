package com.clorand.spherix.model;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import main.Quaternion;
import main.Vec3;



public class QuaternionTest {

    @Test
    public void testConstructorWithParallelVectors() {
        Vec3 u = new Vec3(1, 0, 0);
        Vec3 v = new Vec3(1, 0, 0); // Parallel to u
        Quaternion q = new Quaternion(u, v);

        // Identity quaternion should have w=1, x=y=z=0
        assertEquals(0.0, q.x, 1e-6);
        assertEquals(0.0, q.y, 1e-6);
        assertEquals(0.0, q.z, 1e-6);
        assertEquals(1.0, q.w, 1e-6);
    }

    @Test
    public void testConstructorWithAntiparallelVectors() {
        Vec3 u = new Vec3(1, 0, 0);
        Vec3 v = new Vec3(-1, 0, 0); // Antiparallel to u
        Quaternion q = new Quaternion(u, v);

        // Verify the rotation works: q.rotate(u) should be close to v
        Vec3 rotatedU = q.rotate(u);
        assertEquals(v.x, rotatedU.x, 1e-6);
        assertEquals(v.y, rotatedU.y, 1e-6);
        assertEquals(v.z, rotatedU.z, 1e-6);

        // Verify the quaternion is normalized
        double length = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
        assertEquals(1.0, length, 1e-6);
    }

    @Test
    public void testConstructorWithPerpendicularVectors() {
        Vec3 u = new Vec3(1, 0, 0);
        Vec3 v = new Vec3(0, 1, 0); // Perpendicular to u
        Quaternion q = new Quaternion(u, v);

        // Should be a 90-degree rotation around the z-axis
        assertEquals(Math.PI / 2, q.getAngle(), 1e-6);
        assertEquals(0.0, q.x, 1e-6);
        assertEquals(0.0, q.y, 1e-6);
        assertEquals(0.70710678118, q.z, 1e-6); // sin(π/4) ≈ 0.7071
        assertEquals(0.70710678118, q.w, 1e-6); // cos(π/4) ≈ 0.7071
    }

    @Test
    public void testConstructorWithGeneralVectors() {
        Vec3 u = new Vec3(1, 0, 0);
        Vec3 v = new Vec3(0, 1, 1).normalize(); // Arbitrary vector
        Quaternion q = new Quaternion(u, v);

        // Verify the quaternion is normalized
        double length = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
        assertEquals(1.0, length, 1e-6);

        // Verify the rotation works: q.rotate(u) should be close to v
        Vec3 rotatedU = q.rotate(u);
        assertEquals(v.x, rotatedU.x, 1e-6);
        assertEquals(v.y, rotatedU.y, 1e-6);
        assertEquals(v.z, rotatedU.z, 1e-6);
    }

    @Test
    public void testRotationMatrix() {
        Vec3 u = new Vec3(1, 0, 0);
        Vec3 v = new Vec3(0, 1, 0);
        Quaternion q = new Quaternion(u, v);

        // Get the rotation matrix
        double[][] R = q.toRotationMatrix();

        // Apply the rotation matrix to u
        Vec3 rotatedU = q.matrixRotate(u);

        // rotatedU should be close to v
        assertEquals(v.x, rotatedU.x, 1e-6);
        assertEquals(v.y, rotatedU.y, 1e-6);
        assertEquals(v.z, rotatedU.z, 1e-6);
    }
}