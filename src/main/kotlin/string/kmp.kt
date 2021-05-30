package string

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KMP {
    var longest = IntArray(0)
    var next = IntArray(0)
    var pattern = ""
    fun setupPattern(s: String){
        pattern = s

        val n = s.length
        val lg = IntArray(n)        // lg[i] := max{L | s[0..L-1] == s[i-L+1..i] && L-1 < i}
        lg[0] = 0
        for(i in 1 until n){
            var pre = lg[i-1]
            while(pre > 0 && s[pre] != s[i])
                pre = lg[pre-1]
            lg[i] = if(s[pre] == s[i]) pre+1 else 0
        }

        var nx = IntArray(n)        // nx[i]: when s[i] unmatched, next previous i to compare
        nx[0] = -1
        for(i in 1 until n){
            val j = lg[i-1]
            nx[i] = if(s[i] == s[j]) nx[j] else j
        }
        longest = lg
        next = nx
    }

    fun find(s: String): Int{
        val n = pattern.length
        var j = 0
        for((i,ch) in s.withIndex()){
            while(j >= 0 && pattern[j] != ch)
                j = next[j]
            j += 1
            if(j == n) return i+1-n
        }
        return -1
    }
}

class TestKMP{
    @Test
    fun testKMP1(){
        val kmp = KMP()
        var pattern = "AABA"
        kmp.setupPattern(pattern)
        var target = "ABAACAADAABAABA"
        var pos = kmp.find(target)
        assertEquals(8, pos)
        println(target.substring(pos))

        println("-".repeat(80))

        // random test:
        pattern = (1..6).map{ ('a'..'b').random()}.joinToString("")
        kmp.setupPattern(pattern)
        println(pattern)
        println(kmp.longest.mapIndexed{i, l -> pattern.substring(i-l+1, i+1)}.joinToString(","))
        println(kmp.next.toList())

        target = (1..50).map{ ('a'..'b').random()}.joinToString("")
        println(target)
        pos = kmp.find(target)
        println("$pos: ${if(pos>0) target.substring(pos) else ""}")
    }
}
