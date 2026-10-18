package io.github.kotlinmania.criterion

import kotlin.time.Duration

/**
 * Execution mode of Criterion.
 */
sealed class Mode {
    object Benchmark : Mode()

    data class List(
        val format: ListFormat,
    ) : Mode()

    object Test : Mode()

    data class Profile(
        val duration: Duration,
    ) : Mode()

    fun isBenchmark(): Boolean = this is Benchmark

    fun isTerse(): Boolean = this is List && this.format == ListFormat.Terse
}
