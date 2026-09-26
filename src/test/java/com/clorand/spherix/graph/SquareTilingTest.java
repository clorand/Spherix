package com.clorand.spherix.graph;

import com.clorand.spherix.model.Configuration;
import com.clorand.spherix.model.DatabaseLoader;
import com.clorand.spherix.utils.ConfigurationTestUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class to investigate Square Tilings.
 *
 * Pipeline:
 *   dual graph --stretch(H pair)--> x-positions (line coordinates)
 *             --stretch(V pair)--> y-positions (topological ranks)
 *             --sortEdges()------> stacking order per line
 *             --sweep------------> square tiling
 *
 * stretch() is a port of the embedding mechanism (Brooks-Smith-Stone-Tutte
 * harmonic embedding in integer scale): fix start=0, end=det(L_ff), and the
 * free positions are adj(L_ff) * (-L_ff,k * fixed) - i.e. the rational
 * harmonic solution scaled by det so all coordinates stay integers.
 *
 * The JavaFX visualization is a MANUAL helper: launch() blocks until the
 * window is closed and can run only once per JVM, so it lives behind main().
 */
public class SquareTilingTest {

    private static final double PADDING = 20.0; // margin around each tiling (px)

    /** A square of the tiling: lower-left corner (x, y) and side length.
     *  Coordinates are long: the stretch port produces exact integers. */
    public record Square(long x, long y, int size) {
        @Override
        public String toString() {
            return String.format("(%.1f, %.1f) s=%d", (double) x, (double) y, size);
        }
    }

    /** An edge of the dual graph (a square spanning its two lines).
     *  NOTE: 'length' is NOT used by the conversion - the actual side length
     *  is computed from the stretched x-positions (x_to - x_from). */
    public record DualEdge(int from, int to, int length) {}

    /** The dual graph: vertex ids 0..n-1 plus its edges (undirected squares). */
    public record DualGraph(int vertexCount, List<DualEdge> edges) {}

    // ------------------------------------------------------------------
    // Sample data: the dual graph of the sample tiling (9 squares, 6 nodes)
    // ------------------------------------------------------------------

    private DualGraph createSampleDualGraph() {
        List<DualEdge> edges = List.of(
                new DualEdge(0, 1, 5),  // square (0,0) s5
                new DualEdge(0, 2, 6),  // square (0,5) s6
                new DualEdge(1, 3, 4),  // square (5,0) s4
                new DualEdge(1, 2, 1),  // square (5,4) s1
                new DualEdge(2, 3, 3),  // square (6,4) s3
                new DualEdge(2, 4, 4),  // square (6,7) s4
                new DualEdge(3, 5, 6),  // square (9,0) s6
                new DualEdge(3, 4, 1),  // square (9,6) s1
                new DualEdge(4, 5, 5)); // square (10,6) s5
        return new DualGraph(6, edges);
    }

    /** The sample tiling (the known solution for stretch pairs 0-5 / 2-3). */
    private Set<Square> createSampleTiling() {
        return Set.of(
                new Square(0, 0, 5),
                new Square(0, 5, 6),
                new Square(5, 0, 4),
                new Square(5, 4, 1),
                new Square(6, 4, 3),
                new Square(6, 7, 4),
                new Square(9, 0, 6),
                new Square(9, 6, 1),
                new Square(10, 6, 5));
    }

    // ------------------------------------------------------------------
    // Port of the embedding: stretch()
    // ------------------------------------------------------------------

    /** Integer determinant via cofactor expansion (n is small here). */
    private static long det(long[][] m) {
        int n = m.length;
        if (n == 1) return m[0][0];
        long d = 0;
        for (int j = 0; j < n; j++) {
            long[][] minor = new long[n - 1][n - 1];
            for (int i = 1; i < n; i++) {
                int c = 0;
                for (int k = 0; k < n; k++) {
                    if (k == j) continue;
                    minor[i - 1][c++] = m[i][k];
                }
            }
            d += ((j % 2 == 0) ? 1 : -1) * m[0][j] * det(minor);
        }
        return d;
    }

