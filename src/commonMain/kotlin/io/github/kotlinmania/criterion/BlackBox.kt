package io.github.kotlinmania.criterion

import kotlin.concurrent.Volatile

@Volatile
private var blackBoxSink: Any? = null

/**
 * An opaque function used to prevent compiler optimizations from removing computations.
 */
fun <T> blackBox(dummy: T): T {
    blackBoxSink = dummy
    return dummy
}
