package com.github.grishberg.cad3d.config

sealed class AstNode {
    data class Program(val statements: List<Statement>) : AstNode()

    sealed class Statement : AstNode() {
        data class FrameDecl(
            val width: Double,
            val depth: Double,
            val height: Double,
            val levels: List<Double> = emptyList(),
            val bottomBeams: List<Double> = emptyList()
        ) : Statement()

        data class BottomEdge(val x: Double) : Statement()

        data class ComponentStmt(
            val transforms: List<Transform>,
            val component: ComponentRef
        ) : Statement()

        data class BlockStmt(
            val transforms: List<Transform>,
            val statements: List<Statement>
        ) : Statement()
    }

    data class Transform(
        val type: TransformType,
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0
    ) : AstNode()

    enum class TransformType { Move, Rotate }

    data class ComponentRef(
        val name: String,
        val count: Int = 1,
        val spacing: Double = 50.0
    ) : AstNode()
}
