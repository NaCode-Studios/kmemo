package dev.nacode.kmemo

import kotlin.math.sqrt

/**
 * Vector maths shared by [SemanticCache] and by [CacheStore] implementations.
 *
 * kmemo works exclusively with **unit-normalized** vectors: once a vector has length 1, cosine
 * similarity collapses into a plain dot product, which removes two square roots from every
 * comparison in the search loop. [CacheEntry] normalizes on construction, so store implementations
 * can safely use [dot] instead of [cosineSimilarity].
 */
public object Vectors {

    /**
     * Returns a copy of [vector] scaled to unit length.
     *
     * @throws IllegalArgumentException if [vector] is empty or has zero magnitude — both indicate a
     *   broken embedder rather than a legitimate value, and silently accepting them would make every
     *   later similarity meaningless.
     */
    public fun normalize(vector: FloatArray): FloatArray {
        require(vector.isNotEmpty()) { "cannot normalize an empty vector" }
        val magnitude = magnitude(vector)
        require(magnitude > 0.0) { "cannot normalize a zero-magnitude vector" }
        val result = FloatArray(vector.size)
        for (i in vector.indices) {
            result[i] = (vector[i] / magnitude).toFloat()
        }
        return result
    }

    /** Euclidean length of [vector], accumulated in [Double] to limit drift on large dimensions. */
    public fun magnitude(vector: FloatArray): Double {
        var sum = 0.0
        for (value in vector) {
            sum += value.toDouble() * value.toDouble()
        }
        return sqrt(sum)
    }

    /**
     * Dot product of [a] and [b]. Equals the cosine similarity **only when both vectors are
     * unit-normalized**; use [cosineSimilarity] when that is not guaranteed.
     *
     * @throws IllegalArgumentException if the two vectors have different dimensions.
     */
    public fun dot(a: FloatArray, b: FloatArray): Double {
        require(a.size == b.size) { "vector dimension mismatch: ${a.size} vs ${b.size}" }
        var sum = 0.0
        for (i in a.indices) {
            sum += a[i].toDouble() * b[i].toDouble()
        }
        return sum
    }

    /**
     * Cosine similarity of [a] and [b], in `[-1.0, 1.0]`. Returns `0.0` when either vector has zero
     * magnitude, since an undefined angle is better reported as "unrelated" than as an exception in
     * the middle of a search loop.
     */
    public fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        val magnitudeA = magnitude(a)
        val magnitudeB = magnitude(b)
        if (magnitudeA == 0.0 || magnitudeB == 0.0) return 0.0
        return dot(a, b) / (magnitudeA * magnitudeB)
    }

    /** Whether [vector] already has unit length, within [tolerance]. */
    public fun isNormalized(vector: FloatArray, tolerance: Double = 1e-4): Boolean =
        kotlin.math.abs(magnitude(vector) - 1.0) <= tolerance
}
