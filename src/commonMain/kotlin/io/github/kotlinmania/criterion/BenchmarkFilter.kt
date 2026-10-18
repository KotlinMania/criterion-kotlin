package io.github.kotlinmania.criterion

/**
 * Filter determining which benchmarks to execute.
 */
sealed class BenchmarkFilter {
    object AcceptAll : BenchmarkFilter()

    data class Regex(
        val pattern: kotlin.text.Regex,
    ) : BenchmarkFilter()

    data class Exact(
        val name: String,
    ) : BenchmarkFilter()

    object RejectAll : BenchmarkFilter()

    fun matches(id: String): Boolean =
        when (this) {
            AcceptAll -> true
            is Regex -> pattern.containsMatchIn(id)
            is Exact -> name == id
            RejectAll -> false
        }
}
