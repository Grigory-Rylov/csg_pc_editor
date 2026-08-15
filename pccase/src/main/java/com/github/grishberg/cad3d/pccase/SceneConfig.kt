package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d

sealed class TransformOp {
    data class Rotate(val angles: Angles3d) : TransformOp()
    data class Move(val x: Double, val y: Double, val z: Double) : TransformOp()
}

enum class EdgeSide { FRONT, BACK, LEFT, RIGHT }

// Промежуточная балка рамки: front/back — вдоль X у передней/задней стенки
// (y — смещение от стенки), left/right — вдоль Y у левой/правой.
// z — высота, length=null — весь пролёт.
data class EdgeBeam(
    val side: EdgeSide,
    val z: Double,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val length: Double? = null
)

data class SceneConfig(
    val frameWidth: Double,
    val frameDepth: Double,
    val frameHeight: Double,
    val frameBottomBeams: List<Double> = emptyList(),
    val frameEdges: List<EdgeBeam> = emptyList(),
    val components: List<ComponentPlacement>
) {
    companion object {
        // Дефолт 1-в-1 с дефолтным скриптом SceneConfigParser.getDefaultScript()
        val DEFAULT = SceneConfig(
            frameWidth = 540.0,
            frameDepth = 340.0,
            frameHeight = 400.0,
            frameBottomBeams = listOf(-30.0, 100.0, -115.0),
            frameEdges = listOf(
                EdgeBeam(EdgeSide.FRONT, z = 200.0, y = 60.0),
                EdgeBeam(EdgeSide.BACK, z = 310.0),
                EdgeBeam(EdgeSide.RIGHT, z = 200.0),
                EdgeBeam(EdgeSide.LEFT, z = 200.0)
            ),
            components = listOf(
                ComponentPlacement("motherboard", transforms = listOf(TransformOp.Move(114.0, 30.0, 20.8))),
                ComponentPlacement("gpu", count = 5, spacing = 55.0, transforms = listOf(TransformOp.Move(-120.0, 0.0, 270.0))),
                ComponentPlacement("psu", transforms = listOf(
                    TransformOp.Move(-190.0, 0.0, 65.0),
                    TransformOp.Move(0.0, 75.0, 0.0),
                    TransformOp.Rotate(Angles3d(90.0, 0.0, 0.0))
                )),
                ComponentPlacement("psu", transforms = listOf(
                    TransformOp.Move(-190.0, 0.0, 65.0),
                    TransformOp.Move(0.0, -75.0, 0.0),
                    TransformOp.Rotate(Angles3d(90.0, 0.0, 0.0))
                )),
                ComponentPlacement("cooler", transforms = listOf(TransformOp.Move(150.0, 35.0, 105.0))),
                ComponentPlacement("radiator", transforms = listOf(TransformOp.Move(0.0, 0.0, 420.0))),
                ComponentPlacement("radiator", transforms = listOf(TransformOp.Move(0.0, 0.0, 420.0), TransformOp.Move(200.0, 0.0, 0.0))),
                ComponentPlacement("radiator", transforms = listOf(TransformOp.Move(0.0, 0.0, 420.0), TransformOp.Move(-200.0, 0.0, 0.0)))
            )
        )
    }
}

data class ComponentPlacement(
    val type: String,
    val count: Int = 1,
    val spacing: Double = 50.0,
    val transforms: List<TransformOp> = emptyList()
)
