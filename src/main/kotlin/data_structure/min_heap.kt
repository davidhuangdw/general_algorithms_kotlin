package data_structure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.Comparator

class Foo

val x = Foo().hashCode()

// min_heap with key_to_index mapping to achieve decrease_key(key), remove_key(key)

open class MinHeapSetWithCmp<T>(val sizeLimit: Int, var comparator: Comparator<T>) {
    var size = 0
    private val all = MutableList<T?>(sizeLimit + 1) { null }      // 1-based
    private val vToInd = hashMapOf<T, Int>()            // same value v have the same hashCode

    private fun lt(i: Int, j: Int) = comparator.compare(all[i], all[j]) < 0
    fun build(arr: Collection<T>) {
        size = arr.size
        for ((i, v) in arr.withIndex()) {
            set(i + 1, v)
        }
        for (i in size / 2 downTo 1)
            down(i)
    }

    private fun set(i: Int, v: T) {
        all[i] = v
        vToInd[v] = i
    }

    private fun down(i: Int) {
        var x = i
        while (x <= size / 2) {
            var ch = x * 2
            if (ch + 1 <= size && lt(ch + 1, ch))
                ch += 1
            if (lt(x, ch)) break
            swap(x, ch)
            x = ch
        }
    }

    private fun up(i: Int) {
        var x = i
        while (x >= 2) {
            var par = x / 2
            if (lt(par, x)) break
            swap(par, x)
            x = par
        }
    }

    private fun swap(i: Int, j: Int) {
        if (i != j) {
            val tmp = all[i]!!
            set(i, all[j]!!)
            set(j, tmp)
        }
    }

    fun decreaseKey(v: T) = up(vToInd[v]!!)
    fun increaseKey(v: T) = down(vToInd[v]!!)
    fun updateKey(v: T) {
        decreaseKey(v)
        increaseKey(v)
    }

    fun add(v: T): Boolean {
        if (vToInd.containsKey(v)) return false
        size += 1
        set(size, v)
        decreaseKey(v)
        return true
    }

    fun removeKey(v: T): Boolean {
        if (!vToInd.containsKey(v)) return false
        removeInd(vToInd[v]!!)
        return true
    }

    fun removeInd(i: Int): T {
        val v = all[i]!!
        swap(i, size)
        vToInd.remove(v)
        size -= 1
        down(i)
        return v
    }

    fun peek() = all[1]!!
    fun pop(): T {
        val v = peek()
        removeKey(v)
        return v
    }

    fun isNotEmpty() = size > 0
    fun exist(v: T) = v in vToInd
}

class MinHeapSet<T : Comparable<T>>(sizeLimit: Int) : MinHeapSetWithCmp<T>(sizeLimit, compareBy { it })


class TestMinHeapSetWithCmp {
    @Test
    fun testMinHeapSet() {
        var heap = MinHeapSet<Int>(100)
        var vs = listOf(5, 4, 1, 2, 6, 3, 7)
        for (v in vs)
            assert(heap.add(v))
        assertEquals(false, heap.add(3))

        assertEquals(vs.size, heap.size)
        var poped = mutableListOf<Int>()
        repeat(vs.size) {
            poped.add(heap.pop())
        }
        assertEquals(0, heap.size)
        assertEquals(vs.sorted(), poped)


        heap = MinHeapSet(100)
        heap.build(vs)
        assertEquals(vs.size, heap.size)
        poped = mutableListOf<Int>()
        repeat(vs.size) {
            poped.add(heap.pop())
        }
        assertEquals(0, heap.size)
        assertEquals(vs.sorted(), poped)
        for(v in vs) assertEquals(false, heap.exist(v))
    }
}