package eu.printingin3d.javascad.utils.optimizator

import eu.printingin3d.javascad.vrl.Polygon

/**
 * Find common edges between all polygons, groups polygon by edges.
 */
interface EdgesFinder {

    suspend fun groupPolygonsByEdges(polygons: List<Polygon>): Map<LineKey, List<PolygonEdge>>
}
