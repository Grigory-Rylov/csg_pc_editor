package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class Gpu {
    private val gpuLength = 290.0
    private val gpuHeight = 112.0
    private val gpuThickness = 38.0
    private val pcbThickness = 1.6

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        val halfL = gpuLength / 2.0
        val halfH = gpuHeight / 2.0
        val halfT = gpuThickness / 2.0

        parts.add(Cube(gpuLength - 6.0, pcbThickness, gpuThickness - 4.0)
            .move(0.0, -halfH + pcbThickness / 2, 0.0))

        parts.add(Cube(gpuLength, gpuHeight, gpuThickness)
            .move(0.0, 0.0, 0.0))

        val finCount = 36
        val finSpacing = (gpuLength - 80.0) / finCount
        for (i in 0 until finCount) {
            val fx = -halfL + 50.0 + i * finSpacing
            parts.add(Cube(0.8, gpuHeight - 10.0, gpuThickness - 6.0)
                .move(fx, 0.0, 0.0))
        }

        val fanLen = 70.0
        parts.add(Cube(fanLen, gpuHeight - 4.0, gpuThickness + 4.0)
            .move(halfL - fanLen / 2, 0.0, 0.0))

        val fanR = 30.0
        parts.add(Cube(fanR, 6.0, fanR)
            .move(halfL - fanLen / 2, -halfH + gpuHeight * 0.3, 0.0))

        for (i in 0 until 7) {
            val a = i * (360.0 / 7)
            val rx = (fanR * 0.35) * Math.cos(Math.toRadians(a))
            val rz = (fanR * 0.35) * Math.sin(Math.toRadians(a))
            parts.add(Cube(3.0, 5.0, 3.0)
                .move(halfL - fanLen / 2 + rx, -halfH + gpuHeight * 0.3, rz))
        }

        parts.add(Cube(3.0, gpuHeight - 20.0, gpuThickness + 4.0)
            .move(-halfL - 1.5, 8.0, 0.0))

        parts.add(Cube(20.0, 6.0, 10.0)
            .move(60.0, -halfH + pcbThickness + 3.0, 0.0))
        parts.add(Cube(20.0, 6.0, 10.0)
            .move(85.0, -halfH + pcbThickness + 3.0, 0.0))

        return Union(parts)
    }
}
