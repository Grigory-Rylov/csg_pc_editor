package com.github.grishberg.viewer.highlighter

/**
 * Helper to find regex matches in text.
 * Copied from KodeHighlighter (MIT license).
 */
object RuleHelper {
    fun findRegexMatches(text: CharSequence, regex: Regex): List<RuleMatch> {
        return regex.findAll(text).map {
            RuleMatch(it.range.first, it.range.last + 1)
        }.toList()
    }
}
