// port-lint: source benchmark_group.rs
package io.github.kotlinmania.criterion.benchmark

/**
 * Identifier for a benchmark, uniquely identifying it within a benchmark group.
 */
data class BenchmarkId(
    val functionName: String?,
    val parameter: String?,
) {
    fun asDirectoryName(): String =
        when {
            functionName != null && parameter != null -> "$functionName/$parameter"
            functionName != null -> functionName
            parameter != null -> parameter
            else -> "benchmark"
        }

    companion object {
        fun new(functionName: String, parameter: Any): BenchmarkId = BenchmarkId(functionName, parameter.toString())

        fun fromParameter(parameter: Any): BenchmarkId = BenchmarkId(null, parameter.toString())

        fun fromName(functionName: String): BenchmarkId = BenchmarkId(functionName, null)

        fun noFunction(): BenchmarkId = BenchmarkId(null, null)
    }
}
