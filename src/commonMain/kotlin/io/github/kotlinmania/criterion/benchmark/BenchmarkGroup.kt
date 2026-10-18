// port-lint: source benchmark_group.rs
package io.github.kotlinmania.criterion.benchmark

import io.github.kotlinmania.criterion.Criterion
import io.github.kotlinmania.criterion.SamplingMode
import io.github.kotlinmania.criterion.Throughput
import io.github.kotlinmania.criterion.analysis.analyze
import io.github.kotlinmania.criterion.bencher.Bencher
import io.github.kotlinmania.criterion.measurement.Measurement
import io.github.kotlinmania.criterion.report.ReportBenchmarkId
import io.github.kotlinmania.criterion.report.ReportContext
import io.github.kotlinmania.criterion.routine.Function
import kotlin.time.Duration

class BenchmarkGroup<Intermediate, Value, M : Measurement<Intermediate, Value>>(
    val criterion: Criterion<Intermediate, Value, M>,
    val groupName: String,
) {
    var partialConfig: PartialBenchmarkConfig = PartialBenchmarkConfig()
    var throughput: Throughput? = null

    fun sampleSize(n: Int): BenchmarkGroup<Intermediate, Value, M> {
        require(n >= 10) { "sample size must be at least 10" }
        partialConfig.sampleSize = n
        return this
    }

    fun warmUpTime(dur: Duration): BenchmarkGroup<Intermediate, Value, M> {
        require(dur > Duration.ZERO) { "warm up time must be positive" }
        partialConfig.warmUpTime = dur
        return this
    }

    fun measurementTime(dur: Duration): BenchmarkGroup<Intermediate, Value, M> {
        require(dur > Duration.ZERO) { "measurement time must be positive" }
        partialConfig.measurementTime = dur
        return this
    }

    fun nresamples(n: Int): BenchmarkGroup<Intermediate, Value, M> {
        require(n > 0) { "nresamples must be positive" }
        partialConfig.nresamples = n
        return this
    }

    fun confidenceLevel(cl: Double): BenchmarkGroup<Intermediate, Value, M> {
        require(cl > 0.0 && cl < 1.0) { "confidence level must be in (0, 1)" }
        partialConfig.confidenceLevel = cl
        return this
    }

    fun significanceLevel(sl: Double): BenchmarkGroup<Intermediate, Value, M> {
        require(sl > 0.0 && sl < 1.0) { "significance level must be in (0, 1)" }
        partialConfig.significanceLevel = sl
        return this
    }

    fun noiseThreshold(threshold: Double): BenchmarkGroup<Intermediate, Value, M> {
        require(threshold > 0.0) { "noise threshold must be positive" }
        partialConfig.noiseThreshold = threshold
        return this
    }

    fun samplingMode(mode: SamplingMode): BenchmarkGroup<Intermediate, Value, M> {
        partialConfig.samplingMode = mode
        return this
    }

    fun throughput(t: Throughput): BenchmarkGroup<Intermediate, Value, M> {
        this.throughput = t
        return this
    }

    fun benchFunction(id: String, f: (Bencher<Intermediate, Value>) -> Unit): BenchmarkGroup<Intermediate, Value, M> {
        benchWithInput(BenchmarkId.fromName(id), Unit) { b, _ -> f(b) }
        return this
    }

    fun <I> benchWithInput(
        id: BenchmarkId,
        input: I,
        f: (Bencher<Intermediate, Value>, I) -> Unit,
    ): BenchmarkGroup<Intermediate, Value, M> {
        val fullId = "$groupName/${id.asDirectoryName()}"
        if (!criterion.filter.matches(fullId)) {
            return this
        }

        val reportId =
            ReportBenchmarkId(
                groupId = groupName,
                functionId = id.functionName,
                valueStr = id.parameter,
                throughput = throughput,
            )

        val config = partialConfig.toComplete(criterion.config)
        val routine = Function<Intermediate, Value, I> { b, param -> f(b, param) }

        if (criterion.mode.isBenchmark()) {
            analyze(
                id = reportId,
                routine = routine,
                config = config,
                measurement = criterion.measurement,
                report = criterion.report,
                reportContext = ReportContext(criterion.outputDirectory, partialConfig.plotConfig),
                parameter = input,
                throughput = throughput,
            )
        } else {
            routine.test(criterion.measurement, input)
        }

        return this
    }

    fun finish() {
        // Explicit finish for benchmark group.
    }
}