    /** Adjugate (transposed cofactor matrix) of a small integer matrix. */
    private static long[][] adjugate(long[][] m) {
        int n = m.length;
        long[][] adj = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long[][] minor = new long[n - 1][n - 1];
                int r = 0;
                for (int a = 0; a < n; a++) {
                    if (a == i) continue;
                    int c = 0;
                    for (int b = 0; b < n; b++) {
                        if (b == j) continue;
                        minor[r][c++] = m[a][b];
                    }
                    r++;
                }
                long cofactor = ((i + j) % 2 == 0 ? 1 : -1) * det(minor);
                adj[j][i] = cofactor; // transpose
            }
        }
        return adj;
    }

    /**
     * Port of stretch(direction, start, end): unit-weight Laplacian embedding.
     * Returns positions per vertex: start = 0, end = det(L_ff), free vertices
     * = adj(L_ff) * b where b = -L[free][end]. This is the rational harmonic
     * solution scaled by det(L_ff), so all values are integers.
     *
     * Returns null if the stretch is invalid (start == end or det == 0).
     */
    private long[] stretch(DualGraph graph, int start, int end) {
        int n = graph.vertexCount();
        if (start == end || start < 0 || end < 0 || start >= n || end >= n) return null;

        long[][] laplacian = new long[n][n];
        for (DualEdge e : graph.edges()) {
            laplacian[e.from()][e.from()]++;
            laplacian[e.to()][e.to()]++;
            laplacian[e.from()][e.to()]--;
            laplacian[e.to()][e.from()]--;
        }

        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (i != start && i != end) free.add(i);
        }
        int m = free.size();

        long[][] lff = new long[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                lff[i][j] = laplacian[free.get(i)][free.get(j)];
            }
        }

        long detLff = det(lff);
        if (detLff == 0) return null; // invalid stretch pair

        long[] b = new long[m];
        for (int i = 0; i < m; i++) {
            b[i] = -laplacian[free.get(i)][end];
        }

        long[][] adj = adjugate(lff);
        long[] positions = new long[n];
        positions[start] = 0;
        positions[end] = detLff;
        for (int i = 0; i < m; i++) {
            long value = 0;
            for (int j = 0; j < m; j++) {
                value += adj[i][j] * b[j];
            }
            positions[free.get(i)] = value;
        }
        return positions;
    }

    // ------------------------------------------------------------------
    // The algorithm: stretched dual graph -> square tiling
    // ------------------------------------------------------------------

    /**
     * Converts stretched positions into the square tiling.
     *
     * Left-to-right sweep over the vertical lines (vertices), ordered by x:
     *   - the bottom of a line is the lowest square already placed to its
     *     left (squares tile each side of a maximal segment contiguously),
     *     defaulting to 0 for lines with no left squares (left boundary);
     *   - each edge is a square whose left side lies on the lower-x line;
     *     edges are stacked bottom-up in descending y-rank of the target
     *     (the sortEdges() order).
     *
     * Edges are re-oriented by x-order (the dual graph's squares are
     * undirected; the lower-x endpoint carries the square).
     *
     * @return null if the positions do not define a valid tiling geometry
     *         (two lines coincide, or a line's squares stack inconsistently).
     */
    private List<Square> convertToTiling(DualGraph graph, long[] x, long[] y) {
        int n = graph.vertexCount();

        // Re-orient each edge from the lower-x line to the higher-x line.
        List<int[]> oriented = new ArrayList<>(); // {from, to}
        for (DualEdge e : graph.edges()) {
            if (x[e.from()] == x[e.to()]) return null; // degenerate: zero-width square
            oriented.add(x[e.from()] < x[e.to()]
                    ? new int[]{e.from(), e.to()}
                    : new int[]{e.to(), e.from()});
        }

        // sortEdges(): source x ascending, then target y-rank descending.
        oriented.sort((a, b2) -> {
            int byX = Long.compare(x[a[0]], x[b2[0]]);
            if (byX != 0) return byX;
            return Long.compare(y[b2[1]], y[a[1]]); // descending target y
        });

        Map<Integer, List<int[]>> outgoing = new HashMap<>();
        for (int[] e : oriented) {
            outgoing.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e);
        }

        Map<Integer, Long> lineBottom = new HashMap<>();
        List<Square> squares = new ArrayList<>();

        Integer[] lines = new Integer[n];
        for (int i = 0; i < n; i++) lines[i] = i;
        java.util.Arrays.sort(lines, (a, b2) -> Long.compare(x[a], x[b2]));

        for (Integer line : lines) {
            long cursor = lineBottom.getOrDefault(line, 0L);
            for (int[] e : outgoing.getOrDefault(line, List.of())) {
                long width = x[e[1]] - x[e[0]];
                squares.add(new Square(x[line], cursor, (int) width));
                lineBottom.merge(e[1], cursor, Math::min);
                cursor += width;
            }
        }
        return squares;
    }

    /**
     * A tiling is valid iff no two squares overlap and the squares exactly
     * cover their bounding box (the area check proves "no gaps" given the
     * no-overlap check).
     */
    private boolean isValidTiling(List<Square> squares) {
        if (squares.isEmpty()) return false;
        for (int i = 0; i < squares.size(); i++) {
            for (int j = i + 1; j < squares.size(); j++) {
                Square a = squares.get(i);
                Square b = squares.get(j);
                boolean separated = a.x() + a.size() <= b.x()
                        || b.x() + b.size() <= a.x()
                        || a.y() + a.size() <= b.y()
                        || b.y() + b.size() <= a.y();
                if (!separated) return false;
            }
        }
        long area = squares.stream().mapToLong(s -> (long) s.size() * s.size()).sum();
        long minX = squares.stream().mapToLong(Square::x).min().orElseThrow();
        long minY = squares.stream().mapToLong(Square::y).min().orElseThrow();
        long maxX = squares.stream().mapToLong(s -> s.x() + s.size()).max().orElseThrow();
        long maxY = squares.stream().mapToLong(s -> s.y() + s.size()).max().orElseThrow();
        return (maxX - minX) * (maxY - minY) == area;
    }

    /** Divides all coordinates and sizes by their gcd (canonical scale). */
    private List<Square> normalize(List<Square> squares) {
        long g = 0;
        for (Square s : squares) {
            g = gcd(g, (long) s.x());
            g = gcd(g, (long) s.y());
            g = gcd(g, s.size());
        }
        if (g == 0) return squares;
        List<Square> out = new ArrayList<>();
        for (Square s : squares) {
            out.add(new Square(s.x() / g, s.y() / g, (int) (s.size() / g)));
        }
        out.sort((a, b) -> (int) (a.x() != b.x() ? a.x() - b.x()
                : a.y() != b.y() ? a.y() - b.y() : a.size() - b.size()));
        return out;
    }

    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // ------------------------------------------------------------------
    // Enumeration: all valid stretches -> all tilings
    // ------------------------------------------------------------------

    /**
     * Enumerates all tilings that result from ALL possible stretch pairs:
     * every ordered vertex pair (s, t) for the horizontal stretch, crossed
     * with every ordered vertex pair for the vertical stretch. A combination
     * is kept iff both stretches are valid (det != 0) and the resulting
     * geometry is a valid tiling. Duplicate tilings (up to scaling) collapse.
     */
    private List<List<Square>> enumerateAllTilings(DualGraph graph) {
        int n = graph.vertexCount();
        Set<List<Square>> distinct = new LinkedHashSet<>();

        for (int hs = 0; hs < n; hs++) {
            for (int ht = 0; ht < n; ht++) {
                long[] x = stretch(graph, hs, ht);
                if (x == null) continue;
                for (int vs = 0; vs < n; vs++) {
                    for (int vt = 0; vt < n; vt++) {
                        long[] y = stretch(graph, vs, vt);
                        if (y == null) continue;
                        List<Square> tiling = convertToTiling(graph, x, y);
                        if (tiling != null && isValidTiling(tiling)) {
                            distinct.add(normalize(tiling));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(distinct);
    }

    /**
     * Finds the FIRST valid tiling without full enumeration: tries stretch
     * pairs in order and returns as soon as one combination yields a valid
     * tiling. Much faster than enumerateAllTilings() for "just show me one".
     *
     * @return the first valid tiling found, or null if none exists
     */
    private List<Square> findFirstTiling(DualGraph graph) {
        int n = graph.vertexCount();
        // Precompute the n*n stretch results once (cheap part, reused below).
        long[][] positions = new long[n * n][];
        for (int s = 0; s < n; s++) {
            for (int t = 0; t < n; t++) {
                positions[s * n + t] = (s == t) ? null : stretch(graph, s, t);
            }
        }
        for (int hs = 0; hs < n; hs++) {
            for (int ht = 0; ht < n; ht++) {
                long[] x = positions[hs * n + ht];
                if (x == null) continue;
                for (int vs = 0; vs < n; vs++) {
                    for (int vt = 0; vt < n; vt++) {
                        long[] y = positions[vs * n + vt];
                        if (y == null) continue;
                        List<Square> tiling = convertToTiling(graph, x, y);
                        if (tiling != null && isValidTiling(tiling)) {
                            return normalize(tiling);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the unique edge whose two endpoints BOTH have degree 3.
     * Throws if there is none or more than one (the strategy assumes exactly
     * one such edge - e.g. dbKey 1056: vertices 0 and 9).
     */
    private int[] findUniqueDegree3Edge(DualGraph graph) {
        int n = graph.vertexCount();
        int[] degree = new int[n];
        for (DualEdge e : graph.edges()) {
            degree[e.from()]++;
            degree[e.to()]++;
        }
        List<int[]> candidates = new ArrayList<>();
        for (DualEdge e : graph.edges()) {
            if (degree[e.from()] == 3 && degree[e.to()] == 3) {
                candidates.add(new int[]{e.from(), e.to()});
            }
        }
        if (candidates.size() != 1) {
            throw new IllegalStateException(
                    "expected exactly one degree3-degree3 edge, found "
                            + candidates.size());
        }
        return candidates.get(0);
    }

    /**
     * Finds the FIRST valid tiling using a principled stretch pair:
     *   - horizontal: the unique degree3-degree3 edge (outer boundary axis),
     *   - vertical: an edge ADJACENT to one of the degree-3 vertices.
     * Vertical candidates are tried in a deterministic order (the degree-3
     * endpoint's other edges first), each in both orientations, and the
     * first combination producing a valid tiling wins.
     *
     * @return the first valid tiling found, or null if none exists
     */
    private List<Square> findFirstTilingFixedHorizontal(DualGraph graph) {
        int[] hEdge = findUniqueDegree3Edge(graph);
        int hs = hEdge[0], ht = hEdge[1];
        System.out.printf("Degree3-degree3 edge: %d-%d (horizontal stretch)%n", hs, ht);

        long[] x = stretch(graph, hs, ht);
        if (x == null) return null;

        // Vertical candidates: edges adjacent to a degree-3 vertex.
        // Prefer candidate edges in graph edge-list order; skip the
        // horizontal edge itself (same pair would degenerate).
        List<int[]> vCandidates = new ArrayList<>();
        for (DualEdge e : graph.edges()) {
            boolean adjacent = e.from() == hs || e.from() == ht
                    || e.to() == hs || e.to() == ht;
            boolean isHEdge = (e.from() == hs && e.to() == ht)
                    || (e.from() == ht && e.to() == hs);
            if (adjacent && !isHEdge) {
                vCandidates.add(new int[]{e.from(), e.to()});
            }
        }

        for (int[] cand : vCandidates) {
            for (int[] dir : new int[][]{{cand[0], cand[1]}, {cand[1], cand[0]}}) {
                int vs = dir[0], vt = dir[1];
                long[] y = stretch(graph, vs, vt);
                if (y == null) continue;
                List<Square> tiling = convertToTiling(graph, x, y);
                if (tiling != null && isValidTiling(tiling)) {
                    System.out.printf("Vertical stretch over edge %d-%d%n", vs, vt);
                    return normalize(tiling);
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // JUnit tests
    // ------------------------------------------------------------------

    @Test
    void stretchHorizontalSampleMatchesProvidedEmbedding() {
        // stretch(HORIZONTAL, 0, 5): start=0, end=det=75, free = 25,30,45,50
        // (= the provided coordinates x 5: "divided by 5" gave the sample).
        long[] x = stretch(createSampleDualGraph(), 0, 5);
        assertNotNull(x);
        assertArrayEquals(new long[]{0, 25, 30, 45, 50, 75}, x);
    }

    @Test
    void stretchVerticalSampleMatchesProvidedEmbedding() {
        // stretch(VERTICAL, 2, 3): start=0, end=det=25, free = 5,10,15,20.
        long[] y = stretch(createSampleDualGraph(), 2, 3);
        assertNotNull(y);
        assertArrayEquals(new long[]{5, 10, 0, 25, 15, 20}, y);
    }

    @Test
    void sampleStretchPairReproducesSampleTiling() {
        DualGraph graph = createSampleDualGraph();
        long[] x = stretch(graph, 0, 5);
        long[] y = stretch(graph, 2, 3);
        List<Square> tiling = normalize(convertToTiling(graph, x, y));

        assertEquals(createSampleTiling(), new LinkedHashSet<>(tiling),
                "the known stretch pair (0-5 horizontal, 2-3 vertical) must"
                        + " reproduce the sample tiling");
    }

    @Test
    void enumerationFindsExactlyAllDistinctTilings() {
        List<List<Square>> tilings = enumerateAllTilings(createSampleDualGraph());
        // Verified by simulation: 596 valid stretch combinations collapse
        // to 118 distinct tilings of this dual graph.
        assertEquals(118, tilings.size(),
                "the dual graph admits 118 distinct square tilings");
        for (List<Square> tiling : tilings) {
            assertTrue(isValidTiling(tiling), "every enumerated tiling is valid");
        }
    }

    @Test
    void enumerationContainsSampleTiling() {
        for (List<Square> tiling : enumerateAllTilings(createSampleDualGraph())) {
            if (new LinkedHashSet<>(tiling).equals(createSampleTiling())) {
                return; // found
            }
        }
        fail("the sample tiling must be among the enumerated tilings");
    }

    @Test
    void saveTilingsAsJpgWritesAllTilings() throws IOException {
        File dir = java.nio.file.Files.createTempDirectory("tilings").toFile();
        try {
            int written = saveTilingsAsJpg(dir);
            assertEquals(118, written, "one JPG per distinct tiling");
            File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg"));
            assertNotNull(files);
            assertEquals(118, files.length);
            for (File f : files) {
                assertTrue(f.length() > 0, "every file must have content");
                // Sanity: a valid JPG must be readable by ImageIO.
                BufferedImage img = ImageIO.read(f);
                assertNotNull(img, "ImageIO must be able to read " + f.getName());
                assertEquals(IMG_SIZE, img.getWidth());
                assertEquals(IMG_SIZE, img.getHeight());
            }
        } finally {
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }

    // ------------------------------------------------------------------
    // Loading graphs from the configuration database
    // ------------------------------------------------------------------

    /**
     * Builds a DualGraph from a Configuration's adjacency matrix.
     * adj[i][j] == true means an edge i-j; every true entry above the
     * diagonal becomes one DualEdge.
     */
    private DualGraph dualGraphFromConfiguration(Configuration config) {
        boolean[][] adj = config.getAdjacencyMatrix();
        int n = adj.length;
        List<DualEdge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (adj[i][j]) {
                    edges.add(new DualEdge(i, j, 1));
                }
            }
        }
        return new DualGraph(n, edges);
    }

    /**
     * Loads the dual graph stored in the database under the given key.
     */
    private DualGraph loadDualGraphFromDb(long dbKey) {
        Configuration config = DatabaseLoader.loadConfiguration(dbKey);
        System.out.println("Points: " + config.getPoints());
        ConfigurationTestUtils.printAdjacencyMatrix(config.getAdjacencyMatrix());
        System.out.println("Degrees: " + config.getDegrees());
        return dualGraphFromConfiguration(config);
    }

    // ------------------------------------------------------------------
    // JPG export: save each tiling as a separate picture (headless, no JavaFX)
    // ------------------------------------------------------------------

    private static final int IMG_SIZE = 800;   // pixels per picture
    private static final int IMG_PAD = 40;     // margin inside each picture

    /**
     * Saves every tiling as a separate JPG: tiling-001.jpg, tiling-002.jpg, ...
     * Pure java.awt + ImageIO, so it runs headlessly without JavaFX.
     *
     * @param outputDir directory to write into (created if missing)
     * @return the number of files written
     */
    public int saveTilingsAsJpg(File outputDir) throws IOException {
        List<List<Square>> tilings = enumerateAllTilings(createSampleDualGraph());
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("cannot create " + outputDir);
        }

        int index = 1;
        for (List<Square> tiling : tilings) {
            BufferedImage img = new BufferedImage(IMG_SIZE, IMG_SIZE,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, IMG_SIZE, IMG_SIZE);
            drawTiling(g, tiling);
            g.dispose();

            File file = new File(outputDir,
                    String.format("tiling-%03d.jpg", index++));
            ImageIO.write(img, "jpg", file);
        }
        return index - 1;
    }

    /** Draws one tiling, fitted and centered in the image (y flipped).
     *  Each square gets a distinct color (same palette as the gallery). */
    private static void drawTiling(Graphics2D g, List<Square> tiling) {
        long minX = Long.MAX_VALUE, minY = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE, maxY = Long.MIN_VALUE;
        for (Square s : tiling) {
            minX = Math.min(minX, s.x());
            minY = Math.min(minY, s.y());
            maxX = Math.max(maxX, s.x() + s.size());
            maxY = Math.max(maxY, s.y() + s.size());
        }
        long boxW = maxX - minX, boxH = maxY - minY;

        double scale = Math.min((double) (IMG_SIZE - 2 * IMG_PAD) / boxW,
                               (double) (IMG_SIZE - 2 * IMG_PAD) / boxH);
        double offX = (IMG_SIZE - boxW * scale) / 2;
        double offY = (IMG_SIZE - boxH * scale) / 2;

        int index = 0;
        for (Square s : tiling) {
            // Math y points up, image y points down: flip.
            int px = (int) Math.round(offX + (s.x() - minX) * scale);
            int py = (int) Math.round(offY + (boxH - (s.y() + s.size() - minY)) * scale);
            int len = (int) Math.round(s.size() * scale);
            // Same golden-ratio hue scheme as the gallery, as opaque RGB.
            double hue = (index++ * 0.618033988749895) % 1.0;
            java.awt.Color hsb = java.awt.Color.getHSBColor((float) hue, 0.55f, 0.85f);
            g.setColor(hsb);
            g.fillRect(px, py, len, len);
            g.setColor(new java.awt.Color(0x2F4F4F));        // darkslategray border
            g.setStroke(new java.awt.BasicStroke(2f));
            g.drawRect(px, py, len, len);
        }
    }

    // ------------------------------------------------------------------
    // Manual visualization (NOT a @Test - blocks until the window closes)
    // ------------------------------------------------------------------

    /** Displays ALL tilings from all valid stretches in one gallery. */
    public void displayAllTilings() {
        displayTilings(enumerateAllTilings(createSampleDualGraph()));
    }

    /**
     * Loads the graph for the given dbKey and displays the FIRST valid tiling,
     * using the fixed-horizontal strategy: the horizontal stretch is the
     * unique degree3-degree3 edge; only vertical pairs are searched.
     */
    public void displayFirstTilingFromDb(long dbKey) {
        DualGraph graph = loadDualGraphFromDb(dbKey);
        List<Square> tiling = findFirstTilingFixedHorizontal(graph);
        if (tiling == null) {
            throw new IllegalStateException("no valid tiling for dbKey " + dbKey);
        }
        displayTilings(List.of(tiling));
    }

    /** Shared launch: one or more tilings in the Visualizer window. */
    private void displayTilings(List<List<Square>> tilings) {
        System.out.printf("Showing %d tiling(s)%n", tilings.size());
        for (List<Square> tiling : tilings) {
            for (Square s : tiling) {
                System.out.printf("Square at (%d, %d) with side length %d%n",
                        s.x(), s.y(), s.size());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (List<Square> tiling : tilings) {
            Map<double[], Integer> asMap = new HashMap<>();
            for (Square s : tiling) {
                asMap.put(new double[]{s.x(), s.y()}, s.size());
            }
            if (sb.length() > 0) sb.append('|');
            sb.append(tilingToString(asMap));
        }
        Application.launch(Visualizer.class, sb.toString());
    }

    /** Serializes the tiling so the JavaFX Application thread can rebuild it. */
    private static String tilingToString(Map<double[], Integer> tiling) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<double[], Integer> e : tiling.entrySet()) {
            sb.append(e.getKey()[0]).append(',').append(e.getKey()[1])
              .append(',').append(e.getValue()).append(';');
        }
        return sb.toString();
    }

    /** Assigns a distinct color to each square (evenly spread hues). */
    private static Color colorForIndex(int index, int total, double opacity) {
        // Golden-ratio hue spacing avoids near-duplicates between neighbors.
        double hue = (index * 0.618033988749895) % 1.0;
        double sat = 0.55;
        double bright = 0.85;
        return Color.hsb(hue * 360, sat, bright, opacity);
    }

    /** One tiling prepared for layout: rects plus its math bounding box. */
    private static final class TilingVisual {
        final List<Rectangle> rects = new ArrayList<>();
        final List<double[]> corners = new ArrayList<>();
        final List<Integer> lengths = new ArrayList<>();
        final double boxX, boxY, boxW, boxH;

        TilingVisual(Map<double[], Integer> tiling) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            int index = 0;
            for (Map.Entry<double[], Integer> e : tiling.entrySet()) {
                double[] c = e.getKey();
                int len = e.getValue();
                Rectangle rect = new Rectangle();
                rect.setFill(colorForIndex(index++, tiling.size(), 0.9));
                rect.setStroke(Color.DARKSLATEGRAY);
                rect.setStrokeType(StrokeType.INSIDE);
                rect.setStrokeWidth(1.5);
                rects.add(rect);
                corners.add(c);
                lengths.add(len);
                minX = Math.min(minX, c[0]);
                minY = Math.min(minY, c[1]);
                maxX = Math.max(maxX, c[0] + len);
                maxY = Math.max(maxY, c[1] + len);
            }
            boxX = minX; boxY = minY;
            boxW = maxX - minX; boxH = maxY - minY;
        }
    }

    /**
     * JavaFX application that draws one tiling, or a gallery of tilings
     * (multiple tilings arranged in a square-ish grid of cells, each tiling
     * auto-fitted inside its cell).
     */
    public static class Visualizer extends Application {
        @Override
        public void start(Stage stage) {
            String[] tilingTokens = getParameters().getRaw().get(0).split("\\|");
            final List<TilingVisual> visuals = new ArrayList<>();
            for (String tilingToken : tilingTokens) {
                Map<double[], Integer> tiling = new HashMap<>();
                for (String token : tilingToken.split(";")) {
                    if (token.isEmpty()) continue;
                    String[] parts = token.split(",");
                    tiling.put(new double[]{Double.parseDouble(parts[0]),
                                            Double.parseDouble(parts[1])},
                               Integer.parseInt(parts[2]));
                }
                visuals.add(new TilingVisual(tiling));
            }

            final Pane pane = new Pane();
            pane.setPrefSize(1200, 800);
            pane.setStyle("-fx-background-color: white;");
            for (TilingVisual v : visuals) {
                pane.getChildren().addAll(v.rects);
            }

            Runnable layout = () -> {
                double W = pane.getWidth(), H = pane.getHeight();
                if (W <= 0 || H <= 0) return;
                int n = visuals.size();
                int cols = (int) Math.ceil(Math.sqrt(n));
                int rows = (int) Math.ceil((double) n / cols);
                double cellW = W / cols, cellH = H / rows;

                for (int i = 0; i < n; i++) {
                    TilingVisual v = visuals.get(i);
                    int col = i % cols, row = i / cols;
                    double availW = cellW - 2 * PADDING;
                    double availH = cellH - 2 * PADDING;
                    if (availW <= 0 || availH <= 0 || v.boxW <= 0 || v.boxH <= 0) continue;
                    double scale = Math.min(availW / v.boxW, availH / v.boxH);
                    double offX = col * cellW + (cellW - v.boxW * scale) / 2;
                    double offY = row * cellH + (cellH - v.boxH * scale) / 2;

                    for (int j = 0; j < v.rects.size(); j++) {
                        double[] c = v.corners.get(j);
                        int len = v.lengths.get(j);
                        Rectangle rect = v.rects.get(j);
                        rect.setX(offX + (c[0] - v.boxX) * scale);
                        // Flip y: math y points up, screen y points down.
                        rect.setY(offY + (v.boxH - (c[1] + len - v.boxY)) * scale);
                        rect.setWidth(len * scale);
                        rect.setHeight(len * scale);
                    }
                }
            };
            pane.widthProperty().addListener((obs, o, nv) -> layout.run());
            pane.heightProperty().addListener((obs, o, nv) -> layout.run());

            stage.setTitle("Square Tilings (" + visuals.size() + ")");
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();
            stage.setMinWidth(300);
            stage.setMinHeight(200);
        }
    }

    public static void main(String[] args) throws IOException {
    	
    	args = new String[]{"--db", "1066"};
    	
        if (args.length > 0 && args[0].equals("--db")) {
            // Visualize the first tiling of the graph stored under a dbKey:
            //   java SquareTilingTest --db 1056
            long dbKey = Long.parseLong(args[1]);
            new SquareTilingTest().displayFirstTilingFromDb(dbKey);
        } else if (args.length > 0 && args[0].equals("--save-jpg")) {
            // Save all tilings as separate JPGs into the given directory.
            File dir = new File(args.length > 1 ? args[1] : "tilings");
            int written = new SquareTilingTest().saveTilingsAsJpg(dir);
            System.out.println("Saved " + written + " JPGs to " + dir);
        } else {
            // Show the gallery window instead.
            new SquareTilingTest().displayAllTilings();
        }
    }
}