package com.clorand.spherix.model;

import main.Vec3;
import com.clorand.spherix.utils.IsometryChecker;
import java.util.List;

public class Configuration {
    private final String dbkey;
    private final int n;
    private final List<Vec3> points;

    public Configuration(String dbkey) {
        this.dbkey = dbkey;
        this.points = DatabaseLoader.loadCoordinates(dbkey); // Now returns List<Vec3>
        this.n = this.points.size();
    }

    public String getDbkey() { return dbkey; }
    public int getN() { return n; }
    public List<Vec3> getPoints() { return points; }

    public boolean isIsometricTo(Configuration other, double tolerance) {
        return IsometryChecker.isIsometric(this.points, other.points, tolerance);
    }

    @Override
    public String toString() {
        return String.format("Configuration(dbkey=%s, n=%d)", dbkey, n);
    }
}