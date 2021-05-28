package general.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class UndirectedConnectivity(val N: Int, val Adj: List<List<Int>>){
    val pre = IntArray(N+1)
    val low = IntArray(N+1)
    val edcc = IntArray(N+1)    // edge double connected component
    val vdcc = mutableListOf<List<Int>>()    // vertex double connected component
    var n_edcc = 0
    var n_vdcc = 0
    val bridges = hashSetOf<Pair<Int, Int>>()
    val cuts = hashSetOf<Int>()

    fun clear(){
        for(x in listOf(pre, low, edcc))
            for(i in 0..N)
                x[i] = -1
        vdcc.clear()
        bridges.clear()
        cuts.clear()
    }

    fun getConnectivity(){      // get bridges, cuts, e-dcc, v-dcc
        clear()
        val vdcc_stack = mutableListOf<Int>()
        n_vdcc = 0
        var time = 0
        fun tarjan(v: Int, parent: Int){
            time += 1
            low[v] = time
            pre[v] = time
            vdcc_stack.add(v)
            var count = 0
            for(w in Adj[v]){
                if(w == parent) continue
                if(pre[w] > 0) // visited
                    low[v] = minOf(low[v], pre[w])
                else{
                    tarjan(w, v)
                    low[v] = minOf(low[v], low[w])
                    if(pre[v] < low[w]) {
                        bridges.add(v to w)
                        bridges.add(w to v)
                    }
                    if(pre[v] <= low[w]){
                        count += 1
                        if(parent != -1 || count > 1){  // if is root, need >=2 cases
                            cuts.add(v)
                        }
                        // although root is not cut, subtree is still a vdcc
                        n_vdcc += 1
                        val sub = mutableListOf<Int>()
                        while(vdcc_stack.last() != v){
                            sub.add(vdcc_stack.removeLast())
                        }
                        sub.add(v)
                        vdcc.add(sub)
                    }
                }
            }
        }
        fun dfs_edcc(v: Int, parent: Int, id: Int){
            edcc[v] = id
            for(w in Adj[v]){
                if(edcc[w] > 0 || bridges.contains(v to w)) continue
                dfs_edcc(w, v, id)
            }
        }

        for(i in 1..N) {
            if (pre[i] == -1)
                tarjan(1, -1)
        }

        n_edcc = 0
        for(i in 1..N){
            if(edcc[i] == -1) {
                n_edcc += 1
                dfs_edcc(i, -1, n_edcc)
            }
        }
    }
}

class DirectedConnectivity(val N: Int, val Adj: List<List<Int>>){
    val pre = IntArray(N+1)
    val low = IntArray(N+1)
    val scc = IntArray(N+1)
    var n_scc = 0
    fun clear(){
        n_scc = 0
        for(x in listOf(pre, low, scc))
            for(i in 0..N)
                x[i] = 0
    }
    fun getScc(){   // strong connected component
        clear()
        val stack = mutableListOf<Int>()
        n_scc = 0
        var time = 0
        fun tarjan(v: Int){
            time += 1
            pre[v] = time
            low[v] = time
            stack.add(v)
            for(w in Adj[v]){
                if(pre[w] <= 0){
                    tarjan(w)
                    low[v] = minOf(low[v], low[w])
                }else{
                    // if v is parent: low[v] = minOf(low[v], pre[w])
                    // if v is older brother: low[v] is not useful(as long as it's <pre[v] to keep inside stack)
                    //  because w's low_value will be used by ancestor low[w], so no need set low[v] = low[w]
                    //  it means: only need the edge back to ancestor, no need compute endpoint of edge back to older brother
                    if(scc[w] == 0)         // still in stack
                        low[v] = minOf(low[v], pre[w])
                }
            }

            if(pre[v] == low[v]){   // perspective: ancestor's view
                n_scc += 1
                do{
                    val y = stack.removeLast()
                    scc[y] = n_scc
                }while(y != v)
            }
        }
        for(v in 1..N)
            if(pre[v] <= 0)
                tarjan(v)

    }

    fun getSccByKosaraju(){ // by Kosaraju's algorithm
        /*
        prove correctness:
        1. reversed graph has the same scc
        2. in reversed graph, previous leaking_edges(from older_scc to other scc) cannot go out now
         */

        clear()
        val topoOrder = mutableListOf<Int>()
        val reversedAdj = List(N+1){ mutableListOf<Int>() }
        val visited = MutableList(N+1){false}
        fun topoDfs(v: Int){
            visited[v] = true
            for(u in Adj[v]){
                reversedAdj[u].add(v)
                if(!visited[u])
                    topoDfs(u)
            }
            topoOrder.add(v)
        }

        for(v in 1..N)
            if(!visited[v]) topoDfs(v)
        topoOrder.reverse()

        fun markScc(v: Int){
            scc[v] = n_scc
            for(u in reversedAdj[v]){
                if(scc[u] == 0)
                    markScc(u)
            }
        }

        for(v in topoOrder)
            if(scc[v] == 0){
                n_scc += 1
                markScc(v)
            }
    }
}

class TestConnectivity(){
    @Test
    fun testScc(){
        val edges = listOf(
            1 to 2,
            2 to 3,
            2 to 4,
            4 to 5,
            4 to 6,
            4 to 8,
            5 to 3,
            6 to 5,
            6 to 7,
            7 to 4,
            8 to 2,
            9 to 10,
        )
        val N = 11
        val Adj = List(N+1){ mutableListOf<Int>()}
        for((v, u) in edges){
            Adj[v].add(u)
        }

        val expectedScc = listOf(
            listOf(1),
            listOf(2, 4, 6, 7, 8),
            listOf(3),
            listOf(5),
            listOf(9),
            listOf(10),
            listOf(11),
        )

        val dc = DirectedConnectivity(N, Adj)
        fun check(){
            assertEquals(expectedScc.size, dc.n_scc)
            for(sub in expectedScc){
//            println(sub)
//            println(sub.map{dc.scc[it]})
                assertEquals(1, sub.map{dc.scc[it]}.toSet().size)
            }
        }
        dc.getScc()
        check()

        dc.getSccByKosaraju()
        check()
    }
}