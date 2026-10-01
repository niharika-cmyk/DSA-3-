package com.niharika.cinematch.algorithm;

import java.util.*;

/**
 * CO1: Problem Classification + Algorithm Strategy Selection
 *
 * CO1 asks us to evaluate the problem-class signature and select an
 * appropriate advanced-algorithm strategy. Here we model the search
 * and recommendation as a multi-objective scoring problem and choose
 * the right algorithm tier per task:
 *
 *   1. Movie title search   → Substring search class → KMP (CO2)
 *   2. Genre similarity     → Subset-matching class  → Bitmask DP (CO3)
 *   3. Recommendation rank  → Network matching class → Max-flow (CO4)
 *
 * This class acts as the top-level strategy selector (CO1) that combines
 * scores from CO2, CO3, and CO4 into a final composite recommendation score.
 */
public class RecommendationScorer {

    /**
     * Computes the final composite recommendation score for a candidate movie.
     *
     * Score components:
     *   - flowScore   (CO4): Max-flow through genre network (primary ranking)
     *   - jaccardScore (CO3): Jaccard similarity via bitmask (tiebreaker)
     *   - ratingBonus  (CO1 strategy): Weighted rating bonus for quality
     *
     * @param queryMask      genre bitmask of the query movie
     * @param candidateMask  genre bitmask of the candidate movie
     * @param candidateRating TMDB vote average of candidate
     * @param candidatePopularity TMDB popularity of candidate
     * @return composite score (higher = better recommendation)
     */
    public static double computeScore(int queryMask, int candidateMask,
                                       double candidateRating, double candidatePopularity) {
        // CO3: Genre overlap via bitmask
        int overlap = BitmaskGenreScorer.overlapScore(queryMask, candidateMask);
        double jaccard = BitmaskGenreScorer.jaccardScore(queryMask, candidateMask);

        // CO4: Max-flow over genre network
        int flowScore = EdmondsKarp.computeRecommendationFlow(overlap);

        // CO1 strategy: composite weighted score
        // Weights: flow dominates, jaccard refines, rating/popularity add quality signal
        double score = (flowScore * 10.0)
                     + (jaccard * 5.0)
                     + (candidateRating * 0.5)
                     + (Math.log1p(candidatePopularity) * 0.3);

        return score;
    }
}
