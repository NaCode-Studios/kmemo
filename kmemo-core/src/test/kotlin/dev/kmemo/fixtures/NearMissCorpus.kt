package dev.kmemo.fixtures

import dev.kmemo.calibration.PromptPair
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The labelled prompt pairs in `near-miss-corpus.json`, and the reason this project has tests worth
 * reading.
 *
 * Each pair is two prompts plus a verdict: may the response cached for one be served to the other?
 * Most of them are deliberately a single token apart — `Australia` against `Austria`, `100` against
 * `250`, `enable` against `disable` — because that is where a semantic cache actually breaks. Pairs
 * that are obviously unrelated prove nothing; no threshold ever confused them.
 *
 * The corpus carries its own control group. Roughly a third of the pairs *should* match — genuine
 * paraphrases, typos, politeness wrappers, reorderings — so a guard that rejects everything scores
 * zero here rather than looking perfect.
 */
object NearMissCorpus {

    data class CorpusPair(
        val a: String,
        val b: String,
        val shouldMatch: Boolean,
        val category: String,
        val note: String,
    )

    /** Every pair in the corpus. */
    val pairs: List<CorpusPair> by lazy { load() }

    /** Pairs that must never be served from cache. */
    val nearMisses: List<CorpusPair> get() = pairs.filter { !it.shouldMatch }

    /** Pairs that must stay cacheable, or the cache is worthless. */
    val paraphrases: List<CorpusPair> get() = pairs.filter { it.shouldMatch }

    /** The same pairs in the shape [dev.kmemo.calibration.ThresholdCalibrator] expects. */
    fun asPromptPairs(): List<PromptPair> =
        pairs.map { PromptPair(a = it.a, b = it.b, shouldMatch = it.shouldMatch, label = it.category) }

    private fun load(): List<CorpusPair> {
        val json = NearMissCorpus::class.java.getResourceAsStream(RESOURCE)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("$RESOURCE is missing from the test classpath")

        return Json.parseToJsonElement(json).jsonObject
            .getValue("pairs").jsonArray
            .map { element ->
                val fields = element.jsonObject
                CorpusPair(
                    a = fields.getValue("a").jsonPrimitive.content,
                    b = fields.getValue("b").jsonPrimitive.content,
                    shouldMatch = fields.getValue("shouldMatch").jsonPrimitive.content.toBoolean(),
                    category = fields.getValue("category").jsonPrimitive.content,
                    note = fields.getValue("note").jsonPrimitive.content,
                )
            }
    }

    private const val RESOURCE = "/near-miss-corpus.json"
}
