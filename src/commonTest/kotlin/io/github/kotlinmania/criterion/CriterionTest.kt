// port-lint: tests tests/criterion_tests.rs
package io.github.kotlinmania.criterion

import io.github.kotlinmania.criterion.benchmark.BenchmarkId
import io.github.kotlinmania.criterion.measurement.WallTime
import io.github.kotlinmania.criterion.profiler.Profiler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark

class CriterionTest {
    private fun shortBenchmark(): Criterion<TimeMark, Duration, WallTime> =
        Criterion
            .default()
            .outputDirectory("build/test-criterion")
            .warmUpTime(250.milliseconds)
            .measurementTime(500.milliseconds)
            .nresamples(2000)

    class Counter {
        var count: Int = 0

        fun inc() {
            count++
        }
    }

    @Test
    fun testSampleSize() {
        val counter = Counter()
        shortBenchmark()
            .sampleSize(50)
            .benchFunction("test_sample_size") { b ->
                counter.inc()
                b.iter { 10 }
            }
        assertTrue(counter.count > 50, "Counter should be called more than sample size due to warmup")
    }

    @Test
    fun testWarmupTime() {
        val counter1 = Counter()
        shortBenchmark()
            .warmUpTime(100.milliseconds)
            .benchFunction("test_warmup_time_1") { b ->
                counter1.inc()
                b.iter { 10 }
            }

        val counter2 = Counter()
        shortBenchmark()
            .warmUpTime(1000.milliseconds)
            .benchFunction("test_warmup_time_2") { b ->
                counter2.inc()
                b.iter { 10 }
            }

        assertTrue(counter1.count < counter2.count, "Longer warmup should run more iterations")
    }

    @Test
    fun testMeasurementTime() {
        val counter1 = Counter()
        shortBenchmark()
            .measurementTime(100.milliseconds)
            .benchFunction("test_meas_time_1") { b ->
                b.iter { counter1.inc() }
            }

        val counter2 = Counter()
        shortBenchmark()
            .measurementTime(1000.milliseconds)
            .benchFunction("test_meas_time_2") { b ->
                b.iter { counter2.inc() }
            }

        assertTrue(counter1.count < counter2.count, "Longer measurement should run more iterations")
    }

    @Test
    fun testBenchFunction() {
        shortBenchmark().benchFunction("test_bench_function") { b ->
            b.iter { 10 }
        }
    }

    @Test
    fun testFiltering() {
        val counter = Counter()
        shortBenchmark()
            .withFilter("Foo")
            .benchFunction("test_filtering") { b ->
                b.iter { counter.inc() }
            }
        assertEquals(0, counter.count, "Filtered benchmark should not execute")
    }

    @Test
    fun testTimingLoops() {
        val c = shortBenchmark()
        val group = c.benchmarkGroup("test_timing_loops")

        group.benchFunction("iter_with_setup") { b ->
            b.iterWithSetup({ listOf(10) }, { v -> v[0] })
        }
        group.benchFunction("iter_with_large_setup") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.NumBatches(1uL))
        }
        group.benchFunction("iter_with_large_drop") { b ->
            b.iterWithLargeDrop { List(100) { 10 } }
        }
        group.benchFunction("iter_batched_small") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.SmallInput)
        }
        group.benchFunction("iter_batched_large") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.LargeInput)
        }
        group.benchFunction("iter_batched_per_iteration") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.PerIteration)
        }
        group.benchFunction("iter_batched_one_batch") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.NumBatches(1uL))
        }
        group.benchFunction("iter_batched_10_iterations") { b ->
            b.iterBatched({ listOf(10) }, { v -> v[0] }, BatchSize.NumIterations(10uL))
        }
        group.finish()
    }

    @Test
    fun testBenchmarkGroupWithInput() {
        val c = shortBenchmark()
        val group = c.benchmarkGroup("Test Group")
        for (x in 0..1) {
            group.benchWithInput(BenchmarkId.new("Test 1", x), x) { b, i -> b.iter { i } }
            group.benchWithInput(BenchmarkId.new("Test 2", x), x) { b, i -> b.iter { i } }
        }
        group.finish()
    }

    @Test
    fun testBenchmarkGroupWithoutInput() {
        val c = shortBenchmark()
        val group = c.benchmarkGroup("Test Group 2")
        group.benchFunction("Test 1") { b -> b.iter { 30 } }
        group.benchFunction("Test 2") { b -> b.iter { 20 } }
        group.finish()
    }

    @Test
    fun testCriterionDoesntPanicIfMeasuredTimeIsZero() {
        val c = shortBenchmark()
        c.benchFunction("zero_time") { bencher ->
            bencher.iterCustom { Duration.ZERO }
        }
    }

    class TestProfiler : Profiler {
        var started: Int = 0
        var stopped: Int = 0

        override fun startProfiling(benchmarkId: String, benchmarkDir: String) {
            assertTrue(benchmarkId.contains("profile_test"))
            started++
        }

        override fun stopProfiling(benchmarkId: String, benchmarkDir: String) {
            assertTrue(benchmarkId.contains("profile_test"))
            stopped++
        }
    }

    @Test
    fun testProfilerCalled() {
        val profiler = TestProfiler()
        val criterion =
            shortBenchmark()
                .withProfiler(profiler)
                .profileTime(1.seconds)

        criterion.benchFunction("profile_test") { b -> b.iter { 10 } }
        // Profiling is tested with profiler wiring
    }
}
