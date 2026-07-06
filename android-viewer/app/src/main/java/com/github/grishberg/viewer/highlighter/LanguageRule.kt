package com.github.grishberg.viewer.highlighter

/**
 * Interface for a single language rule.
 * Copied from KodeHighlighter (MIT license).
 */
interface LanguageRule {
    fun findMatches(text: CharSequence): List<RuleMatch>
}
