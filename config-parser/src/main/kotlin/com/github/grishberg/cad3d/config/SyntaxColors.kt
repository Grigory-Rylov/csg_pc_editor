package com.github.grishberg.cad3d.config

import java.io.File
import java.util.Properties

data class SyntaxColors(
    val comment: Int = 0x6A9955,
    val keyword: Int = 0x569CD6,
    val dataType: Int = 0x4EC9B0,
    val function: Int = 0xDCDCAA,
    val number: Int = 0xB5CEA8,
    val separator: Int = 0xD4D4D4,
    val identifier: Int = 0xD4D4D4,
    val background: Int = 0x1E1E1E,
    val foreground: Int = 0xD4D4D4,
    val currentLine: Int = 0x2D2D2D,
    val gutterBackground: Int = 0x252526,
    val gutterForeground: Int = 0x858585
) {
    companion object {
        private const val CONFIG_FILE = "syntax-colors.properties"

        fun load(): SyntaxColors {
            val file = File(CONFIG_FILE)
            if (!file.exists()) return SyntaxColors()

            val props = Properties()
            file.inputStream().use { props.load(it) }
            return SyntaxColors(
                comment = decodeColor(props.getProperty("comment")) ?: 0x6A9955,
                keyword = decodeColor(props.getProperty("keyword")) ?: 0x569CD6,
                dataType = decodeColor(props.getProperty("dataType")) ?: 0x4EC9B0,
                function = decodeColor(props.getProperty("function")) ?: 0xDCDCAA,
                number = decodeColor(props.getProperty("number")) ?: 0xB5CEA8,
                separator = decodeColor(props.getProperty("separator")) ?: 0xD4D4D4,
                identifier = decodeColor(props.getProperty("identifier")) ?: 0xD4D4D4,
                background = decodeColor(props.getProperty("background")) ?: 0x1E1E1E,
                foreground = decodeColor(props.getProperty("foreground")) ?: 0xD4D4D4,
                currentLine = decodeColor(props.getProperty("currentLine")) ?: 0x2D2D2D,
                gutterBackground = decodeColor(props.getProperty("gutterBackground")) ?: 0x252526,
                gutterForeground = decodeColor(props.getProperty("gutterForeground")) ?: 0x858585
            )
        }

        private fun decodeColor(hex: String?): Int? {
            if (hex == null) return null
            val clean = hex.removePrefix("0x").removePrefix("#")
            return clean.toIntOrNull(16)
        }
    }
}

