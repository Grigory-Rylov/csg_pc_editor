package com.github.grishberg.viewer.highlighter

import android.text.Spannable
import android.text.style.CharacterStyle

/**
 * Applies highlighting spans to a Spannable.
 * Adapted from KodeHighlighter (MIT license).
 */
class SpannableHighlighter(
    private val ruleBook: LanguageRuleBook,
    private val colorScheme: ColorScheme
) {
    fun highlight(spannable: Spannable) {
        for (rule in ruleBook.getRules()) {
            val matches = rule.findMatches(spannable)
            val styles = colorScheme.getStyles(rule)
            if (styles.isEmpty()) continue

            for (match in matches) {
                for (styleFactory in styles) {
                    val style = styleFactory()
                    spannable.setSpan(
                        style,
                        match.startIndex,
                        match.endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}
