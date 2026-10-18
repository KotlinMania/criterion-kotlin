// port-lint: source report.rs
package io.github.kotlinmania.criterion.report

import io.github.kotlinmania.criterion.PlotConfiguration
import io.github.kotlinmania.criterion.Throughput
import io.github.kotlinmania.criterion.estimate.ChangeDistributions
import io.github.kotlinmania.criterion.estimate.ChangeEstimates
import io.github.kotlinmania.criterion.estimate.Distributions
import io.github.kotlinmania.criterion.estimate.Estimates
import io.github.kotlinmania.criterion.format.change
import io.github.kotlinmania.criterion.format.iterCount
import io.github.kotlinmania.criterion.format.time
import io.github.kotlinmania.criterion.measurement.ValueFormatter
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.bivariate.Data
import io.github.kotlinmania.criterion.stats.univariate.Sample
import io.github.kotlinmania.criterion.stats.univariate.outliers.LabeledSample
import kotlinx.serialization.Serializable

const val MAX_DIRECTORY_NAME_LEN: Int = 64
const val MAX_TITLE_LEN: Int = 100

fun makeFilenameSafe(input: String): String {
    val unsafe = charArrayOf('?', '"', '/', '\\', '*', '<', '>', ':', '|', '^')
    var s = input
    for (c in unsafe) {
        s = s.replace(c, '_')
    }
    if (s.length > MAX_DIRECTORY_NAME_LEN) {
        s = s.substring(0, MAX_DIRECTORY_NAME_LEN)
    }
    return s.trimEnd()
}

@Serializable
internal data class ReportBenchmarkId(
    val groupId: String,
    val functionId: String? = null,
    val valueStr: String? = null,
    val throughput: Throughput? = null,
) {
    val fullId: String
        get() =
            when {
                functionId != null && valueStr != null -> "$groupId/$functionId/$valueStr"
                functionId != null -> "$groupId/$functionId"
                valueStr != null -> "$groupId/$valueStr"
                else -> groupId
            }

    val asTitle: String
        get() =
            if (fullId.length > MAX_TITLE_LEN) {
                fullId.substring(0, MAX_TITLE_LEN) + "..."
            } else {
                fullId
            }

    val asDirectoryName: String
        get() =
            when {
                functionId != null && valueStr != null -> {
                    "${makeFilenameSafe(groupId)}/${makeFilenameSafe(functionId)}/${makeFilenameSafe(valueStr)}"
                }

                functionId != null -> {
                    "${makeFilenameSafe(groupId)}/${makeFilenameSafe(functionId)}"
                }

                valueStr != null -> {
                    "${makeFilenameSafe(groupId)}/${makeFilenameSafe(valueStr)}"
                }

                else -> {
                    makeFilenameSafe(groupId)
                }
            }
}

internal data class ComparisonData(
    val pValue: Double,
    val tDistribution: Distribution<Double>,
    val tValue: Double,
    val relativeEstimates: ChangeEstimates,
    val relativeDistributions: ChangeDistributions,
    val significanceThreshold: Double,
    val noiseThreshold: Double,
    val baseIterCounts: List<Double>,
    val baseSampleTimes: List<Double>,
    val baseAvgTimes: List<Double>,
    val baseEstimates: Estimates,
)

internal data class MeasurementData(
    val data: Data,
    val avgTimes: LabeledSample,
    val absoluteEstimates: Estimates,
    val distributions: Distributions,
    val comparison: ComparisonData? = null,
    val throughput: Throughput? = null,
) {
    fun iterCounts(): Sample = data.x()

    fun sampleTimes(): Sample = data.y()
}

internal data class ReportContext(
    val outputDirectory: String,
    val plotConfig: PlotConfiguration = PlotConfiguration(),
)

/**
 * Report interface for receiving progress and benchmark results.
 */
internal interface Report {
    fun benchmarkStart(id: ReportBenchmarkId, context: ReportContext) {}

