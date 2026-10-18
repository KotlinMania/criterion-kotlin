package io.github.kotlinmania.criterion.stats.univariate.kde

import io.github.kotlinmania.criterion.stats.univariate.Sample
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class KdeTest {
    @Test
    fun integral() {
        val data = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
        val sample = Sample(data)
        val kde = Kde(sample, Gaussian, Bandwidth.Silverman)
        val h = kde.bandwidth
        val a = sample.min() - 5.0 * h
        val b = sample.max() + 5.0 * h

        val dx = 1e-3
        var acc = 0.0
        var x = a
        var y = kde.estimate(a)

        while (x < b) {
            acc += dx * y / 2.0
            x += dx
            y = kde.estimate(x)
            acc += dx * y / 2.0
        }

        assertTrue(abs(acc - 1.0) < 2e-3, "KDE integral should be approximately 1.0, got $acc")
    }
}
