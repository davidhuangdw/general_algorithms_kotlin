package misc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class PythonMRO {
    data class Node(val name: String, val parents: List<Node> = listOf())

    fun c3Mro(node: Node): List<String> {
        fun merge(ls: List<List<String>>): List<String> {
            val count = hashMapOf<String, Int>()
            for (l in ls) {
                for (i in 1 until l.size)
                    count[l[i]] = count.getOrDefault(l[i], 0) + 1
            }

            val res = mutableListOf<String>()
            val pos = ls.map { 0 }.toMutableList()
            while (true) {
                // pick head
                var k = -1
                for ((i, p) in pos.withIndex()) {
                    if (p < ls[i].size && count.getOrDefault(ls[i][p], 0) == 0) {
                        k = i
                        break
                    }
                }
                if (k == -1) break

                val name = ls[k][pos[k]]
                res.add(name)

                // remove
                for ((i, l) in ls.withIndex()) {
                    if (pos[i] < l.size && l[pos[i]] == name) {
                        pos[i] += 1
                        if (pos[i] < l.size)
                            count[l[pos[i]]] = count[l[pos[i]]]!! - 1
                    }
                }
            }
            return res
        }

        return listOf(node.name) + merge(node.parents.map { c3Mro(it) } + listOf(node.parents.map { it.name }))
    }

    fun topoMro(node: Node): List<String> {
        val order = hashMapOf<Node, MutableList<Node>>()
        var rank = 0
        val ranks = hashMapOf<Node, Int>()
        val degree = hashMapOf<Node, Int>()
        fun buildOrder(u: Node) {
            if (u in order) return
            order[u] = mutableListOf()
            degree[u] = 0
            rank += 1
            ranks[u] = rank
            for (p in u.parents) {
                buildOrder(p)
                order[u]!!.add(p)
                degree[p] = degree[p]!! + 1
            }
            for ((x, y) in u.parents.zipWithNext()) {
                order[x]!!.add(y)
                degree[y] = degree[y]!! + 1
            }
        }
        buildOrder(node)

        val res = mutableListOf<String>()
        val vis = hashSetOf<Node>()
        val que = PriorityQueue<Node>(compareBy { ranks[it]!! })
        que.add(node)
        while (que.isNotEmpty()) {
            val u = que.poll()
            vis.add(u)
            res.add(u.name)
            for (v in order[u]!!)
                if (v !in vis) {
                    degree[v] = degree[v]!! - 1
                    if (degree[v] == 0)
                        que.add(v)
                }

        }
        return res
    }
}

class TestPythonMRO {
    @Test
    fun testC3() {
        val x = PythonMRO()

        val o = PythonMRO.Node("o")
        val a = PythonMRO.Node("a", listOf(o))
        val b = PythonMRO.Node("b", listOf(o))
        val c = PythonMRO.Node("c", listOf(o))
        val d = PythonMRO.Node("d", listOf(o))
        val e = PythonMRO.Node("e", listOf(o))
        val k1 = PythonMRO.Node("k1", listOf(a, b, c))
        val k2 = PythonMRO.Node("k2", listOf(d, b, e))
        val k3 = PythonMRO.Node("k3", listOf(d, a))
        val z = PythonMRO.Node("z", listOf(k1, k2, k3))

        val mro = x.c3Mro(z)
        println(mro)
        assertEquals(mro, x.topoMro(z))
    }
}