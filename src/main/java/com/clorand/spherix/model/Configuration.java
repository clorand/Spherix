package com.clorand.spherix.model;

import com.clorand.spherix.utils.Vec3;
import com.clorand.spherix.utils.IsometryChecker;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a spherical configuration loaded from a database.
 */
public class Configuration {
    private final String dbkey;
    private final int n;
    private final List<Vec3> points;

    /**
     * Loads a configuration from the database by dbkey.
     * Assumes DatabaseLoader.loadCoordinates(dbkey) returns List<double[]>.
     */
    public Configuration(String dbkey) {
        this.dbkey = dbkey;
        List<double[]> coords = DatabaseLoader.loadCoordinates(dbkey);
        this.n = coords.size();
        this.points = coords.stream()
            .map(coord -> new Vec3(coord[0], coord[1], coord[2]))
            .collect(Collectors.toList());
    }

    public String getDbkey() {
        return dbkey;
    }

    public int getN() {
        return n;
    }

    public List<Vec3> getPoints() {
        return points;
    }

    /**
     * Check if this configuration is isometric to another.
     */
    public boolean isIsometricTo(Configuration other, double tolerance) {
        return IsometryChecker.isIsometric(this.points, other.points, tolerance);
    }

    @Override
    public String toString() {
        return String.format("Configuration(dbkey=%s, n=%d)", dbkey, n);
    }
}
