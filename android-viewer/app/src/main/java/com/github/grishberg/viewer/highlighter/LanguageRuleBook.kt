package com.github.grishberg.viewer.highlighter

import android.text.Spannable
import android.text.style.CharacterStyle

/**
 * A function that creates a style that can be applied to a Spannable.
 */
typealias StyleFactory = () -> CharacterStyle

/**
 * Interface for a language rule book.
 * Copied from KodeHighlighter (MIT license).
 */
interface LanguageRuleBook {
    fun getRules(): List<LanguageRule>
}
