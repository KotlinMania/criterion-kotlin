// port-lint: source format.rs
package io.github.kotlinmania.criterion.format

import kotlin.math.abs
import kotlin.math.roundToLong

internal fun change(pct: Double, signed: Boolean): String {
    val formatted =
        if (signed) {
            signedShort(pct * 100.0)
        } else {
            short(pct * 100.0)
        }
    return formatted.padStart(6) + "%"
}

internal fun time(ns: Double): String =
    if (ns < 1.0) {
        "${short(ns * 1000.0).padStart(6)} ps"
    } else if (ns < 1000.0) {
        "${short(ns).padStart(6)} ns"
    } else if (ns < 1000000.0) {
        "${short(ns / 1000.0).padStart(6)} µs"
    } else if (ns < 1000000000.0) {
        "${short(ns / 1000000.0).padStart(6)} ms"
    } else {
        "${short(ns / 1000000000.0).padStart(6)} s"
    }

private fun formatDecimals(n: Double, decimals: Int): String {
    if (decimals == 0) {
        return n.roundToLong().toString()
    }
    var mult = 1.0
    repeat(decimals) { mult *= 10.0 }
    val rounded = (n * mult).roundToLong()
    val integerPart = rounded / mult.toLong()
    val fractionPart = abs(rounded % mult.toLong())
    val fracStr = fractionPart.toString().padStart(decimals, '0')
    return "$integerPart.$fracStr"
}

internal fun short(n: Double): String =
    if (n < 10.0) {
        formatDecimals(n, 4)
    } else if (n < 100.0) {
        formatDecimals(n, 3)
    } else if (n < 1000.0) {
        formatDecimals(n, 2)
    } else if (n < 10000.0) {
        formatDecimals(n, 1)
    } else {
        formatDecimals(n, 0)
    }

internal fun signedShort(n: Double): String {
    val nAbs = abs(n)
    val sign = if (n >= 0.0) "+" else "\u2212"
    val formatted =
        if (nAbs < 10.0) {
            formatDecimals(nAbs, 4)
        } else if (nAbs < 100.0) {
            formatDecimals(nAbs, 3)
        } else if (nAbs < 1000.0) {
            formatDecimals(nAbs, 2)
        } else if (nAbs < 10000.0) {
            formatDecimals(nAbs, 1)
        } else {
            formatDecimals(nAbs, 0)
        }
    return "$sign$formatted"
}

internal fun iterCount(iterations: ULong): String {
    val iters = iterations.toDouble()
    return if (iterations < 10000uL) {
        "$iterations iterations"
    } else if (iterations < 1000000uL) {
        "${formatDecimals(iters / 1000.0, 0)}k iterations"
    } else if (iterations < 10000000uL) {
        "${formatDecimals(iters / 1000000.0, 1)}M iterations"
    } else if (iterations < 1000000000uL) {
        "${formatDecimals(iters / 1000000.0, 0)}M iterations"
    } else if (iterations < 10000000000uL) {
        "${formatDecimals(iters / 1000000000.0, 1)}B iterations"
    } else {
        "${formatDecimals(iters / 1000000000.0, 0)}B iterations"
    }
}

internal fun thousandsSep(n: ULong, sep: Char): String {
    val s = n.toString()
    val sb = StringBuilder()
    val remainder = s.length % 3
    val initial = if (remainder == 0 && s.isNotEmpty()) 3 else remainder
    sb.append(s.substring(0, initial))
    var i = initial
    while (i < s.length) {
        sb.append(sep)
        sb.append(s.substring(i, i + 3))
        i += 3
    }
    return sb.toString()
}

internal fun integer(n: Double): String = thousandsSep(n.toULong(), ',')
