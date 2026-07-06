package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d

sealed class TransformOp {
    data class Rotate(val angles: Angles3d) : TransformOp()
    data class Move(val x: Double, val y: Double, val z: Double) : TransformOp()
}

data class SceneConfig(
    val frameWidth: Double,
    val frameDepth: Double,
    val frameHeight: Double,
    val frameLevels: List<Double>,
    val frameBottomBeams: List<Double> = emptyList(),
    val components: List<ComponentPlacement>
) {
    companion object {
        val DEFAULT = SceneConfig(
            frameWidth = 530.0,
            frameDepth = 330.0,
            frameHeight = 350.0,
            frameLevels = listOf(140.0),
            frameBottomBeams = emptyList(),
            components = listOf(
                ComponentPlacement("motherboard", transforms = listOf(TransformOp.Move(90.0, 0.0, 20.8))),
                ComponentPlacement("gpu", count = 5, spacing = 55.0, transforms = listOf(TransformOp.Move(0.0, 0.0, 100.0))),
                ComponentPlacement("psu", transforms = listOf(TransformOp.Move(-240.0, 95.0, 0.0), TransformOp.Rotate(Angles3d(90.0, 0.0, 0.0)))),
                ComponentPlacement("psu", transforms = listOf(TransformOp.Move(-240.0, -95.0, 0.0), TransformOp.Rotate(Angles3d(90.0, 0.0, 0.0)))),
                ComponentPlacement("cooler", transforms = listOf(TransformOp.Move(150.0, 35.0, 105.0))),
                ComponentPlacement("radiator", transforms = listOf(TransformOp.Move(0.0, 0.0, 363.5), TransformOp.Rotate(Angles3d(0.0, 0.0, 90.0))))
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
