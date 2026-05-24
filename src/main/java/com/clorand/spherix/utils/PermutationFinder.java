package com.clorand.spherix.utils;

import java.util.Arrays;

import com.clorand.spherix.model.Configuration;
import com.clorand.spherix.model.DatabaseLoader;

public class PermutationFinder {

    /**
     * Finds a permutation P such that P^T * A1 * P = A2 using brute-force search.
     * @param A1 The adjacency matrix of the first configuration.
     * @param A2 The adjacency matrix of the second configuration.
     * @return The permutation as an array, or null if no permutation exists.
     */
    public static int[] findPermutationBruteForce(boolean[][] A1, boolean[][] A2) {
        int n = A1.length;
        int[] permutation = new int[n];
        for (int i = 0; i < n; i++) {
            permutation[i] = i; // Initialize as identity permutation
        }

        return backtrack(A1, A2, permutation, 0);
    }

    /**
     * Recursive backtracking to generate and test permutations.
     */
    private static int[] backtrack(boolean[][] A1, boolean[][] A2, int[] permutation, int pos) {
        if (pos == permutation.length) {
            if (isValidPermutation(A1, A2, permutation)) {
                return permutation.clone();
            }
            return null;
        }

        for (int i = pos; i < permutation.length; i++) {
            swap(permutation, pos, i);
            int[] result = backtrack(A1, A2, permutation, pos + 1);
            if (result != null) {
                return result;
            }
            swap(permutation, pos, i); // Backtrack
        }
        return null;
    }

    /**
     * Checks if a permutation maps A1 to A2.
     */
    private static boolean isValidPermutation(boolean[][] A1, boolean[][] A2, int[] permutation) {
        int n = A1.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (A1[permutation[i]][permutation[j]] != A2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Swaps two elements in an array.
     */
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    /**
     * Main method for testing the permutation finder.
     */
    public static void main(String[] args) {
        // Load configurations
        Configuration config1 = DatabaseLoader.loadConfiguration(1056L);
        Configuration config2 = DatabaseLoader.loadConfiguration(1058L);

        // Get adjacency matrices
        boolean[][] A1 = config1.getAdjacencyMatrix();
        boolean[][] A2 = config2.getAdjacencyMatrix();

        System.out.println("Reference adjacency matrix (dbkey=1056L):");
        printAdjacencyMatrix(A1);

        System.out.println("\nComparison adjacency matrix (dbkey=1058L):");
        printAdjacencyMatrix(A2);

        // Find permutation
        long startTime = System.currentTimeMillis();
        int[] permutation = findPermutationBruteForce(A1, A2);
        long endTime = System.currentTimeMillis();

        if (permutation != null) {
            System.out.println("\nFound permutation: " + Arrays.toString(permutation));
            System.out.println("Time taken: " + (endTime - startTime) + " ms");

            // Verify the permutation
            System.out.println("\nVerifying permutation...");
            boolean[][] reconstructedMatrix = new boolean[A1.length][A1.length];
            for (int i = 0; i < A1.length; i++) {
                for (int j = 0; j < A1.length; j++) {
                    reconstructedMatrix[i][j] = A1[permutation[i]][permutation[j]];
                }
            }

            System.out.println("\nReconstructed adjacency matrix (P^T * A1 * P):");
            printAdjacencyMatrix(reconstructedMatrix);

            // Check if reconstructed matrix matches A2
            boolean matches = true;
            for (int i = 0; i < A2.length; i++) {
                for (int j = 0; j < A2.length; j++) {
                    if (reconstructedMatrix[i][j] != A2[i][j]) {
                        matches = false;
                        break;
                    }
                }
            }
            System.out.println("\nReconstructed matrix matches comparison matrix: " + matches);
        } else {
            System.out.println("\nNo permutation found. Matrices are not isomorphic.");
        }
    }

    /**
     * Prints an adjacency matrix in a readable format (■ for true, □ for false).
     */
    public static void printAdjacencyMatrix(boolean[][] matrix) {
        for (boolean[] row : matrix) {
            for (boolean val : row) {
                System.out.printf("%3s", val ? "■" : "□");
            }
            System.out.println();
        }
    }
}
