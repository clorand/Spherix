import numpy as np

# Laplacian matrix of the dual graph
# Vertices: E=0, A=1, B=2, C=3, D=4
L_dual = np.array([
    [4, -1, -1, -1, -1],  # Vertex 0 (E)
    [-1, 3, -1, 0, -1],   # Vertex 1 (A)
    [-1, -1, 4, -1, -1],  # Vertex 2 (B)
    [-1, 0, -1, 3, -1],   # Vertex 3 (C)
    [-1, -1, -1, -1, 4]   # Vertex 4 (D)
])

# Fixed vertices: A (1) and E (4)
fixed_indices = [1, 4]
free_indices = [i for i in range(L_dual.shape[0]) if i not in fixed_indices]

# Fixed coordinates: X1 = 0, X4 = 1/5
Xk = np.array([0.0, 1.0/5])

# Partition the Laplacian matrix
L_ff = L_dual[np.ix_(free_indices, free_indices)]
L_fk = L_dual[np.ix_(free_indices, fixed_indices)]

# Solve for free vertices: L_ff * Xf = -L_fk * Xk
Xf = np.linalg.solve(L_ff, -L_fk @ Xk)

# Combine fixed and free coordinates
X = np.zeros(L_dual.shape[0])
X[fixed_indices] = Xk
X[free_indices] = Xf

# Compute the determinant of L_ff
det_Lff = np.linalg.det(L_ff)
print(f"Determinant of L_ff: {det_Lff:.4f}")

# Scale all coordinates by det(L_ff)
X_scaled = X * det_Lff

# Print the scaled X coordinates
print("\nScaled X Coordinates for Dual Graph:")
for i, x in enumerate(X_scaled):
    vertex_name = ["E", "A", "B", "C", "D"][i]
    print(f"Vertex {vertex_name} ({i}): {x:.4f}")