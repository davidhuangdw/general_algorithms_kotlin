package math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

object Bitwise {

    fun lowBit(x: Int) = x and -x
    fun removeLowBit(x: Int) = x and x - 1
    fun bitMask(k: Int) = if (k == 32) -1 else (1 shl k) - 1        // in kotlin (1 shl 32) is not 0

    fun swap(p: Pair<Int, Int>): Pair<Int, Int> {
        var (x, y) = p
        x = x xor y
        y = x xor y
        x = x xor y
        return x to y
    }

    fun reverseBits(x: Int, n: Int = 32): Int {
        var r = 0
        repeat(n) { k ->
            r = (r shl 1) or (x shr k and 1)
        }
        return r
    }

    fun reverseBits2(x: Int, n: Int = 32): Int {       //parallel, n bits, O(log(n))
        assert(removeLowBit(n) == 0)    // n should be 2^k
        var mask = (1 shl n) - 1
        var half_size = n / 2
        var r = x
        while (half_size > 0) {
            mask = mask xor (mask shl half_size)
            r = (r ushr half_size and mask) or (r shl half_size and mask.inv())
            half_size /= 2
        }
        return r
    }

    fun countBits(x: Int): Int {
        var (r, cnt) = x to 0
        while (r > 0) {
            cnt += 1
            r = removeLowBit(r)
        }
        return cnt
    }

    fun countBits2(x: Int): Int {     //parallel, O(log(32))
        var cur = x
        fun cnt(half_mask: Int, half_size: Int) {
            if (half_size > 1) cnt(half_mask xor (half_mask shl half_size / 2), half_size / 2)
            cur = (cur and half_mask.inv() shr half_size) + (cur and half_mask)
        }
        cnt((1 shl 16) - 1, 16)
        return cur
    }

    fun subSetsOfSubSet(sub_set: Int): Sequence<Int> {      // only choose '1' bits in sub_set
        return sequence {
            var b = 0
            do {
                yield(b)
                b = sub_set and (b - sub_set)       // rightmost 0 become 1, and its right-side become all zeros
            } while (b != 0)
        }
    }

    fun mostSignificantBit(x: Int): Int {
        var r = x
        while (removeLowBit(r) > 0) r = removeLowBit(r)
        return r
    }

    fun countTrailingZeros(x: Int): Int = (0 until 32).firstOrNull { k -> x ushr k and 1 == 1 } ?: 32
    fun countTrailingZeros2(x: Int): Int {
        var (l, r) = 0 to 31
        while (l <= r) {
            val m = (l + r) shr 1
            if (bitMask(m + 1) and x == 0)
                l = m + 1
            else
                r = m - 1
        }
        return l
    }

    fun combinations(k: Int, n: Int): Sequence<Int> {
        return sequence {
            var x = (1 shl k) - 1
            while (x < (1 shl n)) {
                yield(x)
                val t = x + lowBit(x)
                x = (x xor t) ushr (countTrailingZeros(x) + 2) or t
            }
        }
    }
}

class TestBitWise {
    val bw = Bitwise

    @Test
    fun testBitWise() {
        assertEquals(
            "0000100".toInt(2),
            bw.lowBit("0101100".toInt(2))
        )

        assertEquals(
            "0101000".toInt(2),
            bw.removeLowBit("0101100".toInt(2))
        )

        assertEquals(1 to -7, bw.swap(-7 to 1))

        assertEquals(
            "00011010".toInt(2),
            bw.reverseBits("01011000".toInt(2), 8)
        )

        assertEquals(
            "00011010".toInt(2),
            bw.reverseBits2("01011000".toInt(2), 8)
        )

        assertEquals(5, bw.countBits("100100011010".toInt(2)))
        assertEquals(5, bw.countBits2("100100011010".toInt(2)))

        assertEquals("100000000000".toInt(2), bw.mostSignificantBit("100100011010".toInt(2)))

        assertEquals(
            listOf(
                "0000000".toInt(2),
                "0000100".toInt(2),
                "0001000".toInt(2),
                "0001100".toInt(2),
                "0100000".toInt(2),
                "0100100".toInt(2),
                "0101000".toInt(2),
                "0101100".toInt(2),
            ),
            bw.subSetsOfSubSet("0101100".toInt(2)).toList()
        )

        assertEquals(3, bw.countTrailingZeros("100101001000".toInt(2)))
        assertEquals(32, bw.countTrailingZeros(0))
        assertEquals(3, bw.countTrailingZeros2("100101001000".toInt(2)))
        assertEquals(32, bw.countTrailingZeros2(0))

        assertEquals(
            listOf(
                "00111".toInt(2),
                "01011".toInt(2),
                "01101".toInt(2),
                "01110".toInt(2),
                "10011".toInt(2),
                "10101".toInt(2),
                "10110".toInt(2),
                "11001".toInt(2),
                "11010".toInt(2),
                "11100".toInt(2),
            ),
            bw.combinations(3, 5).toList()
        )
    }
}
