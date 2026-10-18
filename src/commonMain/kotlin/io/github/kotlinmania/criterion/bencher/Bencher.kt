// port-lint: source bencher.rs
package io.github.kotlinmania.criterion.bencher

import io.github.kotlinmania.criterion.BatchSize
import io.github.kotlinmania.criterion.blackBox
import io.github.kotlinmania.criterion.measurement.Measurement
import io.github.kotlinmania.criterion.measurement.WallTime
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Timer struct used to iterate a benchmarked function and measure runtime.
 */
class Bencher<Intermediate, Value>(
    val measurement: Measurement<Intermediate, Value>,
) {
    var iterated: Boolean = false
        internal set
    var iters: ULong = 1uL
    var value: Value = measurement.zero()
        internal set
    var elapsedTime: Duration = Duration.ZERO
        internal set

    fun assertIterated() {
        check(iterated) { "Benchmark function must call Bencher::iter or related method." }
    }

    /**
     * Times a routine by executing it many times and timing total elapsed time.
     */
    fun <O> iter(routine: () -> O) {
        iterated = true
        val timeStart = TimeSource.Monotonic.markNow()
        val start = measurement.start()
        var count = iters.toLong()
        while (count > 0L) {
            blackBox(routine())
            count--
        }
        value = measurement.end(start)
        elapsedTime = timeStart.elapsedNow()
    }

    /**
     * Times a routine by relying on routine to measure its own execution time.
     */
    fun iterCustom(routine: (ULong) -> Value) {
        iterated = true
        val timeStart = TimeSource.Monotonic.markNow()
        value = routine(iters)
        elapsedTime = timeStart.elapsedNow()
    }

    /**
     * Times a routine requiring per-batch setup.
     */
    fun <I, O> iterBatched(setup: () -> I, routine: (I) -> O, size: BatchSize) {
        iterated = true
        val batchSize = size.itersPerBatch(iters)
        require(batchSize != 0uL) { "Batch size must not be zero." }
        val timeStart = TimeSource.Monotonic.markNow()
        value = measurement.zero()

        if (batchSize == 1uL) {
            var count = iters.toLong()
            while (count > 0L) {
                val input = blackBox(setup())
                val start = measurement.start()
                val output = routine(input)
                val end = measurement.end(start)
                value = measurement.add(value, end)
                blackBox(output)
                count--
            }
        } else {
            var iterationCounter = 0uL
            while (iterationCounter < iters) {
                val currentBatchSize = minOf(batchSize, iters - iterationCounter)
                val batchCount = currentBatchSize.toInt()
                val inputs = ArrayList<I>(batchCount)
                repeat(batchCount) {
                    inputs.add(setup())
                }
                val boxedInputs = blackBox(inputs)
                val outputs = ArrayList<O>(batchCount)
                val start = measurement.start()
                for (input in boxedInputs) {
                    outputs.add(routine(input))
                }
                val end = measurement.end(start)
                value = measurement.add(value, end)
                blackBox(outputs)
                iterationCounter += currentBatchSize
            }
        }

        elapsedTime = timeStart.elapsedNow()
    }

    fun <I, O> iterBatchedRef(setup: () -> I, routine: (I) -> O, size: BatchSize) {
        iterBatched(setup, routine, size)
    }

    fun <I, O> iterWithSetup(setup: () -> I, routine: (I) -> O) {
        iterBatched(setup, routine, BatchSize.PerIteration)
    }

    fun <O> iterWithLargeDrop(routine: () -> O) {
        iterBatched({ }, { routine() }, BatchSize.SmallInput)
    }

    companion object {
        fun default(): Bencher<TimeMark, Duration> = Bencher(WallTime())
    }
}
