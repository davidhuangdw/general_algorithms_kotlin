package graph.tree

import data_structure.UnionFinSet
import graph.undirectedWeightEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MinimalSpanningTree {
    companion object {
        const val MAX_DIS = 1e9.toInt()
    }

    // edge: (from, to, dis), vertex 1-based
    fun kruskal(N: Int, edges: List<Triple<Int, Int, Int>>): Pair<Int, List<Pair<Int, Int>>> { //O(m*log)
        var sum = 0
        var picked = mutableListOf<Pair<Int, Int>>()
        val unionSet = UnionFinSet(N)
        for (e in edges.sortedWith(compareBy { it.third })) {
            val (u, v, d) = e
            if (!unionSet.same(u, v)) {
                unionSet.union(u, v)
                sum += d
                picked.add(minOf(u, v) to maxOf(u, v))
            }
        }
        return sum to picked
    }

    fun prim(
        N: Int,
        edges: List<Triple<Int, Int, Int>>
    ): Pair<Int, List<Pair<Int, Int>>> { //O(n^2), or O(m + n*log) by fibonacci-heap
        val dis = Array(N + 1) { MAX_DIS to -1 }
        val adj = undirectedWeightEdgesToAdj(edges, N)
        val done = hashSetOf(1)
        var picked = mutableListOf<Pair<Int, Int>>()
        var sum = 0
        dis[1] = 0 to -1
        for ((v, d) in adj[1])
            dis[v] = d to 1
        repeat(N - 1) {
            val v = (1..N).filter { it !in done }.minByOrNull { dis[it].first }!!
            done.add(v)
            val (dv, pre) = dis[v]
            sum += dv
            picked.add(minOf(pre, v) to maxOf(pre, v))
            for ((x, d) in adj[v])
                if (x !in done && d < dis[x].first) {
                    dis[x] = d to v
                }
        }
        return sum to picked
    }
}

class TestMinimalSpanningTree {
    @Test
    fun testMST() {
        val edges = listOf(
            Triple(1, 2, 1),
            Triple(1, 3, 1),
            Triple(2, 3, 1),
            Triple(1, 4, 4),
            Triple(2, 4, 5),
            Triple(2, 5, 3),
            Triple(3, 5, 2),
            Triple(4, 5, 3),
        )

        val expected = 7 to hashSetOf(
            1 to 2,
            1 to 3,
            3 to 5,
            4 to 5
        )
        val tree = MinimalSpanningTree()
        val (sum, picked) = tree.kruskal(5, edges)
        assertEquals(expected, sum to picked.toHashSet())

        val (psum, ppicked) = tree.prim(5, edges)
        assertEquals(expected, psum to ppicked.toHashSet())
    }
}