package graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BipartiteGraph {
    fun maxMatch(N: Int, edges: List<Pair<Int, Int>>): List<Pair<Int, Int>> {      // left: 1,3,5... right: 2,4,6...
        val match = MutableList(N * 2 + 1) { -1 }
        val vis = MutableList(N * 2 + 1) { false }

        val adj = undirectedEdgesToAdj(edges, N * 2)
        fun argument(x: Int): Boolean {
            for (y in adj[x]) {
                if(vis[y]) continue
                vis[y] = true
                if (match[y] == -1 || argument(match[y])) {
                    match[y] = x
                    return true
                }
            }
            return false
        }

        val matches = (2..2 * N step 2).count {
            vis.replaceAll { false }
            argument(it)
        }
        val matchEdges = (1..2 * N step 2).mapNotNull { if (match[it] == -1) null else it to match[it] }
        return matchEdges
    }
}

class TestBipartiteGraph {
    val bg = BipartiteGraph()

    @Test
    fun testMaxMatch() {
        val edges = listOf(
            1 to 2,
            3 to 2,
            3 to 6,
            3 to 8,
            5 to 4,
            5 to 8,
            7 to 6,
        )
        assertEquals(
            listOf(
                1 to 2,
                3 to 8,
                5 to 4,
                7 to 6,
            ),
            bg.maxMatch(8, edges)
        )
    }
}