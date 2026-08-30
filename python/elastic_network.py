import numpy as np
import matplotlib.pyplot as plt

# Laplacian matrix for a 6-vertex graph (0-5)
L = np.array([
    [3, -1, -1, 0, -1, 0],  # Vertex 0
    [-1, 3, -1, 0, 0, -1],  # Vertex 1
    [-1, -1, 3, -1, 0, 0],  # Vertex 2
    [0, 0, -1, 3, -1, -1],  # Vertex 3
    [-1, 0, 0, -1, 3, -1],  # Vertex 4
    [0, -1, 0, -1, -1, 3]   # Vertex 5
])

# Fixed vertices: 0, 4 (fully fixed)
fixed_indices_full = [0, 5]
# Partially fixed vertex: 5 (only y fixed)
partially_fixed_vertex = 1

# Free vertices for x-coordinates: 1, 2, 3, 5
free_indices_x = [1, 2, 3, 4]
# Free vertices for y-coordinates: 1, 2, 3
free_indices_y = [2, 3, 4]

# Fixed indices for y-coordinates: 0, 4, 5
fixed_indices_y = [0, 1, 5]

# Partition the Laplacian matrix for x-coordinates
L_ff_x = L[np.ix_(free_indices_x, free_indices_x)]
L_fk_x = L[np.ix_(free_indices_x, fixed_indices_full)]

# Fixed x-coordinates: Xk = [0, 1]
Xk = np.array([0.0, 1.0/5])

# Partition the Laplacian matrix for y-coordinates
L_ff_y = L[np.ix_(free_indices_y, free_indices_y)]
L_fk_y = L[np.ix_(free_indices_y, fixed_indices_y)]

# Fixed y-coordinates: Yk = [0, 0, 1]
Yk = np.array([0.0, 0*1/21, 0])

# Solve for Xf: L_ff_x * Xf = -L_fk_x * Xk
Xf = np.linalg.solve(L_ff_x, -L_fk_x @ Xk)
# Solve for Yf: L_ff_y * Yf = -L_fk_y * Yk
Yf = np.linalg.solve(L_ff_y, -L_fk_y @ Yk)

# Combine fixed and free coordinates
X = np.zeros(6)
Y = np.zeros(6)

# Assign fixed x-coordinates
X[fixed_indices_full] = Xk
# Assign free x-coordinates (including vertex 5)
X[free_indices_x] = Xf

# Assign fixed y-coordinates
Y[fixed_indices_y] = Yk
# Assign free y-coordinates
Y[free_indices_y] = Yf

# Compute the determinant of L_ff_x and L_ff_y
det_Lff_x = np.linalg.det(L_ff_x)
det_Lff_y = np.linalg.det(L_ff_y)
print(f"Determinant of L_ff_x: {det_Lff_x:.4f}")
print(f"Determinant of L_ff_y: {det_Lff_y:.4f}")

# Scale all coordinates by det(L_ff_x) and det(L_ff_y)
X_scaled = X * det_Lff_x
Y_scaled = Y * det_Lff_y

# Print the scaled coordinates
print("\nScaled Vertex Coordinates:")
for i in range(6):
    print(f"Vertex {i}: ({X_scaled[i]:.4f}, {Y_scaled[i]:.4f})")

# Print squared lengths and energy contributions for each edge
print("\nEdge Contributions to Squared Energy:")
edges = [
    (0, 1), (0, 2), (0, 4),
    (1, 2), (1, 5),
    (2, 3),
    (3, 4), (3, 5),
    (4, 5)
]

# Initialize total horizontal and vertical squared energy
total_horizontal_energy = 0.0
total_vertical_energy = 0.0

for i, j in edges:
    dx = X_scaled[i] - X_scaled[j]
    dy = Y_scaled[i] - Y_scaled[j]
    squared_length = dx**2 + dy**2
    horizontal_energy = dx**2
    vertical_energy = dy**2

    total_horizontal_energy += horizontal_energy
    total_vertical_energy += vertical_energy

    print(f"Edge ({i}, {j}): Squared Length = {squared_length:.4f}, Horizontal (dx²) = {horizontal_energy:.4f}, Vertical (dy²) = {vertical_energy:.4f}")

# Print total horizontal and vertical squared energy
print(f"\nTotal Horizontal Squared Energy (sum of dx²): {total_horizontal_energy:.4f}")
print(f"Total Vertical Squared Energy (sum of dy²): {total_vertical_energy:.4f}")

# Plot the scaled graph
plt.figure(figsize=(8, 6))
plt.scatter(X_scaled, Y_scaled, c='red', label='Vertices')
for i in range(6):
    plt.text(X_scaled[i], Y_scaled[i], str(i), fontsize=12, ha='right')

# Draw edges
for i, j in edges:
    plt.plot([X_scaled[i], X_scaled[j]], [Y_scaled[i], Y_scaled[j]], 'b-')

plt.xlabel('X (scaled)')
plt.ylabel('Y (scaled)')
plt.title(f'Elastic Network (Fixed: 0, 4 | Partially Fixed: 5 (y=1))')
plt.grid(True)
plt.legend()
plt.axis('equal')
plt.show()