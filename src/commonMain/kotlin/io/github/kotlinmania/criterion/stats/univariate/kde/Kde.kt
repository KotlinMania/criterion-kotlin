package io.github.kotlinmania.criterion.stats.univariate.kde

import io.github.kotlinmania.criterion.stats.univariate.Sample
import kotlin.math.pow

/**
 * Method to estimate the bandwidth.
 */
internal enum class Bandwidth {
    Silverman,
    ;

    fun estimate(sample: Sample): Double =
        when (this) {
            Silverman -> {
                val factor = 4.0 / 3.0
                val exponent = 1.0 / 5.0
                val n = sample.size.toDouble()
                val sigma = sample.stdDev()
                sigma * (factor / n).pow(exponent)
            }
        }
}

/**
 * Univariate kernel density estimator.
 */
internal class Kde(
    val sample: Sample,
    val kernel: Kernel,
    bw: Bandwidth,
) {
    val bandwidth: Double = bw.estimate(sample)

    /**
     * Estimates the probability density of x.
     */
    fun estimate(x: Double): Double {
        val h = bandwidth
        val n = sample.size.toDouble()
        var sum = 0.0
        for (i in 0 until sample.size) {
            val xi = sample[i]
            sum += kernel.evaluate((x - xi) / h)
        }
        return sum / (h * n)
    }

    /**
     * Maps the KDE over xs.
     */
    fun map(xs: DoubleArray): DoubleArray {
        val result = DoubleArray(xs.size)
        for (i in xs.indices) {
            result[i] = estimate(xs[i])
        }
        return result
    }
}
