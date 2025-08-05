package com.github.grishberg.cad3d.util

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.Polygon
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test

class PolygonValidatorTest {
    @Test
    fun testFixPolygons_addsMissingVertices() {
        val color = Color.BLUE
        // Полигон 1: треугольник (0,0,0)-(1,0,0)-(0,1,0)
        val p1 = Polygon.fromPolygons(
            listOf(
                V3d(0.0, 0.0, 10.0),
                V3d(0.0, 10.0, 10.0),
                V3d(10.0, 10.0, 10.0),
                V3d(10.0, 0.0, 10.0),
            ),
            color
        )
        // Полигон 2: треугольник (1,0,0)-(1,1,0)-(0,1,0)
        val p2 = Polygon.fromPolygons(
            listOf(
                V3d(10.0, 8.0, 10.0),
                V3d(10.0, 10.0, 10.0),
                V3d(20.0, 10.0, 10.0),
                V3d(20.0, 80.0, 10.0),
            ),
            color
        )

        val p3 = Polygon.fromPolygons(
            listOf(
                V3d(10.0, 2.0, 10.0),
                V3d(10.0, 8.0, 10.0),
                V3d(10.0, 8.0, 0.0),
                V3d(10.0, 2.0, 0.0),
            ),
            color
        )

        val p4 = Polygon.fromPolygons(
            listOf(
                V3d(10.0, 0.0, 10.0),
                V3d(10.0, 2.0, 10.0),
                V3d(20.0, 2.0, 10.0),
                V3d(20.0, 0.0, 10.0),
            ),
            color
        )
        // Ожидаем, что после fixPolygons оба полигона станут четырёхугольниками с общей стороной
        val validator = PolygonValidator()
        val fixed = validator.fixPolygons(listOf(p1, p2,p3,p4))
        assertEquals(4, fixed.size)
        // Проверяем, что оба полигона теперь имеют по 4 вершины
        assertEquals(6, fixed[0].vertices.size)
    }
}
