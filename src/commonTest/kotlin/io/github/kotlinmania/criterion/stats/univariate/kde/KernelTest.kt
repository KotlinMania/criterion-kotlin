// port-lint: tests stats/univariate/kde/kernel.rs
package io.github.kotlinmania.criterion.stats.univariate.kde

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class KernelTest {
    @Test
    fun symmetric() {
        val testValues = doubleArrayOf(0.0, 0.5, 1.0, 2.5, 5.0, 10.0)
        for (x in testValues) {
            val left = Gaussian.evaluate(-x)
            val right = Gaussian.evaluate(x)
            assertTrue(abs(left - right) < 1e-12, "Kernel should be symmetric at $x")
        }
    }

    @Test
    fun integral() {
        val dx = 1e-3
        var acc = 0.0
        var x = -5.0
        val b = 5.0
        var y = Gaussian.evaluate(x)

        while (x < b) {
            acc += dx * y / 2.0
            x += dx
            y = Gaussian.evaluate(x)
            acc += dx * y / 2.0
        }

        assertTrue(acc > 0.0, "Integral should be positive")
        assertTrue(abs(acc - 1.0) < 1e-3, "Integral should be approximately 1.0, got $acc")
    }
}
