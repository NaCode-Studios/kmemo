package dev.kmemo.spring

import dev.kmemo.CacheListener
import dev.kmemo.CacheStore
import dev.kmemo.Embedder
import dev.kmemo.SemanticCache
import dev.kmemo.Verifier
import dev.kmemo.store.InMemoryStore
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import kotlin.time.toKotlinDuration

/**
 * Auto-configures a [SemanticCache] bean the moment an [Embedder] bean is present.
 *
 * kmemo ships no embedder — you bring one (OpenAI, a local ONNX model, whatever) as a Spring bean, and
 * this wires the cache around it, reading the [KmemoProperties] under `kmemo.*`. Everything is
 * conditional and overridable:
 *
 * - The **store** defaults to [InMemoryStore]. Define your own [CacheStore] bean — a Redis or pgvector
 *   adapter, say — and it is used instead (`@ConditionalOnMissingBean`).
 * - A [Verifier] bean, if you define one, is attached; multiple [CacheListener] beans (metrics, logging)
 *   are all attached, in `@Order`.
 * - Define your own [SemanticCache] bean and this backs off entirely.
 *
 * ```kotlin
 * @Bean fun embedder(client: OpenAiClient) = Embedder { text -> client.embed(text) }
 * // …and a SemanticCache bean is now available to inject anywhere.
 * ```
 */
@AutoConfiguration
@EnableConfigurationProperties(KmemoProperties::class)
public open class KmemoAutoConfiguration {

    /** The default store, used unless the application defines its own [CacheStore] bean. */
    @Bean
    @ConditionalOnMissingBean(CacheStore::class)
    public open fun kmemoCacheStore(): CacheStore = InMemoryStore()

    /** The cache itself: built only when an [Embedder] is available and no [SemanticCache] bean exists. */
    @Bean
    @ConditionalOnBean(Embedder::class)
    @ConditionalOnMissingBean(SemanticCache::class)
    public open fun semanticCache(
        embedder: Embedder,
        store: CacheStore,
        properties: KmemoProperties,
        verifier: ObjectProvider<Verifier>,
        listeners: ObjectProvider<CacheListener>,
    ): SemanticCache = SemanticCache(
        embedder = embedder,
        store = store,
        threshold = properties.threshold,
        candidates = properties.candidates,
        coalesceConcurrentMisses = properties.coalesceConcurrentMisses,
        negativeCacheSize = properties.negativeCacheSize,
        negativeCacheTtl = properties.negativeCacheTtl?.toKotlinDuration(),
        verifier = verifier.ifAvailable,
        listeners = listeners.orderedStream().toList(),
    )
}
