package string

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Palindrome {
    fun odd_manacher(s: String): List<Int> {
        val n = s.length
        var (l, r) = 0 to -1
        val odd_longest = MutableList(n){0}
        for(i in 0 until n){
            var k = if(i > r) 1 else minOf(r+1-i, odd_longest[r-i+l])
            while(0 <= i-k && i+k < n && s[i-k] == s[i+k])
                k += 1
            odd_longest[i] = k
            if(i+k-1 > r){
                r = i+k-1
                l = i-k+1
            }
        }
        return odd_longest
    }
}

class TestPalindrome{
    @Test
    fun test_odd_manacher(){
        val p = Palindrome()
        assertEquals(listOf(1, 2, 2, 1, 2, 1), p.odd_manacher("ababbb"))
    }
}