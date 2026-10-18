// port-lint: source stats/univariate/mixed.rs
package io.github.kotlinmania.criterion.stats.univariate

import io.github.kotlinmania.criterion.stats.Dist1
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.Tuple1

/**
 * Performs a mixed two-sample bootstrap.
 */
internal fun mixedBootstrap(
    a: Sample,
    b: Sample,
    nresamples: Int,
    statistic: (Sample, Sample) -> Tuple1<Double>,
): Dist1<Double> {
    val na = a.size
    val nb = b.size
    val cData = DoubleArray(na + nb)
    for (i in 0 until na) {
        cData[i] = a[i]
    }
    for (i in 0 until nb) {
        cData[na + i] = b[i]
    }
    val c = Sample(cData)
    val resamples = Resamples(c)

    val results = DoubleArray(nresamples)
    val aBuf = DoubleArray(na)
    val bBuf = DoubleArray(nb)
    for (i in 0 until nresamples) {
        val resample = resamples.next()
        for (j in 0 until na) {
            aBuf[j] = resample[j]
        }
        for (j in 0 until nb) {
            bBuf[j] = resample[na + j]
        }
        val resA = Sample(aBuf.copyOf())
        val resB = Sample(bBuf.copyOf())
        results[i] = statistic(resA, resB).first
    }
    return Dist1(Distribution(results))
}
