package com.github.grishberg.cad3d.viewer

import com.github.grishberg.cad3d.config.SyntaxColors
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Style
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenMap
import org.fife.ui.rsyntaxtextarea.TokenTypes
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory
import java.awt.Color
import javax.swing.text.Segment

class DslTokenMaker(private val colors: SyntaxColors = SyntaxColors()) : AbstractTokenMaker() {

    companion object {
        const val SYNTAX_STYLE = "text/dsl"

        fun register() {
            val factory = TokenMakerFactory.getDefaultInstance()
            if (factory is AbstractTokenMakerFactory) {
                factory.putMapping(SYNTAX_STYLE, DslTokenMaker::class.java.name)
            } else {
                System.err.println("Warning: could not register DslTokenMaker")
            }
        }
    }

    override fun getWordsToHighlight(): TokenMap {
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
        return map
    }

    override fun getTokenList(text: Segment, initialTokenType: Int, startOffset: Int): Token {
        resetTokenList()

        val array = text.array
        val end = text.offset + text.count
        var i = text.offset

        while (i < end) {
            val ch = array[i]

            when {
                ch == '#' -> {
                    val start = i
                    while (i < end && array[i] != '\n' && array[i] != '\r') i++
                    addToken(array, start, i - 1, TokenTypes.COMMENT_EOL, startOffset)
                }

                ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == '=' -> {
                    addToken(array, i, i, TokenTypes.SEPARATOR, startOffset)
                    i++
                }

                ch == '-' && i + 1 < end && array[i + 1].isDigit() -> {
                    val start = i
                    i++
                    while (i < end && (array[i].isDigit() || array[i] == '.')) i++
                    addToken(array, start, i - 1, TokenTypes.LITERAL_NUMBER_FLOAT, startOffset)
                }

                ch.isDigit() -> {
                    val start = i
                    while (i < end && (array[i].isDigit() || array[i] == '.')) i++
                    addToken(array, start, i - 1, TokenTypes.LITERAL_NUMBER_FLOAT, startOffset)
                }

                ch.isLetter() || ch == '_' -> {
                    val start = i
                    while (i < end && (array[i].isLetterOrDigit() || array[i] == '_')) i++
                    val existingType = getWordsToHighlight().get(array, start, i - 1)
                    val type = if (existingType != 0) existingType else TokenTypes.IDENTIFIER
                    addToken(array, start, i - 1, type, startOffset)
                }

                ch == ' ' || ch == '\t' -> {
                    val start = i
                    while (i < end && (array[i] == ' ' || array[i] == '\t')) i++
                    addToken(array, start, i - 1, TokenTypes.WHITESPACE, startOffset)
                }

                ch == '\n' || ch == '\r' -> {
                    addToken(array, i, i, TokenTypes.NULL, startOffset)
                    i++
                    if (ch == '\r' && i < end && array[i] == '\n') i++
                }

                else -> {
                    addToken(array, i, i, TokenTypes.IDENTIFIER, startOffset)
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
