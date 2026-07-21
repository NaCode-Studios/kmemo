package dev.kmemo.spring

import dev.kmemo.SemanticCache
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration for the auto-configured [dev.kmemo.SemanticCache], bound from the `kmemo.*` properties.
 *
 * ```yaml
 * kmemo:
 *   threshold: 0.9
 *   candidates: 8
 *   negative-cache-size: 10000
 *   negative-cache-ttl: 5m
 * ```
 *
 * Only the model-agnostic knobs are exposed here. Anything that needs a real object — the [dev.kmemo.Embedder],
 * a [dev.kmemo.CacheStore], a [dev.kmemo.Verifier], the guard set — is a **bean** you define, which the
 * auto-configuration picks up. See [KmemoAutoConfiguration].
 *
 * A plain mutable JavaBean rather than a data class: Spring's setter binding is the one path that works
 * cleanly for a Kotlin config class whose every field has a default.
 */
@ConfigurationProperties(prefix = "kmemo")
public class KmemoProperties {

    /** Minimum cosine similarity for a candidate to be considered; calibrate it for your model. */
    public var threshold: Double = SemanticCache.DEFAULT_THRESHOLD

    /** How many nearest entries to examine per lookup. */
    public var candidates: Int = SemanticCache.DEFAULT_CANDIDATES

    /** Whether concurrent identical misses wait for the first to compute. */
    public var coalesceConcurrentMisses: Boolean = true

    /** Size of the negative cache (`0` disables it); reuses a just-missed embedding. */
    public var negativeCacheSize: Int = 0

    /** How long a remembered miss stays usable, e.g. `5m`; `null` keeps it until evicted. */
    public var negativeCacheTtl: Duration? = null
}
