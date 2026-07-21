package dev.kmemo.examples

import dev.kmemo.CacheLookup
import dev.kmemo.MissReason
import dev.kmemo.store.InMemoryStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Keeps the demo honest in CI: the paraphrase hits and the near-miss is caught by the guard. */
class DemoTest {

    @Test
    fun `the demo serves a paraphrase and refuses a numeric near-miss`() = runTest {
        val cache = demoCache(InMemoryStore())
        cache.warm(faq)

        val paraphrase = cache.lookup("How can I reverse a Python list?")
        val hit = assertIs<CacheLookup.Hit>(paraphrase)
        assertEquals("Use reversed() or list.reverse().", hit.response)

        val nearMiss = cache.lookup("Convert 250 USD to EUR")
        val miss = assertIs<CacheLookup.Miss>(nearMiss)
        assertEquals(MissReason.REJECTED_BY_GUARD, miss.reason)
    }
}
