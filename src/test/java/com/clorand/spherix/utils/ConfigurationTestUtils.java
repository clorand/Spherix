package com.clorand.spherix.utils;


import com.clorand.spherix.model.Configuration;

import main.Vec3;


public class ConfigurationTestUtils {

    /**
     * Expected adjacency matrix for dbkey=1056L (reference config).
     * Represented as a boolean[][] where true = ■ and false = □.
     */
    public static final boolean[][] EXPECTED_ADJACENCY_MATRIX_1056 = {
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
    
    /**import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;import static org.junit.jupiter.api.Assertions.assertEquals;
(-0.363507693205, 0.839242149695, 0.404394326316)
     * Expected adjacency matrix for dbkey=1056L (reference config).
     * Represented as a boolean[][] where true = ■ and false = □.	
     */
    public static final boolean[][]  EXPECTED_ADJACENCY_MATRIX_1058 = {
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

    
    public static final Vec3 a0 = new Vec3(0.000000009747, 0.000000000000, 1.000000000000);
    public static final Vec3 a1 = new Vec3(0.999131063604, 0.000000000000, 0.041678744487);
    public static final Vec3 b0 = new Vec3(-0.746250799630, 0.046407324208, -0.664045257728);
    public static final Vec3 b1 = new Vec3(-0.363507693205, 0.839242149695, 0.404394326316);
    		
    public static final double cosAlpha = 0.041678744487;
    
    		
    /**
     * Asserts that the adjacency matrix of a configuration matches             
     * the expected matrix.import static org.junit.jupiter.api.Assertions.assertEquals;
     */
    
    public static void assertAdjacencyMatrixEquals(
        Configuration config,
        boolean[][] expectedMatrix
    ) {
        boolean[][] actualMatrix = config.getAdjacencyMatrix();	
        
        org.junit.jupiter.api.Assertions.assertArrayEquals(
            new int[]{expectedMatrix.length, expectedMatrix[0].length},
            new int[]{actualMatrix.length, actualMatrix[0].length}
        );

        for (int i = 0; i < expectedMatrix.length; i++) {
            for (int j = 0; j < expectedMatrix[i].length; j++) {
           org.junit.jupiter.api.Assertions.assertEquals(
                    expectedMatrix[i][j],
                    actualMatrix[i][j]
                );
            }
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