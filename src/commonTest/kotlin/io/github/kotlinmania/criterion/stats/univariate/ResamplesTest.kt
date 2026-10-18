// port-lint: tests stats/univariate/resamples.rs
package io.github.kotlinmania.criterion.stats.univariate

import kotlin.test.Test
import kotlin.test.assertTrue

class ResamplesTest {
    @Test
    fun differentSubsets() {
        val size = 1000
        val v = DoubleArray(size) { i -> i.toDouble() }
        val sample = Sample(v)
        val resamples = Resamples(sample)

        var numDuplicated = 0
        repeat(1000) {
            val s1 = resamples.next().data
            val s2 = resamples.next().data
            if (s1.contentEquals(s2)) {
                numDuplicated++
            }
        }

        assertTrue(numDuplicated <= 1, "Found $numDuplicated duplicate samples")
    }
}
