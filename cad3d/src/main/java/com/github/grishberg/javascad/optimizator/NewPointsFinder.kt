package com.github.grishberg.javascad.optimizator

import eu.printingin3d.javascad.vrl.Polygon

interface NewPointsFinder {

    suspend fun findNewPoints(edges: Map<LineKey, List<PolygonEdge>>): Map<Polygon, MutableSet<PointInsert>>
}
