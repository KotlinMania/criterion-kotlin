// port-lint: source stats/univariate/outliers/tukey.rs
package io.github.kotlinmania.criterion.stats.univariate.outliers

import io.github.kotlinmania.criterion.stats.univariate.Sample

/**
 * Labels used to classify outliers.
 */
internal enum class Label {
    HighMild,
    HighSevere,
    LowMild,
    LowSevere,
    NotAnOutlier,
    ;

    fun isHigh(): Boolean = this == HighMild || this == HighSevere

    fun isMild(): Boolean = this == HighMild || this == LowMild

    fun isLow(): Boolean = this == LowMild || this == LowSevere

    fun isOutlier(): Boolean = this != NotAnOutlier

    fun isSevere(): Boolean = this == HighSevere || this == LowSevere
}

/**
 * Four fences used to classify outliers: (lost, lomt, himt, hist).
 */
internal data class Fences(
    val lowSevere: Double,
    val lowMild: Double,
    val highMild: Double,
    val highSevere: Double,
)

internal data class OutlierCounts(
    val lowSevere: Int,
    val lowMild: Int,
    val notAnOutlier: Int,
    val highMild: Int,
    val highSevere: Int,
)

/**
 * A classified and labeled sample.
 */
internal class LabeledSample(
    val fences: Fences,
    val sample: Sample,
) {
    val size: Int get() = sample.size

    operator fun get(i: Int): Label {
        val x = sample[i]
        return if (x < fences.lowSevere) {
            Label.LowSevere
        } else if (x > fences.highSevere) {
            Label.HighSevere
        } else if (x < fences.lowMild) {
            Label.LowMild
        } else if (x > fences.highMild) {
            Label.HighMild
        } else {
            Label.NotAnOutlier
        }
    }

    fun count(): OutlierCounts {
        var los = 0
        var lom = 0
        var noa = 0
        var him = 0
        var his = 0
        for (i in 0 until sample.size) {
            when (get(i)) {
                Label.LowSevere -> los++
                Label.LowMild -> lom++
                Label.NotAnOutlier -> noa++
                Label.HighMild -> him++
                Label.HighSevere -> his++
            }
        }
        return OutlierCounts(los, lom, noa, him, his)
    }
}

/**
 * Classifies the sample and returns a labeled sample.
 */
internal fun classify(sample: Sample): LabeledSample {
    val (q1, _, q3) = sample.percentiles().quartiles()
    val iqr = q3 - q1
    val km = 1.5
    val ks = 3.0
    val fences =
        Fences(
            lowSevere = q1 - ks * iqr,
            lowMild = q1 - km * iqr,
            highMild = q3 + km * iqr,
            highSevere = q3 + ks * iqr,
        )
    return LabeledSample(fences, sample)
}
