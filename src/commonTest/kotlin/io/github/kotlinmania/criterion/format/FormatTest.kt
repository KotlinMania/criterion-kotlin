// port-lint: tests format.rs
package io.github.kotlinmania.criterion.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormatTest {
    @Test
    fun shortMaxLen() {
        var floatVal = 1.0
        while (floatVal < 999999.9) {
            val string = short(floatVal)
            assertTrue(string.length <= 6, "Expected length <= 6, got ${string.length} for $string")
            floatVal *= 2.0
        }
    }

    @Test
    fun signedShortMaxLen() {
        var floatVal = -1.0
        while (floatVal > -999999.9) {
            val string = signedShort(floatVal)
            assertTrue(string.length <= 7, "Expected length <= 7, got ${string.length} for $string")
            floatVal *= 2.0
        }
    }

    @Test
    fun integerThousandsSep() {
        val n = 140352319.0
        assertEquals("140,352,319", integer(n))
    }
}
