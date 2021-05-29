package math
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.*

data class Complex(val re: Double, val im: Double){
    companion object { val ONE = 1.toComplex() }
    operator fun times(y: Complex) = Complex(re*y.re - im*y.im, re*y.im + im*y.re)
    operator fun plus(y: Complex) = Complex(re + y.re, im + y.im)
    operator fun minus(y: Complex) = Complex(re - y.re, im - y.im)
    operator fun div(y: Number) = Complex(re/y.toDouble(), im/y.toDouble())

}
fun Number.toComplex() = Complex(toDouble(), .0)
operator fun Double.times(x: Complex) = Complex(times(x.re), times(x.im))
fun exp(x: Complex) = exp(x.re) * Complex(cos(x.im), sin(x.im))

class FastFourierTransform{
    fun fftRecursive(vv: List<Complex>, d: Int=1): List<Complex>{
        fun _fft(v: List<Complex>): List<Complex>{
            val n = v.size
            if(n == 1) return v
            assert(n and (n-1) == 0)    // n == 2^k

            val even = _fft((0 until n step 2).map{v[it]})
            val odd = _fft((1 until n step 2).map{v[it]})
            val unit = exp(Complex(.0, d * 2 * PI / n))
            var x = Complex.ONE
            val r = MutableList(n){ Complex.ONE }
            for(i in 0 until n/2){
                r[i] = even[i] + x*odd[i]
                r[i+n/2] = even[i] - x*odd[i]
                x *= unit
            }
            return r
        }
        val res = _fft(vv)
        if(d == -1)
            return res.map{it / vv.size}
        return res
    }

    fun bit_size(xx: Int): Int{
        var (x, l) = xx to 0
        while(x > 0){
            x = x shr 1
            l += 1
        }
        return l
    }
    fun bit_reverse(xx: Int, bit_len: Int): Int{
        var (x, r) = xx to 0
        repeat(bit_len){
            r = r shl 1 or (x and 1)
            x = x shr 1
        }
        return r
    }

    fun fft(v: List<Complex>, d: Int = 1): List<Complex>{       // iterative version

        val n = v.size
        assert(n and (n-1) == 0)    // n == 2^k
        val len = bit_size(n) - 1

        val res = MutableList(n){ i -> v[bit_reverse(i, len)]}
        var m = 2
        while(m <= n){
            val half = m/2
            val unit = exp(Complex(.0, d * 2 * PI / m))
            for(off in 0 until n step m){
                var x = 1.toComplex()       // x=w(n, k)
                /*
                w(n, 2k) = w(n/2, k), so
                f(w(n, k)) = f_even(w(n, 2k)) +  w(n, k) * f_odd(w(n, 2k))
                           = f_even(w(n/2, k)) + w(n, k) * f_odd(w(n/2, k))

                w(n, 2(k+n/2)) = w(n/2, k+n/2) = w(n/2, k)
                w(n, k + n/2)  = w(n, n/2) * w(n, k) = -w(n, k)
                f(w(n, k+n/2)) = f_even(w(n, 2(k+n/2))) + w(n, k+n/2) * f_odd(w(n, 2(k+n/2)))
                               = f_even(w(n/2, k+n/2))  + w(n, k+n/2) * f_odd(w(n/2, k+n/2))
                               = f_even(w(n/2, k))      + w(n, k+n/2) * f_odd(w(n/2, k))
                               = f_even(w(n/2, k))      - w(n, k) * f_odd(w(n/2, k))
                */
                for(k in 0 until half){
                    val even = res[off+k]
                    val odd = res[off+half+k]
                    res[off+k] = even + x*odd
                    res[off+half+k] = even - x*odd    // u^half = u^PI = -1, so u^(half+k) = -u^k
                    x *= unit
                }
            }
            m *= 2
        }
        if(d == -1){        // don't forget
            for(i in 0 until n) res[i] = res[i] / n
        }
        return res
    }

    fun convolution(a: List<Int>, b: List<Int>): List<Int>{
        var n = a.size + b.size - 1
        if(n and (n-1) != 0)
            n = 1 shl bit_size(n)

        val fa = fft(List(n){i -> (if(i < a.size) a[i].toDouble() else .0).toComplex()})
        val fb = fft(List(n){i -> (if(i < b.size) b[i].toDouble() else .0).toComplex()})
        val product = fft(List(n){fa[it] * fb[it]}, -1)
        return product.map{it.re.toInt()}
    }
}

class TestFastFourierTransform{
    @Test
    fun testFastFourierTransform1(){
        val f = FastFourierTransform()
        assertEquals(listOf(3, 17, 10 , 0), f.convolution(listOf(3,2), listOf(1,5)))
    }
}