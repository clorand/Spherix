package com.clorand.spherix.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import main.Vec3;
import java.util.Arrays;
import java.util.List;

class MathUtilsTest {

    // Test allClose for Vec3 objects
    @Test
    void testAllCloseVec3_EqualVectors() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(1.0, 2.0, 3.0);
        assertTrue(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseVec3_WithinTolerance() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(1.00001, 2.00001, 3.00001);
        assertTrue(MathUtils.allClose(a, b, 0.001));
    }

    @Test
    void testAllCloseVec3_OutsideTolerance() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(1.1, 2.1, 3.1);
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseVec3_DifferentX() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(2.0, 2.0, 3.0);
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseVec3_DifferentY() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(1.0, 3.0, 3.0);
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseVec3_DifferentZ() {
        Vec3 a = new Vec3(1.0, 2.0, 3.0);
        Vec3 b = new Vec3(1.0, 2.0, 4.0);
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    // Test allClose for List<Vec3>
    @Test
    void testAllCloseList_EqualLists() {
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        assertTrue(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseList_WithinTolerance() {
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.00001, 2.00001, 3.00001),
            new Vec3(4.00001, 5.00001, 6.00001)
        );
        assertTrue(MathUtils.allClose(a, b, 0.001));
    }

    @Test
    void testAllCloseList_OutsideTolerance() {
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.1, 2.1, 3.1),
            new Vec3(4.1, 5.1, 6.1)
        );
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseList_DifferentSizes() {
        List<Vec3> a = Arrays.asList(new Vec3(1.0, 2.0, 3.0));
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }

    @Test
    void testAllCloseList_OneElementDifferent() {
        List<Vec3> a = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.0, 5.0, 6.0)
        );
        List<Vec3> b = Arrays.asList(
            new Vec3(1.0, 2.0, 3.0),
            new Vec3(4.1, 5.1, 6.1)
        );
        assertFalse(MathUtils.allClose(a, b, 0.0001));
    }
}