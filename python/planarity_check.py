# Install the planarity library if you haven't already
# Run: pip install planarity

from planarity import is_planar, PlanarEmbedding
import matplotlib.pyplot as plt
import networkx as nx

# Define the edges of your graph
edges = [
    (0, 1), (0, 2), (0, 4),
    (1, 2), (1, 5),
    (2, 3),
    (3, 4), (3, 5),
    (4, 5)
]

# Check if the graph is planar
planar, embedding = is_planar(edges)
print(f"Is the graph planar? {planar}")

if planar:
    # Create a PlanarEmbedding object
    pe = PlanarEmbedding(embedding)

    # Compute the dual graph
    dual_edges = pe.dual_graph_edges()
    print("\nDual Graph Edges:")
    for edge in dual_edges:
        print(f"Dual Edge: {edge}")

    # Visualize the original graph
    G = nx.Graph()
    G.add_edges_from(edges)

    plt.figure(figsize=(8, 6))
    nx.draw(G, with_labels=True, node_color='lightblue', edge_color='gray')
    plt.title("Original Graph")
    plt.show()

    # Visualize the dual graph
    dual_G = nx.Graph()
    dual_G.add_edges_from(dual_edges)

    plt.figure(figsize=(8, 6))
    nx.draw(dual_G, with_labels=True, node_color='lightgreen', edge_color='gray')
    plt.title("Dual Graph")
    plt.show()
else:
    print("The graph is not planar. Dual graph cannot be computed.")