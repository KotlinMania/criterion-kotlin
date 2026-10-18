// port-lint: source profiler.rs
package io.github.kotlinmania.criterion.profiler

/**
 * Extension interface for external profilers to provide start/stop hooks.
 */
interface Profiler {
    /**
     * Called when Criterion starts profiling a particular benchmark.
     */
    fun startProfiling(benchmarkId: String, benchmarkDir: String)

    /**
     * Called after Criterion stops profiling a particular benchmark.
     */
    fun stopProfiling(benchmarkId: String, benchmarkDir: String)
}

/**
 * Default profiler implementation that does nothing.
 */
object ExternalProfiler : Profiler {
    override fun startProfiling(benchmarkId: String, benchmarkDir: String) {}

    override fun stopProfiling(benchmarkId: String, benchmarkDir: String) {}
}
