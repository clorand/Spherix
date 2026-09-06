import numpy as np
import matplotlib.pyplot as plt
import networkx as nx

def solve_1d_elastic_network(L, fixed_indices, fixed_coords):
    free_indices = [i for i in range(L.shape[0]) if i not in fixed_indices]
    L_ff = L[np.ix_(free_indices, free_indices)]
    L_fk = L[np.ix_(free_indices, fixed_indices)]
    Xk = np.array(fixed_coords)
    Xf = np.linalg.solve(L_ff, -L_fk @ Xk)
    X = np.zeros(L.shape[0])
    X[fixed_indices] = Xk
    X[free_indices] = Xf
    det_Lff = np.linalg.det(L_ff)
    X_scaled = X * det_Lff
    return X_scaled

# Laplacian matrix for a 6-vertex graph (0-5)
L = np.array([
    [3, -1, -1, 0, -1, 0],  # Vertex 0
    [-1, 3, -1, 0, 0, -1],  # Vertex 1
    [-1, -1, 3, -1, 0, 0],  # Vertex 2
    [0, 0, -1, 3, -1, -1],  # Vertex 3
    [-1, 0, 0, -1, 3, -1],  # Vertex 4
    [0, -1, 0, -1, -1, 3]   # Vertex 5
])

# Solve for x-coordinates (fixed vertices: 1 and 4)
X_indices = [1, 4]
X_coords = [0.0, 1.0/5]
X_scaled = solve_1d_elastic_network(L, X_indices, X_coords)

# Solve for y-coordinates (fixed vertices: 5 and 0)
Y_indices = [5, 0]
Y_coords = [0.0, 1.0/5]
Y_scaled = solve_1d_elastic_network(L, Y_indices, Y_coords)

# Print the scaled coordinates
print("Scaled 1D X Coordinates:")
for i, x in enumerate(X_scaled):
    print(f"Vertex {i}: {x:.4f}")

print("\nScaled 1D Y Coordinates:")
for i, y in enumerate(Y_scaled):
    print(f"Vertex {i}: {y:.4f}")

# Edges in the original graph
edges = [
    (0, 1), (0, 2), (0, 4),
    (1, 2), (1, 5),
    (2, 3),
    (3, 4), (3, 5),
    (4, 5)
]

# Define the faces of the planar embedding
faces = [
    [0, 1, 2],      # Face A
    [0, 2, 3, 4],   # Face B
    [1, 2, 3, 5],   # Face C
    [3, 4, 5],      # Face D
    [0, 4, 5, 1],   # Face E
]


# Plot the original graph
plt.figure(figsize=(10, 8))
plt.scatter(X_scaled, Y_scaled, c='black', s=100, zorder=2, label='Vertices')
for i in range(len(X_scaled)):
    plt.text(X_scaled[i], Y_scaled[i], str(i), fontsize=12, ha='center', va='center', color='white')

# Draw edges with unique colors
colors = plt.cm.rainbow(np.linspace(0, 1, len(edges)))
for idx, (i, j) in enumerate(edges):
    plt.plot(
        [X_scaled[i], X_scaled[j]],
        [Y_scaled[i], Y_scaled[j]],
        color=colors[idx],
        linewidth=2,
        label=f'Edge ({i}, {j})'
    )

plt.xlabel('X (scaled)')
plt.ylabel('Y (scaled)')
plt.title('Elastic Network: Edges with Unique Colors')
plt.grid(True)
plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
plt.axis('equal')
plt.tight_layout()
plt.show()


