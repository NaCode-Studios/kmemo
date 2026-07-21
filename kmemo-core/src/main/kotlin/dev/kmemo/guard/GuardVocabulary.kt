package dev.kmemo.guard

/**
 * The complete set of word lists the standard guards read from, bundled so a whole language can be
 * swapped in one object.
 *
 * Every guard already takes its markers as a constructor parameter, so adapting kmemo to another
 * language was always *possible* — this makes it *one line*. [MatchGuards.standard] takes a
 * `GuardVocabulary`, and [dev.kmemo.guard.Vocabularies] ships curated ones for the highest-traffic
 * languages. Build your own from a language's traffic when a shipped pack does not fit; the same
 * conservative rule applies as for English — a marker earns its place only if it will not reject a
 * genuine paraphrase.
 *
 * @param stopwords function words removed before comparison; read by most guards.
 * @param sentenceOpeners words that may open a sentence without naming anything (the entity guard).
 * @param nonEntityCapitals words capitalized by grammar, not reference (the entity guard).
 * @param negationMarkers words that negate (the negation guard).
 * @param antonyms symmetric pairs that flip an answer (the antonym guard).
 * @param temporalMarkers absolute time references (the temporal guard).
 * @param scopeMarkers words describing the shape of the answer — format, length, depth (the scope guard).
 * @param directionalCues cues that make argument order significant — comparisons, conversions (the direction guard).
 * @param units unit and currency tokens mapped to a canonical [MeasurementUnit] (the unit & substitution guards).
 */
public data class GuardVocabulary(
    public val stopwords: Set<String>,
    public val sentenceOpeners: Set<String>,
    public val nonEntityCapitals: Set<String>,
    public val negationMarkers: Set<String>,
    public val antonyms: Set<Pair<String, String>>,
    public val temporalMarkers: Set<String>,
    public val scopeMarkers: Set<String>,
    public val directionalCues: Set<String>,
    public val units: Map<String, MeasurementUnit>,
) {
    public companion object {

        /**
         * The English pack — the historical default, drawn from [Vocabulary]. [MatchGuards.standard]
         * with no argument uses exactly this, so nothing changes for existing callers.
         */
        public val ENGLISH: GuardVocabulary = GuardVocabulary(
            stopwords = Vocabulary.STOPWORDS,
            sentenceOpeners = Vocabulary.SENTENCE_OPENERS,
            nonEntityCapitals = Vocabulary.NON_ENTITY_CAPITALS,
            negationMarkers = Vocabulary.NEGATION_MARKERS,
            antonyms = Vocabulary.ANTONYMS,
            temporalMarkers = Vocabulary.TEMPORAL_MARKERS,
            scopeMarkers = Vocabulary.SCOPE_MARKERS,
            directionalCues = Vocabulary.DIRECTIONAL_CUES,
            units = Vocabulary.UNITS,
        )
    }
}
