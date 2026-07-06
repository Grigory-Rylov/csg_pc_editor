package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class PcFrame(
    private val width: Double,
    private val depth: Double,
    private val height: Double,
    private val levels: List<Double> = emptyList(),
    private val bottomBeams: List<Double> = emptyList()
) {
    fun buildVertical(): Abstract3dModel {
        val p = AluminumProfile.PROFILE_SIZE
        val hw = width / 2.0
        val hh = height / 2.0
        val hd = depth / 2.0

        val posts = mutableListOf<Abstract3dModel>()
        posts.add(AluminumProfile.horizontalZ(height).move(-hw + p / 2, -hd + p / 2, hh))
        posts.add(AluminumProfile.horizontalZ(height).move(hw - p / 2, -hd + p / 2, hh))
        posts.add(AluminumProfile.horizontalZ(height).move(-hw + p / 2, hd - p / 2, hh))
        posts.add(AluminumProfile.horizontalZ(height).move(hw - p / 2, hd - p / 2, hh))
        return Union(posts)
    }

    fun buildHorizontal(): Abstract3dModel {
        val p = AluminumProfile.PROFILE_SIZE
        val hw = width / 2.0
        val hd = depth / 2.0

        val beams = mutableListOf<Abstract3dModel>()

        val beamW = width - 2 * p
        val beamD = depth - 2 * p

        // Bottom layer: 2 along X (front/back), 2 along Y (left/right)
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, -hd + p / 2, p / 2))
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, hd - p / 2, p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(-hw + p / 2, 0.0, p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(hw - p / 2, 0.0, p / 2))

        // Top layer
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, -hd + p / 2, height - p / 2))
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, hd - p / 2, height - p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(-hw + p / 2, 0.0, height - p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(hw - p / 2, 0.0, height - p / 2))

        // Additional bottom beams along Y at specified X offsets (via b= param)
        for (bx in bottomBeams) {
            beams.add(AluminumProfile.vertical(beamD).move(bx, 0.0, p / 2))
        }

        // Intermediate levels
        for (levelZ in levels) {
            beams.add(AluminumProfile.horizontalX(beamW).move(0.0, -hd + p / 2, levelZ))
            beams.add(AluminumProfile.horizontalX(beamW).move(0.0, hd - p / 2, levelZ))
            beams.add(AluminumProfile.vertical(beamD).move(-hw + p / 2, 0.0, levelZ))
            beams.add(AluminumProfile.vertical(beamD).move(hw - p / 2, 0.0, levelZ))
        }

        return Union(beams)
    }

    fun build(): Abstract3dModel {
        return Union(listOf(buildVertical(), buildHorizontal()))
    }
}
