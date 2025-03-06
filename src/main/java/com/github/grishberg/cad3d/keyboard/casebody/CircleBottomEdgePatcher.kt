package com.github.grishberg.cad3d.keyboard.casebody

import com.github.grishberg.cad3d.keyboard.Utils
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

class CircleBottomEdgePatcher(
    private val thickness: Double,
    private val objectHeight: Double,
    private val centerX: Double = 0.0,
    private val centerY: Double = 0.0,
    private val radiusX: Double = 0.0,
    private val radiusY: Double = radiusX,

    ) : WallBottomEdgePatcher {

    override fun backPoint(o: Abstract3dModel): Abstract3dModel {
        if (radiusX == 0.0 || radiusY == 0.0) {
            return projection(o)
        }
        val point = o.move
        val convertedPoint = projectToEllipseY(point, centerX, centerY, radiusX, radiusY)
        return borderObject(thickness, objectHeight).move(convertedPoint)
    }

    override fun leftPoint(o: Abstract3dModel): Abstract3dModel {
        return projection(o)
    }

    override fun frontPoint(o: Abstract3dModel): Abstract3dModel {
        return projection(o)
    }

    override fun rightPoint(o: Abstract3dModel): Abstract3dModel {
        if (radiusX == 0.0 || radiusY == 0.0) {
            return projection(o)
        }
        val point = o.move
        val convertedPoint = projectToEllipseX(point, centerX, centerY, radiusX, radiusY)
        return borderObject(thickness, objectHeight).move(convertedPoint)
    }

    /**
     * Проецирует точку на окружность, изменяя координату Y.
     *
     * @param point     Исходная точка
     * @param centerX   X-координата центра окружности
     * @param centerY   Y-координата центра окружности
     * @param radius    Радиус окружности
     * @return Новая точка на окружности с исходными X и Z
     */
    private fun projectToCircle(point: V3d, centerX: Double, centerY: Double, radius: Double): V3d {
        val dx = point.x - centerX
        val discriminant = radius * radius - dx * dx

        // Если X за пределами радиуса, возвращаем ближайшую точку на окружности
        if (discriminant < 0) {
            return V3d(
                point.x, centerY,  // Берем центр по Y
                point.z
            )
        }

        // Вычисляем возможные значения Y
        val sqrtDisc = sqrt(discriminant)
        val y1 = centerY + sqrtDisc
        val y2 = centerY - sqrtDisc

        // Выбираем Y, ближайший к исходному значению
        val newY = if ((abs(y1 - point.y) < abs(y2 - point.y))) y1 else y2

        return V3d(point.x, newY, 0.0)
    }

    /**
     * Проецирует точку на эллипс, изменяя координату Y.
     *
     * @param point     Исходная точка
     * @param centerX   X-координата центра эллипса
     * @param centerY   Y-координата центра эллипса
     * @param a         Полуось эллипса по X
     * @param b         Полуось эллипса по Y
     * @return Новая точка на эллипсе с исходными X и Z
     */
    private fun projectToEllipseY(
        point: V3d, centerX: Double, centerY: Double, a: Double, b: Double
    ): V3d {
        // Смещаем координаты относительно центра
        val dx = (point.x - centerX) / a
        val discriminant = 1.0 - dx * dx

        // Если X за пределами эллипса, фиксируем на границе
        if (discriminant < 0) {
            val boundaryX = sign(dx) * a + centerX
            return V3d(boundaryX, centerY, point.z)
        }

        // Вычисляем возможные значения Y
        val sqrtTerm = b * sqrt(discriminant)
        val y1 = centerY + sqrtTerm
        val y2 = centerY - sqrtTerm

        // Выбираем Y, ближайший к исходному значению
        val targetY = if ((abs(y1 - point.y) < abs(y2 - point.y))) y1
        else y2

        return V3d(point.x, targetY, objectHeight / 2)
    }

    private fun projectToEllipseX(
        point: V3d, centerX: Double, centerY: Double, a: Double, b: Double
    ): V3d {
        // Смещаем координаты относительно центра
        val dy = (point.y - centerY) / b
        val discriminant = 1.0 - dy * dy

        // Если Y за пределами эллипса, фиксируем на границе
        if (discriminant < 0) {
            val boundaryY = sign(dy) * b + centerY
            return V3d(centerX, boundaryY, point.z)
        }

        // Вычисляем возможные значения Y
        val sqrtTerm = a * sqrt(discriminant)
        val x1 = centerX + sqrtTerm
        val x2 = centerX - sqrtTerm

        // Выбираем Y, ближайший к исходному значению
        val targetX = if ((abs(x1 - point.x) < abs(x2 - point.x))) x1
        else x2

        return V3d(targetX, point.y, objectHeight / 2)
    }

    override fun projection(obj: Abstract3dModel): Abstract3dModel {
        val point = obj.move
        return borderObject(thickness, objectHeight).move(V3d(point.x, point.y, objectHeight / 2))
    }

    private fun borderObject(thickness: Double, height: Double): Abstract3dModel {
        return Utils.cylinder(thickness, height)
    }
}
