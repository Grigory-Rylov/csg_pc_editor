package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class Gpu {
    private val gpuLength = 290.0   // Y — длинная сторона
    private val gpuHeight = 112.0   // Z — высота/ширина карты
    private val gpuThickness = 38.0 // X — самая короткая (толщина)
    private val pcbThickness = 1.6

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        val halfL = gpuLength / 2.0   // Y/2 = 145
        val halfH = gpuHeight / 2.0   // Z/2 = 56
        val halfT = gpuThickness / 2.0// X/2 = 19

        // Плата (тонкий слой внизу)
        parts.add(Cube(gpuThickness - 6.0, pcbThickness, gpuHeight - 4.0)
            .move(0.0, -halfL + pcbThickness / 2, 0.0))

        // Основной корпус: X=38, Y=290, Z=112
        parts.add(Cube(gpuThickness, gpuLength, gpuHeight)
            .move(0.0, 0.0, 0.0))

        // Рёбра радиатора — тонкие пластины по Y (0.8мм), идут вдоль X и Z
        val finCount = 36
        val finSpacing = (gpuLength - 80.0) / finCount
        for (i in 0 until finCount) {
            val fy = -halfL + 50.0 + i * finSpacing
            parts.add(Cube(gpuThickness - 6.0, 0.8, gpuHeight - 10.0)
                .move(0.0, fy, 0.0))
        }

        // Вентилятор на конце Y+ (длинной стороны): X=thickness+4, Y=fanLen, Z=height-4
        val fanLen = 70.0
        parts.add(Cube(gpuThickness + 4.0, fanLen, gpuHeight - 4.0)
            .move(0.0, halfL - fanLen / 2, 0.0))

        // Мотор вентилятора — на верхней грани (Z+), тонкий по Y
        val fanR = 30.0
        parts.add(Cube(fanR, 6.0, fanR)
            .move(0.0, halfL - fanLen / 2, halfH))

        // Лопасти вентилятора — маленькие кубики в плоскости XZ
        for (i in 0 until 7) {
            val a = i * (360.0 / 7)
            val rx = (fanR * 0.35) * Math.cos(Math.toRadians(a))
            val rz = (fanR * 0.35) * Math.sin(Math.toRadians(a))
            parts.add(Cube(3.0, 5.0, 3.0)
                .move(rx, halfL - fanLen / 2, halfH + rz))
        }

        // Крепление на конце Y- (PCIe bracket): X=thickness+4, Y=3, Z=height-20
        parts.add(Cube(gpuThickness + 4.0, 3.0, gpuHeight - 20.0)
            .move(0.0, -halfL - 1.5, 8.0))

        // Коннекторы питания — выступают из торца Z+, на отрицательном Y
        parts.add(Cube(gpuThickness + 6.0, 20.0, 10.0)
            .move(0.0, -halfL + 50.0, halfH))
        parts.add(Cube(gpuThickness + 6.0, 20.0, 10.0)
            .move(0.0, -halfL + 75.0, halfH))

        return Union(parts)
    }
}
