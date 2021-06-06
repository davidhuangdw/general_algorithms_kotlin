package graph.tree

import graph.undirectedEdgesToAdj
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TreeDiameter{
    fun diameterByDP(edges: List<Pair<Int, Int>>): Pair<Int, Set<Int>> {
        val n = edges.size + 1
        var centers = setOf<Int>()
        var diameter = -1
        val adj = undirectedEdgesToAdj(edges, n)

        fun dfs(u: Int, parent: Int=-1): Int{
            var toLeaf = 0
            var longest = 0
            var ch = -1
            for(v in adj[u]) {
                if (v == parent) continue
                val vToLeaf = dfs(v, u)
                longest = maxOf(longest, toLeaf + (1+vToLeaf))
                if(1 + vToLeaf > toLeaf) {
                    toLeaf = 1 + vToLeaf
                    ch = v
                }
            }
            if(diameter < longest){
                diameter = longest
                centers = if(diameter % 2 == 0) setOf(u) else setOf(u, ch)
            }
            return toLeaf
        }
        dfs(1)
        return diameter to centers
    }

    fun diameterByTwoDfs(edges: List<Pair<Int, Int>>): Pair<Int, Set<Int>> {
        val n = edges.size + 1
        val adj = undirectedEdgesToAdj(edges, n)
        var farthest = -1 to -1
        val par = IntArray(n+1)

        fun dfs(u: Int, d: Int, parent: Int=-1){
            par[u] = parent
            if(d > farthest.second)
                farthest = u to d
            for(v in adj[u])
                if(v != parent)
                    dfs(v, d+1, u)
        }
        dfs(1, 0)

        val root = farthest.first
        farthest = -1 to -1
        dfs(root, 0)

        val diameter = farthest.second
        var v = farthest.first
        repeat(diameter/2){ v = par[v]}
        val centers = if(diameter % 2 == 0) setOf(v) else setOf(v, par[v])
        return diameter to centers
    }
}

class TestTreeDiameter{
    @Test
    fun testDiameter(){
        var edges = listOf(
            1 to 2,
            2 to 3,
            3 to 4,
            4 to 5,
            2 to 6,
            6 to 7,
        )
        var expected = 5 to setOf(2, 3)

        val tr = TreeDiameter()
        assertEquals(expected, tr.diameterByDP(edges))
        assertEquals(expected, tr.diameterByTwoDfs(edges))

        edges = listOf(
            1 to 2,
            2 to 3,
            3 to 4,
            4 to 5,
            3 to 6,
            6 to 7,
            2 to 8,
        )
        expected = 4 to setOf(3)
        assertEquals(expected, tr.diameterByDP(edges))
        assertEquals(expected, tr.diameterByTwoDfs(edges))

        edges = listOf()
        expected = 0 to setOf(1)
        assertEquals(expected, tr.diameterByDP(edges))
        assertEquals(expected, tr.diameterByTwoDfs(edges))
    }
}