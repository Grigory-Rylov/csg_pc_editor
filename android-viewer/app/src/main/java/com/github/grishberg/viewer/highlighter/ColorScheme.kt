package com.github.grishberg.viewer.highlighter

import android.text.style.CharacterStyle

/**
 * Defines styles for each rule type.
 * Copied from KodeHighlighter (MIT license).
 */
interface ColorScheme {
    fun getStyles(type: LanguageRule): Set<StyleFactory>
}
