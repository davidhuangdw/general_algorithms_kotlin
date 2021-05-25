package math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

fun gcd(aa: Int, bb: Int): Int{
    var (a, b) = aa to bb
    while(b > 0){ a = b.also{b = a % b} }
    return a
}

// return (g, x, y) that ax+by = g where g==gcd(a,b); so a*x = g (mod b)
fun exGcd(a: Int, b: Int): Triple<Int, Int, Int>{
    if(b==0){
        return Triple(a, 1, 0)
    }
    val (g, xx, yy) = exGcd(b, a%b)
    val (x, y) = yy to xx - (a/b)*yy
    return Triple(g, x, y)
}

// a^k % m
fun powMod(a: Int, kk: Int, m: Int): Int{
    var (x, mul, k) = Triple(1L, a.toLong(), kk)
    while(k > 0){
        if(k and 1 != 0) x = x*mul % m
        k = k shr 1
        mul = mul * mul % m
    }
    return x.toInt()
}

class BinomialCoefficient(val MAX_N: Int, val mod: Int = 1000_000_007){
    val fact = IntArray(MAX_N+1)
    val inv = IntArray(MAX_N+1)
    val fact_inv = IntArray(MAX_N+1)
    init {
        for(i in 0..1){
            fact[i] = 1; inv[i] = 1; fact_inv[i] = 1
        }

        for(i in 2..MAX_N){
            fact[i] = (1L*fact[i-1]*i % mod).toInt()
            inv[i] = (mod - 1L * (mod/i) * inv[mod%i] % mod).toInt()
            fact_inv[i] = (1L * fact_inv[i-1] * inv[i] % mod).toInt()
//            fact_inv[i] = powMod(fact[i], mod-2, mod)
        }
    }

    fun comb(n: Int, k: Int) =
        if (n < 0 || k < 0 || n < k) 0 else (1L * fact[n] * fact_inv[k] % mod * fact_inv[n - k] % mod).toInt()

    fun perm(n: Int, k: Int) = if(n < 0 || k < 0 || n < k) 0 else (1L * fact[n] * fact_inv[k] % mod).toInt()
}

class TestMathMod{
    @Test
    fun testExGcdAndPow(){
        for((pair, gg) in listOf((6 to 15) to 3, (4 to 6) to 2, (0 to 30) to 30)){
            val (a, b) = pair
            val (g, x, y) = exGcd(a, b)
            assertEquals(gg, gcd(a, b))
            assertEquals(gg, g)
            assertEquals(g, a*x + b*y)
        }
        for((a, b) in listOf(7 to 5, 3 to 11, 10 to 7, 12 to 11)){
            val (g, x, y) = exGcd(a, b)
            assertEquals(g, x*a+b*y)
            assertEquals((x+b)%b, powMod(a, b-2, b))  // because b is prime
//            println(listOf(a, b, x, y, g, (x+b)%b, powMod(a, b-2, b)))
        }
    }

    @Test
    fun testBinomialCoefficient(){
        val bc = BinomialCoefficient(1000)
        assertEquals(bc.comb(4, 0), 1)
        assertEquals(bc.comb(4, 2), 6)
        assertEquals(bc.comb(4, 4), 1)
        assertEquals(bc.comb(5, 2), 10)
        assertEquals(bc.comb(7, 3), 35)


        assertEquals(bc.comb(4, -1), 0)
        assertEquals(bc.comb(2, 3), 0)

    }
}
