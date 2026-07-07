package com.github.grishberg.cad3d.viewer

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.swing.SwingUtilities

class DslEditorBracketMatchingTest {

    @Test
    fun dslEditorNoNpeOnCaretMovement() {
        SwingUtilities.invokeAndWait {
            val textArea = RSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val script = """
frame() {
    motherboard()
    gpu()
}
            """.trimIndent() + "\n"
            textArea.text = script

            for (pos in 0 until script.length) {
                textArea.caretPosition = pos
                Thread.sleep(5)
            }
        }
    }

    @Test
    fun dslEditorCaretMovementBracketsEnabled() {
        SwingUtilities.invokeAndWait {
            val textArea = RSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val script = """
frame() {
    motherboard()
    gpu()
}
            """.trimIndent() + "\n"
            textArea.text = script

            textArea.caretPosition = 0
            assertEquals(0, textArea.caretPosition)

            textArea.caretPosition = 5
            assertEquals(5, textArea.caretPosition)
        }
    }

    @Test
    fun typeSymbolBySymbolAllLinesMatch() {
        SwingUtilities.invokeAndWait {
            val textArea = SafeRSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val lines = listOf(
                "frame() {",
                "    motherboard()",
                "    gpu()",
                "    radiator()",
                "    move(0 0 363.5) rotate(0 0 90)",
                "}"
            )
            val fullExpected = lines.joinToString("\n") + "\n"
            val built = StringBuilder()

            for (line in lines) {
                for (ch in line) {
                    val pos = built.length
                    textArea.document.insertString(pos, ch.toString(), null)
                    built.append(ch)
                }
                val pos = built.length
                textArea.document.insertString(pos, "\n", null)
                built.append("\n")
            }

            assertEquals(fullExpected, textArea.text)
            assertEquals(fullExpected.length, textArea.text.length)
        }
    }

    @Test
    fun typeCharacterByCharacterOneLine() {
        SwingUtilities.invokeAndWait {
            val textArea = SafeRSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val text = "move(0 0 363.5) rotate(0 0 90) radiator()\n"
            val built = StringBuilder()

            for (ch in text) {
                val pos = built.length
                textArea.document.insertString(pos, ch.toString(), null)
                built.append(ch)
            }

            assertEquals(text, textArea.text)
            assertEquals(text.length, textArea.text.length)
        }
    }

    @Test
    fun typeLinesWithEdits() {
        SwingUtilities.invokeAndWait {
            val textArea = SafeRSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val text = "frame() {\n    motherboard()\n    gpu()\n}\n"
            val built = StringBuilder()

            for (ch in text) {
                val pos = built.length
                textArea.document.insertString(pos, ch.toString(), null)
                built.append(ch)
            }

            assertEquals(text, textArea.text)

            textArea.caretPosition = "frame() {\n    ".length
            for (ch in listOf('X', 'Y', 'Z')) {
                val pos = textArea.caretPosition
                textArea.document.insertString(pos, ch.toString(), null)
                // caret was at insert pos, after insert it moves past inserted char
                textArea.caretPosition = pos + 1
            }

            val expected = "frame() {\n    XYZmotherboard()\n    gpu()\n}\n"
            assertEquals(expected, textArea.text)
        }
    }

    @Test
    fun typeBracketsSymbolBySymbol() {
        SwingUtilities.invokeAndWait {
            val textArea = SafeRSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            // Simulate typing a script with brackets character by character
            val script = "frame() {\n    gpu()\n}\n"
            val built = StringBuilder()

            for (ch in script) {
                val pos = built.length
                try {
                    textArea.document.insertString(pos, ch.toString(), null)
                } catch (e: Exception) {
                    throw AssertionError(
                        "NPE at pos=$pos after building '${built}' inserting '$ch'",
                        e
                    )
                }
                built.append(ch)
            }

            assertEquals(script, textArea.text)
        }
    }

    @Test
    fun typeThenBackspaceSymbolBySymbol() {
        SwingUtilities.invokeAndWait {
            val textArea = SafeRSyntaxTextArea(10, 40)
            DslTokenMaker.register()
            textArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE

            val text = "gpu()\n"
            val built = StringBuilder()

            for (ch in text) {
                val pos = built.length
                textArea.document.insertString(pos, ch.toString(), null)
                built.append(ch)
            }

            // Now backspace each char
            for (i in text.indices.reversed()) {
                textArea.document.remove(i, 1)
                built.deleteCharAt(i)
                assertEquals(built.toString(), textArea.text)
            }

            assertEquals("", textArea.text.trim())
        }
    }
}
