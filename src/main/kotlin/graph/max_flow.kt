package graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

object MaxFlow {
    data class Edge(val u: Int, val v: Int, var cap: Int, val cost: Int = 0)

    private fun edgesToAdj(N: Int, edges: List<Triple<Int, Int, Int>>): List<HashMap<Int, Int>> {    // source=1, sink=N
        val adj = (0..N).map { hashMapOf<Int, Int>() }
        for ((u, v, w) in edges) {
            adj[u][v] = w
            adj[v][u] = 0
        }
        return adj
    }

    private fun argumentEdge(u: Int, v: Int, w: Int, adj: List<HashMap<Int, Int>>) {
        adj[u][v] = adj[u][v]!! - w
        adj[v][u] = adj[v][u]!! + w
    }

    fun fordFulkerson(N: Int, edges: List<Triple<Int, Int, Int>>): Int {    // O(E*f)
        val adj = edgesToAdj(N, edges)
        val vis = MutableList(N + 1) { false }
        fun dfs(u: Int, preFlow: Int): Int {
            val res = preFlow
            vis[u] = true
            if (u == N)
                return preFlow
            for ((v, w) in adj[u]) {
                if (vis[v] || w == 0) continue
                val flow = dfs(v, minOf(preFlow, w))
                if (flow > 0) {
                    argumentEdge(u, v, flow, adj)
                    return flow
                }
            }
            return 0
        }

        var sum = 0
        do {
            vis.replaceAll { false }
            val f = dfs(1, Int.MAX_VALUE)
            sum += f
        } while (f > 0)
        return sum
    }

    fun edmondKarp(N: Int, edges: List<Triple<Int, Int, Int>>): Int {   // O(E^2 * V)
        // BFS each round to augment with shortest path,
        // O(E^2 * V): because d2(u) = d2(v) + 1 >= d(v) + 1 = d(u) + 2   =>  each edge's critical rounds <= |v|/2
        val adj = edgesToAdj(N, edges)
        fun bfs(): Int {
            val que = ArrayDeque<Pair<Int, Int>>(N + 1)
            val pre = MutableList(N + 1) { -1 }
            pre[1] = 0
            que.add(1 to Int.MAX_VALUE)
            while (que.isNotEmpty()) {
                val (u, preFlow) = que.removeFirst()
                for ((v, w) in adj[u]) {
                    if (!(pre[v] == -1 && w > 0)) continue
                    pre[v] = u
                    val f = minOf(preFlow, w)
                    if (v == N) {
                        var x = v
                        while (x != 1) {
                            argumentEdge(pre[x], x, f, adj)
                            x = pre[x]
                        }
                        return f
                    }
                    que.add(v to f)
                }
            }
            return 0
        }

        var sum = 0
        do {
            val f = bfs()
            sum += f
        } while (f > 0)
        return sum
    }

    fun capacityScaling(N: Int, edges: List<Triple<Int, Int, Int>>): Int {  // O(E^2*log(maxWeight))
        val adj = edgesToAdj(N, edges)
        val maxw = edges.maxOf { it.third }
        var limit = 1 shl ((1..maxw).first { k -> (1 shl k) > maxw } - 1)

        val vis = MutableList(N + 1) { false }
        fun dfs(u: Int, preFlow: Int): Int {
            vis[u] = true
            if (u == N)
                return preFlow
            for ((v, w) in adj[u]) {
                if (!vis[v] && w >= limit) {
                    val f = dfs(v, minOf(w, preFlow))
                    if (f > 0) {
                        argumentEdge(u, v, f, adj)
                        return f
                    }
                }
            }
            return 0
        }

        var sum = 0
        while (limit > 0) {
            do {
                vis.replaceAll { false }
                val f = dfs(1, Int.MAX_VALUE)
                sum += f
            } while (f > 0)
            limit /= 2
        }
        return sum
    }

