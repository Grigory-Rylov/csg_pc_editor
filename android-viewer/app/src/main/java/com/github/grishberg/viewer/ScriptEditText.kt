package com.github.grishberg.viewer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.widget.EditText
import com.github.grishberg.viewer.highlighter.SpannableHighlighter

class ScriptEditText : EditText {

    companion object {
        private const val DEBOUNCE_MS = 200L
    }

    private var highlighting = false
    private val handler = Handler(Looper.getMainLooper())
    private val highlighter = SpannableHighlighter(DslRuleBook(), DslColorScheme())
    private var runnable: Runnable? = null

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs, android.R.attr.editTextStyle) { init() }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) { scheduleHighlight(editable) }
        })
    }

    private fun scheduleHighlight(editable: Editable?) {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { applyHighlight(editable) }
        handler.postDelayed(runnable!!, DEBOUNCE_MS)
    }

    private fun applyHighlight(editable: Editable?) {
        if (highlighting || editable == null || editable.isEmpty()) return
        highlighting = true

        // Remove only our spans (ForegroundColorSpan)
        for (span in editable.getSpans(0, editable.length, CharacterStyle::class.java)) {
            editable.removeSpan(span)
        }

        highlighter.highlight(editable)
        highlighting = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        runnable?.let { handler.removeCallbacks(it) }
    }
}
