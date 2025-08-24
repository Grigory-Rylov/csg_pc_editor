package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.Triangulator
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.utils.optimizator.CrossEdgeValidator
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator.PointInsert
import eu.printingin3d.javascad.vrl.Polygon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PolygonValidatorTest {

    @Test
    fun test1() {
        val mainPolygon = Polygon.fromPolygons(
            listOf(
                V3d(25.0, -17.955837819827096, -24.433756729740683),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, -12.499999999999993, 21.24999999999998),
                V3d(25.0, -16.816243270259367, 21.24999999999998),
                V3d(25.0, -25.0, -3.3012701892219276)
            ), Color.RED
        )

        val targetPoint = V3d(25.0, 4.316243270259353, 21.249999999999986);
        val vertices = listOf(
            targetPoint,
            V3d(25.0, 12.500000000000007, 21.24999999999999),
            V3d(25.0, 15.000000000000014, 25.0),
            V3d(25.0, 5.566243270259358, 25.0)
        )
        val p = Polygon.fromPolygons(vertices, Color.GRAY)
        val edges = PolygonValidator.getCommonPolygons(listOf(mainPolygon, p))

        val mergedPoints: MutableMap<Polygon, MutableSet<PointInsert>> = HashMap()

        for (entry in edges.entries) {
            if (entry.value.size < 2) {
                continue
            }
            val newPoints = PolygonValidator.findNewPoints(entry.value)

            // Объединяем новые точки с общим результатом
            for (newEntry in newPoints.entries) {
                val polygon = newEntry.key
                val points = newEntry.value

                val dst = mergedPoints.computeIfAbsent(polygon) { k: Polygon -> HashSet() }
                dst.addAll(points)
            }
        }
        val mainPolygonNewPoints = mergedPoints.get(mainPolygon)
        assertEquals(1, mainPolygonNewPoints!!.size)
        assertEquals(targetPoint, mainPolygonNewPoints.first().point)

    }

    @Test
    fun test2() {
        val mainPolygon = Polygon.fromPolygons(
            listOf(
                V3d(25.0, -17.955837819827096, -24.433756729740683),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, -12.499999999999993, 21.24999999999998),
                V3d(25.0, -16.816243270259367, 21.24999999999998),
                V3d(25.0, -25.0, -3.3012701892219276)
            ), Color.RED
        )

        val p1 = Polygon.fromPolygons(
            listOf(
                V3d(25.0, 4.316243270259353, 21.249999999999986),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, 15.000000000000014, 25.0),
                V3d(25.0, 5.566243270259358, 25.0)
            ), Color.GRAY
        )

        val edges = PolygonValidator.getCommonPolygons(listOf(mainPolygon, p1))

        val edgesValues = edges.values.toList()
        val polygonInserts = edgesValues.get(4)

        val newPoints = PolygonValidator.findNewPoints(polygonInserts)

        val fixedPolygon = PolygonValidator.addPolygonNewVertices(newPoints)
        val targetPolygon = fixedPolygon.first { it.vertices.size > 4 }

        assertEquals(6, targetPolygon.vertices.size)

        val triangles = Triangulator.triangulate(targetPolygon)
        assertEquals(targetPolygon.vertices.size - 2, triangles.size)
    }

    @Test
    fun test3() {
        val mainPolygon = Polygon.fromPolygons(
            listOf(
                V3d(25.0, -17.955837819827096, -24.433756729740683),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, -12.499999999999993, 21.24999999999998),
                V3d(25.0, -16.816243270259367, 21.24999999999998),
                V3d(25.0, -25.0, -3.3012701892219276),
            ), Color.RED
        )

        val p1 = Polygon.fromPolygons(
            listOf(
                V3d(25.0, 4.316243270259353, 21.249999999999986),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, 15.000000000000014, 25.0),
                V3d(25.0, 5.566243270259358, 25.0)
            ), Color.GRAY
        )

        val p2 = Polygon.fromPolygons(
            listOf(
                V3d(25.0, -15.00000000000001, 25.0),
                V3d(25.0, -12.499999999999993, 21.24999999999998),
                V3d(25.0, 4.316243270259353, 21.249999999999986),
                V3d(25.0, 5.566243270259358, 25.0)
            ), Color.GRAY
        )

        val p3 = Polygon.fromPolygons(
            listOf(
                V3d(25.0, -16.816243270259367, 21.24999999999998),
                V3d(25.0, -12.499999999999993, 21.24999999999998),
                V3d(25.0, -15.00000000000001, 25.0),
                V3d(25.0, -15.56624327025936, 25.0)
            ), Color.GRAY
        )

        val newPolygons = PolygonValidator.fixPolygons(listOf(mainPolygon, p1, p2, p3))

        val vertices = newPolygons.get(0).vertices
        assertEquals(6, vertices.size)

    }

    @Test
    fun lineKeyEqualsTest() {
        val a1 = V3d(12.500000000000007, 25.0, 21.24999999999999)
        val b1 = V3d(-12.499999999999993, 25.0, 21.24999999999998)
        val key1 = PolygonValidator.LineKey.fromSegment(a1, b1)

        val a2 = V3d(4.316243270259353, 25.0, 21.249999999999986)
        val b2 = V3d(12.500000000000007, 25.0, 21.24999999999999)
        val key2 = PolygonValidator.LineKey.fromSegment(a2, b2)

        assertEquals(key1, key2)
    }

    @Test
    fun test() {
        val p = V3d(
            76.45451605516418, -51.73123034352204, 23.72222358968054
        )

        val a = V3d(76.51101271115557, -51.79415715919706, 23.72429834360106)
        val b = V3d(76.44699811096302, -51.72285674546214, 23.721947504593167)
        val c = V3d(76.44586067774136, -51.723946108246864, 23.72266600210672)
        val d = V3d(76.51052392142027, -51.79536350256047, 23.72484529487683)
        val polygon = Polygon.fromPolygons(
            listOf(a, b, c, d), Color.GRAY
        )

        val polygon2 = Polygon.fromPolygons(
            listOf(a, p, b, c, d), polygon.normal, Color.GRAY
        )

        assertTrue(CrossEdgeValidator.isPointBetween(p, a, b))

    }

    @Test
    fun test4() {
        val p0 = V3d(
            85.68772822900522, -9.912880642267941, 24.017398885379336
        )

        val p1 = V3d(
            85.687063799896, -9.91814747838063, 24.037663803889966

        )

        val p2 = V3d(
            85.68662376880474, -9.921635542961209, 24.051084640037672

        )

        val p3 = V3d(
            85.71404857634208, -9.704242950256665, 24.04993026978045

        )

        val a = V3d(85.68625432486834, -9.924564073091435, 24.06235258594964)
        val b = V3d(85.69057646476155, -9.890303077963765, 23.93052842064428)
        val c = V3d(85.74171703302851, -9.484918985010747, 24.037564176122657)
        val polygon = Polygon.fromPolygons(
            listOf(a, b, c), Color.GRAY
        )

        val vertices = listOf(a, p0, p1, p2, b, c, p3)
        val polygon2 = Polygon.fromPolygons(
            vertices, polygon.normal, Color.GRAY
        )

        assertTrue(CrossEdgeValidator.isPointBetween(p0, a, b))
        assertTrue(CrossEdgeValidator.isPointBetween(p1, a, b))
        assertTrue(CrossEdgeValidator.isPointBetween(p2, a, b))
        assertTrue(CrossEdgeValidator.isPointBetween(p3, a, c))

    }

}
