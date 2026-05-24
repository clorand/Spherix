
package com.clorand.spherix.model;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import main.Vec3;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Color;

	public class LineMesh extends MeshView {
	    public LineMesh(Vec3 p1, Vec3 p2, double thickness, Color color) {
	        TriangleMesh mesh = new TriangleMesh();

	        // Calculate the edge vector
	        Vec3 edge = p2.sub(p1);
	        double length = edge.norm();
	        if (length < 0.0001) {
	            // Points are the same; return an empty mesh
	            this.setMesh(mesh);
	            return;
	        }

	        // Normalize the edge vector
	        Vec3 edgeDir = edge.normalize();

	        // Find a perpendicular vector
	        Vec3 perpendicular;
	        if (Math.abs(edgeDir.x) > 0.5 || Math.abs(edgeDir.y) > 0.5) {
	            perpendicular = new Vec3(0, 0, 1).cross(edgeDir).normalize();
	        } else {
	            perpendicular = new Vec3(1, 0, 0).cross(edgeDir).normalize();
	        }

	        // Calculate the 8 vertices of the cylindrical line
	        Vec3[] vertices = new Vec3[8];
	        for (int i = 0; i < 2; i++) {
	            Vec3 point = (i == 0) ? p1 : p2;
	            for (int j = 0; j < 4; j++) {
	                double angle = j * Math.PI / 2.0; // 0°, 90°, 180°, 270°
	                double dx = Math.cos(angle) * thickness / 2.0;
	                double dy = Math.sin(angle) * thickness / 2.0;
	                Vec3 offset = perpendicular.mul(dx).add(edgeDir.cross(perpendicular).mul(dy));
	                vertices[i * 4 + j] = point.add(offset);
	            }
	        }

	        // Add vertices and normals to the mesh
	        for (Vec3 v : vertices) {
	            mesh.getPoints().addAll((float) v.x, (float) v.y, (float) v.z);
	            // Add a normal (use the same vector as the point for simplicity)
	            mesh.getNormals().addAll((float) v.x, (float) v.y, (float) v.z);
	            // Add texture coordinates (optional, but can help with rendering)
	            mesh.getTexCoords().addAll(0, 0);
	        }

	        // Define the faces (triangles) for the cylindrical line
	        for (int i = 0; i < 4; i++) {
	            int next = (i + 1) % 4;
	            // Triangle 1: (i, next, i+4)
	            mesh.getFaces().addAll(
	                i, 0, next, 0, i + 4, 0
	            );
	            // Triangle 2: (next, i+4, next+4)
	            mesh.getFaces().addAll(
	                next, 0, i + 4, 0, next + 4, 0
	            );
	        }

	        this.setMesh(mesh);
	        this.setMaterial(new PhongMaterial(color));
	    }
	}
