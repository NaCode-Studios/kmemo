package dev.kmemo.guard

import dev.kmemo.fixtures.NearMissCorpus
import dev.kmemo.fixtures.NearMissCorpus.CorpusPair
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The guard layer measured against the whole near-miss corpus.
 *
 * No embedder is involved. These tests ask only what the guards do with a pair of prompts, which is
 * the part of the decision that has to hold whatever embedding model sits in front of it — and the
 * part a threshold cannot do at all.
 *
 * Both directions of every pair are evaluated, because either prompt could be the one already in
 * the cache when the other arrives.
 */
class NearMissCorpusTest {

    @Test
    fun `standard guards reject no genuine paraphrase`() {
        val guards = MatchGuards.standard()
        val rejected = NearMissCorpus.paraphrases.mapNotNull { pair ->
            rejectionFor(guards, pair)?.let { pair to it }
        }

        assertTrue(
            rejected.isEmpty(),
            buildString {
                appendLine("${rejected.size} genuine paraphrases were rejected by a guard:")
                for ((pair, reason) in rejected) {
                    appendLine("  [${pair.category}] ${pair.a}")
                    appendLine("                 vs ${pair.b}")
                    appendLine("    -> $reason")
                }
            },
        )
    }

    @Test
    fun `standard guards catch near misses a similarity threshold cannot`() {
        val guards = MatchGuards.standard()
        val nearMisses = NearMissCorpus.nearMisses
        val caught = nearMisses.count { rejectionFor(guards, it) != null }
        val rate = caught.toDouble() / nearMisses.size

        assertTrue(
            rate >= MIN_NEAR_MISS_CATCH_RATE,
            "standard guards caught $caught/${nearMisses.size} near misses " +
                "(${percent(rate)}), below the ${percent(MIN_NEAR_MISS_CATCH_RATE)} floor",
        )
    }

    @Test
    fun `strict guards catch more near misses, and the cost is paid in paraphrases`() {
        val standardCaught = NearMissCorpus.nearMisses.count { rejectionFor(MatchGuards.standard(), it) != null }
        val strictCaught = NearMissCorpus.nearMisses.count { rejectionFor(MatchGuards.strict(), it) != null }

        assertTrue(
            strictCaught >= standardCaught,
            "strict guards ($strictCaught) caught fewer near misses than standard ($standardCaught)",
        )
    }

    @Test
    fun `no guards means no protection at all`() {
        val caught = NearMissCorpus.nearMisses.count { rejectionFor(MatchGuards.none(), it) != null }
        assertTrue(caught == 0, "MatchGuards.none() rejected $caught pairs; it must reject nothing")
    }

    /**
     * Not an assertion — a printout.
     *
     * Every number quoted in the README comes from this table, so it stays honest as the guards and
     * the corpus change. Run `./gradlew :kmemo-core:test --tests '*NearMissCorpusTest*'` to see it.
     */
    @Test
    fun `print corpus report`() {
        val standard = MatchGuards.standard()
        val strict = MatchGuards.strict()

        println()
        println("near-miss corpus: ${NearMissCorpus.pairs.size} pairs " +
            "(${NearMissCorpus.nearMisses.size} must not match, ${NearMissCorpus.paraphrases.size} must)")
        println()
        println(String.format(Locale.ROOT, "%-24s %6s %10s %10s", "category", "pairs", "standard", "strict"))

        for ((category, pairs) in NearMissCorpus.pairs.groupBy { it.category }.entries.sortedBy { it.key }) {
            val expectation = if (pairs.first().shouldMatch) "must match" else "must not match"
            println(
                String.format(
                    Locale.ROOT,
                    "%-24s %6d %9s%% %9s%% %s",
                    category,
                    pairs.size,
                    scoreFor(standard, pairs),
                    scoreFor(strict, pairs),
                    expectation,
                ),
            )
        }

        println()
        println("Columns are the share of pairs handled correctly: near misses rejected, paraphrases kept.")
        println()
        println(String.format(Locale.ROOT, "%-20s %10s %14s", "guard (alone)", "caught", "false rejects"))
        for (guard in standard) {
            println(
                String.format(
                    Locale.ROOT,
                    "%-20s %10d %14d",
                    guard.name,
                    NearMissCorpus.nearMisses.count { rejectionFor(listOf(guard), it) != null },
                    NearMissCorpus.paraphrases.count { rejectionFor(listOf(guard), it) != null },
                ),
            )
        }
        println()
        println("Near misses that still get through (standard):")
        NearMissCorpus.nearMisses
            .filter { rejectionFor(standard, it) == null }
            .forEach { println("  [${it.category}] ${it.a}  ||  ${it.b}") }
        println()
    }

    private fun scoreFor(guards: List<MatchGuard>, pairs: List<CorpusPair>): String {
        val correct = pairs.count { pair ->
            val rejected = rejectionFor(guards, pair) != null
            rejected != pair.shouldMatch
        }
        return String.format(Locale.ROOT, "%.0f", 100.0 * correct / pairs.size)
    }

    /** The first guard to veto the pair in either direction, or `null` if all of them abstained. */
    private fun rejectionFor(guards: List<MatchGuard>, pair: CorpusPair): String? {
        for (guard in guards) {
            val forward = guard.evaluate(pair.b, pair.a)
            if (forward is GuardVerdict.Reject) return "${guard.name}: ${forward.reason}"
            val backward = guard.evaluate(pair.a, pair.b)
            if (backward is GuardVerdict.Reject) return "${guard.name}: ${backward.reason}"
        }
        return null
    }

    private fun percent(value: Double): String = String.format(Locale.ROOT, "%.0f%%", value * 100)

    private companion object {
        /**
         * Floor, not a target. The guards catch more than this today; the assertion exists to fail
         * loudly if a change quietly gives that back.
         */
        private const val MIN_NEAR_MISS_CATCH_RATE = 0.90
    }
}
