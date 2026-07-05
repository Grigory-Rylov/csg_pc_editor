package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.tranzitions.Union

class Motherboard {
    private val boardWidth = 305.0   // X — ширина платы
    private val boardDepth = 205.8   // Y — длина/глубина платы
    private val pcbThickness = 1.6     // Z — толщина PCB (вертикальная ось)

    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()

        // Плата лежит в плоскости XY, толщиной по Z
        val pcb = Cube(boardWidth, boardDepth, pcbThickness)
        parts.add(pcb)

        /*
        // CPU socket SP3 — один сокет, лежит в XY, высота по Z
        val cpuX = -50.0
        val cpuY = -20.0
        parts.add(Cube(62.0, 62.0, 8.0)
            .move(cpuX, cpuY, pcbThickness / 2 + 4.0))

        // DIMM слоты (возле CPU, по обе стороны, вдоль Y)
        val dimmLength = 130.0
        val dimmWidth = 5.0
        val dimmHeight = 32.0
        for (i in 0 until 4) {
            parts.add(Cube(dimmWidth, dimmLength, dimmHeight)
                .move(cpuX - 35.0 - i * 7.0, 30.0, pcbThickness / 2 + 16.0))
        }
        for (i in 0 until 4) {
            parts.add(Cube(dimmWidth, dimmLength, dimmHeight)
                .move(cpuX + 35.0 + i * 7.0, 30.0, pcbThickness / 2 + 16.0))
        }

        // PCIe слоты — стоят вертикально вдоль X, отверстие по Y, высота по Z
        val numPcie = 4
        val pcieSpacing = 25.0
        val pcieStartY = -90.0
        for (i in 0 until numPcie) {
            parts.add(Cube(120.0, 5.0, 12.0)
                .move(-35.0, pcieStartY + i * pcieSpacing, pcbThickness / 2 + 6.0))
        }

        // Радиаторные крепления возле CPU
        parts.add(Cube(15.0, 80.0, 12.0)
            .move(cpuX - 48.0, -10.0, pcbThickness / 2 + 6.0))
        parts.add(Cube(15.0, 80.0, 12.0)
            .move(cpuX + 48.0, -10.0, pcbThickness / 2 + 6.0))

        // Чипсет
        parts.add(Cube(25.0, 25.0, 6.0)
            .move(0.0, 85.0, pcbThickness / 2 + 3.0))

        // Задняя панель I/O
        parts.add(Cube(boardWidth - 60, 12.0, 50.0)
            .move(0.0, boardDepth / 2 - 6.0, pcbThickness / 2 + 25.0))

        // SATA контроллер
        parts.add(Cube(45.0, 22.0, 10.0)
            .move(100.0, -80.0, pcbThickness / 2 + 5.0))

        // Крепления кулера возле CPU
        parts.add(Cube(10.0, 10.0, 8.0)
            .move(cpuX, -boardDepth / 2 + 25.0, pcbThickness / 2 + 4.0))

        // Фронтальная панель (пины)
        for (i in 0 until 6) {
            parts.add(Cube(10.0, 5.0, 5.0)
                .move(50.0 + i * 14.0, boardDepth / 2 - 20.0, pcbThickness / 2 + 2.5))
        }
*/

        val board = Union(parts)

        // Отверстия для винтов — по оси Z (вертикально через плату XY)
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
        val holes = screwPositions.map { (hx, hy) ->
            Cylinder(screwH, Radius.fromRadius(screwR))
                .move(hx, hy, 0.0)
        }
        val holeSubtract = Union(holes)
        return board.subtractModel(holeSubtract)
    }
}
