package com.github.grishberg.cad3d.keyboard.casebody.controllers.battery

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube

class SquareBattery : Battery {

    private val innerWidth = 39.71
    private val innerDepth = 50.31
    private val innerHeight = 7.8
    private val wallWidth = 1.0

    override val width: Double = innerWidth + wallWidth * 2
    override val depth: Double = innerDepth + wallWidth * 2
    override val height: Double = innerHeight + wallWidth * 2

    override fun create(): Abstract3dModel {
        val holeHeight = 5.0
        val hole = Cube(innerWidth + 0.5, innerDepth + 0.5, holeHeight)

        return Cube(width, depth, height)
            .subtractModel(hole.moveZ(holeHeight/2 + wallWidth))
            .moveZ(height/2)
    }
}
