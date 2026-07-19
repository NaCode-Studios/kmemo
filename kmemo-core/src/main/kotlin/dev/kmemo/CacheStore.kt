package dev.kmemo

/** A [CacheEntry] together with its similarity to the query vector, in `[-1.0, 1.0]`. */
public class ScoredEntry(
    public val entry: CacheEntry,
    public val similarity: Double,
) {
    override fun toString(): String = "ScoredEntry(similarity=$similarity, entry=$entry)"
}

/**
 * Storage and nearest-neighbour search behind a [SemanticCache].
 *
 * kmemo splits responsibilities on purpose: the store owns **where entries live and when they are
 * dropped** (capacity, eviction, TTL), while [SemanticCache] owns **whether a candidate is good
 * enough to serve** (threshold, guards, verification). That split is what lets a Redis or pgvector
 * adapter delegate expiry to the database without reimplementing any match logic.
 *
 * Implementations must honour three rules:
 *  1. [search] never returns an expired entry. Expiry is the store's business; the cache trusts it.
 *  2. [search] returns at most `limit` results, sorted by descending similarity.
 *  3. Every method is safe to call concurrently from multiple coroutines.
 */
public interface CacheStore {

    /** Writes [entry], replacing any existing entry with the same [CacheEntry.id]. */
    public suspend fun put(entry: CacheEntry)

    /**
     * Returns up to [limit] entries from [scope] closest to [embedding], best first.
     *
     * [embedding] is unit-normalized, as is [CacheEntry.embedding], so similarity is a plain
     * [Vectors.dot]. Entries outside [scope] and entries past their TTL must not appear.
     */
    public suspend fun search(scope: String, embedding: FloatArray, limit: Int): List<ScoredEntry>

    /**
     * Signals that the entry [id] was actually served to a caller.
     *
     * [SemanticCache] calls this on a confirmed hit only — not for every candidate inspected — so
     * recency-based eviction reflects entries that earned their place rather than entries that
     * merely scored well. Stores without recency eviction can leave the default no-op.
     */
    public suspend fun touch(id: String) {
        // no-op by default
    }

    /** Removes the entry [id]. Returns `true` if an entry was actually removed. */
    public suspend fun remove(id: String): Boolean

    /** Removes every entry in [scope], or the whole store when [scope] is `null`. */
    public suspend fun clear(scope: String? = null)

    /** Number of live entries in [scope], or in the whole store when [scope] is `null`. */
    public suspend fun size(scope: String? = null): Int
}
