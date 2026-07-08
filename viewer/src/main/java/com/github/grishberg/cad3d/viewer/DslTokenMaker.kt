package com.github.grishberg.cad3d.viewer

import com.github.grishberg.cad3d.config.SyntaxColors
import org.fife.ui.rsyntaxtextarea.*
import java.awt.Color
import javax.swing.text.Segment

class DslTokenMaker(private val colors: SyntaxColors = SyntaxColors()) : AbstractTokenMaker() {

    companion object {
        const val SYNTAX_STYLE = "text/dsl"

        fun register() {
            val factory = TokenMakerFactory.getDefaultInstance()
            if (factory is AbstractTokenMakerFactory) {
                factory.putMapping(SYNTAX_STYLE, "com.github.grishberg.cad3d.viewer.DslTokenMaker")
            } else {
                System.err.println("Warning: could not register DslTokenMaker")
            }
        }
    }

    private val words = run {
        val map = TokenMap(true)
        map.put("frame", TokenTypes.RESERVED_WORD)
        map.put("motherboard", TokenTypes.DATA_TYPE)
        map.put("gpu", TokenTypes.DATA_TYPE)
        map.put("psu", TokenTypes.DATA_TYPE)
        map.put("cooler", TokenTypes.DATA_TYPE)
        map.put("radiator", TokenTypes.DATA_TYPE)
        map.put("move", TokenTypes.FUNCTION)
        map.put("rotate", TokenTypes.FUNCTION)
        map.put("bottomEdge", TokenTypes.FUNCTION)
        map
    }

    override fun getWordsToHighlight(): TokenMap = words

    override fun getTokenList(text: Segment, initialTokenType: Int, startOffset: Int): Token {
        resetTokenList()

        val array = text.array
        val textCount = text.count
        val segmentOffset = text.offset
        val end = segmentOffset + textCount

        if (textCount == 0) {
            addNullToken()
            return firstToken
        }

        var i = segmentOffset
        while (i < end) {
            val ch = array[i]

            when {
                ch == '#' -> {
                    val start = i
                    while (i < end && array[i] != '\n' && array[i] != '\r') i++
                    addToken(array, start, i - 1, TokenTypes.COMMENT_EOL, startOffset + start - segmentOffset)
                }

                ch == '(' || ch == ')' || ch == '{' || ch == '}' ||
                        ch == '[' || ch == ']' || ch == '=' -> {
                    addToken(array, i, i, TokenTypes.SEPARATOR, startOffset + i - segmentOffset)
                    i++
                }

                ch == '-' && i + 1 < end && array[i + 1].isDigit() -> {
                    val start = i
                    i++
                    while (i < end && (array[i].isDigit() || array[i] == '.')) i++
                    addToken(array, start, i - 1, TokenTypes.LITERAL_NUMBER_FLOAT, startOffset + start - segmentOffset)
                }

                ch.isDigit() -> {
                    val start = i
                    while (i < end && (array[i].isDigit() || array[i] == '.')) i++
                    addToken(array, start, i - 1, TokenTypes.LITERAL_NUMBER_FLOAT, startOffset + start - segmentOffset)
                }

                ch.isLetter() || ch == '_' -> {
                    val start = i
                    while (i < end && (array[i].isLetterOrDigit() || array[i] == '_')) i++
                    val existingType = words.get(array, start, i - 1)
                    val type = if (existingType > 0) existingType else TokenTypes.IDENTIFIER
                    addToken(array, start, i - 1, type, startOffset + start - segmentOffset)
                }

                ch == ' ' || ch == '\t' -> {
                    val start = i
                    while (i < end && (array[i] == ' ' || array[i] == '\t')) i++
                    addToken(array, start, i - 1, TokenTypes.WHITESPACE, startOffset + start - segmentOffset)
                }

                ch == '\n' || ch == '\r' -> {
                    val pos = i
                    i++
                    if (ch == '\r' && i < end && array[i] == '\n') i++
                    addToken(array, pos, i - 1, TokenTypes.WHITESPACE, startOffset + pos - segmentOffset)
                }

                else -> {
                    addToken(array, i, i, TokenTypes.IDENTIFIER, startOffset + i - segmentOffset)
                    i++
                }
            }
        }

        addNullToken()
        return firstToken
    }

    fun applyColors(textArea: RSyntaxTextArea) {
        val defaultFg = Color(colors.foreground)
        val defaultBg = Color(colors.background)
        val scheme = textArea.syntaxScheme
        val count = scheme.styleCount
        for (i in 0 until count) {
            val existing = scheme.getStyle(i)
            if (existing != null) {
                existing.foreground = defaultFg
                existing.background = null
            } else {
                scheme.setStyle(i, Style(defaultFg))
            }
        }
        styleOf(scheme, TokenTypes.COMMENT_EOL)?.foreground = Color(colors.comment)
        styleOf(scheme, TokenTypes.RESERVED_WORD)?.foreground = Color(colors.keyword)
        styleOf(scheme, TokenTypes.DATA_TYPE)?.foreground = Color(colors.dataType)
        styleOf(scheme, TokenTypes.FUNCTION)?.foreground = Color(colors.function)
        styleOf(scheme, TokenTypes.LITERAL_NUMBER_FLOAT)?.foreground = Color(colors.number)
        styleOf(scheme, TokenTypes.LITERAL_NUMBER_DECIMAL_INT)?.foreground = Color(colors.number)
        styleOf(scheme, TokenTypes.SEPARATOR)?.foreground = Color(colors.separator)
        styleOf(scheme, TokenTypes.IDENTIFIER)?.foreground = Color(colors.identifier)
        textArea.background = defaultBg
        textArea.caretColor = Color.WHITE
        textArea.currentLineHighlightColor = Color(colors.currentLine)
    }

    private fun styleOf(scheme: SyntaxScheme, type: Int): Style? {
        var s = scheme.getStyle(type)
        if (s == null) {
            s = Style(Color(colors.foreground))
            scheme.setStyle(type, s)
        }
        return s
    }
}
