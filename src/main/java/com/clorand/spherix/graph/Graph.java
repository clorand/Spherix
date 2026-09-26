package com.clorand.spherix.graph;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.*;
import java.util.*;

public class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<Integer, Vertex> vertexMap; // For quick lookup by ID

    public enum ExtendDirection {
        HORIZONTAL,
        VERTICAL
    }
    
    public enum BoundaryDirection {
        LEFT,   // Leftmost vertex (smallest x-coordinate)
        RIGHT,  // Rightmost vertex (largest x-coordinate)
        UP,     // Northmost vertex (largest y-coordinate)
        DOWN    // Southmost vertex (smallest y-coordinate)
    }
    
    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.vertexMap = new HashMap<>();
    }

    // Add a vertex to the graph
    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
        vertexMap.put(vertex.getId(), vertex);
    }

    // Add an edge to the graph
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    // Get all vertices
    public List<Vertex> getVertices() {
        return new ArrayList<>(vertices);
    }

    // Get all edges
    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    // Get a vertex by its ID
    public Vertex getVertexById(int id) {
        return vertexMap.get(id);
    }

    // Helper method to parse edge pairs from a string
    private static List<int[]> parseEdgePairs(String graphStr) {
        List<int[]> edgePairs = new ArrayList<>();
        String[] pairs = graphStr.replaceAll("[()]", "").split(",\\s*");
        for (int i = 0; i < pairs.length; i += 2) {
            int sourceId = Integer.parseInt(pairs[i].trim());
            int targetId = Integer.parseInt(pairs[i + 1].trim());
            edgePairs.add(new int[]{sourceId, targetId});
        }
        return edgePairs;
    }

    // Build a graph from a string (without coordinates)
    public static Graph fromString(String graphStr) {
        Graph graph = new Graph();
        List<int[]> edgePairs = parseEdgePairs(graphStr);

        // Add vertices with default coordinates (0, 0)
        Set<Integer> vertexIds = new HashSet<>();
        for (int[] pair : edgePairs) {
            vertexIds.add(pair[0]);
            vertexIds.add(pair[1]);
        }
        for (int id : vertexIds) {
            graph.addVertex(new Vertex(id));
        }

        // Add edges
        for (int[] pair : edgePairs) {
            int sourceId = pair[0];
            int targetId = pair[1];
            Vertex source = graph.getVertexById(sourceId);
            Vertex target = graph.getVertexById(targetId);
            graph.addEdge(new Edge(source, target));
        }
        return graph;
    }

    // Build a graph from a string with coordinates
    public static Graph fromString(String graphStr, Map<Integer, double[]> coordinates) {
        Graph graph = new Graph();
        List<int[]> edgePairs = parseEdgePairs(graphStr);

        // Add vertices with coordinates
        for (int id : coordinates.keySet()) {
            double[] coord = coordinates.get(id);
            graph.addVertex(new Vertex(id, coord[0], coord[1]));
        }

        // Add edges
        for (int[] pair : edgePairs) {
            int sourceId = pair[0];
            int targetId = pair[1];
            Vertex source = graph.getVertexById(sourceId);
            Vertex target = graph.getVertexById(targetId);
            graph.addEdge(new Edge(source, target));
        }
        return graph;
    }

    // Compute the adjacency matrix of the graph
    public int[][] getAdjacencyMatrix() {
        int n = vertices.size();
        int[][] adjacencyMatrix = new int[n][n];

        // Initialize all entries to 0
        for (int i = 0; i < n; i++) {
            Arrays.fill(adjacencyMatrix[i], 0);
        }

        // Populate the adjacency matrix for undirected graph
        for (Edge edge : edges) {
            int sourceIndex = edge.getSource().getId();
            int targetIndex = edge.getTarget().getId();
            adjacencyMatrix[sourceIndex][targetIndex] = 1;
            adjacencyMatrix[targetIndex][sourceIndex] = 1; // Undirected graph
        }

        return adjacencyMatrix;
    }

    // Compute the Laplacian matrix of the graph
    public int[][] getLaplacianMatrix() {
        int n = vertices.size();
        int[][] adjacencyMatrix = getAdjacencyMatrix();
        int[][] laplacianMatrix = new int[n][n];

        // Compute the degree of each vertex
        int[] degrees = new int[n];
        for (Edge edge : edges) {
            degrees[edge.getSource().getId()]++;
            degrees[edge.getTarget().getId()]++;
        }

        // Populate the Laplacian matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    laplacianMatrix[i][j] = degrees[i] - adjacencyMatrix[i][j];
                } else {
                    laplacianMatrix[i][j] = -adjacencyMatrix[i][j];
                }
            }
        }

        return laplacianMatrix;
    }

 // Helper method to print a matrix
    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
    
    // Compute the cyclic order of neighbors around each vertex
    public Map<Integer, List<Integer>> computeCyclicOrder() {
        Map<Integer, List<Integer>> cyclicOrder = new HashMap<>();

        for (Vertex vertex : vertices) {
            int v = vertex.getId();
            List<Integer> neighbors = new ArrayList<>();

            // Collect all neighbors of vertex v
            for (Edge edge : edges) {
                if (edge.getSource().getId() == v) {
                    neighbors.add(edge.getTarget().getId());
                } else if (edge.getTarget().getId() == v) {
                    neighbors.add(edge.getSource().getId());
                }
            }

            // Sort neighbors by polar angle around vertex v
            neighbors.sort((u1, u2) -> {
                Vertex v1 = getVertexById(u1);
                Vertex v2 = getVertexById(u2);
                double angle1 = Math.atan2(v1.getY() - vertex.getY(), v1.getX() - vertex.getX());
                double angle2 = Math.atan2(v2.getY() - vertex.getY(), v2.getX() - vertex.getX());
                return Double.compare(angle2, angle1); // Sort in clockwise order
            });

            cyclicOrder.put(v, neighbors);
        }

        return cyclicOrder;
    }

 // Find all faces in the planar embedding
    public List<Face> findFaces() {
        // Check if the graph is embedded (all vertices have coordinates)
        if (!isEmbedded()) {
            throw new IllegalStateException("Graph is not embedded. Cannot find faces.");
        }

        Map<Integer, List<Integer>> cyclicOrder = computeCyclicOrder();

        // Create a set of directed edges
        Set<String> directedEdges = new HashSet<>();
        for (Edge edge : edges) {
            int u = edge.getSource().getId();
            int v = edge.getTarget().getId();
            directedEdges.add(u + "," + v);
            directedEdges.add(v + "," + u);
        }

        Set<String> visited = new HashSet<>();
        List<Face> faces = new ArrayList<>();

        for (String startEdge : directedEdges) {
            if (visited.contains(startEdge)) {
                continue;
            }

            List<Vertex> faceVertices = new ArrayList<>();
            List<Edge> faceEdges = new ArrayList<>();
            String currentEdge = startEdge;
            String[] edgeParts = currentEdge.split(",");
            int uId = Integer.parseInt(edgeParts[0]);
            int vId = Integer.parseInt(edgeParts[1]);

            Vertex u = getVertexById(uId);
            Vertex v = getVertexById(vId);

            // Start traversing the face
            while (!visited.contains(currentEdge)) {
                visited.add(currentEdge);
                faceVertices.add(u);
                faceEdges.add(new Edge(u, v));

                // Get the cyclic order of neighbors around v
                List<Integer> neighbors = cyclicOrder.get(v.getId());
                if (neighbors == null || neighbors.isEmpty()) {
                    break; // No neighbors to traverse
                }

                // Find the index of u in the neighbors list
                int index = neighbors.indexOf(u.getId());
                if (index == -1) {
                    break; // u is not a neighbor of v (should not happen for valid graphs)
                }

                // Take the previous edge in cyclic order (left-hand rule)
                int nextNeighborId = neighbors.get((index - 1 + neighbors.size()) % neighbors.size());
                Vertex nextNeighbor = getVertexById(nextNeighborId);

                // Move to the next directed edge
                currentEdge = v.getId() + "," + nextNeighbor.getId();
                edgeParts = currentEdge.split(",");
                uId = Integer.parseInt(edgeParts[0]);
                vId = Integer.parseInt(edgeParts[1]);
                u = getVertexById(uId);
                v = getVertexById(vId);
            }

            // Remove consecutive duplicates (but preserve the start/end vertex)
            List<Vertex> uniqueFaceVertices = new ArrayList<>();
            for (Vertex vertex : faceVertices) {
                if (uniqueFaceVertices.isEmpty() || !uniqueFaceVertices.get(uniqueFaceVertices.size() - 1).equals(vertex)) {
                    uniqueFaceVertices.add(vertex);
                }
            }

            // Remove duplicate edges
            List<Edge> uniqueFaceEdges = new ArrayList<>();
            for (Edge edge : faceEdges) {
                if (uniqueFaceEdges.isEmpty() || !uniqueFaceEdges.get(uniqueFaceEdges.size() - 1).equals(edge)) {
                    uniqueFaceEdges.add(edge);
                }
            }

            // Only add the face if it has at least 3 unique vertices
            if (uniqueFaceVertices.size() >= 3) {
                Face face = new Face(uniqueFaceVertices, uniqueFaceEdges);
                faces.add(face);
            }
        }

        // Sort faces by the x-coordinate of their center of mass
        faces.sort((face1, face2) -> {
            double x1 = face1.getCenterOfMass().x;
            double x2 = face2.getCenterOfMass().x;
            return Double.compare(x1, x2);
        });

        return faces;
    }


    // Helper method to check if the graph is embedded (all vertices have coordinates)
    private boolean isEmbedded() {
        for (Vertex vertex : vertices) {
            if (vertex.getX() == 0.0 && vertex.getY() == 0.0) {
                // Assuming (0, 0) is the default coordinate for non-embedded vertices
                return false;
            }
        }
        return true;
    }    
 // Get the boundary vertex based on the specified direction
    public Vertex getBoundaryVertex(BoundaryDirection direction) {
        if (vertices.isEmpty()) {
            return null;
        }

        switch (direction) {
            case LEFT:
                return vertices.stream()
                        .min(Comparator.comparingDouble(Vertex::getX))
                        .orElse(null);
            case RIGHT:
                return vertices.stream()
                        .max(Comparator.comparingDouble(Vertex::getX))
                        .orElse(null);
            case UP:
                return vertices.stream()
                        .max(Comparator.comparingDouble(Vertex::getY))
                        .orElse(null);
            case DOWN:
                return vertices.stream()
                        .min(Comparator.comparingDouble(Vertex::getY))
                        .orElse(null);
            default:
                return null;
        }
    }
    
    public void extend(ExtendDirection direction) {
        if (vertices.isEmpty()) {
            return; // No vertices to extend
        }

        Vertex leftmost = getBoundaryVertex(BoundaryDirection.LEFT);
        Vertex rightmost = getBoundaryVertex(BoundaryDirection.RIGHT);
        Vertex northmost = getBoundaryVertex(BoundaryDirection.UP);
        Vertex southmost = getBoundaryVertex(BoundaryDirection.DOWN);

        if (direction == ExtendDirection.HORIZONTAL) {
            // Add left and right vertices (6 and 7)
            Vertex left = new Vertex(vertices.size(), leftmost.getX() - 1, leftmost.getY());
            Vertex right = new Vertex(vertices.size() + 1, rightmost.getX() + 1, rightmost.getY());
            addVertex(left);
            addVertex(right);

            // Add edges: 6-5, 6-0, 7-5, 7-0
            addEdge(new Edge(left, southmost));   // 6-5
            addEdge(new Edge(left, northmost));   // 6-0
            addEdge(new Edge(right, southmost));  // 7-5
            addEdge(new Edge(right, northmost));  // 7-0

        } else if (direction == ExtendDirection.VERTICAL) {
            // Add top and bottom vertices
            Vertex top = new Vertex(vertices.size(), northmost.getX(), northmost.getY() + 1);
            Vertex bottom = new Vertex(vertices.size() + 1, southmost.getX(), southmost.getY() - 1);
            addVertex(top);
            addVertex(bottom);

            // Add edges to connect the new vertices to the leftmost and rightmost vertices
            addEdge(new Edge(top, leftmost));     // Top to leftmost
            addEdge(new Edge(top, rightmost));    // Top to rightmost
            addEdge(new Edge(bottom, leftmost));  // Bottom to leftmost
            addEdge(new Edge(bottom, rightmost)); // Bottom to rightmost
        }
    }
    
 // Get the list of faces that contain the boundary vertex (e.g., leftmost, rightmost, etc.)
    public List<Face> getBoundaryFaces(BoundaryDirection direction) {
        Vertex boundaryVertex = getBoundaryVertex(direction);
        if (boundaryVertex == null) {
            return new ArrayList<>(); // No boundary vertex found
        }

        List<Face> boundaryFaces = new ArrayList<>();
        List<Face> allFaces = findFaces();

        for (Face face : allFaces) {
            if (face.containsVertex(boundaryVertex)) {
                boundaryFaces.add(face);
            }
        }

        return boundaryFaces;
    }
    
    
    public RealVector stretch(ExtendDirection direction, Vertex start, Vertex end) {
        if (vertices.isEmpty() || start == null || end == null) {
            return null; // No vertices or invalid start/end
        }

        // Step 1: Fix start position = 0 and end position = 1
        int n = vertices.size();
        int startIndex = start.getId();
        int endIndex = end.getId();

        // Step 2: Extract the free vertices Laplacian matrix by removing the fixed positions
        int[][] laplacianMatrixInt = getLaplacianMatrix();

        // Convert int[][] to double[][] for RealMatrix
        double[][] laplacianMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                laplacianMatrix[i][j] = laplacianMatrixInt[i][j]; // Manual copy
            }
        }

        // Create RealMatrix for the full Laplacian
        RealMatrix laplacian = MatrixUtils.createRealMatrix(laplacianMatrix);

        // Identify free vertices (all vertices except start and end)
        List<Integer> freeIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i != startIndex && i != endIndex) {
                freeIndices.add(i);
            }
        }

        // Extract the submatrix for free vertices (L_ff)
        int m = freeIndices.size();
        int[] freeIndicesArray = freeIndices.stream().mapToInt(Integer::intValue).toArray();
        RealMatrix freeLaplacian = laplacian.getSubMatrix(freeIndicesArray, freeIndicesArray);

        // Step 3: Compute the comatrix (adjugate) of the free Laplacian matrix
        RealMatrix comatrix = computeAdjugate(freeLaplacian);

        // Step 4: Compute the fixed positions vector (b_free)
        // Extract the submatrix L_fk (rows: free vertices, columns: fixed vertices)
        int[] fixedIndices = {startIndex, endIndex};
        RealMatrix L_fk = laplacian.getSubMatrix(freeIndicesArray, fixedIndices);

        // Compute b_free = -L_fk * [0; 1] = -L_fk[:, end] (since x_start = 0)
        double[] bFree = new double[m];
        for (int i = 0; i < m; i++) {
            bFree[i] = -L_fk.getEntry(i, 1); // endIndex is 1 in the fixed vector [0; 1]
        }

        // Step 5: Compute stretched positions: x_free = comatrix * b_free
        RealVector stretchedPositions = comatrix.operate(new ArrayRealVector(bFree));
        

        // Step 6: Update the coordinates of the free vertices
        for (int i = 0; i < m; i++) {
            int vertexId = freeIndices.get(i);
            Vertex vertex = getVertexById(vertexId);
            double stretchedValue = stretchedPositions.getEntry(i);

            if (direction == ExtendDirection.HORIZONTAL) {
                vertex.setX(stretchedValue);
            } else if (direction == ExtendDirection.VERTICAL) {
                vertex.setY(stretchedValue);
            }
        }
        
        // Step 7: Compute the determinant of free Laplacian
        double detL_ff = new LUDecomposition(freeLaplacian).getDeterminant();
        
        // Create a new vector with 0 as the first entry and detL_ff as the last entry
        double[] newStretchedPositionsData = new double[m + 2];
        newStretchedPositionsData[0] = 0.0; // First entry is 0
        System.arraycopy(stretchedPositions.toArray(), 0, newStretchedPositionsData, 1, m); // Copy stretchedPositions
        newStretchedPositionsData[m + 1] = detL_ff; // Last entry is detL_ff

        
     // Create a new RealVector with the updated data
        RealVector newStretchedPositions = new ArrayRealVector(newStretchedPositionsData);
        
        //System.out.println("stretchedPositions:"+newStretchedPositions);


        // Step 8: Update the coordinates of the fixed vertices (start and end)
        if (direction == ExtendDirection.HORIZONTAL) {
            start.setX(0.0 * detL_ff);
            end.setX(1.0 * Math.round(detL_ff));
        } else if (direction == ExtendDirection.VERTICAL) {
            start.setY(0.0 * detL_ff);
            end.setY(1.0 * Math.round(detL_ff));
        }
        
        // Step 9: refresh edges after stretching       
        for (Edge e:edges)
        {
        	e.refresh();
        }
        
        return newStretchedPositions;
    }

    // Helper method to compute the adjugate (comatrix) of a matrix
    private RealMatrix computeAdjugate(RealMatrix matrix) {
        int n = matrix.getRowDimension();
        double[][] adjugateData = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // Compute the cofactor C_ij = (-1)^(i+j) * det(M_ij)
                RealMatrix minor = matrix.getSubMatrix(
                    removeIndex(matrix.getRowDimension(), i),
                    removeIndex(matrix.getColumnDimension(), j)
                );
                double cofactor = Math.pow(-1, i + j) * new LUDecomposition(minor).getDeterminant();
                adjugateData[j][i] = cofactor; // Transpose for adjugate
            }
        }

        return MatrixUtils.createRealMatrix(adjugateData);
    }

    // Helper method to create an array without a specific index
    private int[] removeIndex(int length, int indexToRemove) {
        int[] indices = new int[length - 1];
        int pos = 0;
        for (int i = 0; i < length; i++) {
            if (i != indexToRemove) {
                indices[pos++] = i;
            }
        }
        return indices;
    }
    
    public Graph getDualGraph() {
        // Step 1: Extend the graph horizontally
        extend(ExtendDirection.HORIZONTAL);


        // Step 2: Detect the faces of the extended graph
        List<Face> faces = findFaces();

        // Step 3: Identify and remove the outer face
        // The outer face is the one that contains the leftmost, rightmost, northmost, and southmost vertices
        Vertex leftmost = getBoundaryVertex(BoundaryDirection.LEFT);
        Vertex rightmost = getBoundaryVertex(BoundaryDirection.RIGHT);
        Vertex northmost = getBoundaryVertex(BoundaryDirection.UP);
        Vertex southmost = getBoundaryVertex(BoundaryDirection.DOWN);

        List<Face> innerFaces = new ArrayList<>();
        for (Face face : faces) {
            if (!face.containsVertex(leftmost) ||
                !face.containsVertex(rightmost) ||
                !face.containsVertex(northmost) ||
                !face.containsVertex(southmost)) {
                innerFaces.add(face);
            }
        }

        // Step 4: Build the dual graph by mapping the remaining faces to vertices
        Graph dualGraph = new Graph();
        Map<Face, Vertex> faceToVertexMap = new HashMap<>();

        for (Face face : innerFaces) {
            Vertex dualVertex = new Vertex(dualGraph.getVertices().size());
            dualGraph.addVertex(dualVertex);
            faceToVertexMap.put(face, dualVertex);
        }

        // Step 5: Add edges whenever two faces share a common edge
        for (int i = 0; i < innerFaces.size(); i++) {
            for (int j = i + 1; j < innerFaces.size(); j++) {
                Face face1 = innerFaces.get(i);
                Face face2 = innerFaces.get(j);
                
                List<Edge> edges1 = face1.getEdges();
                List<Edge> edges2 = face2.getEdges();

                // Check if face1 and face2 share a common edge
                for (int k = 0; k < edges1.size(); k++) {
                    for (int l = 0; l < edges2.size(); l++) {
                        if (edges1.get(k).equals(edges2.get(l))) 
                        {
                            Vertex dualVertex1 = faceToVertexMap.get(face1);
                            Vertex dualVertex2 = faceToVertexMap.get(face2);
                            dualGraph.addEdge(new Edge(dualVertex1, dualVertex2));
                            break;
                        }
                    }
                }
            }
        }
        
        return dualGraph;
    }
    
    /**
     * Sorts the edges in the graph by:
     * 1. The x-coordinate of the start vertex.
     * 2. The y-coordinate of the end vertex (if start vertices have the same x-coordinate).
     */
    public void sortEdges() {
        edges.sort((edge1, edge2) -> {
            Vertex source1 = edge1.getSource();
            Vertex source2 = edge2.getSource();
            Vertex target1 = edge1.getTarget();
            Vertex target2 = edge2.getTarget();

            // Compare by start vertex x-coordinate
            int xCompare = Double.compare(source1.getX(), source2.getX());
            if (xCompare != 0) {
                return xCompare;
            }

            // If start vertices have the same x-coordinate, compare by end vertex y-coordinate
            return Double.compare(target2.getY(), target1.getY());
        });
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edges: ");
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (i > 0) sb.append(", ");
            sb.append("(").append(e.getSource().getId())
              .append(", ").append(e.getTarget().getId()).append(")");
        }
        sb.append("\nCoordinates: ");
        List<Vertex> sorted = new ArrayList<>(vertices);
        sorted.sort(Comparator.comparingInt(Vertex::getId));
        for (int i = 0; i < sorted.size(); i++) {
            Vertex v = sorted.get(i);
            if (i > 0) sb.append(", ");
            sb.append(v.getId()).append("=(")
              .append(v.getX()).append(", ").append(v.getY()).append(")");
        }
        return sb.toString();
    }
    
    public String toEdgeString() {
        return edges.stream()
                .map(e -> "(" + e.getSource().getId() + ", " + e.getTarget().getId() + ")")
                .collect(Collectors.joining(", "));
    }

    public Map<Integer, double[]> toCoordinateMap() {
        Map<Integer, double[]> coords = new LinkedHashMap<>();
        for (Vertex v : vertices) {
            coords.put(v.getId(), new double[]{v.getX(), v.getY()});
        }
        return coords;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph)) return false;
        Graph other = (Graph) o;
        // same vertex set and same edge set, ignoring insertion order
        return new HashSet<>(vertices).equals(new HashSet<>(other.vertices))
                && new HashSet<>(edges).equals(new HashSet<>(other.edges));
    }

    @Override
    public int hashCode() {
        return Objects.hash(new HashSet<>(vertices), new HashSet<>(edges));
    }
}