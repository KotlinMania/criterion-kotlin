// port-lint: source stats/univariate/resamples.rs
package io.github.kotlinmania.criterion.stats.univariate

import io.github.kotlinmania.criterion.stats.Rng
import io.github.kotlinmania.criterion.stats.newRng

/**
 * Resamples a sample with replacement.
 */
internal class Resamples(
    private val sample: Sample,
) {
    private val rng: Rng = newRng()
    private val stage: DoubleArray = DoubleArray(sample.size)

    /**
     * Produces the next resampled sample.
     */
    fun next(): Sample {
        val n = sample.size
        val uLen = n.toULong()
        for (i in 0 until n) {
            val idx = rng.randRange(uLen).toInt()
            stage[i] = sample[idx]
        }
        return Sample(stage.copyOf())
    }
}
