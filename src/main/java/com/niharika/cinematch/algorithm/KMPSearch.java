package com.niharika.cinematch.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * CO2: KMP (Knuth-Morris-Pratt) Algorithm
 * Used for fast movie title search — O(n+m) linear-time string matching.
 * Avoids redundant comparisons using a failure function (prefix table).
 */
public class KMPSearch {

    /**
     * Builds the prefix (failure) table for pattern.
     * lps[i] = length of longest proper prefix of pattern[0..i] that is also a suffix.
     */
    private static int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    /**
     * Returns all starting indices in text where pattern occurs (case-insensitive).
     * CO2 topic: KMP linear-time pattern matching.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern == null || pattern.isEmpty()) return matches;

        String t = text.toLowerCase();
        String p = pattern.toLowerCase();
        int n = t.length();
        int m = p.length();
        int[] lps = buildLPS(p);

        int i = 0, j = 0;
        while (i < n) {
            if (t.charAt(i) == p.charAt(j)) {
                i++; j++;
            }
            if (j == m) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < n && t.charAt(i) != p.charAt(j)) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
        return matches;
    }

    /**
     * Returns true if the text contains the pattern (case-insensitive).
     */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
}
