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

            return new Configuration(dbkey, mean, points);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration for dbkey=" + dbkey, e);
        }
    }

}