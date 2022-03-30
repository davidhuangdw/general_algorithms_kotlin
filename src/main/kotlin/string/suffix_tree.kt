package string

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTree(str: String){
    class Node(var a: Int, var b: Int){
        var n = hashMapOf<Char, Node>()
        var suf_link: Node? = null
        var cnt = 1
    }
    //input
    val str = "$str$"
    val n = str.length

    // constant
    val root = Node(0, 0)

    // state
    var active = root  // root or internal_node
    var edge = '$'  // no_need when walked == 0; edge might != str[i-remainder+1], because might cross multiple internal nodes
    var walked = 0 // walked might != remainder-1, because might cross internal nodes
    var remainder = 0

    init {
        build()
        count(root)
        debug(root, 0, n)
    }

    fun debug(x: Node, dep: Int, ed: Int){
        val sep = "----"
        println(listOf(sep.repeat(dep), "(", str.substring(x.a, minOf(x.b, ed+1)), ")", x.cnt).joinToString(""))
        for((ch, y) in x.n){
            print(ch)
            debug(y, dep+1, ed)
        }
        if(x == root) {
            println("$ed:${str.substring(ed+1-walked,ed+1)}:${active==root}:$walked" + "=".repeat(20))
        }
    }

    fun count(x: Node): Int {
        x.cnt = 0
        for((_, y) in x.n){
            x.cnt += count(y)
        }
        x.cnt = maxOf(1, x.cnt)
        return x.cnt
    }

    // assume:
    //  current suffix == [i-remainder+1, i] == [root -...-> active --> active.a+walked)
    //  active.a + walked < active.b
    //  warning: not continuous, pre.b could != nxt.a
    //  when walked: >0 -- implicit node in the middle, so active.n[edge] must exist; ==0 -- still current active
    //  when active.n[edge] exist: walked could be 0
    fun build(){
        for((i, ch) in str.withIndex()){
            remainder ++
            var prev_middle: Node? = null
            while(remainder > 0) {
                if(walked == 0) edge = ch   // don't forget
                if (edge in active.n) {
                    val nxt = active.n[edge]!!
                    val st = nxt.a + walked
                    if (str[st] == ch) {
                        walked++
                        if (nxt.a + walked == nxt.b) {
                            active = nxt
                            walked = 0
                        }
                        break
                    }
                }

                if(walked > 0){
                    val nxt = active.n[edge]!!  // edge must exist when walked > 0
                    val st = nxt.a + walked
                    val middle = Node(nxt.a, st)
                    nxt.a = st
                    middle.n[str[st]] = nxt
                    active.n[edge] = middle

                    if(prev_middle != null)
                        prev_middle.suf_link = middle
                    prev_middle = middle

                    active = middle
                    walked = 0
                }
                active.n[ch] = Node(i, n)   // new node just start from i

                remainder --
                if(active.suf_link != null){
                    active = active.suf_link!!
                }else{
                    active = root
                    if(remainder - 1 > 0){ // !!! important
                        walked = remainder - 1
                        edge = str[i - walked]
                    }
                }
//                debug(root, 0, i)
            }
            debug(root, 0, i)
        }
    }

    fun find(p: String): Pair<Int, Int>{
        var ed = root
        var d = 0
        for(ch in p){
            if(ed.a + d == ed.b){
                if(ch !in ed.n)
                    return -1 to 0
                ed = ed.n[ch]!!
                d = 0
            }
            if(str[ed.a + d] != ch)
                return -1 to 0
            d ++
        }
        return ed.a+d - p.length to ed.cnt
    }
}


class TestSuffixTree{
    fun getCount(s: String, p: String): Int {
        var cnt = 0
        for(i in 0..s.length - p.length){
            var ok = true
            for((j, ch) in p.withIndex())
                if(s[i+j] != ch){
                    ok = false
                    break
                }
            if(ok)
                cnt ++
        }
        return cnt
    }

    @Test
    fun testSuffixTree(){
        val s = "bananasnaan"
        val n = s.length
        val ps = hashSetOf("bak")
        for(i in 0 until n)
            for(j in i until n){
                ps.add(s.substring(i, j+1))
            }

        val tree = SuffixTree(s)
        for(p in ps){
            val expected_count = getCount(s, p)
            val (i, cnt) = tree.find(p)
            var sub = if(i >= 0)tree.str.substring(i) else ""
            assertEquals(expected_count, cnt)
            println("$p : $expected_count : $cnt: $sub")
        }
    }
}