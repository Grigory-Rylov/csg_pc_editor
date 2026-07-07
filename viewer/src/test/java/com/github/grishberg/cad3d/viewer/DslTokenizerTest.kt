package com.github.grishberg.cad3d.viewer

import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenMaker
import org.fife.ui.rsyntaxtextarea.TokenTypes
import org.junit.jupiter.api.Test
import javax.swing.text.Segment
import kotlin.test.assertTrue

class DslTokenizerTest {

    private fun makeTokenizer(): TokenMaker = DslTokenMaker()

    private fun tokenize(text: String, startOffset: Int = 0): Token {
        val seg = Segment(text.toCharArray(), 0, text.length)
        return makeTokenizer().getTokenList(seg, TokenTypes.NULL, startOffset)
    }

    private fun dump(tok: Token): String {
        val parts = mutableListOf<String>()
        var t = tok
        while (true) {
            val lex = t.lexeme ?: "<null>"
            val esc = lex.replace("\n", "\\n").replace("\r", "\\r")
            val p = t.isPaintable
            parts.add("'$esc' type=${t.type} off=${t.offset} paintable=$p")
            val next = t.nextToken ?: break
            t = next
        }
        return parts.joinToString(" -> ")
    }

    @Test
    fun dumpAbc() {
        val t = tokenize("abc", 100)
        println(dump(t))
    }

    @Test
    fun dumpNumbers() {
        val t = tokenize("123 456", 0)
        println(dump(t))
    }

    @Test
    fun dumpSingleChar() {
        val t = tokenize("x", 0)
        println(dump(t))
    }

    @Test
    fun tokenListCoversFullRange() {
        val inputs = listOf(
            "abc",
            "abc 123",
            "frame() {",
            "    motherboard()",
            "move(0 0 363.5)",
            "a.b",
            "a-b",
            "a,b",
            "a~b",
            "a!b@c#d\$e%f^g&h*i(j)k_l+m=n",
            "a  b  c",
            "a\tb\tc",
            "abc\n"
        )
        var allOk = true
        for (input in inputs) {
            val startOffset = 100
            val tok = tokenize(input, startOffset)
            val paintable = mutableListOf<Token>()
            var t = tok
            while (t.isPaintable) {
                paintable.add(t)
                t = t.nextToken ?: break
            }

            val totalChars = paintable.sumOf { it.length() }
            if (totalChars != input.length) {
                allOk = false
                println("FAIL: '$input' -> total=$totalChars, expected=${input.length}")
                println("  " + dump(tok))
            }
        }
        assertTrue(allOk, "Some inputs failed, see stdout")
    }

    @Test
    fun invalidCharsBetweenTokens() {
        val invalidChars = listOf('.', ',', ';', ':', '~', '!', '@', '%', '^', '&', '*', '+', '/', '<', '>', '?')
        val template = "abc<CHAR>def"
        var allOk = true

        for (ch in invalidChars) {
            val input = template.replace("<CHAR>", ch.toString())
            val tok = tokenize(input, 0)
            val paintable = mutableListOf<Token>()
            var t = tok
            while (t.isPaintable) {
                paintable.add(t)
                t = t.nextToken ?: break
            }
            val totalChars = paintable.sumOf { it.length() }
            if (totalChars != input.length) {
                allOk = false
                println("FAIL: '$input' ch='$ch' -> total=$totalChars, expected=${input.length}")
                println("  " + dump(tok))
            }
        }
        assertTrue(allOk, "Some chars failed, see stdout")
    }
}
