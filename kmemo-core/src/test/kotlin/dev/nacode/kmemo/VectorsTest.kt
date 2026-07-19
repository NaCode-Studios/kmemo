package dev.nacode.kmemo

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VectorsTest {

    @Test
    fun `normalize produces a unit vector`() {
        val normalized = Vectors.normalize(floatArrayOf(3.0f, 4.0f))

        assertEquals(1.0, Vectors.magnitude(normalized), 1e-6)
        assertEquals(0.6, normalized[0].toDouble(), 1e-6)
        assertEquals(0.8, normalized[1].toDouble(), 1e-6)
    }

    @Test
    fun `a vector that cannot be normalized is a broken embedder, not a valid input`() {
        assertFailsWith<IllegalArgumentException> { Vectors.normalize(floatArrayOf()) }
        assertFailsWith<IllegalArgumentException> { Vectors.normalize(floatArrayOf(0.0f, 0.0f)) }
    }

    @Test
    fun `cosine similarity spans identical to opposite`() {
        val vector = floatArrayOf(1.0f, 2.0f, 3.0f)

        assertEquals(1.0, Vectors.cosineSimilarity(vector, vector), 1e-6)
        assertEquals(-1.0, Vectors.cosineSimilarity(vector, floatArrayOf(-1.0f, -2.0f, -3.0f)), 1e-6)
        assertEquals(0.0, Vectors.cosineSimilarity(floatArrayOf(1.0f, 0.0f), floatArrayOf(0.0f, 1.0f)), 1e-6)
    }

    @Test
    fun `cosine similarity ignores magnitude`() {
        val short = floatArrayOf(1.0f, 1.0f)
        val long = floatArrayOf(50.0f, 50.0f)

        assertEquals(1.0, Vectors.cosineSimilarity(short, long), 1e-6)
    }

    @Test
    fun `on normalized vectors the dot product is the cosine`() {
        val a = Vectors.normalize(floatArrayOf(0.4f, 0.9f, -0.2f))
        val b = Vectors.normalize(floatArrayOf(0.5f, 0.7f, 0.1f))

        assertEquals(Vectors.cosineSimilarity(a, b), Vectors.dot(a, b), 1e-6)
    }

    @Test
    fun `comparing vectors of different sizes is an error, not a zero`() {
        assertFailsWith<IllegalArgumentException> {
            Vectors.dot(floatArrayOf(1.0f), floatArrayOf(1.0f, 2.0f))
        }
    }

    @Test
    fun `a zero-magnitude vector is unrelated to everything rather than an exception`() {
        assertEquals(0.0, Vectors.cosineSimilarity(floatArrayOf(0.0f, 0.0f), floatArrayOf(1.0f, 1.0f)), 1e-9)
    }

    @Test
    fun `an entry normalizes whatever the embedder handed over`() {
        val entry = CacheEntry(
            id = "id",
            scope = "default",
            prompt = "prompt",
            response = "response",
            embedding = floatArrayOf(30.0f, 40.0f),
            createdAt = Instant.EPOCH,
        )

        assertTrue(Vectors.isNormalized(Vectors.normalize(entry.embedding)))
        assertEquals(2, entry.dimensions)
    }

    @Test
    fun `an entry with an empty embedding is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CacheEntry("id", "default", "prompt", "response", floatArrayOf(), Instant.EPOCH)
        }
    }
}
