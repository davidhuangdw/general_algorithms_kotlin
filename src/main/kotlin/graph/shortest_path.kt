package graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayDeque

class ShortestPath {
    companion object {
        const val INF_DIS = 1e9.toInt()
    }

    fun bellmanFord(src: Int, adj: List<List<Pair<Int, Int>>>): List<Int>? {      // O(n*m)
        val n = adj.size
        val shortest = MutableList(n) { INF_DIS }
        // SPFA - improve by queue
        var que = ArrayDeque<Int>()
        val queued = BooleanArray(n)
        shortest[src] = 0
        que.add(src)
        queued[src] = true
        repeat(n) {          // when no negative-cycle, only update n-1 times -- shortest_path with <= n-1 edges
            repeat(que.size) {
                val v = que.removeFirst()
                for ((u, dis) in adj[v]) {
                    if (shortest[v] + dis >= shortest[u]) continue
                    shortest[u] = shortest[v] + dis
                    if (!queued[u]) {
                        que.add(u)
                        queued[u] = true
                    }
                }
                queued[v] = false
            }
        }
        if (que.isNotEmpty()) return null      // has negative cycle
        return shortest
    }

    fun dijkstra(src: Int, adj: List<List<Pair<Int, Int>>>): List<Int> {
        // O(m*log), or findMin by array: O(n^2) (by fibonacci-heap: O(m+n*log), how?)
        val n = adj.size
        val shortest = MutableList(n) { INF_DIS }
        val que = PriorityQueue<Pair<Int, Int>>(compareBy({ it.first }, { it.second }))
        que.add(0 to src)
        shortest[src] = 0
        while (que.isNotEmpty()) {
            val (dis, v) = que.remove()
            if(shortest[v] < dis) continue      // v has done
            for ((u, e) in adj[v])
                if (dis + e < shortest[u]) {  // shortest[v] == dis; no worry if u has done: dis+e >= shortest[u]
                    shortest[u] = dis + e
                    que.add(shortest[u] to u)
                }
        }
        return shortest
    }

    fun floydWarshall(adj: List<List<Pair<Int, Int>>>): List<List<Int>> {
        val n = adj.size
        val dis = List(n) { i -> MutableList(n) { INF_DIS } }
        for (v in 0 until n) {
            dis[v][v] = 0
            for ((u, e) in adj[v])
                dis[v][u] = e
        }
        for (k in 0 until n)
            for (i in 0 until n)
                for (j in 0 until n)
                    dis[i][j] = minOf(dis[i][j], dis[i][k] + dis[k][j])
        return dis
    }
}

class TestShortestPath {
    @Test
    fun testShortestPath() {
        val adj = mutableListOf(   //v -> (u to dis)
            listOf(1 to 1, 4 to -1),
            listOf(2 to 5, 3 to 1),
            listOf(0 to -2),
            listOf(2 to 3),
            listOf(1 to 1, 5 to 1, 6 to 1),
            listOf(),
            listOf(2 to 3)
        )

        val shortestFromZero = listOf(0, 0, 3, 1, -1, 0, 0)
        val sp = ShortestPath()
        assertEquals(shortestFromZero, sp.bellmanFord(0, adj))
        assertEquals(shortestFromZero, sp.dijkstra(0, adj))
        assertEquals(shortestFromZero, sp.floydWarshall(adj)[0])

        adj[2] = listOf(0 to -10)   // negative cycle
        assertEquals(null, sp.bellmanFord(0, adj))
    }
}