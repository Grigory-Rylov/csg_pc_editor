package com.github.grishberg.cad3d.util

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.utils.NonManifoldFinder
import eu.printingin3d.javascad.utils.STLParser
import eu.printingin3d.javascad.utils.StlExporter
import eu.printingin3d.javascad.utils.optimizator.LineKey
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator
import eu.printingin3d.javascad.vrl.CSG
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class PolygonValidatorTest {

    @Test
    fun test2() {
        val cube1 = Cube(10.0)
        val cube2 = Cube(5.0, 5.0, 12.0)
        val result = createModel()
        val fixed = PolygonValidator.fixPolygons(result.polygons)
        StlExporter.writeBinaryStl(result.toFacets(), "sample.stl")
        StlExporter.saveStl(fixed, "sample_fixed.stl")
    }

    @Test
    fun testMatrixEdges() {
        println("read stl")
        val polygons = STLParser().parseBinarySTL("stl/test2.stl")

        println("find non manifold edges...")
        // Ищем проблемные рёбра
        val problems = NonManifoldFinder.findNonManifoldEdges(polygons)

        println("Найдено проблемных рёбер: " + problems.size)
        assertEquals(0, problems.size)
    }

    @Test
    fun testLineKey() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(5.0, 5.0, 0.0)
        val b2 = V3d(12.0, 12.0, 0.0)

        val key1 = LineKey.fromSegment(a1, b1)
        val key2 = LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
    }

    @Test
    fun testLineKey2() {
        val a1 = V3d(-8.6602540378444, -25.0, 25.0)
        val b1 = V3d(-25.0, -15.566243270259356, 25.0)

        val a2 = V3d(-25.0, -15.566243270259356, 25.0)
        val b2 = V3d(-12.99038105676658, -22.5, 25.0)

        val key1 = LineKey.fromSegment(a1, b1)
        val key2 = LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun testOppositeLineKey() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(12.0, 12.0, 0.0)
        val b2 = V3d(5.0, 5.0, 0.0)

        val key1 = LineKey.fromSegment(a1, b1)
        val key2 = LineKey.fromSegment(a2, b2)
        assertEquals(key1, key2)
    }

    @Test
    fun testLineKeysNotEquals() {
        val a1 = V3d(0.0, 0.0, 0.0)
        val b1 = V3d(10.0, 10.0, 0.0)

        val a2 = V3d(5.0, 5.5, 0.0)
        val b2 = V3d(12.0, 12.0, 0.0)

        val key1 = LineKey.fromSegment(a1, b1)
        val key2 = LineKey.fromSegment(a2, b2)
        assertNotEquals(key1, key2)
    }

    private fun createModel(): CSG {
        val cube: Abstract3dModel = Cube(50.0)
        val sphere = Sphere(25.0).move(0, 0, 40.0)
        val model = cube.addModel(sphere)
        return model.toCSG()
    }
}
