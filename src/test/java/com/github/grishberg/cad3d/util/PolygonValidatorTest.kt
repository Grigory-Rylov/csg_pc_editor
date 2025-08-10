package com.github.grishberg.cad3d.util

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.utils.StlExporter
import eu.printingin3d.javascad.vrl.CSG
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

    private fun createModel(): CSG {
        val cube: Abstract3dModel = Cube(50.0)
        val sphere = Sphere(25.0).move(0, 0, 40.0)
        val model = cube.addModel(sphere)
        return model.toCSG()
    }
}
