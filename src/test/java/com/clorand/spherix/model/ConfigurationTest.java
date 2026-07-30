package com.clorand.spherix.model;


import com.clorand.spherix.utils.ConfigurationTestUtils;
import com.clorand.spherix.utils.MathUtils;
import com.clorand.spherix.utils.PermutationFinder;

import main.Quaternion;
import main.Vec3;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class ConfigurationTest {

    @Test
    public void testAdjacencyMatrixForDbKey1056() {
        // Load the configuration from the database
        Configuration config = DatabaseLoader.loadConfiguration(1056L);

        // Verify the adjacency matrix matches the expected oneQuaternion.
        ConfigurationTestUtils.assertAdjacencyMatrixEquals(
            config,
            ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1056
        );

        // Optional: Print the adjacency matrix for debugging
        System.out.println("Adjacency matrix for dbkey=1056L:");
        ConfigurationTestUtils.printAdjacencyMatrix(config.getAdjacencyMatrix());
    }
    
    @Test
    public void testAdjacencyMatrixForDbKey1058() {
        // Load the configuration from the database
        Configuration config = DatabaseLoader.loadConfiguration(1058L);

        // Verify the adjacency matrix matches the expected one
        ConfigurationTestUtils.assertAdjacencyMatrixEquals(
            config,
            ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1058
        );

        // Optional: Print the adjacency matrix for debugging
        System.out.println("Adjacency matrix for dbkey=1058L:");
        ConfigurationTestUtils.printAdjacencyMatrix(config.getAdjacencyMatrix());
    }
    
    @Test
    public void testFindPermutationBetween1056And1058() {
        // Get the expected adjacency matrices
        boolean[][] A1 = ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1056;
        boolean[][] A2 = ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1058;

        // Print the matrices for debuggingQuaternion.
        System.out.println("Reference adjacency matrix (1056L):");
        ConfigurationTestUtils.printAdjacencyMatrix(A1);
        System.out.println("\n Comparison adjacency matrix (1058L):");
        ConfigurationTestUtils.printAdjacencyMatrix(A2);

        // Find the permutation
        int[] permutation = PermutationFinder.findPermutationBruteForce(A1, A2);
        assertNotNull(permutation, "Permutation should not be null");

        System.out.println("\nFound permutation: " + Arrays.toString(permutation));

        // Verify the permutation by reconstructing the adjacency matrix
        boolean[][] reconstructedMatrix = new boolean[A1.length][A1.length];
        for (int i = 0; i < A1.length; i++) {
            for (int j = 0; j < A1.length; j++) {
                reconstructedMatrix[i][j] = A1[permutation[i]][permutation[j]];
            }
        }

        // Check if the reconstructed matrix matches A2Quaternion.
        for (int i = 0; i < A2.length; i++) {
            for (int j = 0; j < A2.length; j++) {
                assertEquals(
                    A2[i][j],
                    reconstructedMatrix[i][j],
                    String.format("Permutation does not map adjacency matrices correctly at (%d,%d)", i, j)
                );
            }        

        }

        System.out.println("Permutation is correct!");
    }
    
    @Test
    public void testTwoPairsOfPoints()
    {
        // Load the configurations from the database
        Configuration ref = DatabaseLoader.loadConfiguration(1056L);
        Configuration comp = DatabaseLoader.loadConfiguration(1058L);
        
        Vec3 a0 = ref.getPoints().get(0);
        Vec3 a1 = ref.getPoints().get(1);

        Vec3 b0 = comp.getPoints().get(4);
        Vec3 b1 = comp.getPoints().get(7);
        
        System.out.println("a0"+a0);
        System.out.println("a1"+a1);
        System.out.println("b0"+b0);
        System.out.println("b1"+b1);
        
        System.out.println("a0.a1:"+a0.dot(a1));
        System.out.println("b0.b1:"+b0.dot(b1));
             
        assertTrue(MathUtils.allClose(a0, ConfigurationTestUtils.a0, 1e-6));
        assertTrue(MathUtils.allClose(a1, ConfigurationTestUtils.a1, 1e-6));

        assertTrue(MathUtils.allClose(b0, ConfigurationTestUtils.b0, 1e-6));
        assertTrue(MathUtils.allClose(b1, ConfigurationTestUtils.b1, 1e-6));
        
        assertTrue(MathUtils.allClose(a0.dot(a1), ConfigurationTestUtils.cosAlpha , 1e-6));
        assertTrue(MathUtils.allClose(b0.dot(b1), ConfigurationTestUtils.cosAlpha , 1e-6));      
    }
    
    @Test
    public void testRotationMatrixFromA0ToB0() {
        // Define the vectors
        Vec3 a0 = new Vec3(0.000000009747, 0.000000000000, 1.000000000000);
        Vec3 b0 = new Vec3(-0.746250799630, 0.046407324208, -0.664045257728);

        // Compute the quaternion that rotates a0 to b0
        Quaternion q0 = new Quaternion(a0, b0);

        // Convert the quaternion to a rotation matrix R0
        double[][] R0Array = q0.toRotationMatrix();
        RealMatrix R0 = MatrixUtils.createRealMatrix(R0Array);

        // Print the rotation matrix R0
        System.out.println("Rotation matrix R0 (a0 -> b0):");
        System.out.println(R0.toString());

        // Apply R0 to a0: R0 * a0
        Vec3 rotatedA0 = new Vec3(
            R0.getEntry(0, 0) * a0.x + R0.getEntry(0, 1) * a0.y + R0.getEntry(0, 2) * a0.z,
            R0.getEntry(1, 0) * a0.x + R0.getEntry(1, 1) * a0.y + R0.getEntry(1, 2) * a0.z,
            R0.getEntry(2, 0) * a0.x + R0.getEntry(2, 1) * a0.y + R0.getEntry(2, 2) * a0.z
        );

        // Print the result of R0 * a0
        System.out.println("\nR0 * a0 = " + rotatedA0);

        // Verify that R0 * a0 ≈ b0 (within floating-point tolerance)
        double tolerance = 1e-6;
        assertEquals(b0.x, rotatedA0.x, tolerance, "x component of R0 * a0 should match b0.x");
        assertEquals(b0.y, rotatedA0.y, tolerance, "y component of R0 * a0 should match b0.y");
        assertEquals(b0.z, rotatedA0.z, tolerance, "z component of R0 * a0 should match b0.z");

        System.out.println("Verification: R0 * a0 ≈ b0 (success!)");
    }
    
    @Test
    public void testRotationMatrixForTwoPairs() {
        // Load the configurations from the database
        Configuration ref = DatabaseLoader.loadConfiguration(1056L);
        Configuration comp = DatabaseLoader.loadConfiguration(1058L);
    	
        Vec3 a0 = ref.getPoints().get(0);
        Vec3 a1 = ref.getPoints().get(1);

        Vec3 b0 = comp.getPoints().get(4);
        Vec3 b1 = comp.getPoints().get(7);

     // Step 1: Rotate a0 to b0 (unchanged)
        Vec3 axis0 = a0.cross(b0).normalize();
        double angle0 = Math.acos(Math.max(-1.0, Math.min(1.0, a0.dot(b0))));
        RealMatrix R0 = Quaternion.rotationMatrixFromAxisAngle(axis0, angle0);

        // Apply R0 to a0 and a1Reverse permutation : [4, 7, 1, 5, 6, 3, 8, 0, 2, 9]
        Vec3 rotatedA0 = ConfigurationTestUtils.rotateVector(a0, R0); // ≈ b0
        Vec3 rotatedA1 = ConfigurationTestUtils.rotateVector(a1, R0);

        System.out.println("rotatedA1.b0: "+rotatedA1.dot(b0));
        System.out.println("b1.b0: "+b1.dot(b0));

        // Step 2: Rotate rotatedA1 to b1 while keeping b0 fixed
     // Project rotatedA1 and b1 onto the plane perpendicular to b0
        Vec3 rotatedA1_perp = rotatedA1.sub(b0.mul(rotatedA1.dot(b0)));
        Vec3 b1_perp = b1.sub(b0.mul(b1.dot(b0)));

        // Normalize the perpendicular components
        Vec3 rotatedA1_perp_normalized = rotatedA1_perp.normalize();
        Vec3 b1_perp_normalized = b1_perp.normalize();
        double dot_perp = rotatedA1_perp_normalized.dot(b1_perp_normalized);
        double angle1 = Math.acos(Math.max(-1.0, Math.min(1.0, dot_perp)));
        
        RealMatrix R1 = Quaternion.rotationMatrixFromAxisAngle(b0, angle1);
        RealMatrix R = R1.multiply(R0); // R = R1 * R0

        // Apply the final rotation
        Vec3 finalRotatedA0 = ConfigurationTestUtils.rotateVector(a0, R); // Should match b0
        Vec3 finalRotatedA1 = ConfigurationTestUtils.rotateVector(a1, R); // Should match b1
        // check rotatedA0 should be b0
        
        assertTrue(MathUtils.allClose(rotatedA0, b0, 1e-8));
        // Final rotation: R = R1 * R0

        
        System.out.println("axis1: " + b0 + ", angle1 (rad): " + angle1);
        
        System.out.println("R0:\n" + R0);
        System.out.println("R1:\n" + R1);
        System.out.println("R (R1 * R0):\n" + R);
        System.out.println("finalRotatedA0:"+finalRotatedA0);
        System.out.println("finalRotatedA1:"+finalRotatedA1);
        
        assertTrue(MathUtils.allClose(finalRotatedA0, b0, 1e-8));
        assertTrue(MathUtils.allClose(finalRotatedA1, b1, 1e-8));
    }
    
    @Test
    public void testEntireConfigurationRotationMatrix()
    {
    	
        // Load the configurations from the database
        Configuration ref = DatabaseLoader.loadConfiguration(1056L);
        Configuration comp = DatabaseLoader.loadConfiguration(1058L);
        
        
        Vec3[] a = new Vec3[10];
        Vec3[] b = new Vec3[10];
        Vec3[] rotatedA = new Vec3[10];
        
        for (int i=0; i<10; i++)
        {
        	a[i] = ref.getPoints().get(i);
        	b[i] = comp.getPoints().get(ConfigurationTestUtils.reversePermutation[i]);
        	rotatedA[i] = ConfigurationTestUtils.rotateVector(a[i], ConfigurationTestUtils.rotationMatrix);
        	System.out.println("rotatedA["+i+"]: "+rotatedA[i]);
        }
       
        
        for (int i=0; i<10; i++)      
        {
        	assertTrue(MathUtils.allClose(rotatedA[i], b[i], 1e-8));
        	System.out.println("b["+i+"]: "+b[i]);
        }
    	
    }
    

    @Test
    public void testIsEquivalentTo() {
        // Load the known equivalent configurations
        Configuration ref = DatabaseLoader.loadConfiguration(1056L);
        Configuration comp = DatabaseLoader.loadConfiguration(1058L);

        // Check if they are equivalent with a small tolerance
        boolean isEquivalent = ref.isEquivalentTo(comp, 1e-6);

        // Assert that they are equivalent
        assertTrue(
            isEquivalent,
            "Configurations 1056L and 1058L should be equivalent."
        );
    }

}