package com.niharika.cinematch.service;

import com.niharika.cinematch.algorithm.*;
import com.niharika.cinematch.model.Movie;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

@Service
public class MovieService {

    private List<Movie> movies = new ArrayList<>();

    // Pre-computed bitmask cache: movieId -> genreMask (computed once on startup)
    private final Map<Integer, Integer> bitmaskCache = new HashMap<>();

    private static final String MOVIES_CSV  = "datasets/tmdb_5000_movies.csv";

    @PostConstruct
    public void loadData() {
        System.out.println("Loading movie data from CSV...");
        try {
            movies = parseMovies(MOVIES_CSV);
            // Pre-compute all bitmasks once on startup (CO3 optimisation)
            movies.forEach(m -> bitmaskCache.put(m.getId(), BitmaskGenreScorer.encode(m.getGenres())));
            System.out.println("Loaded " + movies.size() + " movies, pre-computed " + bitmaskCache.size() + " genre bitmasks.");
        } catch (Exception e) {
            System.err.println("Failed to load CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    //  CSV Parsing
    // ---------------------------------------------------------------

    private List<Movie> parseMovies(String path) throws IOException {
        List<Movie> result = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Movie m = parseLine(line);
                    if (m != null) result.add(m);
                } catch (Exception ignored) {}
            }
        }
        return result;
    }

    private Movie parseLine(String line) {
        List<String> fields = tokenize(line);
        if (fields.size() < 20) return null;
        try {
            String genresJson    = fields.get(1);
            String idStr         = fields.get(3);
            String overview      = fields.get(7).trim();
            String popularityStr = fields.get(8);
            String releaseDate   = fields.get(11).trim();
            String runtimeStr    = fields.get(13);
            String title         = fields.get(17).trim();
            String ratingStr     = fields.get(18);

            int    id         = parseInt(idStr);
            double rating     = parseDouble(ratingStr);
            double popularity = parseDouble(popularityStr);
            int    runtime    = parseInt(runtimeStr);
            List<String> genres = extractNames(genresJson);

            return new Movie(id, title, overview, genres, rating, popularity, releaseDate, runtime);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> extractNames(String json) {
        List<String> names = new ArrayList<>();
        if (json == null || json.isBlank()) return names;
        int i = 0;
        while ((i = json.indexOf("\"name\"", i)) != -1) {
            int colon = json.indexOf(':', i);
            if (colon == -1) break;
            int q1 = json.indexOf('"', colon + 1);
            if (q1 == -1) break;
            int q2 = json.indexOf('"', q1 + 1);
            if (q2 == -1) break;
            names.add(json.substring(q1 + 1, q2));
            i = q2 + 1;
        }
        return names;
    }

    private List<String> tokenize(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields;
    }

    private int parseInt(String s) {
        try { return (int) Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }
    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    // ---------------------------------------------------------------
    //  Public API
    // ---------------------------------------------------------------

    /**
     * CO2: KMP-based movie title search — O(n+m) per movie.
     */
    public List<Movie> searchMovies(String query) {
        if (query == null || query.isBlank()) return List.of();
        return movies.stream()
                .filter(m -> KMPSearch.contains(m.getTitle(), query))
                .sorted(Comparator.comparingDouble(Movie::getPopularity).reversed())
                .limit(15)
                .collect(Collectors.toList());
    }

    /**
     * CO1 + CO3 + CO4: Recommendations via genre bitmask + Edmonds-Karp max-flow.
     *
     * Optimised pipeline:
     *   1. Fetch pre-computed queryMask from cache (O(1))
     *   2. Pre-filter using bitmask AND: skip movies with zero genre overlap -- O(1) per movie
     *   3. Only run Edmonds-Karp (CO4) on the genre-matching subset
     *   4. Sort by composite CO1 score and return top 20
     */
    public List<Map<String, Object>> getRecommendations(int movieId) {
        Optional<Movie> queryOpt = movies.stream().filter(m -> m.getId() == movieId).findFirst();
        if (queryOpt.isEmpty()) return List.of();

        Movie query = queryOpt.get();
        int queryMask = bitmaskCache.getOrDefault(movieId, BitmaskGenreScorer.encode(query.getGenres()));
        if (queryMask == 0) return List.of();

        return movies.stream()
                .filter(m -> m.getId() != movieId)
                .filter(m -> {
                    // CO3 fast bitmask pre-filter: bitwise AND tells us overlap in O(1)
                    int cm = bitmaskCache.getOrDefault(m.getId(), 0);
                    return (queryMask & cm) != 0;
                })
                .map(candidate -> {
                    int candMask   = bitmaskCache.getOrDefault(candidate.getId(), 0);
                    int overlap    = BitmaskGenreScorer.overlapScore(queryMask, candMask);
                    double jaccard = BitmaskGenreScorer.jaccardScore(queryMask, candMask);
                    // CO4: Edmonds-Karp max-flow — only runs for genre-matched movies
                    double score   = RecommendationScorer.computeScore(
                            queryMask, candMask, candidate.getRating(), candidate.getPopularity());

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("movie", candidate);
                    entry.put("score", score);
                    entry.put("genreOverlap", overlap);
                    entry.put("jaccard", jaccard);
                    return entry;
                })
                .sorted((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")))
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<Movie> getTopMovies() {
        return movies.stream()
                .sorted(Comparator.comparingDouble(Movie::getPopularity).reversed())
                .limit(30)
                .collect(Collectors.toList());
    }

    public Optional<Movie> getMovieById(int id) {
        return movies.stream().filter(m -> m.getId() == id).findFirst();
    }
}
