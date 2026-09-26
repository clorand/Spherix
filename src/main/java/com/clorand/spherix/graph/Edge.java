package com.clorand.spherix.graph;

import java.awt.geom.Point2D;

public class Edge {
    private Vertex source;
    private Vertex target;
    private double length;
    private Point2D corner;

    public Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
        this.length = Math.abs(source.getX()-target.getX());
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }
    
    public void refresh()
    {
    	this.length = Math.abs(source.getX()-target.getX());
    }
    
    @Override
    public String toString() {
        return "Edge: " + source.getId() +"("+source.getX()+","+source.getY()+") -> " + target.getId()+"("+target.getX()+","+target.getY()+"), length:"+length;
    }

    // Override equals to compare edges as undirected (order-independent)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return (source.equals(edge.source) && target.equals(edge.target)) ||
               (source.equals(edge.target) && target.equals(edge.source));
    }

    // Override hashCode to maintain consistency with equals
    @Override
    public int hashCode() {
        // Use a hash that is order-independent for undirected edges
        return source.hashCode() + target.hashCode();
    }

	public Point2D getCorner() {
		return corner;
	}

	public void setCorner(Point2D corner) {
		this.corner = corner;
	}
}