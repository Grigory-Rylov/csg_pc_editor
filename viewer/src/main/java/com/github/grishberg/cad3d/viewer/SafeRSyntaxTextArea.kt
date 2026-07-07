package com.github.grishberg.cad3d.viewer

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import javax.swing.event.CaretEvent

class SafeRSyntaxTextArea(rows: Int, cols: Int) : RSyntaxTextArea(rows, cols) {
    override fun fireCaretUpdate(e: CaretEvent?) {
        try {
            super.fireCaretUpdate(e)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
}
