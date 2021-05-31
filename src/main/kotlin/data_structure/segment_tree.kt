package data_structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


fun IntRange.split(): Pair<IntRange, IntRange> {
    val md = (first + last) / 2
    return (first..md) to (md + 1..last)
}

fun IntRange.isExclusive(other: IntRange) = last < other.first || other.last < first
fun IntRange.contain(other: IntRange) = first <= other.first && other.last <= last
fun IntRange.length() = last - first + 1


abstract class SegmentTree<Node>(val N: Int, val fullRange: IntRange, private val defaultNode: Node) {
    protected abstract fun merge(x: Node, y: Node): Node
    protected abstract fun updateNode(x: Int, v: Int, xr: IntRange)
    protected open fun push(x: Int, xr: IntRange) {}

    val tree = MutableList(N * 4) { defaultNode }

    open fun build(a: List<Int>, x: Int = 1, xr: IntRange = fullRange, makeNode: (Int) -> Node) {
        if (xr.first == xr.last) {
            tree[x] = makeNode(a[xr.first])
            return
        }
        val lch = x * 2
        val rch = x * 2 + 1
        val (lch_r, rch_r) = xr.split()
        build(a, lch, lch_r, makeNode)
        build(a, rch, rch_r, makeNode)
        tree[x] = merge(tree[lch], tree[rch])
    }

    fun query(qr: IntRange, x: Int = 1, xr: IntRange = fullRange): Node { // tree[x] -- [lx, rx)
        if (qr.isExclusive(xr)) return defaultNode
        if (qr.contain(xr)) return tree[x]

        val lch = x * 2
        val rch = x * 2 + 1
        val (lch_r, rch_r) = xr.split()
        push(x, xr)
        return merge(query(qr, lch, lch_r), query(qr, rch, rch_r))
    }

    fun update(qr: IntRange, v: Int, x: Int = 1, xr: IntRange = fullRange) {
        if (qr.isExclusive(xr)) return
        if (qr.contain(xr)) {
            updateNode(x, v, xr)
            return
        }
        val lch = x * 2
        val rch = x * 2 + 1
        val (lch_r, rch_r) = xr.split()

        push(x, xr)

        update(qr, v, lch, lch_r)
        update(qr, v, rch, rch_r)
        tree[x] = merge(tree[lch], tree[rch])
    }

    fun update(pos: Int, v: Int) = update(pos..pos, v)
}

data class CounterNode(val cnt: Int, val lazySetAll: Int = -1)

val COUNTER_ZERO_NODE = CounterNode(0)

class CounterSegment(fullRange: IntRange) :
    SegmentTree<CounterNode>(N = fullRange.length(), fullRange, COUNTER_ZERO_NODE) {
    override fun merge(x: CounterNode, y: CounterNode): CounterNode {
        return CounterNode(x.cnt + y.cnt)
    }

    override fun updateNode(x: Int, v: Int, xr: IntRange) {
        assert(v in 0..1)
        tree[x] = CounterNode(v * xr.length(), v)
    }

    override fun push(x: Int, xr: IntRange) {
        if (tree[x].lazySetAll == -1 || xr.first >= xr.last) return
        val lazySetAll = tree[x].lazySetAll
        tree[x] = CounterNode(tree[x].cnt, -1)

        val lch = x * 2
        val rch = x * 2 + 1
        val (lch_r, rch_r) = xr.split()
        tree[lch] = CounterNode(lazySetAll * lch_r.length(), lazySetAll)
        tree[rch] = CounterNode(lazySetAll * rch_r.length(), lazySetAll)
    }

    fun kth(k: Int, x: Int = 1, xr: IntRange = fullRange): Int {
        if (k !in 1..tree[x].cnt) return -1
        if (xr.length() == 1) return xr.first

        push(x, xr)
        val lch = x * 2
        val rch = x * 2 + 1
        val (lch_r, rch_r) = xr.split()
        return if (k <= tree[lch].cnt) kth(k, lch, lch_r) else kth(k - tree[lch].cnt, rch, rch_r)
    }

    fun count() = tree[1].cnt
}

fun inversionToPermutation(leftGreaterCount: List<Int>): List<Int> {
    val n = leftGreaterCount.size
    val seg = CounterSegment(1..n)
    for (v in 1..n) seg.update(v, 1)

    val perm = ArrayDeque<Int>(n)
    for ((i, leftGtCnt) in leftGreaterCount.withIndex().reversed()) {
        val v = seg.kth(i - leftGtCnt + 1)
        perm.addFirst(v)
        seg.update(v, 0)
    }
    return perm.toList()
}

class TestSegmentTree {
    @Test
    fun testCounterKth() {
        val seg = CounterSegment(0..100)
        seg.update(1..8, 1)
        seg.update(15, 1)
        seg.update(31..38, 1)

        assertEquals(17, seg.count())
        assertEquals(-1, seg.kth(18))

        for ((i, v) in ((1..8).toList() + (15..15).toList() + (31..38).toList()).withIndex()) {
            assertEquals(v, seg.kth(i + 1))
        }

        seg.update(8..31, 0)
        for ((i, v) in ((1..7).toList() + (32..38).toList()).withIndex()) {
            assertEquals(v, seg.kth(i + 1))
        }
    }

    @Test
    fun testInversionToPermutation() {
        val perm = listOf(4, 1, 2, 5, 6, 3)
        val n = perm.size
        val leftGreaterCounts = (0 until n).map { i ->
            (0 until i).sumOf { j -> if (perm[j] > perm[i]) 1 else 0 as Int }
        }

        println(perm)
        println(leftGreaterCounts)
        assertEquals(perm, inversionToPermutation(leftGreaterCounts))
    }

    @Test
    fun testMinCount() {
        data class MinCountNode(val min: Int, val cnt: Int)
        class MinCountSegment(N: Int, fullRange: IntRange) :
            SegmentTree<MinCountNode>(N, fullRange, MinCountNode(Int.MAX_VALUE, 0)) {
            override fun merge(x: MinCountNode, y: MinCountNode): MinCountNode =
                if (x.min < y.min) x else if (x.min > y.min) y else MinCountNode(x.min, x.cnt + y.cnt)

            override fun updateNode(x: Int, v: Int, xr: IntRange) {
                tree[x] = MinCountNode(v, 1)
            }
        }

        val arr = listOf(3, 4, 3, 5, 2)
        val n = arr.size
        val seg = MinCountSegment(n, 0 until n)
        seg.build(arr) { v -> MinCountNode(v, 1) }

        assertEquals(MinCountNode(3, 2), seg.query(0..3))
        seg.update(1, 2)
        assertEquals(MinCountNode(2, 1), seg.query(0..3))
        seg.update(0, 2)
        assertEquals(MinCountNode(2, 3), seg.query(0..5))
    }
}
