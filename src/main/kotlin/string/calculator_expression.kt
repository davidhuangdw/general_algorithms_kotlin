package string

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class Calculator{
    companion object{
        val TO_UNARY_OPS = hashMapOf(
            '-' to 'n',
            '+' to 'p',
        )
        val UNARY_OPS = TO_UNARY_OPS.values.toHashSet()
        val PRIORITY = hashMapOf(
            '+' to 0,
            '-' to 0,
            '*' to 1,
            '/' to 1,
//            '#' to 2,
            'n' to 100,   // negative: unary '-' op
            'p' to 100,   // negative: unary '+' op
            '(' to -1,
        )
    }
    private fun calc(a: Long?=null, b: Long, op: Char): Long{
        return when(op){
            '+' -> a!!+b
            '-' -> a!!-b
            '*' -> a!!*b
            '/' -> a!!/b
            'n' -> -b
            'p' -> b
            '(' -> throw RuntimeException("unmatched '('")
            else -> throw RuntimeException("unexpected operator: $op")
        }
    }
    fun computeByConvert(exp: String): Long{
        val st = mutableListOf<Long>()
        val suf = convertToSuffixFormat(exp)
        for(s in suf){
            if(s[0].isDigit()){
                st.add(s.toLong())
            } else{
                val b = st.removeLast()
                val a = st.removeLast()
                st.add(calc(a, b, s[0]))
            }
        }
        assert(st.size == 1)
        return st[0]
    }

    fun convertToSuffixFormat(e: String): List<String>{
        val ops = mutableListOf<Char>()
        val res = mutableListOf<String>()
        var i = 0
        while(i < e.length){
            if(e[i].isDigit()){
                val j = i
                while(i < e.length && e[i].isDigit()) i+=1
                res.add(e.substring(j, i))
                continue
            }
            when(e[i]){
                ' ' -> {}
                '(' -> ops.add(e[i])
                ')' -> {
                    while(ops.last() != '('){
                        res.add(ops.removeLast().toString())
                    }
                    ops.removeLast()
                }
                else -> {
                    // compute previous greater_or_equal ops
                    // ops: ascending order of ops inside each '(' split section
                    while(ops.isNotEmpty() && PRIORITY[ops.last()]!! >= PRIORITY[e[i]]!!){
                        res.add(ops.removeLast().toString())
                    }
                    ops.add(e[i])
                }
            }
            i += 1
        }
        while(ops.isNotEmpty()){
            res.add(ops.removeLast().toString())
        }
        return res
    }

    fun compute(e: String): Long{
        val values = mutableListOf<Long>()
        val ops = mutableListOf<Char>()
        fun calcLast(op: Char){
            val b = values.removeLast()
            val a = if(op in UNARY_OPS) null else values.removeLast()
            values.add(calc(a, b, op))
        }
        var digits = mutableListOf<Char>()
        var lastIsOp = false
        for((i, chh) in e.withIndex()){
            var ch = chh
            if(ch.isWhitespace()) continue
            if(!ch.isDigit() && digits.isNotEmpty()){
                values.add(digits.joinToString("").toLong())
                digits.clear()
            }
            when{
                ch.isDigit() -> digits.add(ch)
                ch == '(' -> ops.add(ch)
                ch == ')' -> {
                    while(ops.isNotEmpty() && ops.last() != '('){
                        calcLast(ops.removeLast())
                    }
                    if(ops.isEmpty())
                        throw RuntimeException("unpaired ')' at ${e.substring(i)}")
                    ops.removeLast()
                }
                else -> {
                    if(i==0 || lastIsOp){
                        if(ch !in TO_UNARY_OPS)
                            throw RuntimeException("missing value before operators: ${e.substring(i)}")
                        ch = TO_UNARY_OPS[ch]!!
                    }
                    while(ops.isNotEmpty() && PRIORITY[ops.last()]!! >= PRIORITY[ch]!!){
                        if(ops.last() in UNARY_OPS && PRIORITY[ops.last()] == PRIORITY[ch])
                            break       // UNARY OPS is right-associate
                        calcLast(ops.removeLast())
                    }
                    ops.add(ch)
                }
            }
            lastIsOp = ch in PRIORITY.keys
        }
        while(ops.isNotEmpty()){
            calcLast(ops.removeLast())
        }
        assert(values.size  == 1)
        return values[0]
    }
}

class TestCalculator{
    @Test
    fun test1(){
        val exp = "1 +4 *3 /6 + 3+(5+6)*10*(1+1)"
        val cal = Calculator()
        println(cal.convertToSuffixFormat(exp))
        assertEquals(226, cal.computeByConvert(exp))
        assertEquals(226, cal.compute(exp))
    }

    @Test
    fun test2() {
        val exp = "---1 + 4*3/--6 + -3 + (-(5+6)) * -10 * (1+1)"
        val cal = Calculator()
        assertEquals(218, cal.compute(exp))
    }

    @Test
    fun test3(){
        val cal = Calculator()
        assertThrows<RuntimeException> { // illegal multiple ops '+*'
            cal.compute("1 +4 *3 /6 + *3+(5+6)*10*(1+1)")
        }
        assertThrows<RuntimeException> { // extra ')'
            cal.compute("1 +4 *3 /6 + *3+(5+6))*10*(1+1)")
        }
        assertThrows<RuntimeException> {    // extra '('
            println(cal.compute("1 +4 *3 /6 + (3+(5+6)*10*(1+1)"))
        }
    }
}
