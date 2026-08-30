package com.clorand.spherix.graph;

//Represents a vertex in the graph
class Vertex {
 private int id; // Unique identifier for the vertex

 public Vertex(int id) {
     this.id = id;
 }

 public int getId() {
     return id;
 }

 // Additional properties (e.g., coordinates) can be added here
}