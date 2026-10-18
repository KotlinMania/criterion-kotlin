// port-lint: source stats/univariate/kde/kernel.rs
package io.github.kotlinmania.criterion.stats.univariate.kde

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Kernel function for density estimation.
 */
internal interface Kernel {
    /**
     * Apply the kernel function to the given x-value.
     */
    fun evaluate(x: Double): Double
}

/**
 * Gaussian kernel.
 */
internal object Gaussian : Kernel {
    override fun evaluate(x: Double): Double = 1.0 / sqrt(exp(x * x) * (2.0 * PI))
}
