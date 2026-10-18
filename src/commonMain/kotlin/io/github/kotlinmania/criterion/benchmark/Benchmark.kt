// port-lint: source benchmark.rs
package io.github.kotlinmania.criterion.benchmark

import io.github.kotlinmania.criterion.PlotConfiguration
import io.github.kotlinmania.criterion.SamplingMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration options for a benchmark.
 */
data class BenchmarkConfig(
    val confidenceLevel: Double = 0.95,
    val measurementTime: Duration = 5.seconds,
    val noiseThreshold: Double = 0.02,
    val nresamples: Int = 100_000,
    val sampleSize: Int = 100,
    val significanceLevel: Double = 0.05,
    val warmUpTime: Duration = 3.seconds,
    val samplingMode: SamplingMode = SamplingMode.Auto,
    val quickMode: Boolean = false,
)

/**
 * Partially-complete per-benchmark configuration.
 */
data class PartialBenchmarkConfig(
    var confidenceLevel: Double? = null,
    var measurementTime: Duration? = null,
    var noiseThreshold: Double? = null,
    var nresamples: Int? = null,
    var sampleSize: Int? = null,
    var significanceLevel: Double? = null,
    var warmUpTime: Duration? = null,
    var samplingMode: SamplingMode? = null,
    var quickMode: Boolean? = null,
    var plotConfig: PlotConfiguration = PlotConfiguration(),
) {
    fun toComplete(defaults: BenchmarkConfig): BenchmarkConfig =
        BenchmarkConfig(
            confidenceLevel = confidenceLevel ?: defaults.confidenceLevel,
            measurementTime = measurementTime ?: defaults.measurementTime,
            noiseThreshold = noiseThreshold ?: defaults.noiseThreshold,
            nresamples = nresamples ?: defaults.nresamples,
            sampleSize = sampleSize ?: defaults.sampleSize,
            significanceLevel = significanceLevel ?: defaults.significanceLevel,
            warmUpTime = warmUpTime ?: defaults.warmUpTime,
            samplingMode = samplingMode ?: defaults.samplingMode,
            quickMode = quickMode ?: defaults.quickMode,
        )
}
