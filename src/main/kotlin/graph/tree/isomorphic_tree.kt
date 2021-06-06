package graph.tree

import graph.edgesToAdj
import graph.undirectedEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IsoMorphicTree {
    fun getCenters(N: Int, adj: List<List<Int>>): List<Int> {
        val deg = (0..N).map { adj[it].size }.toMutableList()
        var leaves = (1..N).filter { deg[it] <= 1 }.toHashSet()
        val done = leaves.toHashSet()
        while (done.size < N) {
            val nextLeaves = hashSetOf<Int>()
            for (u in leaves)
                for (v in adj[u])
                    if (v !in done) {
                        deg[v] -= 1
                        if (deg[v] <= 1)
                            nextLeaves.add(v)
                    }

            leaves = nextLeaves
            done.addAll(leaves)
        }
        return leaves.sorted()
    }

    fun encode(root: Int, adj: List<List<Int>>, parent: Int = -1): String = buildString {
        append('0')
        for (ch in adj[root].filter { it != parent }.map { encode(it, adj, root) }.sorted())
            append(ch)
        append('1')
    }

    fun decode(code: String): List<Pair<Int, Int>> {
        var cnt = 0
        var edges = mutableListOf<Pair<Int, Int>>()
        var stack = mutableListOf<Int>()
        for (ch in code)
            if (ch == '0') {
                cnt += 1
                stack.add(cnt)
            } else {
                val v = stack.removeLast()
                if (stack.isNotEmpty())
                    edges.add(stack.last() to v)
            }
        return edges.sortedWith(compareBy({ it.first }, { it.second }))
    }

    fun isomorphicSame(tree1_edges: List<Pair<Int, Int>>, tree2_edges: List<Pair<Int, Int>>): Boolean {
        if (tree2_edges.size != tree1_edges.size)
            return false
        val n = tree1_edges.size + 1
        var adj = undirectedEdgesToAdj(tree1_edges)
        val tree1_code = encode(getCenters(n, adj)[0], adj)
        adj = undirectedEdgesToAdj(tree2_edges)
        return getCenters(n, adj).any {
            tree1_code == encode(it, adj)
        }
    }
}


class TestIsomorphicTree {
    @Test
    fun testGetCenters() {
        val tree = IsoMorphicTree()
        assertEquals(listOf(1), tree.getCenters(1, undirectedEdgesToAdj(listOf())))
        assertEquals(
            listOf(2), tree.getCenters(
                4, undirectedEdgesToAdj(
                    listOf(
                        1 to 2,
                        2 to 3,
                        2 to 4,
                    )
                )
            )
        )
        assertEquals(
            listOf(3), tree.getCenters(
                7, undirectedEdgesToAdj(
                    listOf(
                        1 to 2,
                        2 to 3,
                        2 to 4,
                        3 to 5,
                        3 to 6,
                        6 to 7,
                    )
                )
            )
        )
        assertEquals(
            listOf(2, 3), tree.getCenters(
                6, undirectedEdgesToAdj(
                    listOf(
                        1 to 2,
                        2 to 3,
                        2 to 4,
                        3 to 5,
                        3 to 6,
                    )
                )
            )
        )
    }

    @Test
    fun testEncodeTree() {
        val tree = IsoMorphicTree()
        assertEquals("01", tree.encode(1, undirectedEdgesToAdj(listOf())))
        assertEquals(
            "0001011011", tree.encode(
                1, undirectedEdgesToAdj(
                    listOf(
                        1 to 2,
                        1 to 3,
                        3 to 4,
                        3 to 5,
                    )
                )
            )
        )
        assertEquals(
            listOf(
                1 to 2,
                1 to 5,
                2 to 3,
                2 to 4,
            ), tree.decode("0001011011")
        )
    }

    @Test
    fun testIsomorphicSame() {
        val tree = IsoMorphicTree()
        assertEquals(
            true, tree.isomorphicSame(
                listOf(
                    1 to 2,
                    1 to 3,
                    3 to 4,
                    3 to 5,
                ), listOf(
                    1 to 2,
                    1 to 5,
                    2 to 3,
                    2 to 4,
                )
            )
        )
        assertEquals(
            false, tree.isomorphicSame(
                listOf(
                    1 to 2,
                    1 to 3,
                    3 to 4,
                    3 to 5,
                ), listOf(
                    1 to 2,
                    2 to 3,
                    3 to 4,
                    5 to 4,
                )
            )
        )
    }
}