package com.clorand.spherix.model;

import main.Vec3;
import com.clorand.spherix.utils.PermutationFinder;
import com.clorand.spherix.utils.RotationUtils;
import com.clorand.spherix.utils.MathUtils;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;

public class Configuration {
    private final Long dbkey;
    private final int n;
    private final List<Vec3> points;
    private final double mean;
    private final List<Edge> contactGraph;
    private final Map<Integer, Integer> degrees;

    public Configuration(Long dbkey, double mean, List<Vec3> points) {
        this.dbkey = dbkey;
        this.mean = mean;
        this.points = points;
        this.n = points.size();
        this.contactGraph = computeContactGraph(points, Math.cos(mean), 0.0001);
        this.degrees = computeDegrees(contactGraph);
    }

    // --- Getters ---
    public Long getDbkey() { return dbkey; }
    public int getN() { return n; }
    public List<Vec3> getPoints() { return points; }
    public double getMean() { return mean; }
    public List<Edge> getContactGraph() { return contactGraph; }
    public Map<Integer, Integer> getDegrees() { return degrees; }

    // --- Core Logic ---
    public static Map<Integer, Integer> computeDegrees(List<Edge> contactGraph) {
        Map<Integer, Integer> degrees = new HashMap<>();
        for (Edge edge : contactGraph) {
            degrees.merge(Integer.valueOf(edge.getVertex1()), 1, Integer::sum);
            degrees.merge(Integer.valueOf(edge.getVertex2()), 1, Integer::sum);
        }
        return degrees;
    }

    public static List<Edge> computeContactGraph(List<Vec3> points, double cosAlpha, double tolerance) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double distance = points.get(i).dot(points.get(j));
                if (MathUtils.allClose(distance, cosAlpha, tolerance)) {
                    edges.add(new Edge(i, j, distance));
                }
            }
        }
        return edges;
    }
    /**
     * Checks if this configuration is equivalent to another.
     * Uses permutation + rotation matrix (from first two point pairs).
     * @param other The other configuration.
     * @param tolerance Tolerance for floating-point comparisons.
     * @return true if configurations are equivalent.
     */
    public boolean isEquivalentTo(Configuration other, double tolerance) {
        // Step 1: Check adjacency matrix isomorphism
        boolean[][] A1 = this.getAdjacencyMatrix();
        boolean[][] A2 = other.getAdjacencyMatrix();
        int[] permutation = PermutationFinder.findPermutationBruteForce(A1, A2);
        if (permutation == null) {
            return false;
        }

        // Step 2: Get the reverse permutation
        int n = permutation.length;
        int[] reversePermutation = new int[n];
        for (int i = 0; i < n; i++) {
            reversePermutation[permutation[i]] = i;
        }

        // Step 3: Use the reverse permutation to map the first two points
        Vec3 a0 = this.getPoints().get(0);
        Vec3 a1 = this.getPoints().get(1);
        Vec3 b0 = other.getPoints().get(reversePermutation[0]); // Use reverse permutation
        Vec3 b1 = other.getPoints().get(reversePermutation[1]); // Use reverse permutation

        // Step 4: Construct rotation matrix from the first two mapped point pairs
        RealMatrix rotationMatrix = RotationUtils.constructRotationMatrix(a0, a1, b0, b1);
        if (rotationMatrix == null) {
            return false;
        }

        // Step 5: Verify all points match after rotation
        for (int i = 0; i < this.getPoints().size(); i++) {
            Vec3 rotatedPoint = RotationUtils.rotateVector(this.getPoints().get(i), rotationMatrix);
            Vec3 targetPoint = other.getPoints().get(reversePermutation[i]); // Use reverse permutation
            if (!MathUtils.allClose(rotatedPoint, targetPoint, tolerance)) {
                return false;
            }
        }
        return true;
    }

    // --- Adjacency Matrix ---
    public boolean[][] getAdjacencyMatrix() {
        boolean[][] matrix = new boolean[n][n];
        for (Edge edge : contactGraph) {
            int i = edge.getVertex1();
            int j = edge.getVertex2();
            matrix[i][j] = true;
            matrix[j][i] = true;
        }
        return matrix;
    }

    // --- toString ---
    @Override
    public String toString() {
        String s1 = String.format("Configuration(dbkey=%s, n=%d, mean=%.4f, edges=%d)",
            dbkey, n, mean, contactGraph.size());
        boolean[][] adjMatrix = getAdjacencyMatrix();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Adjacency Matrix (n=%d):%n", n));

        sb.append("   ");
        for (int j = 0; j < n; j++) sb.append(String.format("%2d ", j));
        sb.append("\n");

        for (int i = 0; i < n; i++) {
            sb.append(String.format("%2d [", i));
            for (int j = 0; j < n; j++) {
                sb.append(adjMatrix[i][j] ? "■" : "□");
                if (j < n - 1) sb.append(" ");
            }
            sb.append("]\n");
        }
        return sb.toString() + s1;
    }
}