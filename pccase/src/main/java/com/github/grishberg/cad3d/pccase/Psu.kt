package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.tranzitions.Union

class Psu {
    // ATX PSU standard dimensions
    private val psuWidth = 150.0
    private val psuHeight = 86.0
    private val psuDepth = 140.0

    // Fan
    private val fanDiameter = 120.0
    private val fanDepth = 25.0

    // Power switch
    private val switchW = 20.0
    private val switchH = 10.0

    // AC inlet
    private val inletW = 25.0
    private val inletH = 15.0

    // Cable exit
    private val cableExitW = 60.0
    private val cableExitH = 30.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        // Main body
        parts.add(Cube(psuWidth, psuHeight, psuDepth)
            .move(0.0, 0.0, 0.0))

        // Fan grille (front face) - represented as slightly recessed area
        val fanR = fanDiameter / 2
        parts.add(Cube(fanDiameter + 4.0, fanDiameter + 4.0, 2.0)
            .move(0.0, 0.0, -psuDepth / 2 - 1.0))

        // Fan hub
        parts.add(Cylinder(3.0, Radius.fromRadius(8.0))
            .move(0.0, 0.0, -psuDepth / 2 - 1.5))

        // Power switch (back face)
        parts.add(Cube(switchW, switchH, 3.0)
            .move(psuWidth / 2 - switchW - 10.0, psuHeight / 2 - switchH - 10.0, psuDepth / 2 + 1.5))

        // AC power inlet (back face)
        parts.add(Cube(inletW, inletH, 5.0)
            .move(-psuWidth / 2 + inletW + 10.0, psuHeight / 2 - inletH - 10.0, psuDepth / 2 + 2.5))

        // Cable exit area (side face) - for non-modular PSU
        parts.add(Cube(3.0, cableExitH, cableExitW)
            .move(psuWidth / 2 + 1.5, -psuHeight / 4, 0.0))

        // Voltage label area
        parts.add(Cube(40.0, 15.0, 0.5)
            .move(-psuWidth / 4, -psuHeight / 2 - 0.25, 0.0))

        // Mounting holes (4 corners)
        val holeR = Radius.fromRadius(2.0)
        val hx = psuWidth / 2 - 8.0
        val hz = psuDepth / 2 - 8.0
        for (dx in listOf(-1.0, 1.0)) {
            for (dz in listOf(-1.0, 1.0)) {
                parts.add(Cylinder(psuHeight + 2.0, holeR)
                    .move(dx * hx, 0.0, dz * hz))
            }
        }

        // Label sticker area (top)
        parts.add(Cube(80.0, 0.3, 40.0)
            .move(0.0, psuHeight / 2 + 0.15, 0.0))

        return Union(parts)
    }
}
