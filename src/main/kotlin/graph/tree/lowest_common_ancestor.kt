package graph.tree

import data_structure.RangeMinimalQuery
import graph.undirectedEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LowestCommonAncestor {
    fun tarjanOffline(
        N: Int,
        root: Int,
        edges: List<Pair<Int, Int>>,
        queries: List<Pair<Int, Int>>
    ): List<Int> { //O(n+q)
        val qr = List(N + 1) { mutableListOf<Pair<Int, Int>>() }
        for ((i, pair) in queries.withIndex()) {
            val (x, y) = pair
            qr[x].add(i to y)
            qr[y].add(i to x)
        }

        val nearestUndoneAncestor = IntArray(N + 1) { it }
        fun find(i: Int): Int {
            if (nearestUndoneAncestor[i] != i)
                nearestUndoneAncestor[i] = find(nearestUndoneAncestor[i])
            return nearestUndoneAncestor[i]
        }

        val ans = MutableList(queries.size) { -1 }
        val vis = Array(N + 1) { false }
        val adj = undirectedEdgesToAdj(edges, N)
        fun dfs(u: Int, parent: Int = -1) {
            vis[u] = true
            for (v in adj[u]) {
                if (!vis[v])
                    dfs(v, u)
            }
            for ((qi, y) in qr[u]) {
                if (vis[y])
                    ans[qi] = find(y)
            }
            nearestUndoneAncestor[u] = parent
        }

        dfs(root)
        return ans
    }
}

class OnlineLCA(N: Int, root: Int, edges: List<Pair<Int, Int>>) {
    val node = mutableListOf<Int>()
    val dep = mutableListOf<Int>()
    val first = IntArray(N + 1) { -1 }        // node to first index

    init {
        val adj = undirectedEdgesToAdj(edges, N)
        fun dfs(u: Int, d: Int) {
            first[u] = dep.size
            node.add(u)
            dep.add(d)
            for (v in adj[u])
                if (first[v] == -1) {
                    dfs(v, d + 1)
                    node.add(u)
                    dep.add(d)
                }
        }
        dfs(root, 1)
    }

    val rmq = RangeMinimalQuery(dep)

    fun query(x: Int, y: Int) = node[rmq.query(first[x], first[y]).second]   //O(1)
}

class ByHeavyPathComposition(N: Int, root: Int, edges: List<Pair<Int, Int>>) {
    val comp = HeavyLightDecomposition(edges, root, N)
    val dep = comp.dep
    val top = comp.top
    val parent = comp.parent

    fun lca(a: Int, b: Int): Int {
        var (x, y) = a to b
        while (top[x] != top[y]) { // go up one light-edge every time, O(log) light-edges parents in total, so O(log)
            if (dep[top[x]] <= dep[top[y]])
                y = parent[top[y]]
            else
                x = parent[top[x]]
        }
        return if (dep[x] <= dep[y]) x else y
    }
}

class TestLowestCommonAncestor {
    val root = 1
    val N = 9
    val edges = listOf(
        1 to 7,
        7 to 8,
        1 to 9,
        1 to 2,
        2 to 3,
        2 to 4,
        4 to 5,
        4 to 6,
    )
    val queriesAnswer = listOf(
        2 to 3 to 2,
        2 to 5 to 2,
        1 to 8 to 1,
        5 to 6 to 4,
        5 to 3 to 2,
        5 to 7 to 1,
        5 to 9 to 1,
        8 to 7 to 7,
        8 to 9 to 1,
    )

    @Test
    fun testTarjan() {
        val lca = LowestCommonAncestor()
        assertEquals(
            queriesAnswer.map { it.second },
            lca.tarjanOffline(N, root, edges, queriesAnswer.map { it.first })
        )
    }

    @Test
    fun testOnlineLca() {
        val lca = OnlineLCA(N, root, edges)
        assertEquals(
            queriesAnswer.map { it.second },
            queriesAnswer.map { lca.query(it.first.first, it.first.second) }
        )
    }

    @Test
    fun testByHeavyPathComposition() {
        val by_comp = ByHeavyPathComposition(N, root, edges)
        assertEquals(
            queriesAnswer.map { it.second },
            queriesAnswer.map { by_comp.lca(it.first.first, it.first.second) }
        )
    }
}