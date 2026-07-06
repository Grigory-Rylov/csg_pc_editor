package com.github.grishberg.viewer

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.github.grishberg.viewer.highlighter.ColorScheme
import com.github.grishberg.viewer.highlighter.LanguageRule
import com.github.grishberg.viewer.highlighter.LanguageRuleBook
import com.github.grishberg.viewer.highlighter.RuleHelper
import com.github.grishberg.viewer.highlighter.RuleMatch
import com.github.grishberg.viewer.highlighter.StyleFactory

object DslCommentRule : LanguageRule {
    override fun findMatches(text: CharSequence): List<RuleMatch> {
        val matches = mutableListOf<RuleMatch>()
        var lineStart = 0
        for (line in text.split("\n")) {
            val trimmed = line.trimStart()
            if (trimmed.startsWith("#")) {
                matches.add(RuleMatch(lineStart, lineStart + line.length))
            }
            lineStart += line.length + 1
        }
        return matches
    }
}

object DslKeywordRule : LanguageRule {
    private val KEYWORDS = Regex("""(?im)\b(frame|motherboard|gpu|psu|cooler)\b""")
    override fun findMatches(text: CharSequence): List<RuleMatch> {
        return RuleHelper.findRegexMatches(text, KEYWORDS)
    }
}

object DslParamRule : LanguageRule {
    private val PARAMS = Regex("""(?im)(\w+)(?==)""")
    override fun findMatches(text: CharSequence): List<RuleMatch> {
        return RuleHelper.findRegexMatches(text, PARAMS)
    }
}

object DslNumberRule : LanguageRule {
    private val NUMBERS = Regex("-?\\d+\\.?\\d*")
    override fun findMatches(text: CharSequence): List<RuleMatch> {
        return RuleHelper.findRegexMatches(text, NUMBERS)
    }
}

class DslRuleBook : LanguageRuleBook {
    override fun getRules() = listOf(DslCommentRule, DslKeywordRule, DslParamRule, DslNumberRule)
}

class DslColorScheme : ColorScheme {
    override fun getStyles(type: LanguageRule): Set<StyleFactory> {
        return when (type) {
            DslParamRule -> setOf { ForegroundColorSpan(Color.parseColor("#9CDCFE")) }
            DslCommentRule -> setOf { ForegroundColorSpan(Color.parseColor("#6A9955")) }
            DslKeywordRule -> setOf { ForegroundColorSpan(Color.parseColor("#569CD6")) }
            DslNumberRule -> setOf { ForegroundColorSpan(Color.parseColor("#B5CEA8")) }
            else -> emptySet()
        }
    }
}