    fun dinic(N: Int, edges: List<Triple<Int, Int, Int>>): Int {
        val adj = edgesToAdj(N, edges)
        val level = MutableList(N + 1) { -1 }

        fun bfs(): Boolean {
            level.replaceAll { -1 }
            val que = ArrayDeque<Int>(N + 1)
            que.add(1)
            level[1] = 1
            while (que.isNotEmpty()) {
                val u = que.removeFirst()
                for ((v, w) in adj[u])
                    if (level[v] == -1 && w > 0) {
                        level[v] = level[u] + 1
                        if (v == N) return true
                        que.add(v)
                    }
            }
            return false
        }

        fun dfs(u: Int, preFlow: Int): Int {
            if (u == N) return preFlow
            var rest = preFlow
            for ((v, w) in adj[u]) {
                if (!(level[v] == level[u] + 1 && w > 0)) continue     // no worry about cycle because level increasing
                val f = dfs(v, minOf(rest, w))
                if (f == 0)
                    level[v] = 0         // prune: remove impossible vertex
                else {
                    argumentEdge(u, v, f, adj)
                    rest -= f
                    if (rest == 0) break
                }
            }
            return preFlow - rest
        }

        var sum = 0
        while (bfs()) {
            do {
                val f = dfs(1, Int.MAX_VALUE)
                sum += f
            } while (f > 0)
        }
        return sum
    }

    fun minCostMaxFlow(N: Int, edges: List<Edge>): Pair<Int, Int> {
        val adj = (0..N).map { hashMapOf<Int, Edge>() }
        var sumFlow = 0
        var sumCost = 0
        for ((u, v, w, c) in edges) {
            adj[u][v] = Edge(u, v, w, c)
            adj[v][u] = Edge(u, v, 0, -c)
        }

        fun argument(u: Int, v: Int, f: Int){
            adj[u][v]!!.cap -= f
            adj[v][u]!!.cap += f
        }

        fun spfa(): Boolean{
            val minCost = MutableList(N+1){Int.MAX_VALUE}
            val preFlow = MutableList(N+1){0}
            val pre = MutableList(N+1){0}

            val queued = MutableList(N+1){false}
            val que = ArrayDeque<Int>(N+1)
            minCost[1] = 0
            preFlow[1] = Int.MAX_VALUE
            que.add(1)
            queued[1] = true
            repeat(N){
                repeat(que.size){
                    val u = que.removeFirst()
                    for((v, e) in adj[u])
                        if(e.cap > 0 && minCost[u] + e.cost < minCost[v]){
                            preFlow[v] = minOf(preFlow[u], e.cap)
                            minCost[v] = minCost[u] + e.cost
                            pre[v] = u
                            if(!queued[v]){
                                que.add(v)
                                queued[v] = true
                            }
                        }
                    queued[u] = false
                }
            }
            if(que.isNotEmpty()) throw RuntimeException("negative cycle!!")
            if(preFlow[N] == 0) return false

            var u = N
            val f = preFlow[N]
            while(u != 1){
                argument(pre[u], u, f)
                u = pre[u]
            }
            sumFlow += f
            sumCost += f * minCost[N]
            return true
        }

        while(spfa());
        return sumFlow to sumCost
    }
}

class TestMaxFlow {
    val edges = listOf(
        Triple(1, 2, 4),
        Triple(1, 4, 5),
        Triple(2, 3, 5),
        Triple(2, 4, 1),
        Triple(3, 7, 5),
        Triple(4, 3, 2),
        Triple(4, 5, 5),
        Triple(5, 3, 6),
        Triple(5, 6, 3),
        Triple(6, 7, 2),
    )
    val N = 7

    @Test
    fun testFordFulkerson() {
        assertEquals(7, MaxFlow.fordFulkerson(N, edges))
    }

    @Test
    fun testEdmondKarp() {
        assertEquals(7, MaxFlow.edmondKarp(N, edges))
    }

    @Test
    fun testCapacityScaling() {
        assertEquals(7, MaxFlow.capacityScaling(N, edges))
    }

    @Test
    fun testDinic() {
        assertEquals(7, MaxFlow.dinic(N, edges))
    }

    val costEdges = listOf(
        MaxFlow.Edge(1, 2, 4, 100),
        MaxFlow.Edge(1, 4, 5, 1),
        MaxFlow.Edge(2, 3, 5, 1),
        MaxFlow.Edge(2, 4, 1, 1),
        MaxFlow.Edge(3, 7, 5, 1),
        MaxFlow.Edge(4, 3, 2, 1),
        MaxFlow.Edge(4, 5, 5, 1),
        MaxFlow.Edge(5, 3, 6, 1),
        MaxFlow.Edge(5, 6, 3, 1),
        MaxFlow.Edge(6, 7, 2, 1),
    )

    @Test
    fun testMinCostMaxFlow(){
        assertEquals(7 to 222, MaxFlow.minCostMaxFlow(N, costEdges))
    }

}
