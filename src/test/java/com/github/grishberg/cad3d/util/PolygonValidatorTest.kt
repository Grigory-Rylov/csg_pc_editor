package com.github.grishberg.cad3d.util

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.utils.NonManifoldFinder
import eu.printingin3d.javascad.utils.STLParser
import eu.printingin3d.javascad.utils.StlExporter
import eu.printingin3d.javascad.utils.optimizator.PolygonValidator
import eu.printingin3d.javascad.vrl.CSG
import kotlin.math.roundToInt
import kotlin.test.assertEquals
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
        val polygons = STLParser().parseBinarySTL("stl/test.stl")

        println("find non manifold edges...")
        // Ищем проблемные рёбра
        val problems = NonManifoldFinder.findNonManifoldEdges(polygons)

        println("Найдено проблемных рёбер: " + problems.size)
        for (edge in problems) {
            println(
                "Проблемное ребро: (" + edge.p1.x + "," + edge.p1.y + "," + edge.p1.z + ") - (" + edge.p2.x + "," + edge.p2.y + "," + edge.p2.z + ")"
            )
        }
        assertEquals(0, polygons.size)
    }

    @Test
    fun testFor() {
        val size = 400000
        var iter = 0
        var percent = 0
        var percentF: Float
        for (i in 0 until size) {

            percentF = (iter++ / size.toFloat()) * 100f
            val newPercent = percentF.roundToInt()
            if (newPercent > percent) {
                println("Progress $newPercent")
            }
            percent = newPercent
        }

        assertTrue(false)

    }

    private fun createModel(): CSG {
        val cube: Abstract3dModel = Cube(50.0)
        val sphere = Sphere(25.0).move(0, 0, 40.0)
        val model = cube.addModel(sphere)
        return model.toCSG()
    }
}
