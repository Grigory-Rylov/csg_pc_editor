package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class Radiator(
    private val width: Double = 120.0,
    private val length: Double = 395.0
) {
    private val thickness = 27.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()
        val hl = length / 2.0

        // Main body core
        parts.add(Cube(width, length, thickness))


        // End tanks
        parts.add(Cube(width + 4.0, 14.0, thickness + 3.0)
            .move(0.0, -hl + 7.0, 0.0))
        parts.add(Cube(width + 4.0, 14.0, thickness + 3.0)
            .move(0.0, hl - 7.0, 0.0))

        return Union(parts)
    }
}
