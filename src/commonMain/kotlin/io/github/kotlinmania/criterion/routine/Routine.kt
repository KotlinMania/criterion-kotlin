// port-lint: source routine.rs
package io.github.kotlinmania.criterion.routine

import io.github.kotlinmania.criterion.ActualSamplingMode
import io.github.kotlinmania.criterion.bencher.Bencher
import io.github.kotlinmania.criterion.benchmark.BenchmarkConfig
import io.github.kotlinmania.criterion.blackBox
import io.github.kotlinmania.criterion.measurement.Measurement
import io.github.kotlinmania.criterion.report.Report
import io.github.kotlinmania.criterion.report.ReportBenchmarkId
import io.github.kotlinmania.criterion.report.ReportContext
import kotlin.time.Duration
import kotlin.time.DurationUnit

internal interface Routine<Intermediate, Value, T> {
    fun bench(m: Measurement<Intermediate, Value>, iters: List<ULong>, parameter: T): DoubleArray

    fun warmUp(m: Measurement<Intermediate, Value>, howLong: Duration, parameter: T): Pair<ULong, ULong>

    fun test(m: Measurement<Intermediate, Value>, parameter: T) {
        bench(m, listOf(1uL), parameter)
    }

    fun sample(
        measurement: Measurement<Intermediate, Value>,
        id: ReportBenchmarkId,
        config: BenchmarkConfig,
        report: Report,
        reportContext: ReportContext,
        parameter: T,
    ): Triple<ActualSamplingMode, DoubleArray, DoubleArray> {
        val wu = config.warmUpTime
        val mNs = config.measurementTime.toDouble(DurationUnit.NANOSECONDS)

        report.warmup(id, reportContext, wu.toDouble(DurationUnit.NANOSECONDS))

        val (wuElapsed, wuIters) = warmUp(measurement, wu, parameter)
        val met = if (wuIters > 0uL) wuElapsed.toDouble() / wuIters.toDouble() else 1.0

        val n = config.sampleSize.toULong()
        val actualSamplingMode = config.samplingMode.chooseSamplingMode(met, n, mNs)
        val mIters = actualSamplingMode.iterationCounts(met, n, config.measurementTime)

        var expectedNs = 0.0
        var totalIters = 0uL
        for (count in mIters) {
            expectedNs += count.toDouble() * met
            totalIters += count
        }

        report.measurementStart(id, reportContext, n, expectedNs, totalIters)

        val mElapsed = bench(measurement, mIters, parameter)
        val mItersF = DoubleArray(mIters.size) { i -> mIters[i].toDouble() }

        return Triple(actualSamplingMode, mItersF, mElapsed)
    }
}

internal class Function<Intermediate, Value, T>(
    private val f: (Bencher<Intermediate, Value>, T) -> Unit,
) : Routine<Intermediate, Value, T> {
    override fun bench(
        m: Measurement<Intermediate, Value>,
        iters: List<ULong>,
        parameter: T,
    ): DoubleArray {
        val b = Bencher(m)
        val results = DoubleArray(iters.size)
        for (i in iters.indices) {
            b.iters = iters[i]
            f(b, blackBox(parameter))
            b.assertIterated()
            results[i] = m.toF64(b.value)
        }
        return results
    }

    override fun warmUp(
        m: Measurement<Intermediate, Value>,
        howLong: Duration,
        parameter: T,
    ): Pair<ULong, ULong> {
        val b = Bencher(m)
        b.iters = 1uL
        var totalIters = 0uL
        var elapsedTime = Duration.ZERO
        val howLongNs = howLong.toDouble(DurationUnit.NANOSECONDS)

        while (true) {
            f(b, blackBox(parameter))
            b.assertIterated()
            totalIters += b.iters
            elapsedTime += b.elapsedTime
            if (elapsedTime.toDouble(DurationUnit.NANOSECONDS) > howLongNs) {
                return Pair(elapsedTime.toDouble(DurationUnit.NANOSECONDS).toULong(), totalIters)
            }
            b.iters = b.iters * 2uL
        }
    }
}
