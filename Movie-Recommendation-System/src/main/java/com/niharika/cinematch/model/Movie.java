package com.niharika.cinematch.model;

import java.util.List;

public class Movie {
    private int id;
    private String title;
    private String overview;
    private List<String> genres;
    private double rating;
    private double popularity;
    private String releaseDate;
    private String posterUrl;
    private int runtime;

    public Movie() {}

    public Movie(int id, String title, String overview, List<String> genres,
                 double rating, double popularity, String releaseDate, int runtime) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.genres = genres;
        this.rating = rating;
        this.popularity = popularity;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.posterUrl = "https://image.tmdb.org/t/p/w500/";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }
}
