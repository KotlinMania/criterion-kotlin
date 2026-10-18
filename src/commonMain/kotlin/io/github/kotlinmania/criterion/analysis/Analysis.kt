package io.github.kotlinmania.criterion.analysis

import io.github.kotlinmania.criterion.Throughput
import io.github.kotlinmania.criterion.benchmark.BenchmarkConfig
import io.github.kotlinmania.criterion.estimate.Distributions
import io.github.kotlinmania.criterion.estimate.Estimates
import io.github.kotlinmania.criterion.estimate.PointEstimates
import io.github.kotlinmania.criterion.estimate.buildEstimates
import io.github.kotlinmania.criterion.measurement.Measurement
import io.github.kotlinmania.criterion.report.MeasurementData
import io.github.kotlinmania.criterion.report.Report
import io.github.kotlinmania.criterion.report.ReportBenchmarkId
import io.github.kotlinmania.criterion.report.ReportContext
import io.github.kotlinmania.criterion.routine.Routine
import io.github.kotlinmania.criterion.stats.Tuple1
import io.github.kotlinmania.criterion.stats.bivariate.Data
import io.github.kotlinmania.criterion.stats.univariate.Sample
import io.github.kotlinmania.criterion.stats.univariate.outliers.classify

internal fun computeEstimates(
    data: Data,
    avgTimes: Sample,
    config: BenchmarkConfig,
): Pair<Estimates, Distributions> {
    val cl = config.confidenceLevel
    val nresamples = config.nresamples

    val meanDist = avgTimes.bootstrap(nresamples) { s -> Tuple1(s.mean()) }.first
    val medianDist = avgTimes.bootstrap(nresamples) { s -> Tuple1(s.median()) }.first
    val madDist = avgTimes.bootstrap(nresamples) { s -> Tuple1(s.medianAbsDev()) }.first
    val stdDevDist = avgTimes.bootstrap(nresamples) { s -> Tuple1(s.stdDev()) }.first

    val distributions =
        Distributions(
            mean = meanDist,
            median = medianDist,
            medianAbsDev = madDist,
            slope = null,
            stdDev = stdDevDist,
        )

    val points =
        PointEstimates(
            mean = avgTimes.mean(),
            median = avgTimes.median(),
            medianAbsDev = avgTimes.medianAbsDev(),
            stdDev = avgTimes.stdDev(),
        )

    val estimates = buildEstimates(distributions, points, cl)
    return Pair(estimates, distributions)
}

internal fun <Intermediate, Value, T> analyze(
    id: ReportBenchmarkId,
    routine: Routine<Intermediate, Value, T>,
    config: BenchmarkConfig,
    measurement: Measurement<Intermediate, Value>,
    report: Report,
    reportContext: ReportContext,
    parameter: T,
    throughput: Throughput? = null,
) {
    report.benchmarkStart(id, reportContext)

    val sampleResult =
        routine.sample(
            measurement = measurement,
            id = id,
            config = config,
            report = report,
            reportContext = reportContext,
            parameter = parameter,
        )

    val iters = sampleResult.second
    val times = sampleResult.third

    val avgTimesArray = DoubleArray(iters.size)
    for (i in iters.indices) {
        val iter = iters[i]
        avgTimesArray[i] = if (iter > 0.0) times[i] / iter else 0.0
    }
    val avgTimes = Sample(avgTimesArray)
    val data = Data(iters, times)
    val labeledSample = classify(avgTimes)

    report.analysis(id, reportContext)

    val (estimates, distributions) = computeEstimates(data, avgTimes, config)

    val measurementData =
        MeasurementData(
            data = data,
            avgTimes = labeledSample,
            absoluteEstimates = estimates,
            distributions = distributions,
            comparison = null,
            throughput = throughput,
        )

    report.measurementComplete(
        id = id,
        context = reportContext,
        measurements = measurementData,
        formatter = measurement.formatter(),
    )
}
