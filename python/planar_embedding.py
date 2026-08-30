import numpy as np
import math
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from collections import defaultdict

# Define the vertex coordinates
vertices = {
    0: (5.0, 11.0),
    1: (0.0, 6.0),
    2: (4.0, 7.0),
    3: (7.0, 4.0),
    4: (11.0, 5.0),
    5: (6.0, 0.0)
}

# Define the adjacency list for the graph
adjacency_list = {
    0: [1, 2, 4],
    1: [0, 2, 5],
    2: [0, 1, 3],
    3: [2, 4, 5],
    4: [0, 3, 5],
    5: [1, 3, 4]
}

# ------------------------------------------------------------
# 1. Compute the cyclic ordering of neighbors around each vertex
# ------------------------------------------------------------
def compute_cyclic_order(vertices, adjacency_list):
    cyclic_order = {}
    for v, neighbors in adjacency_list.items():
        x, y = vertices[v]
        neighbors_sorted = sorted(
            neighbors,
            key=lambda u: math.atan2(vertices[u][1] - y, vertices[u][0] - x)
        )
        cyclic_order[v] = neighbors_sorted
    return cyclic_order

# ------------------------------------------------------------
# 2. Face traversal using directed edges and left-hand rule
# ------------------------------------------------------------
def find_faces(vertices, adjacency_list):
    cyclic_order = compute_cyclic_order(vertices, adjacency_list)

    # Every undirected edge gives us two directed edges
    directed_edges = set()
    for u in adjacency_list:
        for v in adjacency_list[u]:
            directed_edges.add((u, v))

    visited = set()
    faces = []

    for start_edge in directed_edges:
        if start_edge in visited:
            continue

        face = []
        current_edge = start_edge

        while current_edge not in visited:
            visited.add(current_edge)
            u, v = current_edge
            face.append(u)

            # Neighbors around v in cyclic order
            neighbors = cyclic_order[v]

            # Find where u occurs in the cyclic order
            index = neighbors.index(u)

            # Take the previous edge in cyclic order (left-hand rule)
            next_neighbor = neighbors[(index - 1) % len(neighbors)]
            current_edge = (v, next_neighbor)

        faces.append(face)

    return faces, cyclic_order

# ------------------------------------------------------------
# 3. Run the algorithm to find faces
# ------------------------------------------------------------
faces, cyclic_order = find_faces(vertices, adjacency_list)

# ------------------------------------------------------------
# 4. Print the detected faces
# ------------------------------------------------------------
print("Detected Faces:")
for i, face in enumerate(faces):
    print(f"  Face {i + 1}: {face}")

# ------------------------------------------------------------
# 5. Plot the graph with faces patched in different colors
# ------------------------------------------------------------
plt.figure(figsize=(10, 8))

# Plot vertices
for vertex, (x, y) in vertices.items():
    plt.scatter(x, y, c='black', s=100, zorder=3)
    plt.text(x, y, str(vertex), fontsize=12, ha='center', va='center', color='white')

# Plot edges
for u in adjacency_list:
    for v in adjacency_list[u]:
        if u < v:  # Avoid plotting edges twice
            x_u, y_u = vertices[u]
            x_v, y_v = vertices[v]
            plt.plot([x_u, x_v], [y_u, y_v], 'k-', linewidth=1, zorder=2)

# Patch each face with a different color
colors = plt.cm.rainbow(np.linspace(0, 1, len(faces)))
for idx, face in enumerate(faces):
    # Extract the coordinates of the face vertices
    face_coords = [vertices[v] for v in face]
    # Create a polygon patch for the face
    polygon = patches.Polygon(face_coords, closed=True, fill=True, color=colors[idx], alpha=0.5, edgecolor='k')
    plt.gca().add_patch(polygon)
    # Annotate the face index at its centroid
    centroid_x = sum(v[0] for v in face_coords) / len(face_coords)
    centroid_y = sum(v[1] for v in face_coords) / len(face_coords)
    plt.text(centroid_x, centroid_y, f'Face {idx + 1}', fontsize=10, ha='center', va='center', color='black')

plt.title("Planar Graph Embedding with Faces")
plt.xlabel("X")
plt.ylabel("Y")
plt.grid(True)
plt.axis('equal')
plt.tight_layout()
plt.show()