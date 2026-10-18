package io.github.kotlinmania.criterion.stats

import io.github.kotlinmania.criterion.stats.univariate.Percentiles
import io.github.kotlinmania.criterion.stats.univariate.Sample
import kotlin.math.min

/**
 * The bootstrap distribution of some parameter.
 */
internal class Distribution<A : Any>(
    val values: DoubleArray,
) {
    val size: Int get() = values.size

    operator fun get(index: Int): Double = values[index]

    fun asSample(): Sample = Sample(values)

    /**
     * Computes the confidence interval of the population parameter using percentiles.
     */
    fun confidenceInterval(confidenceLevel: Double): Pair<Double, Double> {
        require(confidenceLevel > 0.0 && confidenceLevel < 1.0) {
            "confidence level must be in (0, 1)"
        }
        val percentiles = percentiles()
        val lower = percentiles.at(50.0 * (1.0 - confidenceLevel))
        val upper = percentiles.at(50.0 * (1.0 + confidenceLevel))
        return Pair(lower, upper)
    }

    /**
     * Computes the likelihood of seeing the value t or more extreme values.
     */
    fun pValue(t: Double, tails: Tails): Double {
        val n = values.size
        var hits = 0
        for (v in values) {
            if (v < t) {
                hits++
            }
        }
        val tailsMultiplier =
            when (tails) {
                Tails.One -> 1.0
                Tails.Two -> 2.0
            }
        return min(hits, n - hits).toDouble() / n.toDouble() * tailsMultiplier
    }

    fun percentiles(): Percentiles = asSample().percentiles()

    fun mean(): Double = asSample().mean()

    fun stdDev(mean: Double? = null): Double = asSample().stdDev(mean)

    fun median(): Double = percentiles().median()

    fun iterator(): DoubleIterator = values.iterator()

    companion object {
        fun from(values: DoubleArray): Distribution<Double> = Distribution(values)
    }
}

/**
 * Number of tails for significance testing.
 */
internal enum class Tails {
    One,
    Two,
}

internal fun dot(xs: DoubleArray, ys: DoubleArray): Double {
    require(xs.size == ys.size) { "Arrays must have the same length" }
    var sum = 0.0
    for (i in xs.indices) {
        sum += xs[i] * ys[i]
    }
    return sum
}

internal fun sum(xs: DoubleArray): Double {
    var acc = 0.0
    for (x in xs) {
        acc += x
    }
    return acc
}
