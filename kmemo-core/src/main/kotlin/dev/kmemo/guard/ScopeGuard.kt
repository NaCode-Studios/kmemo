package dev.kmemo.guard

/**
 * Rejects matches that ask for the same subject in a different shape.
 *
 * "Write a haiku about the ocean" and "Write a sonnet about the ocean" differ in one word and share
 * a topic, so they sit close together in embedding space. They are not the same request. Nor are
 * "Give me a code example of a Python decorator" and "Explain the theory behind Python decorators",
 * or "What is a Kalman filter?" and "Derive the Kalman filter equations step by step".
 *
 * What changed in each case is not the subject but the **answer being asked for**: its format, its
 * length, its depth. Those words are a small, closed set, so the guard reads them directly.
 *
 * Unlike [EntityGuard] and [UnitGuard], one-sided evidence counts here. "How does HTTPS work?" and
 * "How does HTTPS work, in detail?" differ precisely because one of them added a requirement — an
 * addition *is* the difference, so there is nothing to swap.
 */
public class ScopeGuard(
    private val markers: Set<String> = Vocabulary.SCOPE_MARKERS,
) : MatchGuard {

    override val name: String get() = "scope"

    override fun evaluate(query: String, candidate: String): GuardVerdict {
        val queryMarkers = markersIn(query)
        val candidateMarkers = markersIn(candidate)
        if (queryMarkers == candidateMarkers) return GuardVerdict.Accept

        return GuardVerdict.Reject(
            "the answers asked for differ in format or depth: " +
                "${queryMarkers.ifEmpty { setOf("unspecified") }} vs " +
                "${candidateMarkers.ifEmpty { setOf("unspecified") }}",
        )
    }

    private fun markersIn(text: String): Set<String> =
        Text.tokens(text).filterTo(LinkedHashSet()) { it in markers }
}
