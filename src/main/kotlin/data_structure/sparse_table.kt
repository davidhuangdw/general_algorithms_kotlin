package data_structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun countBits(x: Int): Int {
    var b = 0
    var r = x
    while (r > 0) {
        r /= 2
        b += 1
    }
    return b
}

class RangeMinimalQuery(val values: List<Int>) {
    val N = values.size
    val B = countBits(N) - 1
    val log = Array(N + 1) { 0 }
    val sparseTable = Array(N) { i -> Array(B + 1) { i } }
    val cmp: Comparator<Int> = compareBy({ values[it] }, { it })

    init {
        for (k in 1..B) {       // O(n*log)
            for (i in (1 shl k) until minOf(N + 1, 1 shl k + 1))
                log[i] = k
            for (i in 0 until N) {
                sparseTable[i][k] = if (i + (1 shl k - 1) < N)
                    minOf(sparseTable[i][k - 1], sparseTable[i + (1 shl k - 1)][k - 1], cmp)
                else sparseTable[i][k - 1]
            }
        }
    }

    fun query(x: Int, y: Int): Pair<Int, Int> {    // O(1)
        val (i, j) = minOf(x, y) to maxOf(x, y)
        val k = log[j + 1 - i]
        val idx = minOf(sparseTable[i][k], sparseTable[j + 1 - (1 shl k)][k], cmp)
        return values[idx] to idx
    }
}

class TestRMQ {
    @Test
    fun testRMQ() {
        val values = listOf(0, 1, 2, -3, 4, 5, -6, -7, 8, 9)
        val rmq = RangeMinimalQuery(values)
        assertEquals(-7 to 7, rmq.query(0, 9))
        assertEquals(0 to 0, rmq.query(0, 2))
        assertEquals(4 to 4, rmq.query(4, 5))
        assertEquals(-3 to 3, rmq.query(1, 5))
        assertEquals(-6 to 6, rmq.query(1, 6))
        assertEquals(-6 to 6, rmq.query(4, 6))
    }
}