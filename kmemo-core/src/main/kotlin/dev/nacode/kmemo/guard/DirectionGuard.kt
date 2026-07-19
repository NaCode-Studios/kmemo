package dev.nacode.kmemo.guard

/**
 * Rejects matches where the same words appear in an order that reverses the question.
 *
 * "Is Postgres better than MySQL?" and "Is MySQL better than Postgres?" contain an identical bag of
 * words. Every set-based check — word overlap, entity comparison, embedding similarity — sees two
 * copies of the same question, because in the bag-of-words view that is exactly what they are.
 *
 * Word order alone cannot be the signal, though: "In Python, how do I sort a dictionary by value?"
 * and "How do I sort a dictionary by value in Python?" are also a reordering, and they must stay a
 * hit. So this guard fires only when a [Vocabulary.DIRECTIONAL_CUES] word is present — a comparison
 * or a conversion, the two constructions where argument order carries meaning — and only when the
 * two prompts use exactly the same words. Different words are somebody else's problem, which keeps
 * "how many miles is 50 km" matching "convert 50 km to miles".
 */
public class DirectionGuard(
    private val cues: Set<String> = Vocabulary.DIRECTIONAL_CUES,
    private val stopwords: Set<String> = Vocabulary.STOPWORDS,
) : MatchGuard {

    override val name: String get() = "direction"

    override fun evaluate(query: String, candidate: String): GuardVerdict {
        if (!hasDirectionalCue(query) && !hasDirectionalCue(candidate)) return GuardVerdict.Accept

        val queryTokens = Text.contentTokens(query, stopwords)
        val candidateTokens = Text.contentTokens(candidate, stopwords)
        if (queryTokens == candidateTokens) return GuardVerdict.Accept
        if (queryTokens.toSet() != candidateTokens.toSet()) return GuardVerdict.Accept

        return GuardVerdict.Reject(
            "same terms in reversed order around a comparison or conversion: " +
                "$queryTokens vs $candidateTokens",
        )
    }

    private fun hasDirectionalCue(text: String): Boolean = Text.tokens(text).any { it in cues }
}
