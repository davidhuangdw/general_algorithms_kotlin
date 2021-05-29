package data_structure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FenwickTree(val N: Int){
    private val nodes = LongArray(N+1)
    fun add(ii: Int, x: Int){
        var i = ii
        while(i <= N){
            nodes[i] = nodes[i] +x
            i += i and -i
        }
    }

    fun preSum(ii: Int): Long{
        var i = ii
        var s = 0L
        while(i > 0){
            s += nodes[i]
            i -= i and -i
        }
        return s
    }
}

class TestFenwickTree{
    @Test
    fun testFenwickTree1(){
        val tree = FenwickTree(10)
        for(i in 1..10)
            tree.add(i, i)
        val acc = (1..10).scan(0L, Long::plus)
        for(i in 1..10) {
            println(tree.preSum(i))
            assertEquals(acc[i], tree.preSum(i))
        }
    }
}