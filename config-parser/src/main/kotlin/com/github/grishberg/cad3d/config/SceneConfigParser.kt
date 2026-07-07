package com.github.grishberg.cad3d.config

import com.github.grishberg.cad3d.pccase.SceneConfig

class SceneConfigParser {

    fun parse(script: String): Result<SceneConfig> {
        return try {
            val tokens = Lexer(script).tokenize()
            val ast = Parser(tokens).parse()
            val config = AstInterpreter().interpret(ast)
            Result.success(config)
        } catch (e: ParseException) {
            Result.failure(SceneConfigParseError(e.message ?: "parse error"))
        } catch (e: Exception) {
            Result.failure(SceneConfigParseError(e.message ?: "unexpected error"))
        }
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
}

class SceneConfigParseError(override val message: String) : Exception(message)
