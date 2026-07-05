package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class Psu {
    private val psuWidth = 150.0
    private val psuHeight = 86.0
    private val psuDepth = 140.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()
        val hw = psuWidth / 2.0
        val hh = psuHeight / 2.0
        val hd = psuDepth / 2.0

        parts.add(Cube(psuWidth, psuHeight, psuDepth))

        parts.add(Cube(psuWidth - 4.0, psuHeight - 4.0, 2.0)
            .move(0.0, 0.0, -hd - 1.0))

        parts.add(Cube(10.0, 10.0, 3.0)
            .move(hw - 25.0, hh - 15.0, hd + 1.5))

        parts.add(Cube(25.0, 15.0, 5.0)
            .move(-hw + 30.0, hh - 15.0, hd + 2.5))

        parts.add(Cube(4.0, 20.0, 50.0)
            .move(hw + 2.0, -hh / 2, 0.0))

        parts.add(Cube(50.0, 0.5, 30.0)
            .move(-hw / 2, hh + 0.25, -20.0))

        val holeR = 2.0
        val hx = hw - 8.0
        val hz = hd - 8.0
        for (dx in listOf(-1.0, 1.0)) {
            for (dz in listOf(-1.0, 1.0)) {
                parts.add(Cube(holeR * 2, psuHeight + 2.0, holeR * 2)
                    .move(dx * hx, 0.0, dz * hz))
            }
        }

        parts.add(Cube(80.0, 0.3, 40.0)
            .move(0.0, hh + 0.15, 0.0))

        return Union(parts)
    }
}
