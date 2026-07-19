package dev.kmemo

/**
 * Counters for one [SemanticCache] instance, from creation to now.
 *
 * The breakdown of misses is the part worth watching. A high [belowThreshold] means your traffic
 * repeats less than you assumed, or your threshold is too tight for your embedding model. A high
 * [guardRejections] means the opposite: prompts are landing close together and the guards are
 * catching near-misses your threshold alone would have served.
 */
public data class CacheStats(
    /** Total [SemanticCache.lookup] and [SemanticCache.getOrPut] calls. */
    public val lookups: Long,
    /** Lookups that served a cached response. */
    public val hits: Long,
    /** Lookups that did not. Always `lookups - hits`. */
    public val misses: Long,
    /** Misses where the best candidate scored below the threshold. */
    public val belowThreshold: Long,
    /** Misses where a guard vetoed a candidate that had cleared the threshold. */
    public val guardRejections: Long,
    /** Misses where the verifier vetoed a candidate that had cleared threshold and guards. */
    public val verifierRejections: Long,
    /** Entries written through [SemanticCache.put] or [SemanticCache.getOrPut]. */
    public val writes: Long,
) {
    /** Fraction of lookups served from cache, in `[0.0, 1.0]`. `0.0` when nothing has been looked up. */
    public val hitRate: Double
        get() = if (lookups == 0L) 0.0 else hits.toDouble() / lookups.toDouble()

    /**
     * Fraction of lookups that reached a guard and were stopped there, in `[0.0, 1.0]`.
     *
     * Each one is a wrong answer that was not served — and, unlike a threshold miss, one your
     * embedding model was ready to hand over.
     */
    public val guardRejectionRate: Double
        get() = if (lookups == 0L) 0.0 else guardRejections.toDouble() / lookups.toDouble()
}
