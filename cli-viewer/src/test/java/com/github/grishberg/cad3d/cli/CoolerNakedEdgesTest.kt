package com.github.grishberg.cad3d.cli

import com.github.grishberg.cad3d.pccase.Cooler
import com.github.grishberg.javascad.StlValidator
import com.github.grishberg.javascad.Triangulator
import com.github.grishberg.javascad.optimizator.PolygonValidatorMultithreading
import com.github.grishberg.javascad.optimizator.ProgressObserver
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.Facet
import eu.printingin3d.javascad.utils.Color
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CoolerNakedEdgesTest {

    @Test
    fun `cooler model should have zero dangling edges after export pipeline`() {
        val context = ColorFacetGenerationContext(Color(180, 180, 180)).apply { setFn(8) }
        val model = Cooler().build()
        val polygons = model.toCSG(context).polygons

        val fixedPolygons = PolygonValidatorMultithreading().fixPolygons(polygons, ProgressObserver.STUB)

        val facets = mutableListOf<Facet>()
        for (p in fixedPolygons) {
            val triangles = Triangulator.triangulate(p.getVertices(), p.getNormal())
            for (t in triangles) {
                val rounded = t.getPoints().map { it.roundedToEpsilon() }
                facets.add(Facet(eu.printingin3d.javascad.coords.Triangle3d(rounded[0], rounded[1], rounded[2]), p.getNormal(), p.getColor()))
            }
        }

        val beforeRepair = StlValidator.analyzeNakedEdges(facets)
        println("Naked edges after fixPolygons: ${beforeRepair.size}")

        val repaired = StlValidator.validateAndRepair(facets)
        val afterRepair = StlValidator.analyzeNakedEdges(repaired)
        println("Naked edges after validateAndRepair: ${afterRepair.size}")

        assertEquals(0, afterRepair.size, "Cooler model has ${afterRepair.size} dangling edges after repair, expected 0")
    }
}