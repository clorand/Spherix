package com.clorand.spherix.utils;

import org.apache.commons.math3.linear.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Utility class for checking if two spherical configurations are isometric (identical up to rotation).
 */
public class IsometryChecker {

    /**
     * Compute the inertia tensor for a list of Vec3 points.
     */
    public static double[][] computeInertiaTensor(List<Vec3> points) {
        double[][] tensor = new double[3][3];
        for (Vec3 p : points) {
            double x = p.x, y = p.y, z = p.z;
            tensor[0][0] += y * y + z * z;
            tensor[0][1] += x * y;
            tensor[0][2] += x * z;
            tensor[1][0] += y * x;
            tensor[1][1] += x * x + z * z;
            tensor[1][2] += y * z;
            tensor[2][0] += z * x;
            tensor[2][1] += z * y;
            tensor[2][2] += x * x + y * y;
        }
        return tensor;
    }

    /**
     * Get eigenvectors of a 3x3 matrix (as columns).
     */
    public static RealMatrix getEigenvectors(double[][] matrix) {
        EigenDecomposition eig = new EigenDecomposition(new Array2DRowRealMatrix(matrix));
        return eig.getV();
    }

    /**
     * Rotate a list of Vec3 points using a rotation matrix.
     */
    public static List<Vec3> rotatePoints(List<Vec3> points, double[][] rotationMatrix) {
        List<Vec3> rotated = new ArrayList<>();
        for (Vec3 p : points) {
            double newX = rotationMatrix[0][0] * p.x + rotationMatrix[0][1] * p.y + rotationMatrix[0][2] * p.z;
            double newY = rotationMatrix[1][0] * p.x + rotationMatrix[1][1] * p.y + rotationMatrix[1][2] * p.z;
            double newZ = rotationMatrix[2][0] * p.x + rotationMatrix[2][1] * p.y + rotationMatrix[2][2] * p.z;
            rotated.add(new Vec3(newX, newY, newZ));
        }
        return rotated;
    }

    /**
     * Sort Vec3 points lexicographically by x, y, z.
     */
    public static List<Vec3> sortPoints(List<Vec3> points) {
        return points.stream()
            .sorted(Comparator.comparingDouble((Vec3 p) -> p.x)
                .thenComparingDouble(p -> p.y)
                .thenComparingDouble(p -> p.z))
            .collect(Collectors.toList());
    }

    /**
     * Compute canonical form: align to principal axes and sort.
     */
    public static List<Vec3> canonicalForm(List<Vec3> points) {
        double[][] tensor = computeInertiaTensor(points);
        double[][] eigenvectors = getEigenvectors(tensor).getData();
        List<Vec3> rotated = rotatePoints(points, eigenvectors);
        return sortPoints(rotated);
    }

    /**
     * Check if two configurations (List<Vec3>) are isometric (up to rotation).
     */
    public static boolean isIsometric(List<Vec3> a, List<Vec3> b, double tolerance) {
        if (a.size() != b.size()) return false;
        List<Vec3> canonicalA = canonicalForm(a);
        List<Vec3> canonicalB = canonicalForm(b);
        return MathUtils.allClose(canonicalA, canonicalB, tolerance);
    }
}
