package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d

class SceneConfigParser {

    private val componentTypes = setOf("motherboard", "gpu", "psu", "cooler", "radiator")
    private val cmdRegex = Regex("""([a-zA-Z_]\w*)\s*(?:\(([^)]*)\))?""")

    private data class FrameResult(
        val w: Double, val d: Double, val h: Double,
        val levels: List<Double>, val bottomBeams: List<Double>
    )

    fun parse(script: String): Result<SceneConfig> {
        return try {
            val lines = script.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()

            val parser = Parser(lines)
            val frame = parser.parseFrame()
            val exprs = parser.parseAll()
            val components = flatten(exprs)

            Result.success(SceneConfig(
                frameWidth = frame.w,
                frameDepth = frame.d,
                frameHeight = frame.h,
                frameLevels = frame.levels,
                frameBottomBeams = frame.bottomBeams,
                components = components
            ))
        } catch (e: NumberFormatException) {
            Result.failure(ParseError("invalid number: ${e.message}"))
        }
    }

    private sealed class Expr {
        data class Comp(val type: String, val count: Int, val spacing: Double) : Expr()
        data class Transform(val op: TransformOp, val children: List<Expr>) : Expr()
    }

    private fun flatten(exprs: List<Expr>): List<ComponentPlacement> {
        val result = mutableListOf<ComponentPlacement>()
        for (expr in exprs) {
            flattenOne(expr, result)
        }
        return result
    }

    private fun flattenOne(expr: Expr, result: MutableList<ComponentPlacement>, outerOps: List<TransformOp> = emptyList()) {
        when (expr) {
            is Expr.Comp -> result.add(ComponentPlacement(expr.type, expr.count, expr.spacing, outerOps))
            is Expr.Transform -> {
                val newOps = outerOps + expr.op
                for (child in expr.children) flattenOne(child, result, newOps)
            }
        }
    }

    private fun extractTokens(line: String): List<Pair<String, String>> {
        return cmdRegex.findAll(line).map { m ->
            m.groupValues[1].lowercase() to (m.groupValues[2].ifBlank { "" })
        }.toList()
    }

