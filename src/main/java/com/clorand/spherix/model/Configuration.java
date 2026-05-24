package com.clorand.spherix.model;

import main.Vec3;
import com.clorand.spherix.utils.IsometryChecker;
import com.clorand.spherix.utils.KabschIsometryChecker;
import com.clorand.spherix.utils.PermutationFinder;
import com.clorand.spherix.utils.EigenHelper;

import java.util.*;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class Configuration {
    private final Long dbkey;
    private final int n;
    private final List<Vec3> points;
    private final double mean;
    private final List<Edge> contactGraph;
    private final Map<Integer, Integer> degrees;

    public Configuration(Long dbkey, double mean, List<Vec3> points, List<Edge> contactGraph) {
        this.dbkey = dbkey;
        this.mean = mean;
        this.points = points;
        this.n = points.size();
        this.contactGraph = contactGraph;
        this.degrees = computeDegrees(contactGraph);
    }

    public Long getDbkey() { return dbkey; }
    public int getN() { return n; }
    public List<Vec3> getPoints() { return points; }
    public double getMean() { return mean; }
    public List<Edge> getContactGraph() { return contactGraph; }
    public Map<Integer, Integer> getDegrees() { return degrees; }

    private Map<Integer, Integer> computeDegrees(List<Edge> contactGraph) {
        Map<Integer, Integer> degrees = new HashMap<>();
        for (Edge edge : contactGraph) {
            degrees.merge(Integer.valueOf(edge.getVertex1()), 1, Integer::sum);
            degrees.merge(Integer.valueOf(edge.getVertex2()), 1, Integer::sum);
        }
        return degrees;
    }

    /**
     * Checks if this configuration is isometric to another.
     * @param other      The other configuration.
     * @param tolerance  Tolerance for RMSD comparison.
     * @return           true if the configurations are isometric.
     */
    public boolean isIsometricTo(Configuration other, double tolerance) {
        // Step 1: Check if adjacency matrices are isomorphic
        boolean[][] A1 = this.getAdjacencyMatrix();
        boolean[][] A2 = other.getAdjacencyMatrix();
        int[] permutation = PermutationFinder.findPermutationBruteForce(A1, A2);
        if (permutation == null) {
            return false;
        }
        else
        {
        	System.out.println("\nFound permutation: " + Arrays.toString(permutation));
        }

        // Step 2: Check geometric isometry using Kabsch
        return KabschIsometryChecker.isIsometric(
            this.getPoints(),
            other.getPoints(),
            permutation,
            tolerance
        );
    }

    @Override
    public String toString() {
    	String s1 = String.format("Configuration(dbkey=%s, n=%d, mean=%.4f, edges=%d)", dbkey, n, mean, contactGraph.size());
        boolean[][] adjMatrix = getAdjacencyMatrix();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Adjacency Matrix (n=%d):%n", n));

        // Header row (column indices)
        sb.append("   "); // Padding for row labels
        for (int j = 0; j < n; j++) {
            sb.append(String.format("%2d ", j));
        }
        sb.append("\n");
        
        // Matrix rows
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%2d ", i)); // Row label
            sb.append("[");
            for (int j = 0; j < n; j++) {
                sb.append(adjMatrix[i][j] ? "■" : "□");
                if (j < n - 1) sb.append(" ");
            }
            sb.append("]\n");
        }

        return ""+sb+s1;
    }
    

    
    /**
     * Converts the contact graph into an adjacency matrix.
     * @return A 2D boolean array where matrix[i][j] is true if there is an edge between i and j.
     */
    public boolean[][] getAdjacencyMatrix() {
        boolean[][] matrix = new boolean[n][n];
        for (Edge edge : contactGraph) {
            int i = edge.getVertex1();
            int j = edge.getVertex2();
            matrix[i][j] = true;
            matrix[j][i] = true; // Undirected graph
        }
        return matrix;
    }
    
    
    /**
     * Checks if this configuration is isometric to another under a given permutation.
     * Uses the Kabsch algorithm to compute the optimal rotation and verify isometry.
     * @param other The other configuration.
     * @param permutation The permutation array (permutation[i] = j means vertex i in this config maps to vertex j in other).
     * @param tolerance The tolerance for RMSD comparison.
     * @return true if the configurations are isometric under the permutation.
     */
    public boolean isIsometricUnderPermutation(Configuration other, int[] permutation, double tolerance) {
        List<Vec3> thisPoints = this.getPoints();
        List<Vec3> otherPoints = other.getPoints();
        int n = thisPoints.size();

        if (permutation.length != n) {
            return false;
        }

        // Apply the permutation to the other configuration's points
        List<Vec3> permutedOtherPoints = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            permutedOtherPoints.add(otherPoints.get(permutation[i]));
        }

        // Compute the Kabsch RMSD
        double rmsd = computeKabschRMSD(thisPoints, permutedOtherPoints);
        return rmsd < tolerance;
    }

    /**
     * Computes the RMSD between two sets of points after optimal rotation (Kabsch algorithm).
     * @param a First set of points.
     * @param b Second set of points (same size as a).
     * @return The RMSD after optimal rotation.
     */
    private double computeKabschRMSD(List<Vec3> a, List<Vec3> b) {
        int n = a.size();
        if (n != b.size() || n == 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Compute centroids (should be zero for centered configurations, but let's verify)
        Vec3 centroidA = new Vec3(0, 0, 0);
        Vec3 centroidB = new Vec3(0, 0, 0);
        for (Vec3 point : a) centroidA = centroidA.add(point);
        for (Vec3 point : b) centroidB = centroidB.add(point);
        centroidA = centroidA.mul(1.0 / n);
        centroidB = centroidB.mul(1.0 / n);

        // Center the points
        List<Vec3> aCentered = new ArrayList<>();
        List<Vec3> bCentered = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            aCentered.add(a.get(i).sub(centroidA));
            bCentered.add(b.get(i).sub(centroidB));
        }

        // Compute the covariance matrix H = A^T * B
        double[][] H = new double[3][3];
        for (int i = 0; i < n; i++) {
            Vec3 ai = aCentered.get(i);
            Vec3 bi = bCentered.get(i);
            H[0][0] += ai.x * bi.x; H[0][1] += ai.x * bi.y; H[0][2] += ai.x * bi.z;
            H[1][0] += ai.y * bi.x; H[1][1] += ai.y * bi.y; H[1][2] += ai.y * bi.z;
            H[2][0] += ai.z * bi.x; H[2][1] += ai.z * bi.y; H[2][2] += ai.z * bi.z;
        }

        // Compute SVD of H: H = U * S * V^T
        RealMatrix HMatrix = MatrixUtils.createRealMatrix(H);
        SingularValueDecomposition svd = new SingularValueDecomposition(HMatrix);
        RealMatrix U = svd.getU();
        RealMatrix Vt = svd.getVT();

        // Compute the rotation matrix R = V * U^T
        RealMatrix R = Vt.transpose().multiply(U.transpose());

        // Check if the rotation is valid (det(R) should be ~1 or ~-1)
        double det = new LUDecomposition(R).getDeterminant();
        if (Math.abs(det - 1.0) > 1e-6 && Math.abs(det + 1.0) > 1e-6) {
            // Not a valid rotation or reflection
            return Double.POSITIVE_INFINITY;
        }

        // Apply rotation to A and compute RMSD
        double sumSquaredDistances = 0.0;
        for (int i = 0; i < n; i++) {
            Vec3 rotatedAi = rotateVector(aCentered.get(i), R);
            Vec3 bi = bCentered.get(i);
            double dx = rotatedAi.x - bi.x;
            double dy = rotatedAi.y - bi.y;
            double dz = rotatedAi.z - bi.z;
            sumSquaredDistances += dx * dx + dy * dy + dz * dz;
        }
        double rmsd = Math.sqrt(sumSquaredDistances / n);
        return rmsd;
    }

    /**
     * Rotates a vector using a rotation matrix.
     */
    private Vec3 rotateVector(Vec3 v, RealMatrix R) {
        return new Vec3(
            R.getEntry(0, 0) * v.x + R.getEntry(0, 1) * v.y + R.getEntry(0, 2) * v.z,
            R.getEntry(1, 0) * v.x + R.getEntry(1, 1) * v.y + R.getEntry(1, 2) * v.z,
            R.getEntry(2, 0) * v.x + R.getEntry(2, 1) * v.y + R.getEntry(2, 2) * v.z
        );
    }
 // In Configuration.java, replace all SVDHelper references with EigenHelper:

    /**
     * Checks if the adjacency matrices of this configuration and another have the same eigenvalues.
     * @param other The other configuration.
     * @param tolerance The tolerance for comparing eigenvalues.
     * @return true if the eigenvalues match (up to sorting and tolerance).
     */
    public boolean haveSameEigenvalues(Configuration other, double tolerance) {
        boolean[][] adjMatrix1 = this.getAdjacencyMatrix();
        boolean[][] adjMatrix2 = other.getAdjacencyMatrix();
        return EigenHelper.haveSameEigenvalues(adjMatrix1, adjMatrix2, tolerance);
    }

    /**
     * Attempts to find a permutation between this configuration and another using eigenvectors.
     * @param other The other configuration.
     * @param tolerance The tolerance for numerical comparisons.
     * @return The permutation array if found, or null if no valid permutation exists.
     */
    public int[] findPermutationViaEigenvectors(Configuration other, double tolerance) {
        boolean[][] adjMatrix1 = this.getAdjacencyMatrix();
        boolean[][] adjMatrix2 = other.getAdjacencyMatrix();
        return EigenHelper.getPermutationFromEigenvectors(adjMatrix1, adjMatrix2, tolerance);
    }

    /**
     * Checks if this configuration is isometric to another using eigenvectors and Kabsch.
     * @param other The other configuration.
     * @param tolerance The tolerance for numerical comparisons.
     * @return true if the configurations are isometric.
     */
    public boolean isIsometricToViaEigenvectors(Configuration other, double tolerance) {
        // Step 1: Check if eigenvalues match
        if (!haveSameEigenvalues(other, tolerance)) {
            return false;
        }

        // Step 2: Find a candidate permutation using eigenvectors
        int[] permutation = findPermutationViaEigenvectors(other, tolerance);
        if (permutation == null) {
            return false;
        }

        // Step 3: Verify the permutation geometrically using Kabsch
        return isIsometricUnderPermutation(other, permutation, tolerance);
    }
    

   
    public static void main(String[] args)
    {
    	Configuration referenceConfig = DatabaseLoader.loadConfiguration(1056L);   	
    	System.out.println("referenceConfig:"+referenceConfig);
    	
    	Configuration comparisonConfig = DatabaseLoader.loadConfiguration(1058L);   	
    	System.out.println("comparisonConfig:"+comparisonConfig);    	
    	

        // Test the full SVD-based isometry check
        boolean isIsometricViaSVD = referenceConfig.isIsometricToViaEigenvectors(comparisonConfig, 1e-6);
        System.out.println("Isometric via SVD: " + isIsometricViaSVD);
        
        int[] permutation = referenceConfig.findPermutationViaEigenvectors(comparisonConfig, 1e-6);
        if (permutation != null) {
            double rmsd = referenceConfig.computeKabschRMSD(referenceConfig.getPoints(), comparisonConfig.getPoints());
            System.out.println("RMSD: " + rmsd);
        }
        
    }
    
}