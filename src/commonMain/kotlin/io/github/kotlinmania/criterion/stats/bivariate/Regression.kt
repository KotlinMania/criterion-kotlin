// port-lint: source stats/bivariate/regression.rs
package io.github.kotlinmania.criterion.stats.bivariate

import io.github.kotlinmania.criterion.stats.dot
import io.github.kotlinmania.criterion.stats.sum
import kotlin.math.pow

/**
 * A straight line that passes through the origin: y = m * x.
 */
internal data class Slope(
    val value: Double,
) {
    /**
     * Computes the goodness of fit (coefficient of determination) for this data set.
     */
    fun rSquared(data: Data): Double {
        val m = value
        val xs = data.xs
        val ys = data.ys
        val n = xs.size.toDouble()
        val yBar = sum(ys) / n

        var ssRes = 0.0
        var ssTot = 0.0

        for (i in xs.indices) {
            val x = xs[i]
            val y = ys[i]
            ssRes += (y - m * x).pow(2)
            ssTot += (y - yBar).pow(2)
        }

        return 1.0 - (ssRes / ssTot)
    }

    companion object {
        /**
         * Fits the data to a straight line passing through the origin using ordinary least squares.
         */
        fun fit(data: Data): Slope {
            val xs = data.xs
            val ys = data.ys
            val xy = dot(xs, ys)
            val x2 = dot(xs, xs)
            return Slope(xy / x2)
        }
    }
}
