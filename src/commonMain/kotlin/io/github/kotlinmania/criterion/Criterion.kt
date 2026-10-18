package io.github.kotlinmania.criterion

import io.github.kotlinmania.criterion.bencher.Bencher
import io.github.kotlinmania.criterion.benchmark.BenchmarkConfig
import io.github.kotlinmania.criterion.benchmark.BenchmarkGroup
import io.github.kotlinmania.criterion.benchmark.BenchmarkId
import io.github.kotlinmania.criterion.measurement.Measurement
import io.github.kotlinmania.criterion.measurement.WallTime
import io.github.kotlinmania.criterion.profiler.ExternalProfiler
import io.github.kotlinmania.criterion.profiler.Profiler
import io.github.kotlinmania.criterion.report.CliReport
import io.github.kotlinmania.criterion.report.Report
import io.github.kotlinmania.criterion.report.ReportContext
import kotlin.time.Duration
import kotlin.time.TimeMark

/**
 * The benchmark manager that lets you configure and execute benchmarks.
 */
class Criterion<Intermediate, Value, M : Measurement<Intermediate, Value>>(
    val measurement: M,
) {
    var config: BenchmarkConfig = BenchmarkConfig()
    var filter: BenchmarkFilter = BenchmarkFilter.AcceptAll
    internal var report: Report = CliReport()
    var outputDirectory: String = "target/criterion"
    var baselineDirectory: String = "base"
    var baseline: Baseline = Baseline.Save
    var loadBaseline: String? = null
    var profiler: Profiler = ExternalProfiler
    var profileTime: Duration? = null
    var plottingBackend: PlottingBackend = PlottingBackend.None
    var mode: Mode = Mode.Benchmark

    fun sampleSize(n: Int): Criterion<Intermediate, Value, M> {
        require(n >= 10) { "sample size must be at least 10" }
        config = config.copy(sampleSize = n)
        return this
    }

    fun warmUpTime(dur: Duration): Criterion<Intermediate, Value, M> {
        require(dur > Duration.ZERO) { "warm up time must be positive" }
        config = config.copy(warmUpTime = dur)
        return this
    }

    fun measurementTime(dur: Duration): Criterion<Intermediate, Value, M> {
        require(dur > Duration.ZERO) { "measurement time must be positive" }
        config = config.copy(measurementTime = dur)
        return this
    }

    fun nresamples(n: Int): Criterion<Intermediate, Value, M> {
        require(n > 0) { "nresamples must be positive" }
        config = config.copy(nresamples = n)
        return this
    }

    fun noiseThreshold(threshold: Double): Criterion<Intermediate, Value, M> {
        require(threshold > 0.0) { "noise threshold must be positive" }
        config = config.copy(noiseThreshold = threshold)
        return this
    }

    fun confidenceLevel(cl: Double): Criterion<Intermediate, Value, M> {
        require(cl > 0.0 && cl < 1.0) { "confidence level must be in (0, 1)" }
        config = config.copy(confidenceLevel = cl)
        return this
    }

    fun significanceLevel(sl: Double): Criterion<Intermediate, Value, M> {
        require(sl > 0.0 && sl < 1.0) { "significance level must be in (0, 1)" }
        config = config.copy(significanceLevel = sl)
        return this
    }

    fun withFilter(pattern: String): Criterion<Intermediate, Value, M> {
        filter = BenchmarkFilter.Regex(kotlin.text.Regex(pattern))
        return this
    }

    fun saveBaseline(baseline: String): Criterion<Intermediate, Value, M> {
        this.baseline = Baseline.Save
        this.baselineDirectory = baseline
        return this
    }

    fun retainBaseline(baseline: String, strict: Boolean): Criterion<Intermediate, Value, M> {
        this.baseline = if (strict) Baseline.CompareStrict else Baseline.CompareLenient
        this.baselineDirectory = baseline
        return this
    }

    fun outputDirectory(path: String): Criterion<Intermediate, Value, M> {
        this.outputDirectory = path
        return this
    }

    fun withoutPlots(): Criterion<Intermediate, Value, M> {
        this.plottingBackend = PlottingBackend.None
        return this
    }

    fun withProfiler(profiler: Profiler): Criterion<Intermediate, Value, M> {
        this.profiler = profiler
        return this
    }

    fun profileTime(dur: Duration?): Criterion<Intermediate, Value, M> {
        this.profileTime = dur
        return this
    }

    fun benchFunction(id: String, f: (Bencher<Intermediate, Value>) -> Unit): Criterion<Intermediate, Value, M> {
        val group = benchmarkGroup(id)
        group.benchFunction(id, f)
        group.finish()
        return this
    }

    fun <I> benchWithInput(
        id: BenchmarkId,
        input: I,
        f: (Bencher<Intermediate, Value>, I) -> Unit,
    ): Criterion<Intermediate, Value, M> {
        val groupName = id.functionName ?: "default"
        val group = benchmarkGroup(groupName)
        group.benchWithInput(id, input, f)
        group.finish()
        return this
    }

    fun benchmarkGroup(groupName: String): BenchmarkGroup<Intermediate, Value, M> = BenchmarkGroup(this, groupName)

    fun finalSummary() {
        report.finalSummary(ReportContext(outputDirectory))
    }

    companion object {
        fun default(): Criterion<TimeMark, Duration, WallTime> = Criterion(WallTime())
    }
}
