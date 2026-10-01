package com.niharika.cinematch.algorithm;

import java.util.*;

/**
 * CO4: Network Flow (Edmonds-Karp / BFS-based Ford-Fulkerson)
 *
 * Models the recommendation problem as a flow network:
 *   - Source node (S) connects to each query-movie genre with capacity 1
 *   - Each genre node connects to candidate movies with capacity 1
 *   - Each candidate movie connects to Sink (T) with capacity 1
 *
 * The maximum flow from S → T gives the number of genre-matched paths,
 * which represents the recommendation strength. Movies with higher flow
 * are ranked first.
 *
 * The max-flow equals the maximum matching between query genres and
 * candidate movies — a direct application of CO4 (max-flow / min-cut duality).
 */
public class EdmondsKarp {

    private final int n;
    private final int[][] capacity;
    private final List<List<Integer>> adj;

    public EdmondsKarp(int n) {
        this.n = n;
        this.capacity = new int[n][n];
        this.adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int cap) {
        adj.get(u).add(v);
        adj.get(v).add(u);
        capacity[u][v] += cap;
    }

    /**
     * BFS to find an augmenting path from source to sink.
     * Returns the parent array if a path exists, or null otherwise.
     */
    private int[] bfs(int source, int sink) {
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        parent[source] = source;
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.get(u)) {
                if (parent[v] == -1 && capacity[u][v] > 0) {
                    parent[v] = u;
                    if (v == sink) return parent;
                    queue.offer(v);
                }
            }
        }
        return null;
    }

    /**
     * Edmonds-Karp max-flow: O(VE^2).
     * CO4: Ford-Fulkerson with BFS (Edmonds-Karp variant).
     */
    public int maxFlow(int source, int sink) {
        int flow = 0;
        int[] parent;

        while ((parent = bfs(source, sink)) != null) {
            // Find bottleneck capacity along the path
            int pathFlow = Integer.MAX_VALUE;
            int v = sink;
            while (v != source) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v]);
                v = u;
            }
            // Update capacities along the path
            v = sink;
            while (v != source) {
                int u = parent[v];
                capacity[u][v] -= pathFlow;
                capacity[v][u] += pathFlow;
                v = u;
            }
            flow += pathFlow;
        }
        return flow;
    }

    /**
     * Computes recommendation flow score for a candidate movie.
     *
     * Network layout (node indices):
     *   0         = Source
     *   1..G      = Genre nodes (G = number of shared genres)
     *   G+1       = Candidate movie node
     *   G+2       = Sink
     *
     * @param sharedGenreCount  number of genres shared between query and candidate
     * @return max-flow value = recommendation strength
     */
    public static int computeRecommendationFlow(int sharedGenreCount) {
        if (sharedGenreCount == 0) return 0;
        int G = sharedGenreCount;
        int nodeCount = G + 3; // source, G genres, movie, sink
        int source = 0, sink = G + 2, movieNode = G + 1;

        EdmondsKarp ek = new EdmondsKarp(nodeCount);
        for (int g = 1; g <= G; g++) {
            ek.addEdge(source, g, 1);     // source → genre
            ek.addEdge(g, movieNode, 1);  // genre → movie
        }
        ek.addEdge(movieNode, sink, G);   // movie → sink (capacity = all genres)

        return ek.maxFlow(source, sink);
    }
}
