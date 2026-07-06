package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class PcFrame(
    private val width: Double,
    private val depth: Double,
    private val height: Double,
    private val levels: List<Double> = emptyList()
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

        // Motherboard support rails (along Y, under screw positions)
        // Default config: motherboard at x=90, screws at X=-120..+100 relative
        val mbX1 = -30.0   // under leftmost screw (90-120)
        val mbX2 = 190.0   // under rightmost screw (90+100)
        beams.add(AluminumProfile.vertical(beamD).move(mbX1, 0.0, p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(mbX2, 0.0, p / 2))

        // PSU support rails (left side, bottom, along Y)
        beams.add(AluminumProfile.vertical(beamD).move(-245.0, 0.0, p / 2))
        beams.add(AluminumProfile.vertical(beamD).move(-195.0, 0.0, p / 2))

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
