package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.Triangulator
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.utils.optimizator.CrossEdgeValidator
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator.PointInsert
import eu.printingin3d.javascad.utils.optimizator.PolygonValidatorMultithreading
import eu.printingin3d.javascad.vrl.CSG
import eu.printingin3d.javascad.vrl.Polygon
import kotlin.test.assertNotEquals
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

    @Test
    fun testV3dEquals() {
        val a1 = V3d(0.9, -0.5, 0.0)
        val b1 = V3d(0.9, -0.5, -0.0)

        assertEquals(a1, b1)
    }

    @Test
    fun testLineKey() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(5.0, 5.0, 0.0)
        val b2 = V3d(12.0, 12.0, 0.0)

        val key1 = PolygonValidator.LineKey.fromSegment(a1, b1)
        val key2 = PolygonValidator.LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
    }

    @Test
    fun testLineKey2() {
        val a1 = V3d(-8.6602540378444, -25.0, 25.0)
        val b1 = V3d(-25.0, -15.566243270259356, 25.0)

        val a2 = V3d(-25.0, -15.566243270259356, 25.0)
        val b2 = V3d(-12.99038105676658, -22.5, 25.0)

        val key1 = PolygonValidator.LineKey.fromSegment(a1, b1)
        val key2 = PolygonValidator.LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun testOppositeLineKey() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(12.0, 12.0, 0.0)
        val b2 = V3d(5.0, 5.0, 0.0)

        val key1 = PolygonValidator.LineKey.fromSegment(a1, b1)
        val key2 = PolygonValidator.LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
    }

    @Test
    fun testLineKeysNotEquals() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(5.0, 5.5, 0.0)
        val b2 = V3d(12.0, 12.0, 0.0)

        val key1 = PolygonValidator.LineKey.fromSegment(a1, b1)
        val key2 = PolygonValidator.LineKey.fromSegment(a2, b2)
        assertNotEquals(key1, key2)
    }

    @Test
    fun testFindCommonEdges1() {
        val src = ArrayList<Polygon>()
        src.add(createPolygon1())
        src.add(createPolygon2())

        val result = PolygonValidator.getCommonPolygons(src)

        assertEquals(5, result.size)
    }

    @Test
    fun testModel() {
        val model = createModel()
        val polygons = model.getPolygons()

        val result = PolygonValidatorMultithreading().fixPolygons(polygons)

        val nonManifoldEdges = NonManifoldFinder.findNonManifoldEdges(result)

        assertEquals(0, nonManifoldEdges.size)
    }

    @Test
    fun testFindNewPoints() {
        val polygons: MutableList<Polygon> = ArrayList<Polygon>()
        val polygon1 = Polygon.fromPolygons(V3d(0.0, 0.0, 0.0), V3d(5.0, 5.0, 0.0), V3d(10.0, 0.0, 0.0), Color.BLUE)
        val polygon2 = Polygon.fromPolygons(
            V3d(4.0, 0.0, 0.0), V3d(9.0, -5.0, 0.0), V3d(14.0, 0.0, 0.0), Color.BLUE
        )
        polygons.add(polygon1)
        polygons.add(polygon2)

        val edges = PolygonValidator.getCommonPolygons(polygons)
        var commonPolygonsEdge: MutableList<PolygonValidator.PolygonEdge> = ArrayList<PolygonValidator.PolygonEdge>()

        for (entry in edges.entries) {
            if (entry.value!!.size == 1) {
                continue
            }
            commonPolygonsEdge = entry.value!!
            break
        }
        val result = PolygonValidator.findNewPoints(commonPolygonsEdge)

        val newPointsPolygon1: MutableSet<PointInsert> = result.get(polygon1)!!
        val newPointsPolygon2: MutableSet<PointInsert> = result.get(polygon2)!!
        assertEquals(1, newPointsPolygon1.size)
        assertEquals(1, newPointsPolygon2.size)
        assertTrue(
            newPointsPolygon1.contains(
                PointInsert(
                    V3d(4.0, 0.0, 0.0), 2
                )
            )
        )
        assertTrue(
            newPointsPolygon2.contains(
                PointInsert(
                    V3d(10.0, 0.0, 0.0), 2
                )
            )
        )
    }

    @Test
    fun testInsert() {
        val result = ArrayList<V3d?>()
        result.add(V3d(3.0, 3.0, 3.0))

        result.add(0, V3d(2.0, 2.0, 2.0))
        result.add(0, V3d(1.0, 1.0, 1.0))

        assertEquals(V3d(1.0, 1.0, 1.0), result.get(0))
        assertEquals(V3d(2.0, 2.0, 2.0), result.get(1))
        assertEquals(V3d(3.0, 3.0, 3.0), result.get(2))
    }

    @Test
    fun testAddPolygonNewVertices() {
        val polygons: MutableList<Polygon?> = ArrayList<Polygon?>()
        val polygon1 = Polygon.fromPolygons(
            V3d(0.0, 0.0, 0.0), V3d(5.0, 5.0, 0.0), V3d(10.0, 0.0, 0.0), Color.BLUE
        )
        val polygon2 = Polygon.fromPolygons(
            V3d(4.0, 0.0, 0.0), V3d(9.0, -5.0, 0.0), V3d(14.0, 0.0, 0.0), Color.BLUE
        )
        polygons.add(polygon1)
        polygons.add(polygon2)

        val edges = PolygonValidator.getCommonPolygons(polygons)
        var commonPolygonsEdge: MutableList<PolygonValidator.PolygonEdge?> = ArrayList<PolygonValidator.PolygonEdge?>()

        for (entry in edges.entries) {
            if (entry.value!!.size == 1) {
                continue
            }
            commonPolygonsEdge = entry.value!!
            break
        }
        val result = PolygonValidator.findNewPoints(commonPolygonsEdge)

        val newPolygons = PolygonValidator.addPolygonNewVertices(result)

        val valid1 = ArrayList<V3d?>()
        valid1.add(V3d(0.0, 0.0, 0.0))
        valid1.add(V3d(5.0, 5.0, 0.0))
        valid1.add(V3d(10.0, 0.0, 0.0))
        valid1.add(V3d(4.0, 0.0, 0.0))

        assertEquals(valid1, newPolygons.get(0)!!.getVertices())

        val valid2 = ArrayList<V3d?>()
        valid2.add(V3d(4.0, 0.0, 0.0))
        valid2.add(V3d(9.0, -5.0, 0.0))
        valid2.add(V3d(14.0, 0.0, 0.0))
        valid2.add(V3d(10.0, 0.0, 0.0))

        assertEquals(valid2, newPolygons.get(1)!!.getVertices())
    }

    @Test
    fun testAddPolygonNewVertices2() {
        val polygons: MutableList<Polygon?> = ArrayList<Polygon?>()
        val polygon1 = Polygon.fromPolygons(
            V3d(0.0, 0.0, 0.0), V3d(5.0, 5.0, 0.0), V3d(10.0, 0.0, 0.0), Color.BLUE
        )
        val polygon2 = Polygon.fromPolygons(
            V3d(4.0, 0.0, 0.0), V3d(9.0, -5.0, 0.0), V3d(14.0, 0.0, 0.0), Color.BLUE
        )
        polygons.add(polygon1)
        polygons.add(polygon2)

        val newPolygons = PolygonValidator.fixPolygons(polygons)

        val valid1 = ArrayList<V3d?>()
        valid1.add(V3d(0.0, 0.0, 0.0))
        valid1.add(V3d(5.0, 5.0, 0.0))
        valid1.add(V3d(10.0, 0.0, 0.0))
        valid1.add(V3d(4.0, 0.0, 0.0))

        assertEquals(valid1, newPolygons.get(0)!!.getVertices())

        val valid2 = ArrayList<V3d?>()
        valid2.add(V3d(4.0, 0.0, 0.0))
        valid2.add(V3d(9.0, -5.0, 0.0))
        valid2.add(V3d(14.0, 0.0, 0.0))
        valid2.add(V3d(10.0, 0.0, 0.0))

        assertEquals(valid2, newPolygons.get(1)!!.getVertices())
    }

    @Test
    fun testPolygonNewVertices() {
        val result = PolygonValidator.fixPolygons(createPolygons())
        assertEquals(3, result.size)
        assertEquals(4, result.get(0)!!.getVertices().size)
        assertEquals(5, result.get(1)!!.getVertices().size)
        assertEquals(4, result.get(2)!!.getVertices().size)
    }

    private fun createPolygons(): MutableList<Polygon?> {
        val src = ArrayList<Polygon?>()
        src.add(createPolygon1())
        src.add(createPolygon2())
        src.add(createPolygon3())
        return src
    }

    private fun createPolygon1(): Polygon {
        val vertices1 = ArrayList<V3d?>()
        vertices1.add(V3d(-25.0, -15.566243270259356, 25.0))
        vertices1.add(V3d(-25.0, -25.0, 25.0))
        vertices1.add(V3d(-8.6602540378444, -25.0, 25.0))
        return Polygon.fromPolygons(vertices1, Color.WHITE)
    }

    private fun createPolygon2(): Polygon {
        val vertices2 = ArrayList<V3d?>()
        vertices2.add(V3d(-12.99038105676658, 12.5, 25.0))
        vertices2.add(V3d(-25.0, 5.566243270259365, 25.0))
        vertices2.add(V3d(-25.0, -15.566243270259356, 25.0))
        vertices2.add(V3d(-12.99038105676658, -22.5, 25.0))
        return Polygon.fromPolygons(vertices2, Color.RED)
    }

    private fun createPolygon3(): Polygon {
        val vertices3 = ArrayList<V3d?>()
        vertices3.add(V3d(-12.99038105676658, -22.5, 25.0))
        vertices3.add(V3d(-8.6602540378444, -25.0, 25.0))
        vertices3.add(V3d(8.660254037844396, -25.0, 25.0))
        vertices3.add(V3d(-12.99038105676658, -12.5, 25.0))
        return Polygon.fromPolygons(vertices3, Color.GREEN)
    }

    private fun createModel(): CSG {
        val cube: Abstract3dModel = Cube(50.0)
        val sphere = Sphere(25.0).move(0, 0, 40.11111111323232)
        val model = cube.addModel(sphere).moveX(0.3333333333333333)
        return model.toCSG()
    }
}
