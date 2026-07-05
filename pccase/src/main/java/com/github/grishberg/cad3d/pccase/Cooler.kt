package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.tranzitions.Union

class Cooler {
    private val width = 124.0
    private val height = 156.0
    private val depth = 145.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        val hw = width / 2.0
        val hh = height / 2.0

        // Heatsink core
        parts.add(Cube(width, depth, height)
            .move(10.0, 0.0, 0.0))


        return Union(parts)
    }
}
