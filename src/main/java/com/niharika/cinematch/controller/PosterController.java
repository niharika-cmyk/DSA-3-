package com.niharika.cinematch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PosterController {
    
    private final ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();
    private final Pattern pattern = Pattern.compile("meta property=\"og:image\" content=\"([^\"]+)\"");

    @GetMapping("/movies/{id}/poster")
    public ResponseEntity<Void> getPosterUrl(@PathVariable int id) {
        if (cache.containsKey(id)) {
            return redirect(cache.get(id));
        }
        try {
            URL url = new URL("https://www.themoviedb.org/movie/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            String foundUrl = "";
            while ((inputLine = in.readLine()) != null) {
                Matcher m = pattern.matcher(inputLine);
                if (m.find()) {
                    foundUrl = m.group(1);
                    break;
                }
            }
            in.close();
            
            if (!foundUrl.isEmpty()) {
                cache.put(id, foundUrl);
                return redirect(foundUrl);
            }
        } catch (Exception e) {
            // Ignore silently
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Void> redirect(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
