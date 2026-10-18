// port-lint: source stats/rand_util.rs
package io.github.kotlinmania.criterion.stats

import kotlin.random.Random

/**
 * 64-bit pseudo-random number generator.
 */
internal class Rand64(
    seed: Long,
) {
    private var state: Long = if (seed == 0L) 0x123456789ABCDEFL else seed

    /**
     * Produces a random 64-bit unsigned integer.
     */
    fun randU64(): ULong {
        var x = state
        x = x xor (x shl 13)
        x = x xor (x ushr 7)
        x = x xor (x shl 17)
        state = x
        return x.toULong()
    }

    /**
     * Produces a random integer in the range [0, end).
     */
    fun randRange(end: ULong): ULong {
        if (end == 0uL) return 0uL
        return randU64() % end
    }

    /**
     * Produces a random integer in the range [start, end).
     */
    fun randRange(start: ULong, end: ULong): ULong {
        require(start < end) { "start must be less than end" }
        return start + randRange(end - start)
    }
}

internal typealias Rng = Rand64

private var globalSeed: Long = Random.nextLong()

/**
 * Creates a new random number generator.
 */
internal fun newRng(): Rng {
    globalSeed = globalSeed xor (globalSeed shl 13)
    globalSeed = globalSeed xor (globalSeed ushr 7)
    globalSeed = globalSeed xor (globalSeed shl 17)
    val seed = globalSeed xor Random.nextLong()
    return Rand64(seed)
}