        private inner class Parser(private val lines: List<String>) {
        private var index = 0
        private var inBlock = false

        fun parseFrame(): FrameResult {
            while (index < lines.size) {
                val line = lines[index]
                val tokens = extractTokens(line)
                if (tokens.isNotEmpty() && tokens[0].first == "frame") {
                    index++
                    return parseFrameLine(line, tokens[0].second)
                }
                if (line != "}" && line != "{" && !componentTypes.contains(tokens.firstOrNull()?.first) &&
                    tokens.firstOrNull()?.first !in setOf("rotate", "move", "frame")) {
                    throw ParseError("unknown: ${tokens.firstOrNull()?.first ?: line}", line)
                }
                break
            }
            throw ParseError("frame is required")
        }

        fun parseAll(): List<Expr> {
            val result = mutableListOf<Expr>()
            while (index < lines.size) {
                val line = lines[index]
                if (line == "}") { index++; break }
                if (line == "frame") { index++; continue }
                if (line == "") { index++; continue }
                val expr = parseLine()
                if (expr != null) result.add(expr)
            }
            return result
        }

        private fun parseLine(): Expr? {
            val line = lines[index]
            val tokens = extractTokens(line)

            if (tokens.isEmpty()) {
                if (line == "{") {
                    index++
                    val children = mutableListOf<Expr>()
                    while (index < lines.size && lines[index].trim() != "}") {
                        val child = parseLine()
                        if (child != null) children.add(child)
                    }
                    if (index >= lines.size) throw ParseError("unclosed block", line)
                    index++
                    return null
                }
                index++
                return null
            }

            val lastCmd = tokens.last().first
            if (componentTypes.contains(lastCmd)) {
                index++
                return buildExprFromTokens(tokens, line)
            }

            if (lastCmd in setOf("rotate", "move")) {
                index++
                val ops = tokens.map { (cmd, content) ->
                    parseOperation(cmd, content, line)
                }
                inBlock = line.trim().endsWith("{")
                val children = parseArgument(line)
                var result: Expr = Expr.Transform(ops.last(), children)
                for (i in (ops.size - 2) downTo 0) {
                    result = Expr.Transform(ops[i], listOf(result))
                }
                return result
            }

            throw ParseError("unexpected token: $lastCmd", line)
        }

        private fun buildExprFromTokens(tokens: List<Pair<String, String>>, line: String): Expr {
            val lastIdx = tokens.size - 1
            val (compCmd, compContent) = tokens[lastIdx]
            val comp = parseComponent(compCmd, compContent, line)
            if (lastIdx == 0) return comp

            var result: Expr = comp
            for (i in (lastIdx - 1) downTo 0) {
                val (cmd, content) = tokens[i]
                if (cmd !in setOf("rotate", "move")) {
                    throw ParseError("expected rotate/move before component, got: $cmd", line)
                }
                val op = parseOperation(cmd, content, line)
                result = Expr.Transform(op, listOf(result))
            }
            return result
        }

        private fun parseArgument(currentLine: String): List<Expr> {
            if (index >= lines.size) {
                throw ParseError("expected argument after operation", currentLine)
            }
            val line = lines[index]
            if (inBlock || line == "{") {
                inBlock = false
                if (line == "{") index++
                val children = mutableListOf<Expr>()
                while (index < lines.size && lines[index].trim() != "}") {
                    val child = parseLine()
                    if (child != null) children.add(child)
                }
                if (index >= lines.size) throw ParseError("unclosed block", currentLine)
                index++
                return children
            }
            val expr = parseLine()
            return if (expr != null) listOf(expr) else emptyList()
        }

        private fun parseOperation(cmd: String, content: String, line: String): TransformOp {
            if (content.contains('=')) {
                val params = parseNamedParams(content)
                val x = params["x"]?.toDouble() ?: 0.0
                val y = params["y"]?.toDouble() ?: 0.0
                val z = params["z"]?.toDouble() ?: 0.0
                return when (cmd) {
                    "rotate" -> TransformOp.Rotate(Angles3d(x, y, z))
                    "move" -> TransformOp.Move(x, y, z)
                    else -> throw ParseError("unknown operation: $cmd", line)
                }
            }
            val values = content.split(Regex("\\s+")).filter { it.isNotEmpty() }.map { it.toDouble() }
            if (values.size < 3) throw ParseError("$cmd needs 3 values, got: '$content'", line)
            return when (cmd) {
                "rotate" -> TransformOp.Rotate(Angles3d(values[0], values[1], values[2]))
                "move" -> TransformOp.Move(values[0], values[1], values[2])
                else -> throw ParseError("unknown operation: $cmd", line)
            }
        }

        private fun parseComponent(type: String, content: String, line: String): Expr.Comp {
            if (!componentTypes.contains(type)) throw ParseError("unknown: $type", line)
            if (content.isBlank()) return Expr.Comp(type, 1, 50.0)
            val params = parseNamedParams(content)
            val count = params["n"]?.toInt() ?: 1
            val spacing = params["s"]?.toDouble() ?: 50.0
            return Expr.Comp(type, count, spacing)
        }

        private fun parseNamedParams(content: String): Map<String, String> {
            val params = mutableMapOf<String, String>()
            for (part in content.split(Regex("\\s+"))) {
                if (part.isEmpty()) continue
                val sepIdx = part.indexOf('=')
                if (sepIdx > 0 && sepIdx < part.length - 1) {
                    params[part.substring(0, sepIdx).lowercase()] = part.substring(sepIdx + 1)
                }
            }
            return params
        }
    }

    private fun parseFrameLine(line: String, content: String): FrameResult {
        if (content.isNotBlank()) {
            val params = parseParams(content)
            val w = params["w"]?.toDouble() ?: throw ParseError("missing param: w", line)
            val d = params["d"]?.toDouble() ?: throw ParseError("missing param: d", line)
            val h = params["h"]?.toDouble() ?: throw ParseError("missing param: h", line)
            val levels = (params["l"]?.split(Regex("\\s+"))?.map { it.toDouble() }) ?: emptyList()
            val bottomBeams = (params["b"]?.split(Regex(",\\s*"))?.map { it.toDouble() }) ?: emptyList()
            return FrameResult(w, d, h, levels, bottomBeams)
        }
        val tokens = line.split(Regex("\\s+"))
        if (tokens.size < 4) throw ParseError("need: frame w d h", line)
        return FrameResult(tokens[1].toDouble(), tokens[2].toDouble(), tokens[3].toDouble(), emptyList(), emptyList())
    }

    private fun parseParams(content: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        for (part in content.split(Regex("\\s+"))) {
            if (part.isEmpty()) continue
            val sepIdx = part.indexOf('=')
            if (sepIdx > 0 && sepIdx < part.length - 1) {
                params[part.substring(0, sepIdx).lowercase()] = part.substring(sepIdx + 1)
            }
        }
        return params
    }

    fun getDefaultScript(): String {
        return """# PC Case Configuration
# b=x1,x2,... - additional bottom Y-beams (comma-separated X offsets)
frame (w=530 d=330 h=350 l=140)
move(90 0 20.8) motherboard()
move(0 0 100) gpu (n=5 s=55)
move(-240 95 0) rotate(90 0 0) psu()
move(-240 -95 0) rotate(90 0 0) psu()
move(150 35 105) cooler()
move(0 0 363.5) rotate(0 0 90) radiator()""".trimIndent()
    }

    class ParseError(override val message: String, val line: String? = null) : Exception(message)
}
