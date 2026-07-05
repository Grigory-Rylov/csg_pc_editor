package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.tranzitions.Union

class Cooler {
    private val width = 130.0
    private val height = 160.0
    private val depth = 120.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        val hw = width / 2.0
        val hh = height / 2.0

        // Heatsink core
        parts.add(Cube(width - 10, height - 20, depth - 10)
            .move(10.0, 0.0, 0.0))

        // Fins
        val finCount = 30
        for (i in 0 until finCount) {
            val fx = -hw + 15.0 + i * (width - 35.0) / finCount
            parts.add(Cube(1.0, height - 30.0, depth - 14.0)
                .move(fx, 0.0, 0.0))
        }

        // Fan
        parts.add(Cube(25.0, height - 20.0, depth - 10.0)
            .move(-hw - 12.5, 0.0, 0.0))

        // Heat pipes (4)
        for (i in 0 until 4) {
            parts.add(Cylinder(8.0, Radius.fromRadius(3.0))
                .move(-hw + 30.0 + i * 15.0, hh - 25.0, 0.0))
        }

        // Base plate
        parts.add(Cube(width - 20.0, 10.0, depth - 10.0)
            .move(5.0, -hh + 5.0, 0.0))

        return Union(parts)
    }
}
