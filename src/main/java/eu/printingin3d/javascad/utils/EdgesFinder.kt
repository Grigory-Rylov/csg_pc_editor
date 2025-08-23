package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.utils.PolygonValidatorMultithreading.LineKey
import eu.printingin3d.javascad.utils.PolygonValidatorMultithreading.PolygonEdge
import eu.printingin3d.javascad.vrl.Polygon

interface EdgesFinder {

    suspend fun getCommonPolygons(polygons: List<Polygon>): Map<LineKey, List<PolygonEdge>>
}
