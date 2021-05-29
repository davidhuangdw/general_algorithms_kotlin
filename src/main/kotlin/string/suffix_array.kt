package string

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixArray{
    var S = ""
    var pos = listOf<Int>()
    var posLcp = listOf<Int>()
    fun suffixOrder(ss: String): List<Int>{
        S = ss + '$'
        val n = S.length
        pos = (0 until n).sortedWith(compareBy({S[it]}, {it}))
        val rank = MutableList(n){0}
        rank[pos[0]] = 0
        pos.zipWithNext{ j, i ->
            rank[i] = rank[j] + if (S[i] == S[j]) 0 else 1
        }
        var half = 1

        fun sort(){ // O(n*log)
            pos = (0 until n).sortedWith(compareBy({rank[it]}, {rank[(it+half) % n]}, {it}))
        }
        fun radixSort(){ // O(n)
            val bucket = (0 until n).map{ mutableListOf<Int>()}
            for(i in pos) {
                val j = (i-half+n) % n
                bucket[rank[j]].add(j)
            }
            pos = bucket.flatten()
        }

        while(half < n){
//            sort()
            radixSort()
            val old = rank.toList()
            fun value(i: Int) = old[i] to old[(i+half) % n]
            rank[pos[0]] = 0
            pos.zipWithNext{ j, i ->
                rank[i] = rank[j] + if(value(i) == value(j)) 0 else 1
            }
            half *= 2
        }
        return pos
    }

    fun buildPosLcp(): List<Int>{       //posLcp[i] = LCP(pos[i+1], pos[i]).length
        var (n, k) = S.length-1 to 0
        val prevPos = IntArray(n)
        pos.zipWithNext{j, i -> prevPos[i] = j}
        val lcp = MutableList(n){0}
        for(i in 0 until S.length-1){
            val j = prevPos[i]
            while(S[i + k] == S[j + k]) k += 1
            lcp[i] = k
            k = maxOf(0, k-1)          // found lcp(S[i+1:], S[j+1:])) == k-1, so lcp[i+1] >= k-1
        }
        posLcp = (0 until n).map{lcp[pos[it+1]]}
        return posLcp
    }

    fun countDistinctSubstrings(): Int{
        val n = S.length-1
        return (0 until n).sumOf { i -> n - pos[i+1] - posLcp[i] }    // lcp[i] = LCP(pos[i+1], pos[i])
    }
}

class TestSuffixArray{
    @Test
    fun testSuffixArray1(){
        val sf = SuffixArray()
        var s = "ababba"
        var pos = sf.suffixOrder(s)
        assertEquals(listOf(6,5,0,2,4,1,3), pos)
        sf.buildPosLcp()
        assertEquals(listOf(0,1,2,0,2,1), sf.posLcp)
        var distinctSubs = (0 until s.length).flatMap { i -> (i+1..s.length).map{s.substring(i, it)} }.toSet()
        assertEquals(distinctSubs.size, sf.countDistinctSubstrings())

        s = "aaaaaa"
        pos = sf.suffixOrder(s)
        assertEquals(listOf(6,5,4,3,2,1,0), pos)
        sf.buildPosLcp()
        distinctSubs = (0 until s.length).flatMap { i -> (i+1..s.length).map{s.substring(i, it)} }.toSet()
        assertEquals(distinctSubs.size, sf.countDistinctSubstrings())


        //random test:
        s = (0 until 50).map { ('a'..'c').random() }.joinToString("")
        println(s)
        pos = sf.suffixOrder(s)
        for(i in pos)
            println("$i\t${s.substring(i)}")

        sf.buildPosLcp()
        for(i in 0 until s.length){
            val (x, y) = sf.pos[i] to sf.pos[i+1]
            println("$i $x $y: ${sf.posLcp[i]}")
            println(s.substring(x))
            println(s.substring(y))
        }
        distinctSubs = (0 until s.length).flatMap { i -> (i+1..s.length).map{s.substring(i, it)} }.toSet()
        assertEquals(distinctSubs.size, sf.countDistinctSubstrings())
    }
}