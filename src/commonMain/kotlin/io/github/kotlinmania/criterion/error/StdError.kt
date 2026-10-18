// port-lint: source error.rs
package io.github.kotlinmania.criterion.error

/**
 * Errors that can occur during benchmarking and report generation.
 */
sealed class CriterionError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class AccessError(
        val path: String,
        cause: Throwable? = null,
    ) : CriterionError("Failed to access file $path: ${cause?.message}", cause)

    class CopyError(
        val from: String,
        val to: String,
        cause: Throwable? = null,
    ) : CriterionError("Failed to copy file $from to $to: ${cause?.message}", cause)

    class SerdeError(
        val path: String,
        cause: Throwable? = null,
    ) : CriterionError("Failed to read or write file $path due to serialization error: ${cause?.message}", cause)

    class CsvError(
        cause: Throwable? = null,
    ) : CriterionError("CSV error: ${cause?.message}", cause)
}

typealias CriterionResult<T> = Result<T>

fun logError(e: CriterionError) {
    println("error: $e")
}
