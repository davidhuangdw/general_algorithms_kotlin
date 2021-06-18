package graph.tree

import graph.undirectedEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HeavyLightDecomposition(edges: List<Pair<Int, Int>>, val root: Int = 1, val N: Int = edges.size + 1) {
    val adj = undirectedEdgesToAdj(edges, N)
    val parent = IntArray(N + 1)
    val dep = IntArray(N + 1)
    val hson = IntArray(N + 1)
    val sz = IntArray(N + 1)
    val top = IntArray(N + 1)
    val dfsn = IntArray(N + 1)

    init {
        build()
    }

    private fun build() {
        var time = 0
        fun dfs1(u: Int) {
            sz[u] = 1
            for (v in adj[u])
                if (v != parent[u]) {
                    parent[v] = u
                    dep[v] = dep[u] + 1
                    dfs1(v)
                    sz[u] += sz[v]
                    if (sz[hson[u]] < sz[v])
                        hson[u] = v
                }
        }

        fun setTop(u: Int) {
            time += 1
            dfsn[u] = time

            if (hson[u] > 0) {
                top[hson[u]] = top[u]
                setTop(hson[u])
            }
            for (v in adj[u])
                if (v != parent[u] && v != hson[u]) {
                    top[v] = v
                    setTop(v)
                }
        }

        dep[root] = 0
        dfs1(root)

        top[root] = root
        setTop(root)
    }
}

class HeavyUnionOnTree(
    val color: List<Int>,
    edges: List<Pair<Int, Int>>,
    val root: Int = 1,
    val N: Int = edges.size + 1
) {
    // https://codeforces.com/blog/entry/67696

    private val adj = undirectedEdgesToAdj(edges, N)
    // each node: count how many of its subtree nodes have the same color
    fun countColorInSubtree(): List<Int> {      // O(n*log), every node are added O(light_edges_till_root)=O(log)
        val result = MutableList(N + 1) { 0 }
        val sz = IntArray(N + 1)
        val hson = IntArray(N + 1)
        fun getSize(u: Int, par: Int) {
            sz[u] = 1
            for (v in adj[u])
                if (v != par) {
                    getSize(v, u)
                    sz[u] += sz[v]
                    if (sz[v] > sz[hson[u]])
                        hson[u] = v
                }
        }

        val sub = MutableList(N + 1) { mutableListOf<Int>() }
        val colorSum = hashMapOf<Int, Int>()
        fun addColor(c: Int, cnt: Int = 1) {
            colorSum[c] = colorSum.getOrDefault(c, 0) + cnt
        }

        fun count(u: Int, par: Int, keep: Boolean) {
            for (v in adj[u])
                if (v != par && v != hson[u])
                    count(v, u, false)

            if (hson[u] > 0) {
                count(hson[u], u, true)
                sub[u] = sub[hson[u]]
            }

            sub[u].add(u)
            addColor(color[u])
            for (v in adj[u])
                if (v != par && v != hson[u]) {
                    for (x in sub[v]) {
                        sub[u].add(x)
                        addColor(color[x])
                    }
                }

            // answer query
            result[u] = colorSum[color[u]]!!

            if (!keep) {
                for (x in sub[u])
                    addColor(color[x], -1)
            }
        }

        getSize(root, -1)
        count(root, -1, false)
        return result
    }
}

class TestHeavyLightDecomposition {
    val root = 1
    val N = 11
    val edges = listOf(
        1 to 2,
        2 to 3,
        2 to 4,
        4 to 5,
        4 to 6,
        5 to 7,
        5 to 8,
        1 to 9,
        9 to 10,
        9 to 11,
    )

    @Test
    fun testHeavyUnionOnTree() {
        val color = listOf(-1, 0, 1, 0, 0, 2, 1, 1, 2, 1, 0, 1)
        val expectedSubTreeSum = listOf(0, 4, 3, 1, 1, 2, 1, 1, 1, 2, 1, 1)
        val union_tree = HeavyUnionOnTree(color, edges, root, N)
        assertEquals(expectedSubTreeSum, union_tree.countColorInSubtree())
    }
}