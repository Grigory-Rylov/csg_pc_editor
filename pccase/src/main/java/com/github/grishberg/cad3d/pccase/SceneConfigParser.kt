package com.github.grishberg.cad3d.pccase

class SceneConfigParser {

    private val componentTypes = setOf("motherboard", "gpu", "psu", "cooler")
    private data class FrameResult(val w: Double, val d: Double, val h: Double, val levels: List<Double>)

    fun parse(script: String): Result<SceneConfig> {
        return try {
            val frame = mutableListOf<FrameResult>()
            val components = mutableListOf<ComponentPlacement>()

            for (line in script.lineSequence()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                val tokens = trimmed.split(Regex("\\s+"))
                val command = tokens[0].lowercase()

                when (command) {
                    "frame" -> frame.add(parseFrame(tokens, trimmed))
                    else -> parseComponent(command, tokens, components, trimmed)
                }
            }

            if (frame.size != 1) {
                return Result.failure(ParseError("frame is required"))
            }
            val fr = frame[0]

            if (components.isEmpty()) {
                return Result.failure(ParseError("at least one component is required"))
            }

            Result.success(SceneConfig(
                frameWidth = fr.w,
                frameDepth = fr.d,
                frameHeight = fr.h,
                frameLevels = fr.levels,
                components = components
            ))
        } catch (e: NumberFormatException) {
            Result.failure(ParseError("invalid number: ${e.message}"))
        }
    }

    private fun parseFrame(tokens: List<String>, line: String): FrameResult {
        val parenContent = extractParenContent(tokens, line)
        if (parenContent != null) {
            val w = getNamedParam(parenContent, "w", line)
            val d = getNamedParam(parenContent, "d", line)
            val h = getNamedParam(parenContent, "h", line)
            val levels = getNamedParamListDouble(parenContent, "l", line)
            return FrameResult(w, d, h, levels)
        }

        // Legacy: frame 530 330 350
        if (tokens.size < 4) {
            throw ParseError("need: name x y z", line)
        }
        return FrameResult(tokens[1].toDouble(), tokens[2].toDouble(), tokens[3].toDouble(), emptyList())
    }

    private fun parseComponent(type: String, tokens: List<String>, components: MutableList<ComponentPlacement>, line: String) {
        if (!componentTypes.contains(type)) {
            throw ParseError("unknown: $type", line)
        }

        val parenContent = extractParenContent(tokens, line)
        if (parenContent != null) {
            val x = getNamedParamDouble(parenContent, "x", line)
            val y = getNamedParamDouble(parenContent, "y", line)
            val z = getNamedParamDouble(parenContent, "z", line)
            val count = getNamedParamInt(parenContent, "n", line, 1)
            val rotation = getNamedParamDouble(parenContent, "r", line, 0.0)
            val spacing = getNamedParamDouble(parenContent, "s", line, 50.0)
            components.add(ComponentPlacement(type, x, y, z, count, rotation, spacing))
            return
        }

        // Legacy: type x y z [n count] [r rotate] [s spacing]
        if (tokens.size < 4) {
            throw ParseError("component requires: type x y z", line)
        }
        val x = tokens[1].toDouble()
        val y = tokens[2].toDouble()
        val z = tokens[3].toDouble()
        var count = 1
        var rotation = 0.0
        var spacing = 50.0
        var i = 4
        while (i < tokens.size) {
            when (tokens[i].lowercase()) {
                "n", "count" -> {
                    i++
                    if (i >= tokens.size) throw ParseError("count requires a value", line)
                    count = tokens[i].toInt()
                }
                "r", "rotate" -> {
                    i++
                    if (i >= tokens.size) throw ParseError("rotate requires a value", line)
                    rotation = tokens[i].toDouble()
                }
                "s", "spacing" -> {
                    i++
                    if (i >= tokens.size) throw ParseError("spacing requires a value", line)
                    spacing = tokens[i].toDouble()
                }
            }
            i++
        }
        components.add(ComponentPlacement(type, x, y, z, count, rotation, spacing))
    }

    /** Extract content inside first (...) from the tokens, or null if no parens found. */
    private fun extractParenContent(tokens: List<String>, line: String): Map<String, String>? {
        val full = tokens.joinToString(" ")
        val start = full.indexOf('(')
        if (start < 0) return null
        val end = full.indexOf(')', start)
        if (end < 0) throw ParseError("unclosed parenthesis", line)
        val inside = full.substring(start + 1, end)
        val params = mutableMapOf<String, String>()
        for (part in inside.split(Regex("\\s+"))) {
            val sepIdx = part.indexOf('=')
            if (sepIdx > 0 && sepIdx < part.length - 1) {
                params[part.substring(0, sepIdx).lowercase()] = part.substring(sepIdx + 1)
            }
        }
        return params
    }

    private fun getNamedParam(params: Map<String, String>, key: String, line: String): Double {
        val v = params[key]
            ?: throw ParseError("missing param: $key", line)
        return v.toDouble()
    }

    private fun getNamedParamDouble(params: Map<String, String>, key: String, line: String, default: Double = 0.0): Double {
        return params[key]?.toDouble() ?: default
    }

    private fun getNamedParamInt(params: Map<String, String>, key: String, line: String, default: Int = 1): Int {
        return params[key]?.toInt() ?: default
    }

    private fun getNamedParamListDouble(params: Map<String, String>, key: String, line: String): List<Double> {
        val v = params[key] ?: return emptyList()
        return v.split(Regex("\\s+")).map { it.toDouble() }
    }

    fun getDefaultScript(): String {
        return """# PC Case Configuration
frame (w=530 d=330 h=350 l=140)
motherboard (x=90 z=20.8)
gpu (x=0 z=100 n=5 s=55)
psu (x=-240 y=95 r=90)
psu (x=-240 y=-95 r=90)
cooler (x=65 y=-20 z=7)""".trimIndent()
    }

    class ParseError(override val message: String, val line: String? = null) : Exception(message)
}
