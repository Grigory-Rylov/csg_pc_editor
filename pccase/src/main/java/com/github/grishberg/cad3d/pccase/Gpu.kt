package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class Gpu {
    // RTX 3090 Founders Edition dimensions
    private val pcbLength = 313.0
    private val pcbWidth = 138.0
    private val pcbThickness = 1.6

    // Cooler/shroud - 3-slot design
    private val coolerLength = 313.0
    private val coolerWidth = 138.0
    private val coolerHeight = 61.0

    // Heatsink fins
    private val finCount = 40
    private val finThickness = 0.5
    private val finHeight = 40.0
    private val finDepth = 2.0

    // Fans
    private val fanDiameter = 85.0
    private val fanHeight = 15.0

    // Backplate
    private val backplateThickness = 1.5

    // Power connectors (2x 8-pin)
    private val powerConnectorW = 15.0
    private val powerConnectorH = 6.0
    private val powerConnectorD = 8.0

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        // PCB
        parts.add(Cube(pcbLength, pcbThickness, pcbWidth)
            .move(0.0, 0.0, 0.0))

        // GPU die area (raised section on PCB)
        parts.add(Cube(25.0, 3.0, 25.0)
            .move(-40.0, pcbThickness / 2 + 1.5, 0.0))

        // VRAM chips around GPU die (12 chips)
        for (i in 0 until 12) {
            val angle = i * 30.0
            val r = 25.0
            val x = -40.0 + r * Math.cos(Math.toRadians(angle))
            val z = r * Math.sin(Math.toRadians(angle))
            parts.add(Cube(6.0, 1.0, 6.0)
                .move(x, pcbThickness / 2 + 0.5, z))
        }

        // VRM area (right side of PCB)
        for (i in 0 until 8) {
            parts.add(Cube(8.0, 4.0, 5.0)
                .move(80.0 + i * 12.0, pcbThickness / 2 + 2.0, -30.0))
        }

        // Main cooler/shroud body
        val coolerY = -(coolerHeight / 2)
        parts.add(Cube(coolerLength, coolerHeight, coolerWidth)
            .move(0.0, coolerY, 0.0))

        // Heatsink fins (visible from sides)
        val finStartX = -coolerLength / 2 + 10.0
        val finSpacing = (coolerLength - 20.0) / finCount
        for (i in 0 until finCount) {
            val x = finStartX + i * finSpacing
            parts.add(Cube(finThickness, finHeight, coolerWidth - 10.0)
                .move(x, coolerY + coolerHeight / 2 - finHeight / 2, 0.0))
        }

        // Fan cutouts (represented as slightly raised cylinders for visual)
        // Fan 1
        val fanR = fanDiameter / 2
        parts.add(Cube(fanDiameter, fanHeight, fanDiameter)
            .move(-70.0, coolerY + coolerHeight / 2 + fanHeight / 2, 0.0))
        // Fan 2
        parts.add(Cube(fanDiameter, fanHeight, fanDiameter)
            .move(50.0, coolerY + coolerHeight / 2 + fanHeight / 2, 0.0))

        // Backplate
        parts.add(Cube(pcbLength, backplateThickness, pcbWidth)
            .move(0.0, pcbThickness / 2 + backplateThickness / 2, 0.0))

        // Backplate thermal pads (small bumps)
        for (i in 0 until 6) {
            parts.add(Cube(8.0, 0.5, 8.0)
                .move(-80.0 + i * 20.0, pcbThickness / 2 + backplateThickness + 0.25, 0.0))
        }

        // IO bracket (bracket end)
        parts.add(Cube(3.0, 80.0, pcbWidth)
            .move(-pcbLength / 2 - 1.5, -20.0, 0.0))

        // Power connectors (2x 8-pin, top edge)
        parts.add(Cube(powerConnectorW, powerConnectorH, powerConnectorD)
            .move(80.0, pcbThickness / 2 + powerConnectorH / 2, -pcbWidth / 2 + powerConnectorD / 2 + 5.0))
        parts.add(Cube(powerConnectorW, powerConnectorH, powerConnectorD)
            .move(100.0, pcbThickness / 2 + powerConnectorH / 2, -pcbWidth / 2 + powerConnectorD / 2 + 5.0))

        // SLI/NVLink connector
        parts.add(Cube(25.0, 3.0, 5.0)
            .move(-100.0, pcbThickness / 2 + 1.5, pcbWidth / 2 - 10.0))

        return Union(parts)
    }
}
