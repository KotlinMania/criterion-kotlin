package io.github.kotlinmania.criterion

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Serializable
enum class SamplingMode {
    Auto,
    Linear,
    Flat,
    ;

    fun chooseSamplingMode(
        warmupMeanExecutionTime: Double,
        sampleSize: ULong,
        targetMeasurementTime: Double,
    ): ActualSamplingMode =
        when (this) {
            Auto -> {
                if (warmupMeanExecutionTime * sampleSize.toDouble() > targetMeasurementTime) {
                    ActualSamplingMode.Flat
                } else {
                    ActualSamplingMode.Linear
                }
            }

            Linear -> {
                ActualSamplingMode.Linear
            }

            Flat -> {
                ActualSamplingMode.Flat
            }
        }
}

@Serializable
enum class ActualSamplingMode {
    Linear,
    Flat,
    ;

    fun iterationCounts(
        warmupMeanExecutionTime: Double,
        sampleSize: ULong,
        targetMeasurementTime: Duration,
    ): List<ULong> {
        val n = sampleSize.toDouble()
        val mNs = targetMeasurementTime.toDouble(DurationUnit.NANOSECONDS)
        return when (this) {
            Linear -> {
                val a = 2.0 * mNs / (n * (n + 1.0))
                val d = a / warmupMeanExecutionTime
                val list = ArrayList<ULong>(sampleSize.toInt())
                for (m in 1..sampleSize.toLong()) {
                    val count =
                        if (d < 1.0) {
                            m.toULong()
                        } else {
                            (d * m.toDouble()).roundToLong().coerceAtLeast(1L).toULong()
                        }
                    list.add(count)
                }
                list
            }

            Flat -> {
                val iters = (mNs / (warmupMeanExecutionTime * n)).roundToLong()
                val finalIters = max(1L, iters).toULong()
                List(sampleSize.toInt()) { finalIters }
            }
        }
    }
}
