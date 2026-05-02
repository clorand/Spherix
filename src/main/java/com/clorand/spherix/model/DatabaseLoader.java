package com.clorand.spherix.model;

import main.SphereParticle;
import main.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import analysis.GoodRunLoader;

public class DatabaseLoader {
	
	private static GoodRunLoader grl = new GoodRunLoader();
	
    public static List<Vec3> loadCoordinates(Long dbkey) {
        try {
            // Use GoodRunLoader to fetch the configuration for the given dbkey
            List<SphereParticle> particles = null;
			try {
				particles = grl.loadGoodRunFromDatabase(dbkey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Convert particles to List<Vec3>
			List<Vec3> coordinates = new ArrayList<Vec3>();
			
			if(null!=particles)
			{
			for (SphereParticle p:particles)
			{
				Vec3 position = p.getP();
				coordinates.add(position);
				
			}
			}
			return coordinates;
        } catch (Exception e) {
            System.err.println("Error loading configuration for dbkey=" + dbkey + ": " + e.getMessage());
            throw new RuntimeException("Failed to load configuration for dbkey=" + dbkey, e);
        }
    }
}