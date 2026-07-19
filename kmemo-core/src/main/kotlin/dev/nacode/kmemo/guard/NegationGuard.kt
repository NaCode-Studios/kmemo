package dev.nacode.kmemo.guard

/**
 * Rejects matches where one prompt is negated and the other is not.
 *
 * Negation is close to invisible to an embedding model: "is this column nullable" and "is this
 * column not nullable" are one short function word apart, and that word carries the entire answer.
 *
 * The test is presence, not count, so two differently-phrased negatives ("no such file" and "file
 * not found") still match each other. Contractions are caught by an `n't` substring test, which
 * survives any tokenizer that splits on apostrophes.
 */
public class NegationGuard(
    private val markers: Set<String> = Vocabulary.NEGATION_MARKERS,
) : MatchGuard {

    override val name: String get() = "negation"

    override fun evaluate(query: String, candidate: String): GuardVerdict {
        val queryNegated = isNegated(query)
        val candidateNegated = isNegated(candidate)
        if (queryNegated == candidateNegated) return GuardVerdict.Accept

        val negated = if (queryNegated) "query" else "cached prompt"
        return GuardVerdict.Reject("only the $negated is negated")
    }

    private fun isNegated(text: String): Boolean {
        if (text.contains("n't", ignoreCase = true)) return true
        return Text.tokens(text).any { it in markers }
    }
}
