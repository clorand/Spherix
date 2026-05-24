package com.clorand.spherix.model;

import main.SphereParticle;
import main.Vec3;
import analysis.GoodRunLoader;
import java.util.*;
import java.util.stream.Collectors;

import com.clorand.spherix.utils.MathUtils;

public class DatabaseLoader {

    private static GoodRunLoader grl = new GoodRunLoader();

    public static Configuration loadConfiguration(Long dbkey) {
        try {
            List<SphereParticle> particles = grl.loadGoodRunFromDatabase(dbkey);
            double mean = grl.getMeanForRun(dbkey);

            if (particles == null) {
                throw new IllegalArgumentException("No particles found for dbkey=" + dbkey);
            }

            List<Vec3> points = particles.stream()
                .map(p -> p.getP())
                .collect(Collectors.toList());

            List<Edge> contactGraph = computeContactGraph(points, Math.cos(mean), 0.0001);
            return new Configuration(dbkey, mean, points, contactGraph);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration for dbkey=" + dbkey, e);
        }
    }

    private static List<Edge> computeContactGraph(List<Vec3> points, double cosAlpha, double tolerance) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double distance = points.get(i).dot(points.get(j));
                if (MathUtils.allClose(distance, cosAlpha, tolerance)) {
                    edges.add(new Edge(i, j, distance));
                }
            }
        }
        return edges;
    }
}