package io.github.kotlinmania.criterion.stats.univariate

import io.github.kotlinmania.criterion.stats.Dist2
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.Tuple2
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Performs a two-sample bootstrap.
 */
internal fun bootstrap(
    a: Sample,
    b: Sample,
    nresamples: Int,
    statistic: (Sample, Sample) -> Tuple2<Double, Double>,
): Dist2<Double, Double> {
    val nresamplesSqrt = ceil(sqrt(nresamples.toDouble())).toInt()
    val perChunk = (nresamples + nresamplesSqrt - 1) / nresamplesSqrt

    val aResamples = Resamples(a)
    val bResamples = Resamples(b)

    val list1 = DoubleArray(nresamples)
    val list2 = DoubleArray(nresamples)
    var count = 0

    for (i in 0 until nresamplesSqrt) {
        val start = i * perChunk
        val end = min((i + 1) * perChunk, nresamples)
        val aResample = aResamples.next()

        repeat(end - start) {
            val bResample = bResamples.next()
            val tuple = statistic(aResample, bResample)
            list1[count] = tuple.first
            list2[count] = tuple.second
            count++
        }
    }

    return Dist2(Distribution(list1), Distribution(list2))
}
