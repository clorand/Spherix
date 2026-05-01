package com.clorand.spherix.utils;
//test
import main.Vec3;
import java.util.List;

public class MathUtils {

    public static boolean allClose(Vec3 a, Vec3 b, double tolerance) {
        return Math.abs(a.x - b.x) < tolerance &&
               Math.abs(a.y - b.y) < tolerance &&
               Math.abs(a.z - b.z) < tolerance;
    }

    public static boolean allClose(List<Vec3> a, List<Vec3> b, double tolerance) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!allClose(a.get(i), b.get(i), tolerance)) return false;
        }
        return true;
    }
}