    fun warmup(id: ReportBenchmarkId, context: ReportContext, warmupNs: Double) {}

    fun measurementStart(
        id: ReportBenchmarkId,
        context: ReportContext,
        sampleCount: ULong,
        estimateNs: Double,
        iterCount: ULong,
    ) {}

    fun analysis(id: ReportBenchmarkId, context: ReportContext) {}

    fun measurementComplete(
        id: ReportBenchmarkId,
        context: ReportContext,
        measurements: MeasurementData,
        formatter: ValueFormatter,
    ) {}

    fun profile(id: ReportBenchmarkId, context: ReportContext, nanos: Double) {}

    fun terminated(id: ReportBenchmarkId, context: ReportContext) {}

    fun finalSummary(context: ReportContext) {}
}

/**
 * Console reporter for printing benchmark results to standard output.
 */
internal class CliReport : Report {
    override fun benchmarkStart(id: ReportBenchmarkId, context: ReportContext) {
        println("Benchmarking ${id.asTitle}:")
    }

    override fun warmup(id: ReportBenchmarkId, context: ReportContext, warmupNs: Double) {
        println("  Warming up in ${time(warmupNs)}")
    }

    override fun measurementStart(
        id: ReportBenchmarkId,
        context: ReportContext,
        sampleCount: ULong,
        estimateNs: Double,
        iterCount: ULong,
    ) {
        println("  Collecting $sampleCount samples in estimated ${time(estimateNs)} (${iterCount(iterCount)})")
    }

    override fun measurementComplete(
        id: ReportBenchmarkId,
        context: ReportContext,
        measurements: MeasurementData,
        formatter: ValueFormatter,
    ) {
        val typical = measurements.absoluteEstimates.typical()
        val timeStr = formatter.formatValue(typical.pointEstimate)
        println("  time:   [${formatter.formatValue(typical.confidenceInterval.lowerBound)} $timeStr ${formatter.formatValue(typical.confidenceInterval.upperBound)}]")

        measurements.comparison?.let { comp ->
            val meanEstimate = comp.relativeEstimates.mean
            val pct = change(meanEstimate.pointEstimate, true)
            println("  change: [${change(meanEstimate.confidenceInterval.lowerBound, true)} $pct ${change(meanEstimate.confidenceInterval.upperBound, true)}]")
        }
    }
}

/**
 * Composite report delegating to multiple reporters.
 */
internal class Reports(
    val reports: List<Report>,
) : Report {
    override fun benchmarkStart(id: ReportBenchmarkId, context: ReportContext) {
        for (r in reports) r.benchmarkStart(id, context)
    }

    override fun warmup(id: ReportBenchmarkId, context: ReportContext, warmupNs: Double) {
        for (r in reports) r.warmup(id, context, warmupNs)
    }

    override fun measurementStart(
        id: ReportBenchmarkId,
        context: ReportContext,
        sampleCount: ULong,
        estimateNs: Double,
        iterCount: ULong,
    ) {
        for (r in reports) r.measurementStart(id, context, sampleCount, estimateNs, iterCount)
    }

    override fun analysis(id: ReportBenchmarkId, context: ReportContext) {
        for (r in reports) r.analysis(id, context)
    }

    override fun measurementComplete(
        id: ReportBenchmarkId,
        context: ReportContext,
        measurements: MeasurementData,
        formatter: ValueFormatter,
    ) {
        for (r in reports) r.measurementComplete(id, context, measurements, formatter)
    }

    override fun profile(id: ReportBenchmarkId, context: ReportContext, nanos: Double) {
        for (r in reports) r.profile(id, context, nanos)
    }

    override fun terminated(id: ReportBenchmarkId, context: ReportContext) {
        for (r in reports) r.terminated(id, context)
    }

    override fun finalSummary(context: ReportContext) {
        for (r in reports) r.finalSummary(context)
    }
}
