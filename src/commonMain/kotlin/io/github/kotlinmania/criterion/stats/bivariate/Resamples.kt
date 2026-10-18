// port-lint: source stats/bivariate/resamples.rs
package io.github.kotlinmania.criterion.stats.bivariate

import io.github.kotlinmania.criterion.stats.Rng
import io.github.kotlinmania.criterion.stats.newRng

/**
 * Resamples bivariate data with replacement.
 */
internal class Resamples(
    private val data: Data,
) {
    private val rng: Rng = newRng()
    private val stageX: DoubleArray = DoubleArray(data.size)
    private val stageY: DoubleArray = DoubleArray(data.size)

    /**
     * Produces the next resampled bivariate dataset.
     */
    fun next(): Data {
        val n = data.size
        val uLen = n.toULong()
        for (i in 0 until n) {
            val idx = rng.randRange(uLen).toInt()
            stageX[i] = data.xs[idx]
            stageY[i] = data.ys[idx]
        }
        return Data(stageX.copyOf(), stageY.copyOf())
    }
}
