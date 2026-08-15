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

    // Дефолтный скрипт — 1-в-1 с web-версией (pc_viewer_3d DEFAULT_SCRIPT) и SceneConfig.DEFAULT
    fun getDefaultScript(): String {
        return """# Корпус
frame (w=540 d=340 h=400) {
  # Нижние ребра
  bottomEdge (x=-30)
  bottomEdge (x=100)
  bottomEdge (x=-115)
  # Передняя штанга под видеокарты
  frontEdge(y = 60 z=200)
  # Задняя штанга под видеокарты
  backEdge(z=310)
  # Боковые промежуточные штанги
  rightEdge(z=200)
  leftEdge(z=200)
}

# Матплата
move(114 30 20.8) motherboard()
# Видеокарты
move(-120 0 270) gpu (n=5 s=55)
# БП
move(x=-190 z=65) {
  move(y=75) rotate(90 0 0) psu()
  move(y=-75) rotate(90 0 0) psu()
}
move(150 35 105) cooler()
move(0 0 420) {
  radiator()
  move(x=200) radiator ()
  move(x=-200) radiator ()
}""".trimIndent()
    }
}

class SceneConfigParseError(override val message: String) : Exception(message)
