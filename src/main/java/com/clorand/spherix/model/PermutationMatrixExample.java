package com.clorand.spherix.model;

import org.apache.commons.math3.linear.*;
import java.util.Arrays;

public class PermutationMatrixExample {
	
    public static void main(String[] args) {
        // Original adjacency matrix (reference config)
        boolean[][] adjMatrix = {
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

        int n = adjMatrix.length;



        // Step 2: Convert adjMatrix to RealMatrix
        RealMatrix A = convertToRealMatrix(adjMatrix);
        RealMatrix Ac = convertToRealMatrix(comparisonAdjMatrix);
        
        // Step 1: Build the permutation matrix P (swap rows/columns swapI and swapJ)
        RealMatrix P = buildPermutationMatrix(n, 4, 0);
        // Step 3: Compute P^T * A * P (since P is orthogonal, P^T = P)
        RealMatrix PTranspose = P.transpose(); // P^T = P for permutation matrices
        RealMatrix PAcP = P.multiply(Ac).multiply(PTranspose);

        // Step 4: Display the results
        System.out.println("Original adjacency matrix:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(Ac);

        System.out.println("\nPermutation matrix P (swap rows/columns " + 4 + " and " + 0 + "):");
        printMatrix(P);
        
        System.out.println("\nP^T * A * P:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(PAcP);
        
        // Step 1: Build the permutation matrix P (swap rows/columns swapI and swapJ)
        int i=6;
        int j=8;
        P = buildPermutationMatrix(n, i, j);
        // Step 3: Compute P^T * A * P (since P is orthogonal, P^T = P)
        PTranspose = P.transpose(); // P^T = P for permutation matrices
        PAcP = P.multiply(PAcP).multiply(PTranspose);
        System.out.println("\nPermutation matrix P (swap rows/columns " + i + " and " + j + "):");
        printMatrix(P);     
        System.out.println("\nP^T * A * P:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(PAcP);
        
        // Step 1: Build the permutation matrix P (swap rows/columns swapI and swapJ)
        i=3;
        j=5;
        P = buildPermutationMatrix(n, i, j);
        // Step 3: Compute P^T * A * P (since P is orthogonal, P^T = P)
        PTranspose = P.transpose(); // P^T = P for permutation matrices
        PAcP = P.multiply(PAcP).multiply(PTranspose);
        System.out.println("\nPermutation matrix P (swap rows/columns " + i + " and " + j + "):");
        printMatrix(P);     
        System.out.println("\nP^T * A * P:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(PAcP);
        
        // Step 1: Build the permutation matrix P (swap rows/columns swapI and swapJ)
        i=1;
        j=2;
        P = buildPermutationMatrix(n, i, j);
        // Step 3: Compute P^T * A * P (since P is orthogonal, P^T = P)
        PTranspose = P.transpose(); // P^T = P for permutation matrices
        PAcP = P.multiply(PAcP).multiply(PTranspose);
        System.out.println("\nPermutation matrix P (swap rows/columns " + i + " and " + j + "):");
        printMatrix(P);     
        System.out.println("\nP^T * A * P:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(PAcP);

        // Step 1: Build the permutation matrix P (swap rows/columns swapI and swapJ)
        i=1;
        j=3;
        P = buildPermutationMatrix(n, i, j);
        // Step 3: Compute P^T * A * P (since P is orthogonal, P^T = P)
        PTranspose = P.transpose(); // P^T = P for permutation matrices
        PAcP = P.multiply(PAcP).multiply(PTranspose);
        System.out.println("\nPermutation matrix P (swap rows/columns " + i + " and " + j + "):");
        printMatrix(P);     
        System.out.println("\nP^T * A * P:");
        printMatrix(A);
        System.out.println("Comparison adjacency matrix:");
        printMatrix(PAcP);

        

    }

 // Builds a permutation matrix for swapping rows/columns i and j
    private static RealMatrix buildPermutationMatrix(int n, int i, int j) {
        double[][] data = new double[n][n];
        // Initialize as identity matrix
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                data[row][col] = (row == col) ? 1.0 : 0.0;
            }
        }
        // Set P_ii = P_jj = 0
        data[i][i] = 0.0;
        data[j][j] = 0.0;
        // Set P_ij = P_ji = 1
        data[i][j] = 1.0;
        data[j][i] = 1.0;

        return MatrixUtils.createRealMatrix(data);
    }

    // Converts boolean[][] to RealMatrix
    private static RealMatrix convertToRealMatrix(boolean[][] matrix) {
        int n = matrix.length;
        double[][] data = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = matrix[i][j] ? 1.0 : 0.0;
            }
        }
        return MatrixUtils.createRealMatrix(data);
    }

    // Prints a RealMatrix in a readable format with ■ and □
    private static void printMatrix(RealMatrix matrix) {
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%3s", matrix.getEntry(i, j) == 1.0 ? "■" : "□");
            }
            System.out.println();
        }
    }
}