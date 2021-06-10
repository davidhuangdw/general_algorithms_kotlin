package math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

object LinearEquation {
    fun gaussReduction(A: List<List<Number>>, Y: List<List<Number>>): List<List<Double>> {   // A*x = Y
        val n = A.size
        val m = A[0].size
        val u = Y[0].size
        assert(n == Y.size)
//        assert(n <= m)

        val C = m + u
        val e = MutableList(n) { i ->
            MutableList(C) { j ->
                if (j < m) A[i][j].toDouble() else Y[i][j - m].toDouble()
            }
        }

        for (i in 0 until minOf(n, m)) {
            val maxLine = (i until n).maxByOrNull { e[it][i].absoluteValue }!!
            e[i] = e[maxLine].also { e[maxLine] = e[i] }
            if (e[i][i] == .0)
                continue

            val rate = e[i][i]
            for (k in 0 until C)
                e[i][k] /= rate

            for (j in 0 until n) {
                if (j == i) continue
                val rate = e[j][i] / e[i][i]
                for (k in i until C)
                    e[j][k] -= e[i][k] * rate
            }
        }
        return List(m) { i ->
            List(u) { j ->
                if (i < n) e[i][m + j] else .0
            }
        }
    }

    fun solve(A: List<List<Number>>, Y: List<Number>) = gaussReduction(A, Y.map { listOf(it) }).map { it[0] }
}

object Matrix {
    fun Unit(n: Int) = List(n) { i -> List(n) { j -> if (i == j) 1 else 0 } }

    fun All(n: Int, v: Number) = List(n) { List(n) { v } }

    fun vectorToMatrix(v: List<Number>) = v.map { listOf(it) }

    fun mul(a: List<List<Number>>, b: List<List<Number>>): List<List<Double>> {
        assert(a[0].size == b.size)
        val (n, m) = a.size to b[0].size
        val res = List(n) { MutableList(m) { .0 } }
        for (i in a.indices)
            for (j in 0 until b[0].size)
                for (k in 0 until a[0].size)
                    res[i][j] += a[i][k].toDouble() * b[k][j].toDouble()
        return res
    }

    fun mulVector(a: List<List<Number>>, v: List<Number>) = mul(a, vectorToMatrix(v)).map { it[0] }

    fun <T : Number> inversion(A: List<List<T>>): List<List<Double>> {
        val n = A.size
        assert(n == A[0].size)
        return LinearEquation.gaussReduction(A, Unit(n))
    }
}


class TestLinearEquation {
    fun assertDoubleListEquals(a: List<Number>, b: List<Number>) {
        assertEquals(a.size, b.size)
        for ((x, y) in a.zip(b))
            assertEquals(x.toDouble(), y.toDouble(), 1e-9)
    }

    fun assertDoubleMatrixEquals(a: List<List<Number>>, b: List<List<Number>>) {
        assert(a.size == b.size)
        for ((x, y) in a.zip(b))
            assertDoubleListEquals(x, y)
    }

    @Test
    fun testLinearEquation1() {
        val eq = LinearEquation
        var y = (1..5).toList()
        assertDoubleListEquals(y.map { it.toDouble() }, eq.solve(Matrix.Unit(5), y))

        assertDoubleListEquals(
            listOf(1.0, .0, .0, .0, .0),
            eq.solve(Matrix.All(5, 1), (1..5).map { 1 })
        )

        var a = listOf(
            listOf(2, 5),
            listOf(-3, -7),
        )
        assertDoubleMatrixEquals(
            listOf(
                listOf(-7.0, -5.0),
                listOf(3.0, 2.0),
            ),
            Matrix.inversion(a)
        )

        y = listOf(3, -4)
        assertDoubleListEquals(
            listOf(-1.0, 1.0),
            eq.solve(a, y)
        )

        assertDoubleListEquals(
            listOf(-1.0, 1.0),
            Matrix.mulVector(Matrix.inversion(a), y)
        )

        a = listOf(
            listOf(2, 5, 100),
            listOf(-3, -7, 200),
        )
        assertDoubleListEquals(
            listOf(-1.0, 1.0, .0),
            eq.solve(a, y)
        )

        a = listOf(
            listOf(1, 2),
            listOf(1, 2),
            listOf(3, 4),
        )
        y = listOf(4, 4, 10)
        assertDoubleListEquals(
            listOf(2, 1),
            eq.solve(a, y)
        )
    }
}
