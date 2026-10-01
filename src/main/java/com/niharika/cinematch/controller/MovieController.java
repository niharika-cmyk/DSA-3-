package com.niharika.cinematch.controller;

import com.niharika.cinematch.model.Movie;
import com.niharika.cinematch.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MovieController {

    @Autowired
    private MovieService movieService;

    /**
     * GET /api/movies/top
     * Returns top 30 popular movies for the home feed.
     */
    @GetMapping("/movies/top")
    public ResponseEntity<List<Movie>> getTopMovies() {
        return ResponseEntity.ok(movieService.getTopMovies());
    }

    /**
     * GET /api/movies/search?q=<query>
     * CO2: KMP-based title search.
     */
    @GetMapping("/movies/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String q) {
        return ResponseEntity.ok(movieService.searchMovies(q));
    }

    /**
     * GET /api/movies/{id}
     * Returns a single movie by TMDB id.
     */
    @GetMapping("/movies/{id}")
    public ResponseEntity<?> getMovie(@PathVariable int id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/movies/{id}/recommendations
     * CO1 + CO3 + CO4: Get genre-based recommendations for a movie.
     */
    @GetMapping("/movies/{id}/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations(@PathVariable int id) {
        return ResponseEntity.ok(movieService.getRecommendations(id));
    }
}
