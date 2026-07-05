package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class PcFrame(
    private val width: Double,
    private val height: Double,
    private val depth: Double
) {
    fun buildVertical(): Abstract3dModel {
        val p = AluminumProfile.PROFILE_SIZE
        val hw = width / 2.0
        val hh = height / 2.0
        val hd = depth / 2.0

        val posts = mutableListOf<Abstract3dModel>()
        posts.add(AluminumProfile.vertical(height).move(-hw + p / 2, hh, -hd + p / 2))
        posts.add(AluminumProfile.vertical(height).move(hw - p / 2, hh, -hd + p / 2))
        posts.add(AluminumProfile.vertical(height).move(-hw + p / 2, hh, hd - p / 2))
        posts.add(AluminumProfile.vertical(height).move(hw - p / 2, hh, hd - p / 2))
        return Union(posts)
    }

    fun buildHorizontal(): Abstract3dModel {
        val p = AluminumProfile.PROFILE_SIZE
        val hw = width / 2.0
        val hd = depth / 2.0

        val beams = mutableListOf<Abstract3dModel>()

        val beamW = width - 2 * p
        val beamD = depth - 2 * p

        // Bottom layer: 2 along X (front/back), 2 along Z (left/right)
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, p / 2, -hd + p / 2))
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, p / 2, hd - p / 2))
        beams.add(AluminumProfile.horizontalZ(beamD).move(-hw + p / 2, p / 2, 0.0))
        beams.add(AluminumProfile.horizontalZ(beamD).move(hw - p / 2, p / 2, 0.0))

        // Top layer
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, height - p / 2, -hd + p / 2))
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, height - p / 2, hd - p / 2))
        beams.add(AluminumProfile.horizontalZ(beamD).move(-hw + p / 2, height - p / 2, 0.0))
        beams.add(AluminumProfile.horizontalZ(beamD).move(hw - p / 2, height - p / 2, 0.0))

        // Motherboard support rails (along Z, under screw positions)
        val mbX1 = -150.0
        val mbX2 = 70.0
        beams.add(AluminumProfile.horizontalZ(beamD).move(mbX1, p / 2, 0.0))
        beams.add(AluminumProfile.horizontalZ(beamD).move(mbX2, p / 2, 0.0))

        // Middle shelf (~40% height)
        val midY = height * 0.4
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, midY, -hd + p / 2))
        beams.add(AluminumProfile.horizontalX(beamW).move(0.0, midY, hd - p / 2))
        beams.add(AluminumProfile.horizontalZ(beamD).move(-hw + p / 2, midY, 0.0))
        beams.add(AluminumProfile.horizontalZ(beamD).move(hw - p / 2, midY, 0.0))

        return Union(beams)
    }

    fun build(): Abstract3dModel {
        return Union(listOf(buildVertical(), buildHorizontal()))
    }
}
