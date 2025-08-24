package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.Triangulator
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.utils.optimizator.PolygonValidatorMultithreading
import eu.printingin3d.javascad.vrl.CSG
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.Polygon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PolygonValidatorMultithreadingTest {

    private val underTest = PolygonValidatorMultithreading()

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

        val p1 = Polygon.fromPolygons(
            listOf(
                V3d(25.0, 4.316243270259353, 21.249999999999986),
                V3d(25.0, 12.500000000000007, 21.24999999999999),
                V3d(25.0, 15.000000000000014, 25.0),
                V3d(25.0, 5.566243270259358, 25.0)
            ), Color.GRAY
        )

        val fixedPolygon = underTest.fixPolygons(listOf(mainPolygon, p1))

        val targetPolygon = fixedPolygon.first { it.vertices.size > 4 }

        assertEquals(6, targetPolygon.vertices.size)

        val triangles = Triangulator.triangulate(targetPolygon)
        assertEquals(targetPolygon.vertices.size - 2, triangles.size)
    }

    @Test
    fun test2() {
        val firtstVertex = V3d(25.0, -17.955837819827096, -24.433756729740683)
        val mainPolygon = Polygon.fromPolygons(
            listOf(
                firtstVertex,
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

        val newPolygons = underTest.fixPolygons(listOf(mainPolygon, p1, p2, p3))

        val vertices = newPolygons.first { it.vertices.first() == firtstVertex}.vertices
        assertEquals(6, vertices.size)
    }



    @Test
    fun performanceTest() {
        val aContext = ColorFacetGenerationContext(Color.GRAY)
        aContext.setFn(100)

        val model = createModel(aContext)
        val startTime = System.currentTimeMillis()
        val newPolygons = underTest.fixPolygons(model.polygons)
        println("Duration = ${System.currentTimeMillis() - startTime}")
    }

    private fun createModel(aContext: FacetGenerationContext): CSG {
        val cube: Abstract3dModel = Cube(50.0)
        val sphere = Sphere(25.0).move(0, 0, 40.0)
        val model = cube.addModel(sphere)
        return model.toCSG(aContext)
    }
}
