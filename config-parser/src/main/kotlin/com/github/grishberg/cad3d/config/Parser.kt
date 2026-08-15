package com.github.grishberg.cad3d.config

class Parser(private val tokens: List<Token>) {
    private var pos = 0

    fun parse(): AstNode.Program {
        val stmts = mutableListOf<AstNode.Statement>()
        while (current() != Token.Eof) {
            skipNewlines()
            if (current() == Token.Eof) break
            stmts.add(parseStatement())
        }
        return AstNode.Program(stmts)
    }

    private val edgeFunctions = mapOf(
        "frontEdge" to com.github.grishberg.cad3d.pccase.EdgeSide.FRONT,
        "backEdge" to com.github.grishberg.cad3d.pccase.EdgeSide.BACK,
        "leftEdge" to com.github.grishberg.cad3d.pccase.EdgeSide.LEFT,
        "rightEdge" to com.github.grishberg.cad3d.pccase.EdgeSide.RIGHT
    )

    private fun parseStatement(): AstNode.Statement {
        val t = current()
        return when {
            peekIdentifier("frame") -> parseFrameDecl()
            peekIdentifier("bottomEdge") -> parseBottomEdge()
            t is Token.Identifier && edgeFunctions.containsKey(t.value) -> parseEdge(t.value)
            else -> parseTransformChain()
        }
    }

    // front/backEdge(z [x] [y] [l]), left/rightEdge(z): z обязательна, l — длина балки (весь пролёт по умолчанию)
    private fun parseEdge(name: String): AstNode.Statement.EdgeDecl {
        consumeIdentifier(name)
        consume(Token.LParen)
        val params = parseNamedParams()
        consume(Token.RParen)
        return AstNode.Statement.EdgeDecl(
            com.github.grishberg.cad3d.pccase.EdgeBeam(
                side = edgeFunctions.getValue(name),
                z = numericParam(params, "z"),
                x = params["x"]?.toDoubleOrNull() ?: 0.0,
                y = params["y"]?.toDoubleOrNull() ?: 0.0,
                length = params["l"]?.toDoubleOrNull()
            )
        )
    }

    private fun numericParam(params: Map<String, String>, key: String): Double {
        if (!params.containsKey(key)) error("Отсутствует параметр '$key'")
        return params.getValue(key).toDoubleOrNull()
            ?: error("Ожидалось числовое значение параметра '$key'")
    }

    private fun frameDim(params: Map<String, String>, key: String): Double {
        if (!params.containsKey(key)) error("Отсутствует параметр '$key' frame()")
        return params.getValue(key).toDoubleOrNull()
            ?: error("Ожидалось числовое значение параметра '$key'")
    }

    private fun parseFrameDecl(): AstNode.Statement.FrameDecl {
        consumeIdentifier("frame")
        consume(Token.LParen)
        val params = parseNamedParams()
        consume(Token.RParen)
        if (params.containsKey("l")) {
            error("Параметр l= frame() больше не поддерживается — используйте frontEdge/backEdge/leftEdge/rightEdge")
        }
        if (params.containsKey("b")) {
            error("Параметр b= frame() больше не поддерживается — используйте bottomEdge(x=...)")
        }
        val w = frameDim(params, "w")
        val d = frameDim(params, "d")
        val h = frameDim(params, "h")
        val bottomBeams = mutableListOf<Double>()
        val edges = mutableListOf<com.github.grishberg.cad3d.pccase.EdgeBeam>()

        skipNewlines()
        if (current() is Token.LCurly) {
            pos++
            while (true) {
                skipNewlines()
                if (current() == Token.RCurly) break
                if (current() == Token.Eof) error("Незакрытый блок frame")
                val stmt = parseStatement()
                when (stmt) {
                    is AstNode.Statement.BottomEdge -> bottomBeams.add(stmt.x)
                    is AstNode.Statement.EdgeDecl -> edges.add(stmt.beam)
                    else -> {}
                }
            }
            consume(Token.RCurly)
        }

        return AstNode.Statement.FrameDecl(w, d, h, bottomBeams, edges)
    }

    private fun parseBottomEdge(): AstNode.Statement.BottomEdge {
        consumeIdentifier("bottomEdge")
        consume(Token.LParen)
        val params = parseNamedParams()
        consume(Token.RParen)
        val x = numericParam(params, "x")
        return AstNode.Statement.BottomEdge(x)
    }

