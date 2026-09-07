package com.clorand.spherix.graph;

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
    public List<List<Integer>> findFaces() {
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
        List<List<Integer>> faces = new ArrayList<>();

        for (String startEdge : directedEdges) {
            if (visited.contains(startEdge)) {
                continue;
            }

            List<Integer> face = new ArrayList<>();
            String currentEdge = startEdge;
            String[] edgeParts = currentEdge.split(",");
            int u = Integer.parseInt(edgeParts[0]);
            int v = Integer.parseInt(edgeParts[1]);

            // Start traversing the face
            while (!visited.contains(currentEdge)) {
                visited.add(currentEdge);
                face.add(u);

                // Get the cyclic order of neighbors around v
                List<Integer> neighbors = cyclicOrder.get(v);
                if (neighbors == null || neighbors.isEmpty()) {
                    break; // No neighbors to traverse
                }

                // Find the index of u in the neighbors list
                int index = neighbors.indexOf(u);
                if (index == -1) {
                    break; // u is not a neighbor of v (should not happen for valid graphs)
                }

                // Take the previous edge in cyclic order (left-hand rule)
                int nextNeighbor = neighbors.get((index - 1 + neighbors.size()) % neighbors.size());

                // Move to the next directed edge
                currentEdge = v + "," + nextNeighbor;
                edgeParts = currentEdge.split(",");
                u = Integer.parseInt(edgeParts[0]);
                v = Integer.parseInt(edgeParts[1]);
            }

            // Remove consecutive duplicates (but preserve the start/end vertex)
            List<Integer> uniqueFace = new ArrayList<>();
            for (int vertex : face) {
                if (uniqueFace.isEmpty() || vertex != uniqueFace.get(uniqueFace.size() - 1)) {
                    uniqueFace.add(vertex);
                }
            }

            // Only add the face if it has at least 3 unique vertices
            if (uniqueFace.size() >= 3) {
                faces.add(uniqueFace);
            }
        }

        return faces;
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
    public List<List<Integer>> getBoundaryFaces(BoundaryDirection direction) {
        Vertex boundaryVertex = getBoundaryVertex(direction);
        if (boundaryVertex == null) {
            return new ArrayList<>(); // No boundary vertex found
        }

        List<List<Integer>> boundaryFaces = new ArrayList<>();
        List<List<Integer>> allFaces = findFaces();

        for (List<Integer> face : allFaces) {
            if (face.contains(boundaryVertex.getId())) {
                boundaryFaces.add(face);
            }
        }

        return boundaryFaces;
    }
    
    
    public void stretch(ExtendDirection direction, Vertex start, Vertex end) {
        if (vertices.isEmpty() || start == null || end == null) {
            return; // No vertices or invalid start/end
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
        
        System.out.println("stretchedPositions:"+stretchedPositions);

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
        
        // Step 7: Compute the determinant of the comatrix
        double detL_ff = new LUDecomposition(freeLaplacian).getDeterminant();

        // Step 8: Update the coordinates of the fixed vertices (start and end)
        if (direction == ExtendDirection.HORIZONTAL) {
            start.setX(0.0 * detL_ff);
            end.setX(1.0 * detL_ff);
        } else if (direction == ExtendDirection.VERTICAL) {
            start.setY(0.0 * detL_ff);
            end.setY(1.0 * detL_ff);
        }
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
        List<List<Integer>> faces = findFaces();

        // Step 3: Identify and remove the outer face
        // The outer face is the one that contains the leftmost, rightmost, northmost, and southmost vertices
        Vertex leftmost = getBoundaryVertex(BoundaryDirection.LEFT);
        Vertex rightmost = getBoundaryVertex(BoundaryDirection.RIGHT);
        Vertex northmost = getBoundaryVertex(BoundaryDirection.UP);
        Vertex southmost = getBoundaryVertex(BoundaryDirection.DOWN);

        List<List<Integer>> innerFaces = new ArrayList<>();
        for (List<Integer> face : faces) {
            if (!face.contains(leftmost.getId()) ||
                !face.contains(rightmost.getId()) ||
                !face.contains(northmost.getId()) ||
                !face.contains(southmost.getId())) {
                innerFaces.add(face);
            }
        }

        // Step 4: Build the dual graph by mapping the remaining faces to vertices
        Graph dualGraph = new Graph();
        Map<List<Integer>, Vertex> faceToVertexMap = new HashMap<>();

        for (List<Integer> face : innerFaces) {
            Vertex dualVertex = new Vertex(dualGraph.getVertices().size());
            dualGraph.addVertex(dualVertex);
            faceToVertexMap.put(face, dualVertex);
        }

        // Step 5: Add edges whenever two faces share a common edge
        for (int i = 0; i < innerFaces.size(); i++) {
            for (int j = i + 1; j < innerFaces.size(); j++) {
                List<Integer> face1 = innerFaces.get(i);
                List<Integer> face2 = innerFaces.get(j);

                // Check if face1 and face2 share a common edge
                for (int k = 0; k < face1.size(); k++) {
                    for (int l = 0; l < face2.size(); l++) {
                        int vertex1 = face1.get(k);
                        int vertex2 = face1.get((k + 1) % face1.size());
                        int vertex3 = face2.get(l);
                        int vertex4 = face2.get((l + 1) % face2.size());

                        // Check if the edge (vertex1, vertex2) is the same as (vertex3, vertex4) or (vertex4, vertex3)
                        if ((vertex1 == vertex3 && vertex2 == vertex4) ||
                            (vertex1 == vertex4 && vertex2 == vertex3)) {
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
}