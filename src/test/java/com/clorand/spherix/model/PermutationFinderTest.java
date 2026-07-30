package com.clorand.spherix.model;

import com.clorand.spherix.utils.ConfigurationTestUtils;
import com.clorand.spherix.utils.MathUtils;
import com.clorand.spherix.utils.PermutationFinder;
import com.clorand.spherix.utils.RotationUtils;

import main.Quaternion;
import main.Vec3;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class PermutationFinderTest {
	
	@Test
	public void configurationDbKey1056()
	{
		long dbKey = 1056L;
		
        // Load the configuration from the database
        Configuration config = DatabaseLoader.loadConfiguration(dbKey);
        List<Vec3> points = config.getPoints();     
        System.out.println(points);
        
        // Optional: Print the adjacency matrix for debugging
        System.out.println("Adjacency matrix for dbkey="+dbKey);
        ConfigurationTestUtils.printAdjacencyMatrix(config.getAdjacencyMatrix());
        
        System.out.println("Degrees: "+dbKey+config.getDegrees());
        
        assert(true);
	}
	
    @Test
    public void testAdjacencyMatrixForDbKey1059() {
    	
		long dbKey = 1059L;
    	
        // Load the configuration from the database
        Configuration config = DatabaseLoader.loadConfiguration(dbKey);

        System.out.println("Degrees: "+dbKey+config.getDegrees());

        // Verify the adjacency matrix matches the expected oneQuaternion.
        ConfigurationTestUtils.assertAdjacencyMatrixEquals(
            config,
            ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1059
        );

        // Optional: Print the adjacency matrix for debugging
        System.out.println("Adjacency matrix for dbkey="+dbKey);
        ConfigurationTestUtils.printAdjacencyMatrix(config.getAdjacencyMatrix());
    }

	 @Test
	    public void testFindPermutationBetween1056And1059() {
	        // Get the expected adjacency matrices
	        boolean[][] A1 = ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1056;
	        boolean[][] A2 = ConfigurationTestUtils.EXPECTED_ADJACENCY_MATRIX_1059;

	        // Print the matrices for debuggingQuaternion.
	        System.out.println("Reference adjacency matrix (1056L):");
	        ConfigurationTestUtils.printAdjacencyMatrix(A1);
	        System.out.println("\n Comparison adjacency matrix (1059L):");
	        ConfigurationTestUtils.printAdjacencyMatrix(A2);

	        // Find the permutation
	        int[] permutation = PermutationFinder.findPermutationBruteForce(A1, A2);
	        assertNotNull(permutation, "Permutation should not be null");

	        System.out.println("\nFound permutation: " + Arrays.toString(permutation));
	        
	        // Step 2: Compute reverse permutation
	        int n = permutation.length;
	        int[] reversePermutation = new int[n];
	        for (int i = 0; i < n; i++) {
	            reversePermutation[permutation[i]] = i;
	        }

	        System.out.println("\nReverse permutation: " + Arrays.toString(reversePermutation));

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
	    public void testRotationMatrixForTwoPairs() {
	        // Load the configurations from the database
	        Configuration ref = DatabaseLoader.loadConfiguration(1056L);
	        Configuration comp = DatabaseLoader.loadConfiguration(1059L);
	    	
	        // Match degree 3 points
	        //Degrees: 1056{0=3, 1=4, 2=4, 3=4, 4=4, 5=4, 6=4, 7=4, 8=4, 9=3}
	        //Degrees: 1059{0=4, 1=4, 2=4, 3=4, 4=4, 5=4, 6=4, 7=4, 8=3, 9=3}
	        
	        
	        Vec3 a0 = ref.getPoints().get(9);
	        Vec3 a1 = ref.getPoints().get(0);

	        Vec3 b0 = comp.getPoints().get(8);
	        Vec3 b1 = comp.getPoints().get(9);
	        
	        System.out.println("a0:"+a0);
	        System.out.println("a1:"+a1);

	        System.out.println("b0:"+b0);
	        System.out.println("b1:"+b1);

	        // Step 4: Construct the rotation matrix (R: a0→b0, a1→b1)
	        RealMatrix rotationMatrix = RotationUtils.constructRotationMatrix(a0, a1, b0, b1);

	        // Apply the final rotation
	        Vec3 finalRotatedA0 = ConfigurationTestUtils.rotateVector(a0, rotationMatrix); // Should match b0
	        Vec3 finalRotatedA1 = ConfigurationTestUtils.rotateVector(a1, rotationMatrix); // Should match b1
	        // check rotatedA0 should be b0



	        System.out.println("finalRotatedA0:"+finalRotatedA0);
	        System.out.println("finalRotatedA1:"+finalRotatedA1);
	        
	        assertTrue(MathUtils.allClose(finalRotatedA0, b0, 1e-8));
	        assertTrue(MathUtils.allClose(finalRotatedA1, b1, 1e-8));
	        /*
	        int reversePermutation[] = {8, 1, 0, 4, 3, 7, 6, 5, 2, 9};
	        
	        Vec3[] a = new Vec3[10];
	        Vec3[] b = new Vec3[10];
	        Vec3[] rotatedA = new Vec3[10];
	        
	        for (int i=0; i<10; i++)
	        {
	        	a[i] = ref.getPoints().get(i);
	        	b[i] = comp.getPoints().get(reversePermutation[i]);
	        	rotatedA[i] = ConfigurationTestUtils.rotateVector(a[i], ConfigurationTestUtils.rotationMatrix);
	        	System.out.println("rotatedA["+i+"]: "+rotatedA[i]);
	        }
	       
	       
	        for (int i=0; i<10; i++)      
	        {
	        	//assertTrue(MathUtils.allClose(rotatedA[i], b[i], 1e-8));
	        	System.out.println("b["+i+"]: "+b[i]);
	        }

	        assertTrue(MathUtils.allClose(rotatedA[0], b[9], 1e-8));
	        */
	    }
	
}