    private fun parseTransformChain(): AstNode.Statement {
        val transforms = mutableListOf<AstNode.Transform>()
        while (isTransformKeyword()) {
            transforms.add(parseTransform())
        }
        return when (current()) {
            is Token.LCurly -> {
                pos++
                val stmts = mutableListOf<AstNode.Statement>()
                while (true) {
                    skipNewlines()
                    if (current() == Token.RCurly) break
                    if (current() == Token.Eof) error("Незакрытый блок '{'")
                    stmts.add(parseStatement())
                }
                consume(Token.RCurly)
                AstNode.Statement.BlockStmt(transforms, stmts)
            }
            is Token.Identifier -> {
                val comp = parseComponentRef()
                AstNode.Statement.ComponentStmt(transforms, comp)
            }
            else -> error("После transform ожидается компонент или '{'")
        }
    }

    private fun parseTransform(): AstNode.Transform {
        val keyword = when (val t = current()) {
            is Token.Identifier -> t.value
            else -> error("expected move/rotate")
        }
        pos++
        val type = when (keyword) {
            "move" -> AstNode.TransformType.Move
            "rotate" -> AstNode.TransformType.Rotate
            else -> error("Неизвестный transform '$keyword'")
        }
        consume(Token.LParen)
        val (x, y, z) = parseTransformArgs()
        consume(Token.RParen)
        return AstNode.Transform(type, x, y, z)
    }

    private fun parseTransformArgs(): Triple<Double, Double, Double> {
        if (current() is Token.Identifier && peekAhead(1) is Token.Equals) {
            val params = parseNamedParams()
            return Triple(
                params["x"]?.toDoubleOrNull() ?: 0.0,
                params["y"]?.toDoubleOrNull() ?: 0.0,
                params["z"]?.toDoubleOrNull() ?: 0.0
            )
        }
        val values = mutableListOf<Double>()
        while (current() is Token.Number) {
            values.add((consume() as Token.Number).value)
        }
        if (values.size < 3) {
            values.addAll(List(3 - values.size) { 0.0 })
        }
        return Triple(
            values.getOrElse(0) { 0.0 },
            values.getOrElse(1) { 0.0 },
            values.getOrElse(2) { 0.0 }
        )
    }

    private fun parseComponentRef(): AstNode.ComponentRef {
        val name = when (val t = current()) {
            is Token.Identifier -> t.value
            else -> error("expected component name")
        }
        pos++
        consume(Token.LParen)
        val params = if (current() != Token.RParen) parseNamedParams() else emptyMap()
        consume(Token.RParen)
        val count = (params["n"]?.toDoubleOrNull()?.toInt()) ?: 1
        val spacing = params["s"]?.toDoubleOrNull() ?: 50.0
        return AstNode.ComponentRef(name, count, spacing)
    }

    private fun parseNamedParams(): Map<String, String> {
        val params = mutableMapOf<String, String>()
        while (current() is Token.Identifier && peekAhead(1) is Token.Equals) {
            val key = (consume() as Token.Identifier).value.lowercase()
            consume(Token.Equals)
            val value = when (val t = current()) {
                is Token.Number -> t.value.toString()
                is Token.Identifier -> t.value
                else -> error("Ожидалось значение после '='")
            }
            pos++
            params[key] = value
        }
        return params
    }

    private fun isTransformKeyword(): Boolean {
        val t = current()
        return t is Token.Identifier && (t.value == "move" || t.value == "rotate")
    }

    private fun peekIdentifier(value: String): Boolean {
        val t = current()
        return t is Token.Identifier && t.value == value
    }

    private fun consumeIdentifier(value: String) {
        val t = current()
        if (t !is Token.Identifier || t.value != value) {
            error("Ожидалось '$value', получено '${t}'")
        }
        pos++
    }

    private fun consume(expected: Token) {
        if (current()::class != expected::class) {
            error("Ожидалось $expected, получено ${current()}")
        }
        pos++
    }

    private fun current(): Token = if (pos < tokens.size) tokens[pos] else Token.Eof

    private fun consume(): Token {
        val t = current()
        pos++
        return t
    }

    private fun peekAhead(n: Int): Token {
        val idx = pos + n
        return if (idx < tokens.size) tokens[idx] else Token.Eof
    }

    private fun skipNewlines() {
        while (current() is Token.Newline) pos++
    }

    private fun error(message: String): Nothing {
        val ctx = tokens.getOrElse(pos.coerceAtMost(tokens.size - 1)) { Token.Eof }
        throw ParseException("$message (near: $ctx)")
    }
}

class ParseException(message: String) : Exception(message)
