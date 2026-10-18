// port-lint: source estimate.rs
package io.github.kotlinmania.criterion.estimate

import io.github.kotlinmania.criterion.stats.Distribution
import kotlinx.serialization.Serializable

@Serializable
internal enum class Statistic {
    Mean,
    Median,
    MedianAbsDev,
    Slope,
    StdDev,
    Typical,
    ;

    override fun toString(): String =
        when (this) {
            Mean -> "mean"
            Median -> "median"
            MedianAbsDev -> "MAD"
            Slope -> "slope"
            StdDev -> "SD"
            Typical -> "typical"
        }
}

@Serializable
internal data class ConfidenceInterval(
    val confidenceLevel: Double,
    val lowerBound: Double,
    val upperBound: Double,
)

@Serializable
internal data class Estimate(
    val confidenceInterval: ConfidenceInterval,
    val pointEstimate: Double,
    val standardError: Double,
)

internal data class PointEstimates(
    val mean: Double,
    val median: Double,
    val medianAbsDev: Double,
    val stdDev: Double,
)

@Serializable
internal data class Estimates(
    val mean: Estimate,
    val median: Estimate,
    val medianAbsDev: Estimate,
    val slope: Estimate? = null,
    val stdDev: Estimate,
) {
    fun typical(): Estimate = slope ?: mean

    fun get(stat: Statistic): Estimate? =
        when (stat) {
            Statistic.Mean -> mean
            Statistic.Median -> median
            Statistic.MedianAbsDev -> medianAbsDev
            Statistic.Slope -> slope
            Statistic.StdDev -> stdDev
            Statistic.Typical -> typical()
        }
}

internal data class Distributions(
    val mean: Distribution<Double>,
    val median: Distribution<Double>,
    val medianAbsDev: Distribution<Double>,
    val slope: Distribution<Double>? = null,
    val stdDev: Distribution<Double>,
) {
    fun typical(): Distribution<Double> = slope ?: mean

    fun get(stat: Statistic): Distribution<Double>? =
        when (stat) {
            Statistic.Mean -> mean
            Statistic.Median -> median
            Statistic.MedianAbsDev -> medianAbsDev
            Statistic.Slope -> slope
            Statistic.StdDev -> stdDev
            Statistic.Typical -> typical()
        }
}

internal data class ChangePointEstimates(
    val mean: Double,
    val median: Double,
)

@Serializable
internal data class ChangeEstimates(
    val mean: Estimate,
    val median: Estimate,
) {
    fun get(stat: Statistic): Estimate =
        when (stat) {
            Statistic.Mean -> mean
            Statistic.Median -> median
            else -> error("Unexpected statistic for change estimates")
        }
}

internal data class ChangeDistributions(
    val mean: Distribution<Double>,
    val median: Distribution<Double>,
) {
    fun get(stat: Statistic): Distribution<Double> =
        when (stat) {
            Statistic.Mean -> mean
            Statistic.Median -> median
            else -> error("Unexpected statistic for change distributions")
        }
}

internal fun buildEstimates(
    distributions: Distributions,
    points: PointEstimates,
    cl: Double,
): Estimates {
    fun toEstimate(pointEstimate: Double, distribution: Distribution<Double>): Estimate {
        val (lb, ub) = distribution.confidenceInterval(cl)
        return Estimate(
            confidenceInterval =
                ConfidenceInterval(
                    confidenceLevel = cl,
                    lowerBound = lb,
                    upperBound = ub,
                ),
            pointEstimate = pointEstimate,
            standardError = distribution.stdDev(),
        )
    }

    return Estimates(
        mean = toEstimate(points.mean, distributions.mean),
        median = toEstimate(points.median, distributions.median),
        medianAbsDev = toEstimate(points.medianAbsDev, distributions.medianAbsDev),
        slope = distributions.slope?.let { toEstimate(points.mean, it) },
        stdDev = toEstimate(points.stdDev, distributions.stdDev),
    )
}

internal fun buildChangeEstimates(
    distributions: ChangeDistributions,
    points: ChangePointEstimates,
    cl: Double,
): ChangeEstimates {
    fun toEstimate(pointEstimate: Double, distribution: Distribution<Double>): Estimate {
        val (lb, ub) = distribution.confidenceInterval(cl)
        return Estimate(
            confidenceInterval =
                ConfidenceInterval(
                    confidenceLevel = cl,
                    lowerBound = lb,
                    upperBound = ub,
                ),
            pointEstimate = pointEstimate,
            standardError = distribution.stdDev(),
        )
    }

    return ChangeEstimates(
        mean = toEstimate(points.mean, distributions.mean),
        median = toEstimate(points.median, distributions.median),
    )
}
