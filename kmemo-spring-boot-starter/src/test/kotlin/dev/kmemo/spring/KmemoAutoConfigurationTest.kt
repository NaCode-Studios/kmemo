package dev.kmemo.spring

import dev.kmemo.CacheStore
import dev.kmemo.Embedder
import dev.kmemo.SemanticCache
import dev.kmemo.micrometer.KmemoMetrics
import dev.kmemo.store.InMemoryStore
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KmemoAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KmemoAutoConfiguration::class.java))

    private val fakeEmbedder = Embedder { floatArrayOf(1.0f, 2.0f, 3.0f) }

    @Test
    fun `no cache is created without an embedder bean`() {
        // kmemo ships no embedder, so with none supplied there is nothing to build a cache around.
        runner.run { context ->
            assertTrue(context.getBeanNamesForType(SemanticCache::class.java).isEmpty())
        }
    }

    @Test
    fun `an embedder yields a cache and the default in-memory store`() {
        runner.withBean("embedder", Embedder::class.java, Supplier { fakeEmbedder }).run { context ->
            assertEquals(1, context.getBeanNamesForType(SemanticCache::class.java).size)
            assertTrue(context.getBean(CacheStore::class.java) is InMemoryStore)
        }
    }

    @Test
    fun `properties bind and a user-supplied store wins`() {
        val customStore = InMemoryStore(maxEntries = 1)
        runner
            .withBean("embedder", Embedder::class.java, Supplier { fakeEmbedder })
            .withBean("customStore", CacheStore::class.java, Supplier { customStore })
            .withPropertyValues("kmemo.threshold=0.5", "kmemo.negative-cache-size=100")
            .run { context ->
                val properties = context.getBean(KmemoProperties::class.java)
                assertEquals(0.5, properties.threshold)
                assertEquals(100, properties.negativeCacheSize)

                // @ConditionalOnMissingBean means the application's own store is the one used.
                assertSame(customStore, context.getBean(CacheStore::class.java))
                assertEquals(1, context.getBeanNamesForType(SemanticCache::class.java).size)
            }
    }

    @Test
    fun `metrics are auto-configured when kmemo-micrometer is present`() {
        // kmemo-micrometer is on the test classpath, so @ConditionalOnClass passes and a KmemoMetrics
        // bean is registered — both a CacheListener (attached to the cache) and a MeterBinder.
        ApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(
                    KmemoAutoConfiguration::class.java,
                    KmemoMetricsAutoConfiguration::class.java,
                ),
            )
            .withBean("embedder", Embedder::class.java, Supplier { fakeEmbedder })
            .run { context ->
                assertEquals(1, context.getBeanNamesForType(KmemoMetrics::class.java).size)
                assertEquals(1, context.getBeanNamesForType(SemanticCache::class.java).size)
            }
    }

    @Test
    fun `an application SemanticCache bean makes the auto-config back off`() {
        val provided = SemanticCache(fakeEmbedder)
        runner
            .withBean("embedder", Embedder::class.java, Supplier { fakeEmbedder })
            .withBean("appCache", SemanticCache::class.java, Supplier { provided })
            .run { context ->
                assertSame(provided, context.getBean(SemanticCache::class.java))
            }
    }
}
