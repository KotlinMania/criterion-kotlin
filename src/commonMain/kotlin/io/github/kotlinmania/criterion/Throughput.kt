package io.github.kotlinmania.criterion

import kotlinx.serialization.Serializable

/**
 * The throughput of a benchmark.
 */
@Serializable
sealed class Throughput {
    @Serializable
    data class Bytes(
        val count: ULong,
    ) : Throughput()

    @Serializable
    data class BytesDecimal(
        val count: ULong,
    ) : Throughput()

    @Serializable
    data class Elements(
        val count: ULong,
    ) : Throughput()

    @Serializable
    data class Bits(
        val count: ULong,
    ) : Throughput()
}
