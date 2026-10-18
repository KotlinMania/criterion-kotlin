package io.github.kotlinmania.criterion.stats.bivariate

import io.github.kotlinmania.criterion.stats.Dist1
import io.github.kotlinmania.criterion.stats.Dist2
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.Tuple1
import io.github.kotlinmania.criterion.stats.Tuple2
import io.github.kotlinmania.criterion.stats.univariate.Sample

/**
 * Bivariate (X, Y) data points.
 * Invariants: length of xs and ys match, length > 1, no NaNs.
 */
internal class Data(
    val xs: DoubleArray,
    val ys: DoubleArray,
) {
    init {
        require(xs.size == ys.size) { "xs and ys must have the same length" }
        require(xs.size > 1) { "Data must contain at least 2 data points" }
        for (x in xs) {
            require(!x.isNaN()) { "xs must not contain NaN" }
        }
        for (y in ys) {
            require(!y.isNaN()) { "ys must not contain NaN" }
        }
    }

    val size: Int get() = xs.size

    fun x(): Sample = Sample(xs)

    fun y(): Sample = Sample(ys)

    fun bootstrap(nresamples: Int, statistic: (Data) -> Tuple1<Double>): Dist1<Double> {
        val resamples = Resamples(this)
        val list = DoubleArray(nresamples)
        for (i in 0 until nresamples) {
            list[i] = statistic(resamples.next()).first
        }
        return Dist1(Distribution(list))
    }

    fun bootstrap2(nresamples: Int, statistic: (Data) -> Tuple2<Double, Double>): Dist2<Double, Double> {
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

    fun iter(): Iterator<Pair<Double, Double>> =
        iterator {
            for (i in xs.indices) {
                yield(Pair(xs[i], ys[i]))
            }
        }

    companion object {
        fun new(xs: DoubleArray, ys: DoubleArray): Data = Data(xs, ys)
    }
}
