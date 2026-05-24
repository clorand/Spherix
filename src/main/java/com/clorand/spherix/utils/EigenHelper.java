package com.clorand.spherix.utils;

import org.apache.commons.math3.linear.*;
import java.util.Arrays;
import java.util.Comparator;

public class EigenHelper {

    /**
     * Computes the eigenvalue decomposition of a symmetric boolean adjacency matrix.
     * @param adjMatrix The adjacency matrix (boolean[][]).
     * @return A pair containing (Λ, Q) as RealMatrix objects, where Λ is diagonal and Q is orthogonal.
     */
    public static Pair<RealMatrix, RealMatrix> computeEigenDecomposition(boolean[][] adjMatrix) {
        RealMatrix matrix = convertToRealMatrix(adjMatrix);
        EigenDecomposition eig = new EigenDecomposition(matrix);

        // Get eigenvalues and eigenvectors
        RealMatrix D = eig.getD(); // Diagonal matrix of eigenvalues
        RealMatrix Q = eig.getV(); // Matrix of eigenvectors (columns)

        // Sort eigenvalues and eigenvectors in descending order
        double[] eigenvalues = D.getData()[0]; // Eigenvalues are on the diagonal
        Integer[] sortedIndices = sortIndicesByValues(eigenvalues, false); // false = descending

        // Reorder D and Q to match sorted eigenvalues
        RealMatrix sortedD = reorderDiagonal(D, sortedIndices);
        RealMatrix sortedQ = reorderColumns(Q, sortedIndices);

        // Fix signs of eigenvectors
        sortedQ = fixSigns(sortedQ);

        return new Pair<>(sortedD, sortedQ);
    }

