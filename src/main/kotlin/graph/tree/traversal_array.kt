package graph.tree

import data_structure.FenwickTree
import graph.undirectedEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TreeTraversalArray(root: Int, edges: List<Pair<Int, Int>>) { // for online subtree update & query by range_query
    val n = edges.size + 1
    val nodes = mutableListOf<Int>()
    val idx = IntArray(n + 1) { -1 }
    val subTreeSize = IntArray(n + 1) { 0 }

    init {
        val adj = undirectedEdgesToAdj(edges)
        fun dfs(u: Int): Int {
            idx[u] = nodes.size
            nodes.add(u)
            subTreeSize[u] = 1
            for (v in adj[u])
                if (idx[v] == -1)
                    subTreeSize[u] += dfs(v)
            return subTreeSize[u]
        }
        dfs(root)
    }
}

class OnlineSubtreeSum(val root: Int, val edges: List<Pair<Int, Int>>, val nodeValue: List<Int>) {
    val tr = TreeTraversalArray(root, edges)
    val fenwick = FenwickTree(tr.nodes.size)
    val values = tr.nodes.map { nodeValue[it] }.toMutableList()

    init {
        fenwick.build(values)
    }

    fun update(node: Int, v: Int) {
        val i = tr.idx[node]
        val diff = v - values[i]
        values[i] = v
        fenwick.add(i + 1, diff)
    }

    fun subTreeSum(node: Int): Int {
        val i = tr.idx[node]
        val sz = tr.subTreeSize[node]
        return (fenwick.preSum(i + sz) - fenwick.preSum(i)).toInt()
    }
}

class OnlineRootPath {}

class TestTreeTraversalArray {
    @Test
    fun testTreeTraversalArray() {
        val root = 1
        val edges = listOf(
            1 to 2,
            2 to 6,
            1 to 3,
            1 to 4,
            4 to 7,
            4 to 8,
            4 to 9,
            1 to 5,
        )
        var tr = TreeTraversalArray(root, edges)
        assertEquals(listOf(1, 2, 6, 3, 4, 7, 8, 9, 5), tr.nodes)
        assertEquals(listOf(-1, 0, 1, 3, 4, 8, 2, 5, 6, 7), tr.idx.toList())
        assertEquals(listOf(0, 9, 2, 1, 4, 1, 1, 1, 1, 1), tr.subTreeSize.toList())
    }

    @Test
    fun testOnlineSubtreeSum() {
        val root = 1
        val edges = listOf(
            1 to 2,
            1 to 3,
            3 to 4,
            3 to 5,
            1 to 6,
        )
        val nodeValue = (0..6).map { 2 }

        val subtree = OnlineSubtreeSum(root, edges, nodeValue)
        assertEquals(12, subtree.subTreeSum(1))
        assertEquals(2, subtree.subTreeSum(2))
        assertEquals(6, subtree.subTreeSum(3))
        assertEquals(2, subtree.subTreeSum(4))

        subtree.update(4, 5)        // +3
        subtree.update(2, 1)        // -1

        assertEquals(14, subtree.subTreeSum(1))
        assertEquals(1, subtree.subTreeSum(2))
        assertEquals(9, subtree.subTreeSum(3))
        assertEquals(5, subtree.subTreeSum(4))
        assertEquals(2, subtree.subTreeSum(5))
    }
}