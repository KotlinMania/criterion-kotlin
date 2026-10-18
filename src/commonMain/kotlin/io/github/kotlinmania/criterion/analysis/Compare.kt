// port-lint: source analysis/compare.rs
package io.github.kotlinmania.criterion.analysis

import io.github.kotlinmania.criterion.benchmark.BenchmarkConfig
import io.github.kotlinmania.criterion.estimate.ChangeDistributions
import io.github.kotlinmania.criterion.estimate.ChangeEstimates
import io.github.kotlinmania.criterion.estimate.ChangePointEstimates
import io.github.kotlinmania.criterion.estimate.buildChangeEstimates
import io.github.kotlinmania.criterion.stats.Distribution
import io.github.kotlinmania.criterion.stats.Tuple1
import io.github.kotlinmania.criterion.stats.Tuple2
import io.github.kotlinmania.criterion.stats.univariate.Sample
import io.github.kotlinmania.criterion.stats.univariate.bootstrap
import io.github.kotlinmania.criterion.stats.univariate.mixedBootstrap

internal fun tTest(
    avgTimes: Sample,
    baseAvgTimes: Sample,
    config: BenchmarkConfig,
): Pair<Double, Distribution<Double>> {
    val nresamples = config.nresamples
    val tStatistic = avgTimes.t(baseAvgTimes)
    val dist =
        mixedBootstrap(avgTimes, baseAvgTimes, nresamples) { a, b ->
            Tuple1(a.t(b))
        }.first
    return Pair(tStatistic, dist)
}

internal fun relativeEstimates(
    avgTimes: Sample,
    baseAvgTimes: Sample,
    config: BenchmarkConfig,
): Pair<ChangeEstimates, ChangeDistributions> {
    fun stats(a: Sample, b: Sample): Tuple2<Double, Double> {
        val relMean = (a.mean() / b.mean()) - 1.0
        val relMedian = (a.percentiles().median() / b.percentiles().median()) - 1.0
        return Tuple2(relMean, relMedian)
    }

    val cl = config.confidenceLevel
    val nresamples = config.nresamples

    val dists = bootstrap(avgTimes, baseAvgTimes, nresamples, ::stats)
    val distributions =
        ChangeDistributions(
            mean = dists.first,
            median = dists.second,
        )

    val statTuple = stats(avgTimes, baseAvgTimes)
    val points =
        ChangePointEstimates(
            mean = statTuple.first,
            median = statTuple.second,
        )

    val estimates = buildChangeEstimates(distributions, points, cl)
    return Pair(estimates, distributions)
}
