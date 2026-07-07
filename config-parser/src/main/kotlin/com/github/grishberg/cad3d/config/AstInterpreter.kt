package com.github.grishberg.cad3d.config

import com.github.grishberg.cad3d.pccase.ComponentPlacement
import com.github.grishberg.cad3d.pccase.SceneConfig
import com.github.grishberg.cad3d.pccase.TransformOp
import eu.printingin3d.javascad.coords.Angles3d

class AstInterpreter {
    fun interpret(program: AstNode.Program): SceneConfig {
        var frameDecl: AstNode.Statement.FrameDecl? = null
        val componentStmts = mutableListOf<AstNode.Statement>()
        val externalBottomEdges = mutableListOf<Double>()

        for (stmt in program.statements) {
            when (stmt) {
                is AstNode.Statement.FrameDecl -> {
                    if (frameDecl != null) error("duplicate frame declaration")
                    frameDecl = stmt
                }
                is AstNode.Statement.BottomEdge -> externalBottomEdges.add(stmt.x)
                is AstNode.Statement.ComponentStmt -> componentStmts.add(stmt)
                is AstNode.Statement.BlockStmt -> flattenBlock(stmt, emptyList(), componentStmts)
            }
        }

        val frame = frameDecl ?: error("frame declaration is required")
        val allBottomBeams = frame.bottomBeams + externalBottomEdges

        val components = componentStmts.map { stmt ->
            val cs = stmt as AstNode.Statement.ComponentStmt
            ComponentPlacement(
                type = cs.component.name,
                count = cs.component.count,
                spacing = cs.component.spacing,
                transforms = cs.transforms.map { it.toTransformOp() }
            )
        }

        return SceneConfig(
            frameWidth = frame.width,
            frameDepth = frame.depth,
            frameHeight = frame.height,
            frameLevels = frame.levels,
            frameBottomBeams = allBottomBeams,
            components = components
        )
    }

    private fun flattenBlock(
        block: AstNode.Statement.BlockStmt,
        outerTransforms: List<AstNode.Transform>,
        result: MutableList<AstNode.Statement>
    ) {
        val combined = outerTransforms + block.transforms
        for (stmt in block.statements) {
            when (stmt) {
                is AstNode.Statement.ComponentStmt -> {
                    val merged = stmt.copy(
                        transforms = combined + stmt.transforms
                    )
                    result.add(merged)
                }
                is AstNode.Statement.BlockStmt -> flattenBlock(stmt, combined, result)
                is AstNode.Statement.BottomEdge -> result.add(stmt)
                is AstNode.Statement.FrameDecl -> error("frame cannot be inside block")
            }
        }
    }

    private fun AstNode.Transform.toTransformOp(): TransformOp {
        return when (type) {
            AstNode.TransformType.Move -> TransformOp.Move(x, y, z)
            AstNode.TransformType.Rotate -> TransformOp.Rotate(Angles3d(x, y, z))
        }
    }
}
