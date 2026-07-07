package com.github.grishberg.cad3d.config

class Lexer(private val input: String) {
    private var pos = 0
    private val tokens = mutableListOf<Token>()

    fun tokenize(): List<Token> {
        while (pos < input.length) {
            val c = input[pos]

            when {
                c == '#' -> skipComment()
                c == '/' && peek() == '/' -> skipLine()
                c == '\n' -> {
                    tokens.add(Token.Newline)
                    pos++
                }
                c.isWhitespace() -> pos++
                c == '(' -> { tokens.add(Token.LParen); pos++ }
                c == ')' -> { tokens.add(Token.RParen); pos++ }
                c == '{' -> { tokens.add(Token.LCurly); pos++ }
                c == '}' -> { tokens.add(Token.RCurly); pos++ }
                c == '=' -> { tokens.add(Token.Equals); pos++ }
                c == ';' -> { tokens.add(Token.Semicolon); pos++ }
                c == '-' && peek().isDigit() -> tokens.add(readNumber(negative = true))
                c.isDigit() -> tokens.add(readNumber())
                c.isLetter() || c == '_' -> tokens.add(readIdentifier())
                else -> error("unexpected character '$c' at position $pos")
            }
        }
        tokens.add(Token.Eof)
        return tokens
    }

    private fun peek(): Char = if (pos + 1 < input.length) input[pos + 1] else '\u0000'

    private fun skipComment() {
        while (pos < input.length && input[pos] != '\n') pos++
    }

    private fun skipLine() {
        while (pos < input.length && input[pos] != '\n') pos++
    }

    private fun readNumber(negative: Boolean = false): Token.Number {
        val start = pos
        if (negative) pos++
        while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
        val value = input.substring(start, pos).toDouble()
        return Token.Number(value)
    }

    private fun readIdentifier(): Token.Identifier {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
        return Token.Identifier(input.substring(start, pos))
    }
}
