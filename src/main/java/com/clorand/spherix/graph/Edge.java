package com.clorand.spherix.graph;

//Represents an edge connecting two vertices
class Edge {
 private Vertex source;
 private Vertex target;

 public Edge(Vertex source, Vertex target) {
     this.source = source;
     this.target = target;
 }

 public Vertex getSource() {
     return source;
 }

 public Vertex getTarget() {
     return target;
 }

 // Additional properties (e.g., weight) can be added here
}