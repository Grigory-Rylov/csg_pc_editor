package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class PcFrame(
    private val width: Double,
    private val height: Double,
    private val depth: Double,
    private val profileSize: Double
) {
    fun build(): Abstract3dModel {
        val p = profileSize
        val hw = width / 2.0
        val hh = height / 2.0
        val hd = depth / 2.0

        val beams = mutableListOf<Abstract3dModel>()

        // 4 vertical corner posts (full height)
        beams.add(Cube(p, height, p).move(-hw + p / 2, hh, -hd + p / 2))
        beams.add(Cube(p, height, p).move(hw - p / 2, hh, -hd + p / 2))
        beams.add(Cube(p, height, p).move(-hw + p / 2, hh, hd - p / 2))
        beams.add(Cube(p, height, p).move(hw - p / 2, hh, hd - p / 2))

        // Bottom horizontal beams along width (X axis) - front and back
        val beamW = width - 2 * p
        beams.add(Cube(beamW, p, p).move(0.0, p / 2, -hd + p / 2))
        beams.add(Cube(beamW, p, p).move(0.0, p / 2, hd - p / 2))

        // Top horizontal beams along width (X axis) - front and back
        beams.add(Cube(beamW, p, p).move(0.0, height - p / 2, -hd + p / 2))
        beams.add(Cube(beamW, p, p).move(0.0, height - p / 2, hd - p / 2))

        // Bottom horizontal beams along depth (Z axis) - left and right
        val beamD = depth - 2 * p
        beams.add(Cube(p, p, beamD).move(-hw + p / 2, p / 2, 0.0))
        beams.add(Cube(p, p, beamD).move(hw - p / 2, p / 2, 0.0))

        // Top horizontal beams along depth (Z axis) - left and right
        beams.add(Cube(p, p, beamD).move(-hw + p / 2, height - p / 2, 0.0))
        beams.add(Cube(p, p, beamD).move(hw - p / 2, height - p / 2, 0.0))

        // Middle support shelf (horizontal frame at ~40% height for motherboard tray)
        val midY = height * 0.4
        beams.add(Cube(beamW, p, p).move(0.0, midY, -hd + p / 2))
        beams.add(Cube(beamW, p, p).move(0.0, midY, hd - p / 2))
        beams.add(Cube(p, p, beamD).move(-hw + p / 2, midY, 0.0))
        beams.add(Cube(p, p, beamD).move(hw - p / 2, midY, 0.0))

        // Cross brace on middle shelf (for rigidity)
        beams.add(Cube(beamW, p, p).move(0.0, midY, 0.0))
        beams.add(Cube(p, p, beamD).move(0.0, midY, 0.0))

        // Bottom cross brace
        beams.add(Cube(beamW, p, p).move(0.0, p / 2, 0.0))
        beams.add(Cube(p, p, beamD).move(0.0, p / 2, 0.0))

        // Top cross brace
        beams.add(Cube(beamW, p, p).move(0.0, height - p / 2, 0.0))
        beams.add(Cube(p, p, beamD).move(0.0, height - p / 2, 0.0))

        return Union(beams)
    }
}
