package data_structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnionFinSet(val N: Int) {  // 1..N, 1-based
    private val parent = IntArray(N + 1) { it }
    fun find(i: Int): Int {
        if (parent[i] != i)
            parent[i] = find(parent[i])
        return parent[i]
    }

    fun union(i: Int, j: Int) {
        parent[find(i)] = find(j)
    }

    fun same(i: Int, j: Int) = find(i) == find(j)
}

class TestUnionFindSet {
    @Test
    fun testUnionFindSet() {
        val s = UnionFinSet(10)
        s.union(1, 2)
        s.union(3, 4)

        assertEquals(false, s.same(1, 3))
        s.union(2, 4)
        assertEquals(true, s.same(1, 3))
    }
}