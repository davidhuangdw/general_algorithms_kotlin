package graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun edgesToAdj(N: Int, edges: List<Pair<Int, Int>>, undirected: Boolean = false): List<List<Int>> {       // 1-based
    val adj = List(N + 1) { hashSetOf<Int>() }
    for ((a, b) in edges) {
        adj[a].add(b)
        if (undirected)
            adj[b].add(a)
    }
    return adj.map { it.toList() }
}

fun weightEdgesToAdj(
    N: Int,                                     // 1-based
    edges: List<Triple<Int, Int, Int>>,         // (from, to, dis)
    undirected: Boolean = false
): List<List<Pair<Int, Int>>> {
    val adj = List(N + 1) { mutableListOf<Pair<Int, Int>>() }
    for ((a, b, d) in edges) {
        adj[a].add(b to d)
        if (undirected)
            adj[b].add(a to d)
    }
    return adj
}

fun undirectedEdgesToAdj(edges: List<Pair<Int, Int>>, N: Int = edges.size + 1) =
    edgesToAdj(N, edges, true)

fun undirectedWeightEdgesToAdj(edges: List<Triple<Int, Int, Int>>, N: Int = edges.size + 1) =
    weightEdgesToAdj(N, edges, true)

class TestGraphUtils {
    @Test
    fun testEdgesToAdj() {
        val edges = listOf(
            1 to 2,
            2 to 3,
            2 to 4,
            4 to 3,
            1 to 5,
        )
        val adj = listOf(
            listOf(),
            listOf(2, 5),
            listOf(3, 4),
            listOf(),
            listOf(3),
            listOf()
        )
        assertEquals(adj, edgesToAdj(5, edges))
    }
}