package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.tranzitions.Union

class Motherboard {
    private val boardWidth = 280.0
    private val boardDepth = 260.0
    private val pcbThickness = 1.6

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        parts.add(Cube(boardWidth, pcbThickness, boardDepth))

        val cpu1X = -50.0
        val cpu2X = 50.0
        val cpuZ = -20.0

        for (cx in listOf(cpu1X, cpu2X)) {
            parts.add(Cube(62.0, 8.0, 62.0)
                .move(cx, pcbThickness / 2 + 4.0, cpuZ))
        }

        val dimmLength = 130.0
        val dimmWidth = 5.0
        val dimmHeight = 32.0
        for (cx in listOf(cpu1X, cpu2X)) {
            for (i in 0 until 4) {
                parts.add(Cube(dimmWidth, dimmHeight, dimmLength)
                    .move(cx - 35.0 - i * 7.0, pcbThickness / 2 + 16.0, 30.0))
            }
            for (i in 0 until 4) {
                parts.add(Cube(dimmWidth, dimmHeight, dimmLength)
                    .move(cx + 35.0 + i * 7.0, pcbThickness / 2 + 16.0, 30.0))
            }
        }

        val numPcie = 4
        val pcieSpacing = 25.0
        val pcieStartZ = -90.0
        for (i in 0 until numPcie) {
            parts.add(Cube(120.0, 12.0, 5.0)
                .move(-35.0, pcbThickness / 2 + 6.0, pcieStartZ + i * pcieSpacing))
        }

        for (cx in listOf(cpu1X, cpu2X)) {
            parts.add(Cube(15.0, 12.0, 80.0)
                .move(cx - 48.0, pcbThickness / 2 + 6.0, -10.0))
            parts.add(Cube(15.0, 12.0, 80.0)
                .move(cx + 48.0, pcbThickness / 2 + 6.0, -10.0))
        }

        parts.add(Cube(25.0, 6.0, 25.0)
            .move(0.0, pcbThickness / 2 + 3.0, 85.0))

        parts.add(Cube(boardWidth - 60, 50.0, 12.0)
            .move(0.0, pcbThickness / 2 + 25.0, boardDepth / 2 - 6.0))

        parts.add(Cube(22.0, 10.0, 45.0)
            .move(100.0, pcbThickness / 2 + 5.0, -80.0))

        for (cx in listOf(cpu1X, cpu2X)) {
            parts.add(Cube(10.0, 8.0, 10.0)
                .move(cx, pcbThickness / 2 + 4.0, -boardDepth / 2 + 25.0))
        }

        for (i in 0 until 6) {
            parts.add(Cube(10.0, 5.0, 5.0)
                .move(50.0 + i * 14.0, pcbThickness / 2 + 2.5, boardDepth / 2 - 20.0))
        }

        val board = Union(parts)

        val screwR = 1.5
        val screwH = 10.0
        val screwPositions = listOf(
            -110.0 to -110.0,
            110.0 to -110.0,
            -110.0 to 0.0,
            110.0 to 0.0,
            -110.0 to 110.0,
            110.0 to 110.0
        )
        val holes = screwPositions.map { (hx, hz) ->
            Cylinder(screwH, Radius.fromRadius(screwR))
                .move(hx, 0.0, hz)
        }
        val holeSubtract = Union(holes)

        return board.subtractModel(holeSubtract)
    }
}
