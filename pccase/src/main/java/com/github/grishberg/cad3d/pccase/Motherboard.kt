package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.tranzitions.Union

class Motherboard {
    // Supermicro H12SSL-i - ATX form factor
    // Board: 305mm x 244mm, PCB thickness ~1.6mm
    private val boardWidth = 305.0
    private val boardDepth = 244.0
    private val pcbThickness = 1.6

    // CPU socket (SP3) - ~58.5mm x 58.5mm
    private val cpuSocketSize = 58.5
    private val cpuSocketHeight = 5.0

    // RAM slots - 8x DDR4 DIMM, each ~133mm x 8mm
    private val dimmLength = 133.0
    private val dimmWidth = 8.0
    private val dimmHeight = 32.0

    // VRM heatsinks near CPU
    private val vrmHeatsinkW = 40.0
    private val vrmHeatsinkD = 20.0
    private val vrmHeatsinkH = 12.0

    // Chipset heatsink
    private val chipsetW = 30.0
    private val chipsetD = 30.0
    private val chipsetH = 8.0

    // PCIe slots - 2x x16
    private val pcieSlotW = 85.0
    private val pcieSlotD = 10.0
    private val pcieSlotH = 12.0

    // I/O shield area
    private val ioShieldW = 170.0
    private val ioShieldD = 45.0
    private val ioShieldH = 25.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        // PCB base
        parts.add(Cube(boardWidth, pcbThickness, boardDepth).move(0.0, 0.0, 0.0))

        // CPU socket (SP3) - positioned upper-center
        val cpuX = 0.0
        val cpuZ = -20.0
        parts.add(Cube(cpuSocketSize, cpuSocketHeight, cpuSocketSize)
            .move(cpuX, pcbThickness / 2 + cpuSocketHeight / 2, cpuZ))

        // CPU heatsink mount holes (4 corners)
        val mountOffset = 38.0
        val mountHoleR = Radius.fromRadius(1.5)
        for (dx in listOf(-1.0, 1.0)) {
            for (dz in listOf(-1.0, 1.0)) {
                parts.add(Cylinder(pcbThickness + 1.0, mountHoleR)
                    .move(cpuX + dx * mountOffset, 0.0, cpuZ + dz * mountOffset))
            }
        }

        // RAM slots - 8 slots to the right of CPU
        val ramStartX = 80.0
        val ramSpacing = 10.0
        for (i in 0 until 8) {
            val x = ramStartX + i * ramSpacing
            parts.add(Cube(dimmWidth, dimmHeight, dimmLength)
                .move(x, pcbThickness / 2 + dimmHeight / 2, 10.0))
        }

        // VRM heatsinks - left side of CPU
        parts.add(Cube(vrmHeatsinkH, vrmHeatsinkH, vrmHeatsinkD * 2.5)
            .move(-cpuSocketSize / 2 - vrmHeatsinkH / 2 - 3.0, pcbThickness / 2 + vrmHeatsinkH / 2, cpuZ))
        // Top VRM heatsink
        parts.add(Cube(vrmHeatsinkW, vrmHeatsinkH, vrmHeatsinkD)
            .move(cpuX, pcbThickness / 2 + vrmHeatsinkH / 2, -cpuSocketSize / 2 - vrmHeatsinkD / 2 - 3.0))

        // Chipset heatsink - lower area
        parts.add(Cube(chipsetW, chipsetH, chipsetD)
            .move(-20.0, pcbThickness / 2 + chipsetH / 2, 60.0))

        // PCIe x16 slots (2 slots) - lower area
        val pcieY = pcbThickness / 2 + pcieSlotH / 2
        parts.add(Cube(pcieSlotW, pcieSlotH, pcieSlotD)
            .move(-20.0, pcieY, 85.0))
        parts.add(Cube(pcieSlotW, pcieSlotH, pcieSlotD)
            .move(-20.0, pcieY, 115.0))

        // I/O shield area (back edge)
        parts.add(Cube(ioShieldW, ioShieldH, ioShieldD)
            .move(20.0, pcbThickness / 2 + ioShieldH / 2, -boardDepth / 2 + ioShieldD / 2))

        // 24-pin ATX power connector
        parts.add(Cube(24.0, 6.0, 50.0)
            .move(130.0, pcbThickness / 2 + 3.0, 50.0))

        // 8-pin CPU power connector
        parts.add(Cube(15.0, 6.0, 8.0)
            .move(-100.0, pcbThickness / 2 + 3.0, -boardDepth / 2 + 10.0))

        // SATA ports (6x) - bottom right
        for (i in 0 until 6) {
            parts.add(Cube(12.0, 5.0, 7.0)
                .move(120.0 + i * 14.0, pcbThickness / 2 + 2.5, boardDepth / 2 - 15.0))
        }

        return Union(parts)
    }
}
