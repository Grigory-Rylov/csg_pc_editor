package com.github.grishberg.cad3d.pccase

data class SceneConfig(
    val frameWidth: Double,
    val frameDepth: Double,
    val frameHeight: Double,
    val frameLevels: List<Double>,
    val components: List<ComponentPlacement>
) {
    companion object {
        val DEFAULT = SceneConfig(
            frameWidth = 530.0,
            frameDepth = 330.0,
            frameHeight = 350.0,
            frameLevels = listOf(140.0),
            components = listOf(
                ComponentPlacement("motherboard", 90.0, 0.0, 20.8, 1, 0.0),
                ComponentPlacement("gpu", 0.0, 0.0, 100.0, 5, 0.0),
                ComponentPlacement("psu", -240.0, 95.0, 0.0, 1, 90.0),
                ComponentPlacement("psu", -240.0, -95.0, 0.0, 1, 90.0),
                ComponentPlacement("cooler", 65.0, -20.0, 7.0, 1, 0.0)
            )
        )
    }
}

data class ComponentPlacement(
    val type: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val count: Int = 1,
    val rotation: Double = 0.0,
    val spacing: Double = 50.0
)
