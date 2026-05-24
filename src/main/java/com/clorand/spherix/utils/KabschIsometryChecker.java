package com.clorand.spherix.utils;

import org.apache.commons.math3.linear.*;

import main.Vec3;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class KabschIsometryChecker {

    /**
     * Checks if two configurations are isometric using the Kabsch algorithm.
     * @param referencePoints   Points of the reference configuration.
     * @param comparisonPoints  Points of the comparison configuration.
     * @param A1               Adjacency matrix of the reference configuration.
     * @param A2               Adjacency matrix of the comparison configuration.
     * @param tolerance        Tolerance for RMSD comparison.
     * @return                true if the configurations are isometric.
     */
	
	public static boolean isIsometric(
		    List<Vec3> referencePoints,
		    List<Vec3> comparisonPoints,
		    int[] permutation,
		    double tolerance
		) {
	    int n = referencePoints.size();
	    if (n != comparisonPoints.size() || n != permutation.length) {
	        return false;
	    }

	    // Step 1: Reorder comparison points using the permutation
	    List<Vec3> permutedComparisonPoints = permutePoints(comparisonPoints, permutation);

	    // Step 2: Center the points (CRITICAL: Ensure centroids are zero)
	    Vec3 centroidRef = computeCentroid(referencePoints);
	    Vec3 centroidComp = computeCentroid(permutedComparisonPoints);
	    List<Vec3> centeredRef = centerPoints(referencePoints, centroidRef);
	    List<Vec3> centeredComp = centerPoints(permutedComparisonPoints, centroidComp);

	    // Debug: Print centroids (should be ~0 after centering)
	    System.out.println("Centroid of reference points (before centering): " + centroidRef);
	    System.out.println("Centroid of comparison points (before centering): " + centroidComp);
	    System.out.println("Centroid of centered reference points: " + computeCentroid(centeredRef));
	    System.out.println("Centroid of centered comparison points: " + computeCentroid(centeredComp));

	    // Step 3: Compute covariance matrix H = A^T * B
	    RealMatrix A = convertToRealMatrix(centeredRef);
	    RealMatrix B = convertToRealMatrix(centeredComp);
	    RealMatrix H = A.transpose().multiply(B);

	    // Debug: Print H
	    System.out.println("Covariance matrix H:\n" + H);

	    // Step 4: Compute SVD of H
	    SingularValueDecomposition svd = new SingularValueDecomposition(H);
	    RealMatrix U = svd.getU();
	    RealMatrix V = svd.getV();

	    // Check rank of H
	    if (svd.getRank() < 3) {
	        System.out.println("Warning: H is rank-deficient (rank = " + svd.getRank() + ").");
	        return false;
	    }

	    // Step 5: Compute rotation matrix R = V * U^T
	    RealMatrix R = V.multiply(U.transpose());

	    // Debug: Print R and its determinant
	    System.out.println("Rotation matrix R:\n" + R);
	    double det = new LUDecomposition(R).getDeterminant();
	    System.out.println("Determinant of R: " + det);

	    // Step 6: Check for reflection (det(R) should be ~1)
	    if (Math.abs(det - 1.0) > 1e-6) {
	        System.out.println("Warning: R is a reflection (det = " + det + "). Flipping last column of V.");
	        double[][] VData = V.getData();
	        for (int i = 0; i < 3; i++) {
	            VData[i][2] *= -1.0;
	        }
	        V = MatrixUtils.createRealMatrix(VData);
	        R = V.multiply(U.transpose());
	    }

	    // Step 7: Apply rotation to centered comparison points
	    List<Vec3> rotatedComparisonPoints = new ArrayList<>();
	    for (Vec3 point : centeredComp) {
	        rotatedComparisonPoints.add(rotateVector(point, R));
	    }

	    // Debug: Print RMSD before and after rotation
	    double rmsdBefore = computeRMSD(centeredRef, centeredComp);
	    double rmsdAfter = computeRMSD(centeredRef, rotatedComparisonPoints);
	    System.out.println("RMSD before rotation: " + rmsdBefore);
	    System.out.println("RMSD after rotation: " + rmsdAfter);

	    return rmsdAfter < tolerance;
	}
	
  

    // Helper: Permute points according to the permutation
    private static List<Vec3> permutePoints(List<Vec3> points, int[] permutation) {
        List<Vec3> permutedPoints = new ArrayList<>();
        for (int i = 0; i < permutation.length; i++) {
            permutedPoints.add(points.get(permutation[i]));
        }
        return permutedPoints;
    }

    // Helper: Compute centroid of points
    private static Vec3 computeCentroid(List<Vec3> points) {
        Vec3 centroid = new Vec3(0, 0, 0);
        for (Vec3 point : points) {
            centroid = centroid.add(point);
        }
        return centroid.mul(1.0 / points.size());
    }

    // Helper: Center points by subtracting centroid
    private static List<Vec3> centerPoints(List<Vec3> points, Vec3 centroid) {
        List<Vec3> centered = new ArrayList<>();
        for (Vec3 point : points) {
            centered.add(point.sub(centroid));
        }
        return centered;
    }

    // Helper: Convert List<Vec3> to RealMatrix
    private static RealMatrix convertToRealMatrix(List<Vec3> points) {
        int n = points.size();
        double[][] data = new double[n][3];
        for (int i = 0; i < n; i++) {
            Vec3 point = points.get(i);
            data[i][0] = point.x;
            data[i][1] = point.y;
            data[i][2] = point.z;
        }
        return MatrixUtils.createRealMatrix(data);
    }

    // Helper: Rotate a vector using a rotation matrix
    private static Vec3 rotateVector(Vec3 v, RealMatrix R) {
        return new Vec3(
            R.getEntry(0, 0) * v.x + R.getEntry(0, 1) * v.y + R.getEntry(0, 2) * v.z,
            R.getEntry(1, 0) * v.x + R.getEntry(1, 1) * v.y + R.getEntry(1, 2) * v.z,
            R.getEntry(2, 0) * v.x + R.getEntry(2, 1) * v.y + R.getEntry(2, 2) * v.z
        );
    }

    // Helper: Compute RMSD between two sets of points
    private static double computeRMSD(List<Vec3> a, List<Vec3> b) {
        if (a.size() != b.size()) {
            return Double.POSITIVE_INFINITY;
        }
        double sumSquaredDistances = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double dx = a.get(i).x - b.get(i).x;
            double dy = a.get(i).y - b.get(i).y;
            double dz = a.get(i).z - b.get(i).z;
            sumSquaredDistances += dx * dx + dy * dy + dz * dz;
        }
        return Math.sqrt(sumSquaredDistances / a.size());
    }
}
