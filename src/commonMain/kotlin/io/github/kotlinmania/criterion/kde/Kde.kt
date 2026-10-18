// port-lint: source kde.rs
package io.github.kotlinmania.criterion.kde

import io.github.kotlinmania.criterion.stats.univariate.Sample
import io.github.kotlinmania.criterion.stats.univariate.kde.Bandwidth
import io.github.kotlinmania.criterion.stats.univariate.kde.Gaussian
import io.github.kotlinmania.criterion.stats.univariate.kde.Kde

internal fun sweep(
    sample: Sample,
    npoints: Int,
    range: Pair<Double, Double>? = null,
): Pair<DoubleArray, DoubleArray> {
    val (xs, ys, _) = sweepAndEstimate(sample, npoints, range, sample[0])
    return Pair(xs, ys)
}

internal fun sweepAndEstimate(
    sample: Sample,
    npoints: Int,
    range: Pair<Double, Double>? = null,
    pointToEstimate: Double,
): Triple<DoubleArray, DoubleArray, Double> {
    val xMin = sample.min()
    val xMax = sample.max()

    val kde = Kde(sample, Gaussian, Bandwidth.Silverman)
    val h = kde.bandwidth

    val (start, end) = range ?: Pair(xMin - 3.0 * h, xMax + 3.0 * h)

    val xs = DoubleArray(npoints)
    val stepSize = (end - start) / (npoints - 1).toDouble()
    for (n in 0 until npoints) {
        xs[n] = start + (stepSize * n.toDouble())
    }

    val ys = kde.map(xs)
    val pointEstimate = kde.estimate(pointToEstimate)

    return Triple(xs, ys, pointEstimate)
}
