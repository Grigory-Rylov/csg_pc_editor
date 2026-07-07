package com.github.grishberg.cad3d.config

sealed class Token {
    data class Identifier(val value: String) : Token()
    data class Number(val value: Double) : Token()
    object LParen : Token()
    object RParen : Token()
    object LCurly : Token()
    object RCurly : Token()
    object Equals : Token()
    object Semicolon : Token()
    object Newline : Token()
    object Eof : Token()
}
