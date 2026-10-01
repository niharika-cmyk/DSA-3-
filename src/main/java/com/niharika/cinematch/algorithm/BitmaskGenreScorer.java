package com.niharika.cinematch.algorithm;

import java.util.List;

/**
 * CO3: Bitmask DP for Genre Overlap Scoring
 *
 * Each genre is assigned a unique bit position. A movie's genre set is
 * encoded as an integer bitmask. The recommendation score is based on
 * how many genres the candidate movie shares with the query movie
 * (popcount of AND of the two bitmasks).
 *
 * This is a classic bitmask DP application: optimal subset-matching
 * over a universe of genres. The bitmask approach runs in O(1) per
 * comparison (after O(G) encoding), making it highly efficient for
 * large catalogs.
 */
public class BitmaskGenreScorer {

    // All TMDB genre names mapped to bit positions (up to 32 genres)
    private static final String[] GENRE_INDEX = {
        "Action", "Adventure", "Animation", "Comedy", "Crime",
        "Documentary", "Drama", "Family", "Fantasy", "History",
        "Horror", "Music", "Mystery", "Romance", "Science Fiction",
        "TV Movie", "Thriller", "War", "Western", "Foreign"
    };

    /**
     * Encodes a list of genre names into an integer bitmask.
     * CO3: bitmask encoding of a subset.
     */
    public static int encode(List<String> genres) {
        int mask = 0;
        if (genres == null) return 0;
        for (String g : genres) {
            for (int i = 0; i < GENRE_INDEX.length; i++) {
                if (GENRE_INDEX[i].equalsIgnoreCase(g.trim())) {
                    mask |= (1 << i);
                    break;
                }
            }
        }
        return mask;
    }

    /**
     * Computes the genre overlap score between two bitmasks.
     * Returns Integer.bitCount(a & b) — the number of shared genres.
     * CO3: popcount on bitmask intersection = DP on subsets.
     */
    public static int overlapScore(int maskA, int maskB) {
        return Integer.bitCount(maskA & maskB);
    }

    /**
     * Normalized score in [0.0, 1.0]:
     *   score = |A ∩ B| / |A ∪ B|   (Jaccard similarity via bitmasks)
     */
    public static double jaccardScore(int maskA, int maskB) {
        int intersection = Integer.bitCount(maskA & maskB);
        int union = Integer.bitCount(maskA | maskB);
        return union == 0 ? 0.0 : (double) intersection / union;
    }
}
