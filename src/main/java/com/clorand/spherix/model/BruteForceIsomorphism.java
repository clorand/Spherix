package com.clorand.spherix.model;

import java.util.Arrays;

public class BruteForceIsomorphism {
    public static void main(String[] args) {
        // Original adjacency matrix (reference config)
        boolean[][] originalAdjMatrix = {
            {false, false, false, false, false, true,  true,  false, false, true }, // 0
            {false, false, false, false, false, true,  true,  true,  true,  false}, // 1
            {false, false, false, false, true,  false, true,  false, true,  true }, // 2
            {false, false, false, false, true,  true,  false, true,  false, true }, // 3
            {false, false, true,  true,  false, false, false, true,  true,  false}, // 4
            {true,  true,  false, true,  false, false, false, true,  false, false}, // 5
            {true,  true,  true,  false, false, false, false, false, true,  false}, // 6
            {false, true,  false, true,  true,  true,  false, false, false, false}, // 7
            {false, true,  true,  false, true,  false, true,  false, false, false}, // 8
            {true,  false, true,  true,  false, false, false, false, false, false}  // 9
        };

        // Comparison adjacency matrix
        boolean[][] comparisonAdjMatrix = {
            {false, false, false, true,  false, true,  true,  true,  false, false}, // 0
            {false, false, true,  false, false, false, true,  false, true,  true }, // 1
            {false, true,  false, false, false, false, true,  true,  true,  false}, // 2
            {true,  false, false, false, true,  true,  false, true,  false, false}, // 3
            {false, false, false, true,  false, false, false, false, true,  true }, // 4
            {true,  false, false, true,  false, false, true,  false, false, true }, // 5
            {true,  true,  true,  false, false, true,  false, false, false, false}, // 6
            {true,  false, true,  true,  false, false, false, false, true,  false}, // 7
            {false, true,  true,  false, true,  false, false, true,  false, false}, // 8
            {false, true,  false, false, true,  true,  false, false, false, false}  // 9
        };

        // Find the permutation
        int[] permutation = findIsomorphismPermutation(originalAdjMatrix, comparisonAdjMatrix);

        if (permutation != null) {
            System.out.println("Found permutation: " + Arrays.toString(permutation));
            System.out.println("Verification:");
            verifyPermutation(originalAdjMatrix, comparisonAdjMatrix, permutation);
        } else {
            System.out.println("No permutation found. Matrices are not isomorphic.");
        }
    }

    /**
     * Finds a permutation P such that P^T * originalAdjMatrix * P = comparisonAdjMatrix.
     * Uses brute-force search over all permutations (backtracking).
     */
    public static int[] findIsomorphismPermutation(boolean[][] original, boolean[][] comparison) {
        int n = original.length;
        int[] permutation = new int[n];
        for (int i = 0; i < n; i++) {
            permutation[i] = i; // Initialize as identity permutation
        }

        return backtrack(original, comparison, permutation, 0);
    }

    /**
     * Recursive backtracking to generate and test permutations.
     */
    private static int[] backtrack(boolean[][] original, boolean[][] comparison, int[] permutation, int pos) {
        if (pos == permutation.length) {
            // Test if this permutation works
            if (isValidPermutation(original, comparison, permutation)) {
                return permutation.clone(); // Return a copy of the valid permutation
            }
            return null;
        }

        for (int i = pos; i < permutation.length; i++) {
            // Swap permutation[pos] and permutation[i]
            swap(permutation, pos, i);

            // Recurse
            int[] result = backtrack(original, comparison, permutation, pos + 1);
            if (result != null) {
                return result; // Found a valid permutation
            }

            // Backtrack (undo the swap)
            swap(permutation, pos, i);
        }
        return null; // No valid permutation found
    }

    /**findPermutationViaEigenvectors
     * Swaps two elements in an array.
     */
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Checks if a permutation maps the original adjacency matrix to the comparison matrix.
     */
    private static boolean isValidPermutation(boolean[][] original, boolean[][] comparison, int[] permutation) {
        int n = original.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (original[permutation[i]][permutation[j]] != comparison[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**findPermutationViaEigenvectors
     * Verifies and prints the result of applying the permutation to the original matrix.
     */
    private static void verifyPermutation(boolean[][] original, boolean[][] comparison, int[] permutation) {
        int n = original.length;
        boolean[][] transformed = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transformed[i][j] = original[permutation[i]][permutation[j]];
            }
        }

        System.out.println("\noriginal matrix");
        printMatrix(original);
        System.out.println("\nTransformed original matrix (P^T * original * P):");
        printMatrix(transformed);

        System.out.println("\nComparison matrix:");
        printMatrix(comparison);

        // Check if they match
        boolean matches = true;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (transformed[i][j] != comparison[i][j]) {
                    matches = false;
                    break;
                }
            }
        }
        System.out.println("\nMatrices match: " + matches);
    }

    /**
     * Prints a boolean matrix using ■ and □.
     */
    private static void printMatrix(boolean[][] matrix) {
        for (boolean[] row : matrix) {
            for (boolean val : row) {
                System.out.printf("%3s", val ? "■" : "□");
            }
            System.out.println();
        }
    }
}