    /**
     * Converts a boolean[][] adjacency matrix to a RealMatrix.
     */
    private static RealMatrix convertToRealMatrix(boolean[][] adjMatrix) {
        int n = adjMatrix.length;
        double[][] data = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = adjMatrix[i][j] ? 1.0 : 0.0;
            }
        }
        return MatrixUtils.createRealMatrix(data);
    }

    /**
     * Extracts the eigenvalues from the diagonal matrix Λ.
     */
    public static double[] extractEigenvalues(RealMatrix D) {
        int n = D.getRowDimension();
        double[] eigenvalues = new double[n];
        for (int i = 0; i < n; i++) {
            eigenvalues[i] = D.getEntry(i, i);
        }
        return eigenvalues;
    }

    /**
     * Checks if two adjacency matrices have the same eigenvalues (up to sorting and tolerance).
     */
    public static boolean haveSameEigenvalues(
        boolean[][] adjMatrix1,
        boolean[][] adjMatrix2,
        double tolerance
    ) {
        Pair<RealMatrix, RealMatrix> eig1 = computeEigenDecomposition(adjMatrix1);
        Pair<RealMatrix, RealMatrix> eig2 = computeEigenDecomposition(adjMatrix2);

        double[] eigenvalues1 = extractEigenvalues(eig1.getLeft());
        double[] eigenvalues2 = extractEigenvalues(eig2.getLeft());

        // Sort eigenvalues (descending order)
        Arrays.sort(eigenvalues1);
        Arrays.sort(eigenvalues2);
        reverseArray(eigenvalues1);
        reverseArray(eigenvalues2);

        // Compare eigenvalues
        if (eigenvalues1.length != eigenvalues2.length) {
            return false;
        }
        for (int i = 0; i < eigenvalues1.length; i++) {
            if (Math.abs(eigenvalues1[i] - eigenvalues2[i]) > tolerance) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the product Q2^T * Q1 and checks if it's a signed permutation matrix.
     * @return The permutation array if the product is a signed permutation matrix, or null otherwise.
     */
    public static int[] getPermutationFromEigenvectors(
        boolean[][] adjMatrix1,
        boolean[][] adjMatrix2,
        double tolerance
    ) {
        Pair<RealMatrix, RealMatrix> eig1 = computeEigenDecomposition(adjMatrix1);
        Pair<RealMatrix, RealMatrix> eig2 = computeEigenDecomposition(adjMatrix2);

        RealMatrix Q1 = eig1.getRight(); // Eigenvectors for matrix1 (sorted and sign-fixed)
        RealMatrix Q2 = eig2.getRight(); // Eigenvectors for matrix2 (sorted and sign-fixed)

        // Compute product = Q2^T * Q1
        RealMatrix product = Q2.transpose().multiply(Q1);

        int n = product.getRowDimension();
        int[] permutation = new int[n];
        Arrays.fill(permutation, -1); // Initialize with -1 (invalid)

        // Check if product is a signed permutation matrix
        for (int i = 0; i < n; i++) {
            int countOnes = 0;
            int candidateJ = -1;
            for (int j = 0; j < n; j++) {
                double val = product.getEntry(i, j);
                if (Math.abs(val) > 0.9) { // Allow for numerical errors
                    countOnes++;
                    candidateJ = j;
                } else if (Math.abs(val) > tolerance) {
                    return null; // Not a signed permutation matrix
                }
            }
            if (countOnes != 1) {
                return null; // Not a signed permutation matrix
            }
            permutation[i] = candidateJ;
        }

        // Verify the permutation is valid (no duplicates)
        boolean[] used = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (permutation[i] < 0 || permutation[i] >= n || used[permutation[i]]) {
                return null;
            }
            used[permutation[i]] = true;
        }

        return permutation;
    }

    /**
     * Fixes the signs of the columns in a matrix by forcing the first non-zero entry to be positive.
     */
    private static RealMatrix fixSigns(RealMatrix matrix) {
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        double[][] data = matrix.getData();
        for (int j = 0; j < cols; j++) {
            // Find the first non-zero entry in column j
            int firstNonZeroRow = -1;
            for (int i = 0; i < rows; i++) {
                if (Math.abs(data[i][j]) > 1e-10) {
                    firstNonZeroRow = i;
                    break;
                }
            }
            if (firstNonZeroRow >= 0 && data[firstNonZeroRow][j] < 0) {
                // Flip the sign of the column
                for (int i = 0; i < rows; i++) {
                    data[i][j] *= -1;
                }
            }
        }
        return MatrixUtils.createRealMatrix(data);
    }

    /**
     * Sorts indices by the corresponding values in descending or ascending order.
     */
    private static Integer[] sortIndicesByValues(double[] values, boolean ascending) {
        Integer[] indices = new Integer[values.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        Comparator<Integer> comparator = (i, j) -> {
            double diff = values[i] - values[j];
            return ascending ? Double.compare(diff, 0) : Double.compare(-diff, 0);
        };
        Arrays.sort(indices, comparator);
        return indices;
    }

    /**
     * Reorders the columns of a matrix according to the given indices.
     */
    private static RealMatrix reorderColumns(RealMatrix matrix, Integer[] newOrder) {
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        double[][] newData = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newData[i][j] = matrix.getEntry(i, newOrder[j]);
            }
        }
        return MatrixUtils.createRealMatrix(newData);
    }

    /**
     * Reorders the diagonal entries of a diagonal matrix according to the given indices.
     */
    private static RealMatrix reorderDiagonal(RealMatrix D, Integer[] newOrder) {
        int n = D.getRowDimension();
        double[][] newData = new double[n][n];
        for (int i = 0; i < n; i++) {
            newData[i][i] = D.getEntry(newOrder[i], newOrder[i]);
        }
        return MatrixUtils.createRealMatrix(newData);
    }

    /**
     * Reverses an array (for descending order).
     */
    private static void reverseArray(double[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            double temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    /**
     * Simple pair class to hold (Λ, Q) from eigenvalue decomposition.
     */
    public static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getLeft() { return first; }
        public B getRight() { return second; }
    }
}