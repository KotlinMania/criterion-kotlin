package io.github.kotlinmania.criterion

/**
 * Controls the batch size for iterBatched and iterBatchedRef in Bencher.
 */
sealed class BatchSize {
    object SmallInput : BatchSize()

    object LargeInput : BatchSize()

    object PerIteration : BatchSize()

    data class NumBatches(
        val batches: ULong,
    ) : BatchSize()

    data class NumIterations(
        val size: ULong,
    ) : BatchSize()

    fun itersPerBatch(iters: ULong): ULong =
        when (this) {
            SmallInput -> (iters + 10uL - 1uL) / 10uL
            LargeInput -> (iters + 1000uL - 1uL) / 1000uL
            PerIteration -> 1uL
            is NumBatches -> (iters + batches - 1uL) / batches
            is NumIterations -> size
        }
}
