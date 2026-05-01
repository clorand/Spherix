package com.clorand.spherix.utils;

import java.util.List;

/**
 * Utility class for numerical comparisons with tolerance.
 */
public class MathUtils {

    /**
     * Compare two Vec3 objects with a tolerance.
     */
    public static boolean allClose(Vec3 a, Vec3 b, double tolerance) {
        return Math.abs(a.x - b.x) < tolerance &&
               Math.abs(a.y - b.y) < tolerance &&
               Math.abs(a.z - b.z) < tolerance;
    }

    /**
     * Compare two double[] arrays (length 3) with a tolerance.
     */
    public static boolean allClose(double[] a, double[] b, double tolerance) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) >= tolerance) return false;
        }
        return true;
    }

    /**
     * Compare two List<Vec3> with a tolerance.
     */
    public static boolean allClose(List<Vec3> a, List<Vec3> b, double tolerance) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!allClose(a.get(i), b.get(i), tolerance)) return false;
        }
        return true;
    }
}
