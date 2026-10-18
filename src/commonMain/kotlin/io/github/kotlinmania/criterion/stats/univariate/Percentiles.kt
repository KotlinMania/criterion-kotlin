// port-lint: source stats/univariate/percentiles.rs
package io.github.kotlinmania.criterion.stats.univariate

import kotlin.math.floor

/**
 * A view into the percentiles of a sample.
 */
internal class Percentiles(
    val values: DoubleArray,
) {
    private fun atUnchecked(p: Double): Double {
        val len = values.size - 1
        if (p == 100.0) {
            return values[len]
        }
        val rank = (p / 100.0) * len.toDouble()
        val intRank = floor(rank)
        val fraction = rank - intRank
        val n = intRank.toInt()
        val floorVal = values[n]
        val ceilVal = values[n + 1]
        return floorVal + (ceilVal - floorVal) * fraction
    }

    /**
     * Returns the percentile at p percent.
     */
    fun at(p: Double): Double {
        require(p in 0.0..100.0) { "p must be in [0, 100]" }
        require(values.isNotEmpty()) { "values must not be empty" }
        return atUnchecked(p)
    }

    /**
     * Returns the interquartile range.
     */
    fun iqr(): Double {
        val q1 = at(25.0)
        val q3 = at(75.0)
        return q3 - q1
    }

    /**
     * Returns the 50th percentile.
     */
    fun median(): Double = at(50.0)

    /**
     * Returns the 25th, 50th, and 75th percentiles.
     */
    fun quartiles(): Triple<Double, Double, Double> = Triple(at(25.0), at(50.0), at(75.0))
}
