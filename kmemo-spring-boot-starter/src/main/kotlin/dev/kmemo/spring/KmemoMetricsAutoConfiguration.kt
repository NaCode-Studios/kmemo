package dev.kmemo.spring

import dev.kmemo.micrometer.KmemoMetrics
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Wires kmemo's metrics into a Spring Boot app when `kmemo-micrometer` is on the classpath.
 *
 * The bean it registers is a [KmemoMetrics], which is at once a [dev.kmemo.CacheListener] — so
 * [KmemoAutoConfiguration] attaches it to the [dev.kmemo.SemanticCache] with every other listener bean
 * — and a Micrometer `MeterBinder`, which Actuator binds to your registry automatically. So adding the
 * `kmemo-micrometer` dependency is all it takes for kmemo's hit rate, per-reason and per-guard counters
 * and stage latencies to show up on `/actuator/prometheus`, no wiring code.
 *
 * The whole class is gated on the class being present (`@ConditionalOnClass` by name), so a starter
 * user who does not depend on `kmemo-micrometer` never loads any of this.
 */
@AutoConfiguration(after = [KmemoAutoConfiguration::class])
@ConditionalOnClass(name = ["dev.kmemo.micrometer.KmemoMetrics"])
public open class KmemoMetricsAutoConfiguration {

    /** The metrics binder/listener, unless the application already defines one. */
    @Bean
    @ConditionalOnMissingBean(KmemoMetrics::class)
    public open fun kmemoMetrics(): KmemoMetrics = KmemoMetrics()
}
