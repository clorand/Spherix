package com.clorand.spherix.utils;

import main.Vec3;
import main.Quaternion;
import org.apache.commons.math3.linear.RealMatrix;

public class RotationUtils {

    /**
     * Constructs a rotation matrix to align a0->b0 and a1->b1.
     * @param a0 First point in reference config.
     * @param a1 Second point in reference config.
     * @param b0 First point in comparison config.
     * @param b1 Second point in comparison config.
     * @return Rotation matrix, or null if alignment is impossible.
     */
    public static RealMatrix constructRotationMatrix(Vec3 a0, Vec3 a1, Vec3 b0, Vec3 b1) {
        // Step 1: Rotate a0 to b0
        Vec3 axis0 = a0.cross(b0).normalize();
        double angle0 = Math.acos(Math.max(-1.0, Math.min(1.0, a0.dot(b0))));
        RealMatrix R0 = Quaternion.rotationMatrixFromAxisAngle(axis0, angle0);

        // Step 2: Rotate R0(a1) to b1 around b0
        Vec3 rotatedA1 = rotateVector(a1, R0);
        Vec3 rotatedA1Perp = rotatedA1.sub(b0.mul(rotatedA1.dot(b0))).normalize();
        Vec3 b1Perp = b1.sub(b0.mul(b1.dot(b0))).normalize();
        double angle1 = Math.acos(Math.max(-1.0, Math.min(1.0, rotatedA1Perp.dot(b1Perp))));
        RealMatrix R1 = Quaternion.rotationMatrixFromAxisAngle(b0, angle1);

        // Combine rotations: R = R1 * R0
        return R1.multiply(R0);
    }

    /**
     * Rotates a vector using a rotation matrix.
     */
    public static Vec3 rotateVector(Vec3 v, RealMatrix R) {
        return new Vec3(
            R.getEntry(0, 0) * v.x + R.getEntry(0, 1) * v.y + R.getEntry(0, 2) * v.z,
            R.getEntry(1, 0) * v.x + R.getEntry(1, 1) * v.y + R.getEntry(1, 2) * v.z,
            R.getEntry(2, 0) * v.x + R.getEntry(2, 1) * v.y + R.getEntry(2, 2) * v.z
        );
    }
}