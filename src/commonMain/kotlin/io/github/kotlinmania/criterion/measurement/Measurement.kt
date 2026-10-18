// port-lint: source measurement.rs
package io.github.kotlinmania.criterion.measurement

import io.github.kotlinmania.criterion.Throughput
import io.github.kotlinmania.criterion.format.short
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Trait providing functions to format measured values to string for display.
 */
interface ValueFormatter {
    fun formatValue(value: Double): String {
        val values = doubleArrayOf(value)
        val unit = scaleValues(value, values)
        return "${short(values[0]).padStart(6)} $unit"
    }

    fun formatThroughput(throughput: Throughput, value: Double): String {
        val values = doubleArrayOf(value)
        val unit = scaleThroughputs(value, throughput, values)
        return "${short(values[0]).padStart(6)} $unit"
    }

    fun scaleValues(typicalValue: Double, values: DoubleArray): String

    fun scaleThroughputs(typicalValue: Double, throughput: Throughput, values: DoubleArray): String

    fun scaleForMachines(values: DoubleArray): String
}

/**
 * Trait for all types which define something Criterion can measure.
 */
interface Measurement<Intermediate, Value> {
    fun start(): Intermediate

    fun end(i: Intermediate): Value

    fun add(v1: Value, v2: Value): Value

    fun zero(): Value

    fun toF64(value: Value): Double

    fun formatter(): ValueFormatter
}

object DurationFormatter : ValueFormatter {
    private fun bytesPerSecond(bytes: Double, typical: Double, values: DoubleArray): String {
        val bytesPerSec = bytes * (1e9 / typical)
        val (denominator, unit) =
            if (bytesPerSec < 1024.0) {
                Pair(1.0, "  B/s")
            } else if (bytesPerSec < 1024.0 * 1024.0) {
                Pair(1024.0, "KiB/s")
            } else if (bytesPerSec < 1024.0 * 1024.0 * 1024.0) {
                Pair(1024.0 * 1024.0, "MiB/s")
            } else {
                Pair(1024.0 * 1024.0 * 1024.0, "GiB/s")
            }

        for (i in values.indices) {
            val bps = bytes * (1e9 / values[i])
            values[i] = bps / denominator
        }

        return unit
    }

    private fun bytesPerSecondDecimal(bytes: Double, typical: Double, values: DoubleArray): String {
        val bytesPerSec = bytes * (1e9 / typical)
        val (denominator, unit) =
            if (bytesPerSec < 1000.0) {
                Pair(1.0, "  B/s")
            } else if (bytesPerSec < 1000.0 * 1000.0) {
                Pair(1000.0, "KB/s")
            } else if (bytesPerSec < 1000.0 * 1000.0 * 1000.0) {
                Pair(1000.0 * 1000.0, "MB/s")
            } else {
                Pair(1000.0 * 1000.0 * 1000.0, "GB/s")
            }

        for (i in values.indices) {
            val bps = bytes * (1e9 / values[i])
            values[i] = bps / denominator
        }

        return unit
    }

    private fun elementsPerSecond(elems: Double, typical: Double, values: DoubleArray): String {
        val elemsPerSec = elems * (1e9 / typical)
        val (denominator, unit) =
            if (elemsPerSec < 1000.0) {
                Pair(1.0, " elem/s")
            } else if (elemsPerSec < 1000.0 * 1000.0) {
                Pair(1000.0, "Kelem/s")
            } else if (elemsPerSec < 1000.0 * 1000.0 * 1000.0) {
                Pair(1000.0 * 1000.0, "Melem/s")
            } else {
                Pair(1000.0 * 1000.0 * 1000.0, "Gelem/s")
            }

        for (i in values.indices) {
            val eps = elems * (1e9 / values[i])
            values[i] = eps / denominator
        }

        return unit
    }

    private fun bitsPerSecond(bits: Double, typical: Double, values: DoubleArray): String {
        val bitsPerSec = bits * (1e9 / typical)
        val (denominator, unit) =
            if (bitsPerSec < 1000.0) {
                Pair(1.0, "  b/s")
            } else if (bitsPerSec < 1000.0 * 1000.0) {
                Pair(1000.0, "Kb/s")
            } else if (bitsPerSec < 1000.0 * 1000.0 * 1000.0) {
                Pair(1000.0 * 1000.0, "Mb/s")
            } else {
                Pair(1000.0 * 1000.0 * 1000.0, "Gb/s")
            }

        for (i in values.indices) {
            val bps = bits * (1e9 / values[i])
            values[i] = bps / denominator
        }

        return unit
    }

    override fun scaleThroughputs(
        typicalValue: Double,
        throughput: Throughput,
        values: DoubleArray,
    ): String =
        when (throughput) {
            is Throughput.Bytes -> bytesPerSecond(throughput.count.toDouble(), typicalValue, values)
            is Throughput.BytesDecimal -> bytesPerSecondDecimal(throughput.count.toDouble(), typicalValue, values)
            is Throughput.Elements -> elementsPerSecond(throughput.count.toDouble(), typicalValue, values)
            is Throughput.Bits -> bitsPerSecond(throughput.count.toDouble(), typicalValue, values)
        }

    override fun scaleValues(typicalValue: Double, values: DoubleArray): String {
        val (factor, unit) =
            if (typicalValue < 1.0) {
                Pair(1000.0, "ps")
            } else if (typicalValue < 1000.0) {
                Pair(1.0, "ns")
            } else if (typicalValue < 1000000.0) {
                Pair(1e-3, "µs")
            } else if (typicalValue < 1000000000.0) {
                Pair(1e-6, "ms")
            } else {
                Pair(1e-9, "s")
            }

        for (i in values.indices) {
            values[i] *= factor
        }

        return unit
    }

    override fun scaleForMachines(values: DoubleArray): String = "ns"
}

/**
 * WallTime is the default measurement in Criterion.
 * It measures the elapsed time from start to end of iterations.
 */
class WallTime : Measurement<TimeMark, Duration> {
    override fun start(): TimeMark = TimeSource.Monotonic.markNow()

    override fun end(i: TimeMark): Duration = i.elapsedNow()

    override fun add(v1: Duration, v2: Duration): Duration = v1 + v2

    override fun zero(): Duration = Duration.ZERO

    override fun toF64(value: Duration): Double = value.toDouble(DurationUnit.NANOSECONDS)

    override fun formatter(): ValueFormatter = DurationFormatter
}
