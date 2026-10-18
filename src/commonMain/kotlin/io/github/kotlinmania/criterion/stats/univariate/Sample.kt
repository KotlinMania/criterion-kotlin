// port-lint: source stats/univariate/sample.rs
package io.github.kotlinmania.criterion.stats.univariate

import io.github.kotlinmania.criterion.stats.Dist1
import io.github.kotlinmania.criterion.stats.Dist2
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.Tuple1
import io.github.kotlinmania.criterion.stats.Tuple2
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A collection of data points drawn from a population.
 * Invariants: contains at least 2 data points, and no NaNs.
 */
internal class Sample(
    val data: DoubleArray,
) {
    init {
        require(data.size > 1) { "Sample must contain at least 2 data points" }
        for (x in data) {
            require(!x.isNaN()) { "Sample must not contain NaN" }
        }
    }

    val size: Int get() = data.size

    operator fun get(index: Int): Double = data[index]

    fun max(): Double {
        var m = data[0]
        for (i in 1 until data.size) {
            val v = data[i]
            if (v > m) m = v
        }
        return m
    }

    fun min(): Double {
        var m = data[0]
        for (i in 1 until data.size) {
            val v = data[i]
            if (v < m) m = v
        }
        return m
    }

    fun sum(): Double {
        var s = 0.0
        for (v in data) {
            s += v
        }
        return s
    }

    fun mean(): Double = sum() / data.size.toDouble()

    fun variance(mean: Double? = null): Double {
        val m = mean ?: mean()
        var s = 0.0
        for (v in data) {
            s += (v - m).pow(2)
        }
        return s / (data.size - 1).toDouble()
    }

    fun stdDev(mean: Double? = null): Double = sqrt(variance(mean))

    fun stdDevPct(): Double {
        val m = mean()
        return (stdDev(m) / m) * 100.0
    }

    fun percentiles(): Percentiles {
        val sorted = data.copyOf()
        sorted.sort()
        return Percentiles(sorted)
    }

    fun median(): Double = percentiles().median()

    fun iqr(): Double = percentiles().iqr()

    fun medianAbsDev(median: Double? = null): Double {
        val med = median ?: percentiles().median()
        val absDevs = DoubleArray(data.size)
        for (i in data.indices) {
            absDevs[i] = abs(data[i] - med)
        }
        val absSample = Sample(absDevs)
        return absSample.percentiles().median() * 1.4826
    }

    fun medianAbsDevPct(): Double {
        val med = percentiles().median()
        val mad = medianAbsDev(med)
        return (mad / med) * 100.0
    }

    fun t(other: Sample): Double {
        val xBar = mean()
        val yBar = other.mean()
        val s2X = variance(xBar)
        val s2Y = other.variance(yBar)
        val nX = data.size.toDouble()
        val nY = other.data.size.toDouble()
        val num = xBar - yBar
        val den = sqrt(s2X / nX + s2Y / nY)
        return num / den
    }

    fun bootstrap(nresamples: Int, statistic: (Sample) -> Tuple1<Double>): Dist1<Double> {
        val resamples = Resamples(this)
        val list = DoubleArray(nresamples)
        for (i in 0 until nresamples) {
            list[i] = statistic(resamples.next()).first
        }
        return Dist1(Distribution(list))
    }

    fun bootstrap2(nresamples: Int, statistic: (Sample) -> Tuple2<Double, Double>): Dist2<Double, Double> {
        val resamples = Resamples(this)
        val list1 = DoubleArray(nresamples)
        val list2 = DoubleArray(nresamples)
        for (i in 0 until nresamples) {
            val tuple = statistic(resamples.next())
            list1[i] = tuple.first
            list2[i] = tuple.second
        }
        return Dist2(Distribution(list1), Distribution(list2))
    }

    fun iterator(): DoubleIterator = data.iterator()

    companion object {
        fun new(slice: DoubleArray): Sample = Sample(slice)
    }
}
