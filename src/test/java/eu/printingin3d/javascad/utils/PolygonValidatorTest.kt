package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.Triangulator
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.utils.PolygonValidator.PointInsert
import eu.printingin3d.javascad.vrl.Polygon
import kotlin.test.assertEquals
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
}
