package data_structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt

class SkipList(val Prob: Double = 0.5) {
    companion object {
        const val MAX_LEVEL = 20
    }

    data class Node(val v: Int, val level: Int) {
        val next = Array<Node?>(level) { null }
        val jump = Array(level) { 0 }
    }

    private val head = Node(0, MAX_LEVEL)
    var size = 0

    private fun randomLevel(): Int {
        var l = 1
        while (l < MAX_LEVEL && nextDouble() <= Prob) l += 1
        return l
    }

    fun add(v: Int): Int {
        val prevs = Array(MAX_LEVEL) { head to -1 }
        var pos = -1
        var cur = head
        for (l in MAX_LEVEL - 1 downTo 0) {
            while (cur.next[l] != null && cur.next[l]!!.v < v) {
                pos += cur.jump[l]
                cur = cur.next[l]!!
            }
            prevs[l] = cur to pos
        }

        val level = randomLevel()

        val node = Node(v, level)
        for (i in MAX_LEVEL - 1 downTo 0) {
            val (prev, prePos) = prevs[i]
            if (i >= level) {
                prev.jump[i] += 1
            } else {
                node.next[i] = prev.next[i]
                prev.next[i] = node
                node.jump[i] = prev.jump[i] - (pos - prePos)
                prev.jump[i] = pos + 1 - prePos
            }
        }

        size += 1
        return pos + 1
    }

    fun remove(v: Int): Int {
        val prevs = Array(MAX_LEVEL) { head }
        var pos = -1
        var cur = head
        for (i in MAX_LEVEL - 1 downTo 0) {
            while (cur.next[i] != null && cur.next[i]!!.v < v) {
                pos += cur.jump[i]
                cur = cur.next[i]!!
            }
            prevs[i] = cur
        }

        if (cur.next[0] != null)
            cur = cur.next[0]!!
        if (cur.v != v) return -1

        for (i in MAX_LEVEL - 1 downTo 0) {
            if (i >= cur.level) {
                prevs[i].jump[i] -= 1
            } else {
                prevs[i].next[i] = cur.next[i]
                prevs[i].jump[i] += cur.jump[i] - 1
            }
        }
        size -= 1
        return pos
    }

    fun floorRank(v: Int): Int {      // floor pos
        var pos = -1
        var prev = head
        for (l in MAX_LEVEL - 1 downTo 0) {
            while (prev.next[l] != null && prev.next[l]!!.v < v) {
                pos += prev.jump[l]
                prev = prev.next[l]!!
            }
        }
        return pos + 1
    }

    fun kth(k: Int): Node? {      // 0-based
        assert(k >= 0)
        var rem = k + 1
        var cur = head
        for (l in MAX_LEVEL - 1 downTo 0) {
            while (cur.next[l] != null && cur.jump[l] <= rem) {
                rem -= cur.jump[l]
                cur = cur.next[l]!!
            }
            if (rem == 0) break
        }
        return if (rem == 0) cur else null
    }

    fun rankRange(from: Int, to: Int): List<Int> {  // [from, to) , 0-based
        assert(from < to)
        var node = kth(from)
        val values = mutableListOf<Int>()
        repeat(to - from) {
            if (node == null)
                return@repeat
            values.add(node!!.v)
            node = node!!.next[0]
        }
        return values
    }

    fun debug() {
        for (i in MAX_LEVEL - 1 downTo 0) {
            if (head.next[i] == null) continue
            var pos = -1
            var cur: Node? = head
            while (cur != null) {
                print("-> ${cur.v} ($pos, ${cur.jump[i]})".padStart(16, ' '))
                repeat(cur.jump[i] - 1) { print(" ".repeat(16)) }
                pos += cur.jump[i]
                cur = cur.next[i]
            }
            println()
        }
        println("-".repeat(80))
    }
}

class TestSkipList {
    @Test
    fun testSkipList1() {
        var arr = (1..20).map { nextInt(1, 30) }
//        arr = listOf(4, 3, 2, 1, 2, 5, 6, 3, 4)
        var sorted = arr.sorted()
        println(arr)
        println(sorted)

        val sk = SkipList()
        for (v in arr) {
            sk.add(v)
            sk.debug()
        }
        println((0..8).map { sk.kth(it)!!.v })
        println((0..8).map { sk.floorRank(it) })

        assertEquals(sorted.size, sk.size)
        assertEquals(null, sk.kth(1000))
        assertEquals(sorted, (arr.indices).map { sk.kth(it)!!.v })
        for (v in 0 until 30) {
            var i = 0
            while (i < arr.size && sorted[i] < v) i += 1
            assertEquals(i, sk.floorRank(v))
        }
        assertEquals(sorted.subList(2, 8), sk.rankRange(2, 8))

        assertEquals(-1, sk.remove(-1000))
        assertEquals(-1, sk.remove(1000))
        for (i in listOf(1, 1, 3, 4)) {
            sk.remove(sorted[i])
            sorted = sorted.subList(0, i) + sorted.subList(i + 1, sorted.size)
        }
        println(sorted)
        assertEquals(sorted.size, sk.size)
        assertEquals(null, sk.kth(1000))
        assertEquals(sorted, (sorted.indices).map { sk.kth(it)!!.v })
    }
}