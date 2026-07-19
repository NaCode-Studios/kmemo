package dev.nacode.kmemo.guard

/**
 * Rejects matches that talk about different named things.
 *
 * "What is the capital of Australia" and "what is the capital of Austria" differ by two letters and
 * one continent. Embedding models place them extremely close — both are capital-city questions
 * about a European-sounding place — which makes this the classic false hit. Capitalization is the
 * cheapest available signal that a token names something specific, so the guard compares the
 * capitalized words of the two prompts.
 *
 * Three rules keep it from misfiring:
 *
 * **Sentence-initial capitals are ignored.** `What` in "What is Docker" is grammar, not an entity.
 *
 * **It rejects on substitution, not on addition.** Each prompt must name something the other does
 * not: `Austria` here, `Australia` there. One prompt merely mentioning an extra entity is not
 * evidence of a different question — "What does SOLID stand for in OOP?" and "what does each letter
 * of SOLID mean in object oriented design?" want the same answer, and only one of them abbreviates.
 *
 * **Pronouns and initials do not count.** English capitalizes `I`, which would otherwise make every
 * first-person question look like it named something.
 */
public class EntityGuard : MatchGuard {

    override val name: String get() = "entity"

    override fun evaluate(query: String, candidate: String): GuardVerdict {
        val queryEntities = Text.entityTokens(query)
        val candidateEntities = Text.entityTokens(candidate)
        if (queryEntities.isEmpty() || candidateEntities.isEmpty()) return GuardVerdict.Accept

        val onlyInQuery = queryEntities - candidateEntities
        val onlyInCandidate = candidateEntities - queryEntities
        if (onlyInQuery.isEmpty() || onlyInCandidate.isEmpty()) return GuardVerdict.Accept

        return GuardVerdict.Reject(
            "named entities swapped: query says $onlyInQuery where cached prompt says $onlyInCandidate",
        )
    }
}
