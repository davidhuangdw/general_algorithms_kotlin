package math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BigInt {
    companion object {
        fun compare(a: List<Int>, b: List<Int>): Int {
            val (n, m) = a.size to b.size
            if (n != m) return n - m
            for (i in n - 1 downTo 0)
                if (a[i] != b[i])
                    return a[i] - b[i]
            return 0
        }

        fun display(x: List<Int>) = x.reversed().joinToString("")
        fun fromString(x: String) = (x.length - 1 downTo 0).map { x[it] - '0' }

        fun compact(x: List<Int>): List<Int> {
            var i = x.size - 1
            while (i >= 0 && x[i] == 0)
                i -= 1
            return x.subList(0, maxOf(1, i + 1))
        }

        fun times(a: List<Int>, b: List<Int>): List<Int> {
            val (n, m) = a.size to b.size
            val c = MutableList(n + m) { 0 }
            for (k in m - 1 downTo 0) {
                val x = b[k]
                var add = 0
                for ((i, y) in a.withIndex()) {
                    val z = x * y + add + c[i + k]
                    c[i + k] = z % 10
                    add = z / 10
                }
                var i = k + n
                while (add > 0) {
                    val z = c[i] + add
                    c[i] = z % 10
                    add = z / 10
                    i += 1
                }
            }
            return compact(c)
        }

        fun div(a: List<Int>, b: List<Int>): List<Int> {
            val (n, m) = a.size to b.size
            val r = a.toMutableList()
            val c = MutableList(n) { 0 }
            var rl = n
            fun update_rl() {
                while (rl > 0 && r[rl - 1] == 0)
                    rl -= 1
            }
            update_rl()
            fun ge(ir: Int): Boolean {
                if (rl - ir != m)
                    return rl - ir > m
                for (i in m - 1 downTo 0)
                    if (r[ir + i] != b[i])
                        return r[ir + i] > b[i]
                return true
            }

            for (k in n - m downTo 0) {
                while (ge(k)) {
                    var add = 0
                    for (i in 0 until m) {
                        val z = r[k + i] - b[i] + add
                        if (z >= 0) {
                            r[k + i] = z
                            add = 0
                        } else {
                            r[k + i] = 10 + z
                            add = -1
                        }
                    }
                    for (i in k + m until rl) {
                        if (add == 0) break
                        r[i] += add
                        if (r[i] >= 0)
                            add = 0
                        else
                            r[i] += 10
                    }
                    c[k] += 1
                    update_rl()
                }
            }
//            println("mod: ${display(compact(r))}")
            return compact(c)
        }
    }
}

class TestBigInt {
    @Test
    fun testTimes() {
        val a = BigInt.fromString("12345")
        val b = BigInt.fromString("6789")
        val c = BigInt.times(a, b)
        assertEquals("83810205", BigInt.display(c))
    }

    @Test
    fun testDiv() {
        val a = BigInt.fromString("12345")
        val b = BigInt.fromString("67")
        val c = BigInt.div(a, b)
        assertEquals("184", BigInt.display(c))
    }
}