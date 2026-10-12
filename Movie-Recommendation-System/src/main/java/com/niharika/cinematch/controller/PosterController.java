package com.niharika.cinematch.controller;

import com.niharika.cinematch.model.Movie;
import com.niharika.cinematch.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PosterController {

    @Autowired
    private MovieService movieService;

    private final ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();
    private final Pattern sourcePattern = Pattern.compile("\"source\"\\s*:\\s*\"([^\"]+)\"");

    public PosterController() {
        seedCache();
    }

    private void seedCache() {
        // High-frequency homepage movies
        cache.put(211672, "https://upload.wikimedia.org/wikipedia/en/1/19/Minions_%282015_film%29.jpg");
        cache.put(157336, "https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg");
        cache.put(293660, "https://upload.wikimedia.org/wikipedia/en/2/23/Deadpool_%282016_poster%29.png");
        cache.put(118340, "https://upload.wikimedia.org/wikipedia/en/3/33/Guardians_of_the_Galaxy_%28film%29_poster.jpg");
        cache.put(76341, "https://upload.wikimedia.org/wikipedia/en/6/6e/Mad_Max_Fury_Road.jpg");
        cache.put(135397, "https://upload.wikimedia.org/wikipedia/en/6/6e/Jurassic_World_poster.jpg");
        cache.put(22, "https://upload.wikimedia.org/wikipedia/en/8/89/Pirates_of_the_Caribbean_-_The_Curse_of_the_Black_Pearl.png");
        cache.put(119450, "https://upload.wikimedia.org/wikipedia/en/7/77/Dawn_of_the_Planet_of_the_Apes.jpg");
        cache.put(131631, "https://upload.wikimedia.org/wikipedia/en/6/63/MockingjayPart1Poster3.jpg");
        cache.put(177572, "https://upload.wikimedia.org/wikipedia/en/4/4b/Big_Hero_6_%28film%29_poster.jpg");
        cache.put(87101, "https://upload.wikimedia.org/wikipedia/en/b/bc/Terminator_Genisys.JPG");
        cache.put(271110, "https://upload.wikimedia.org/wikipedia/en/5/53/Captain_America_Civil_War_poster.jpg");
        cache.put(244786, "https://upload.wikimedia.org/wikipedia/en/0/01/Whiplash_poster.jpg");
        cache.put(155, "https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29.jpg");
        cache.put(286217, "https://upload.wikimedia.org/wikipedia/en/3/37/The_Martian_film_poster.jpg");
        cache.put(27205, "https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg");
        cache.put(109445, "https://upload.wikimedia.org/wikipedia/en/0/05/Frozen_%282013_film%29_poster.jpg");
        cache.put(209112, "https://upload.wikimedia.org/wikipedia/en/2/20/Batman_v_Superman_poster.jpg");
        cache.put(19995, "https://upload.wikimedia.org/wikipedia/en/d/d6/Avatar_%282009_film%29_poster.jpg");
        cache.put(550, "https://upload.wikimedia.org/wikipedia/en/f/fc/Fight_Club_poster.jpg");
        cache.put(58, "https://upload.wikimedia.org/wikipedia/en/2/29/Pirates_of_the_Caribbean_Dead_Man%27s_Chest.jpg");
        cache.put(205596, "https://upload.wikimedia.org/wikipedia/en/5/5e/The_Imitation_Game_poster.jpg");
        cache.put(24428, "https://upload.wikimedia.org/wikipedia/en/8/8a/The_Avengers_%282012_film%29_poster.jpg");
        cache.put(238, "https://upload.wikimedia.org/wikipedia/en/1/1c/Godfather_ver1.jpg");
        cache.put(98566, "https://upload.wikimedia.org/wikipedia/en/7/7b/TMNT_2014_poster.jpg");
        cache.put(210577, "https://upload.wikimedia.org/wikipedia/en/0/05/Gone_Girl_Poster.jpg");
        cache.put(257344, "https://upload.wikimedia.org/wikipedia/en/0/01/Pixels_Poster.jpg");
        cache.put(228150, "https://upload.wikimedia.org/wikipedia/en/1/17/Fury_2014_poster.jpg");
        cache.put(246655, "https://upload.wikimedia.org/wikipedia/en/0/04/X-Men_-_Apocalypse.png");
        cache.put(285, "https://upload.wikimedia.org/wikipedia/en/5/5a/Pirates_of_the_Caribbean_-_At_World%27s_End.jpg");

        // Frequently recommended titles
        cache.put(9620, "https://upload.wikimedia.org/wikipedia/en/b/bf/Paycheck_poster.jpg");
        cache.put(5137, "https://upload.wikimedia.org/wikipedia/en/2/23/Sky_captain_poster.jpg");
        cache.put(13811, "https://upload.wikimedia.org/wikipedia/en/0/04/Knowing_poster.jpg");
        cache.put(10329, "https://upload.wikimedia.org/wikipedia/en/6/6d/Congo_poster.jpg");
        cache.put(198663, "https://upload.wikimedia.org/wikipedia/en/b/be/The_Maze_Runner_poster.jpg");
        cache.put(16320, "https://upload.wikimedia.org/wikipedia/en/9/9e/Serenity_One_Sheet.jpg");
        cache.put(106, "https://upload.wikimedia.org/wikipedia/en/9/95/Predator_Movie.jpg");
        cache.put(180, "https://upload.wikimedia.org/wikipedia/en/4/44/Minority_Report_Poster.jpg");
        cache.put(604, "https://upload.wikimedia.org/wikipedia/en/b/ba/Poster_-_The_Matrix_Reloaded.jpg");
        cache.put(605, "https://upload.wikimedia.org/wikipedia/en/3/34/Matrix_revolutions_ver7.jpg");
        cache.put(563, "https://upload.wikimedia.org/wikipedia/en/d/df/Starship_Troopers_-_film_poster.jpg");
        cache.put(2756, "https://upload.wikimedia.org/wikipedia/en/2/20/The_Abyss_poster.jpg");
        cache.put(199, "https://upload.wikimedia.org/wikipedia/en/1/12/Star_Trek_First_Contact_poster.jpg");
        cache.put(75612, "https://upload.wikimedia.org/wikipedia/en/2/22/Oblivion_Poster.jpg");
        cache.put(2059, "https://upload.wikimedia.org/wikipedia/en/6/6f/National_Treasure_poster.jpg");
        cache.put(603, "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Matrix_Poster.jpg");
        cache.put(597, "https://upload.wikimedia.org/wikipedia/en/1/18/Titanic_%281997_film%29_poster.png");
        cache.put(680, "https://upload.wikimedia.org/wikipedia/en/3/3b/Pulp_Fiction_%281994%29_poster.jpg");
        cache.put(13, "https://upload.wikimedia.org/wikipedia/en/6/67/Forrest_Gump_poster.jpg");
        cache.put(278, "https://upload.wikimedia.org/wikipedia/en/3/34/The_Shawshank_Redemption_poster.jpg");
        cache.put(120, "https://upload.wikimedia.org/wikipedia/en/8/8a/The_Lord_of_the_Rings_The_Fellowship_of_the_Ring_%282001%29.jpg");
        cache.put(121, "https://upload.wikimedia.org/wikipedia/en/a/ad/Lord_of_the_Rings_-_The_Two_Towers.jpg");
        cache.put(122, "https://upload.wikimedia.org/wikipedia/en/b/be/The_Lord_of_the_Rings_-_The_Return_of_the_King.jpg");
        cache.put(671, "https://upload.wikimedia.org/wikipedia/en/7/7a/Harry_Potter_and_the_Philosopher%27s_Stone_banner.jpg");
        cache.put(672, "https://upload.wikimedia.org/wikipedia/en/c/c0/Harry_Potter_and_the_Chamber_of_Secrets_movie.jpg");
        cache.put(673, "https://upload.wikimedia.org/wikipedia/en/b/bc/Prisoner_of_azkaban_UK_poster.jpg");
    }

    @GetMapping("/movies/{id}/poster")
    public ResponseEntity<?> getPosterUrl(@PathVariable int id) {
        if (cache.containsKey(id)) {
            return redirect(cache.get(id));
        }

        Optional<Movie> movieOpt = movieService != null ? movieService.getMovieById(id) : Optional.empty();
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();
        String posterUrl = resolvePosterFromWikipedia(movie);
        if (posterUrl != null && !posterUrl.isBlank()) {
            cache.put(id, posterUrl);
            return redirect(posterUrl);
        }

        // Beautiful SVG poster fallback if not found on Wikipedia
        String svg = generateSvgPoster(movie);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/svg+xml;charset=UTF-8")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(svg);
    }

    private String resolvePosterFromWikipedia(Movie movie) {
        String title = movie.getTitle();
        if (title == null || title.isBlank()) return null;

        String year = "";
        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            year = movie.getReleaseDate().substring(0, 4);
        }

        List<String> candidates = new ArrayList<>();
        if (!year.isBlank()) {
            candidates.add(title + " (" + year + " film)");
        }
        candidates.add(title + " (film)");
        candidates.add(title);

        for (String candidate : candidates) {
            String url = fetchSummaryThumbnail(candidate);
            if (url != null) return url;
        }

        return null;
    }

    private String fetchSummaryThumbnail(String pageTitle) {
        try {
            String slug = pageTitle.replace(' ', '_');
            String endpoint = "https://en.wikipedia.org/api/rest_v1/page/summary/"
                    + URLEncoder.encode(slug, StandardCharsets.UTF_8);

            URL url = URI.create(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "CineMatch/1.0 (https://github.com/niharika-cmyk; student@klh.edu.in)");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(2500);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            Matcher m = sourcePattern.matcher(sb.toString());
            if (m.find()) {
                String imgUrl = m.group(1).replace("\\/", "/").replace("&amp;", "&");
                if (imgUrl.startsWith("http")) {
                    return imgUrl;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ResponseEntity<?> redirect(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String generateSvgPoster(Movie movie) {
        String title = movie.getTitle() != null ? escapeXml(movie.getTitle()) : "Film";
        String year = (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4)
                ? movie.getReleaseDate().substring(0, 4) : "";
        String rating = movie.getRating() > 0 ? String.format(Locale.US, "%.1f", movie.getRating()) : "";
        String genre = (movie.getGenres() != null && !movie.getGenres().isEmpty())
                ? escapeXml(String.join(" · ", movie.getGenres().subList(0, Math.min(2, movie.getGenres().size()))))
                : "";

        String displayTitle = title.length() > 24 ? title.substring(0, 22) + "…" : title;

        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 300 450\" width=\"300\" height=\"450\">"
                + "<defs>"
                + "<linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">"
                + "<stop offset=\"0%\" stop-color=\"#1e1b4b\"/>"
                + "<stop offset=\"50%\" stop-color=\"#0f172a\"/>"
                + "<stop offset=\"100%\" stop-color=\"#312e81\"/>"
                + "</linearGradient>"
                + "</defs>"
                + "<rect width=\"300\" height=\"450\" fill=\"url(#g)\"/>"
                + "<circle cx=\"150\" cy=\"180\" r=\"85\" fill=\"#ffffff\" fill-opacity=\"0.04\"/>"
                + "<circle cx=\"150\" cy=\"180\" r=\"65\" fill=\"#ffffff\" fill-opacity=\"0.06\"/>"
                + "<g transform=\"translate(130, 160) scale(1.6)\">"
                + "<path d=\"M4 4h16v16H4z\" fill=\"none\" stroke=\"#f43f5e\" stroke-width=\"1.8\" rx=\"2\"/>"
                + "<circle cx=\"8\" cy=\"8\" r=\"1.2\" fill=\"#ffffff\"/>"
                + "<circle cx=\"16\" cy=\"8\" r=\"1.2\" fill=\"#ffffff\"/>"
                + "<circle cx=\"8\" cy=\"16\" r=\"1.2\" fill=\"#ffffff\"/>"
                + "<circle cx=\"16\" cy=\"16\" r=\"1.2\" fill=\"#ffffff\"/>"
                + "<polygon points=\"10,10 15,12 10,14\" fill=\"#ffffff\"/>"
                + "</g>"
                + (rating.isEmpty() ? "" :
                   "<rect x=\"20\" y=\"20\" width=\"55\" height=\"24\" rx=\"12\" fill=\"#000000\" fill-opacity=\"0.55\"/>"
                   + "<text x=\"32\" y=\"36\" font-family=\"sans-serif\" font-size=\"12\" font-weight=\"bold\" fill=\"#f59e0b\">★</text>"
                   + "<text x=\"48\" y=\"36\" font-family=\"sans-serif\" font-size=\"12\" font-weight=\"bold\" fill=\"#ffffff\">" + rating + "</text>")
                + (year.isEmpty() ? "" :
                   "<rect x=\"225\" y=\"20\" width=\"55\" height=\"24\" rx=\"12\" fill=\"#000000\" fill-opacity=\"0.55\"/>"
                   + "<text x=\"252\" y=\"36\" font-family=\"sans-serif\" font-size=\"11\" font-weight=\"600\" fill=\"#94a3b8\" text-anchor=\"middle\">" + year + "</text>")
                + "<text x=\"150\" y=\"330\" font-family=\"sans-serif\" font-size=\"20\" font-weight=\"bold\" fill=\"#ffffff\" text-anchor=\"middle\">"
                + displayTitle + "</text>"
                + (genre.isEmpty() ? "" :
                   "<text x=\"150\" y=\"360\" font-family=\"sans-serif\" font-size=\"12\" font-weight=\"500\" fill=\"#94a3b8\" text-anchor=\"middle\">" + genre + "</text>")
                + "<text x=\"150\" y=\"410\" font-family=\"sans-serif\" font-size=\"10\" font-weight=\"600\" letter-spacing=\"1.5\" fill=\"#475569\" text-anchor=\"middle\">CINEMATCH</text>"
                + "</svg>";
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
