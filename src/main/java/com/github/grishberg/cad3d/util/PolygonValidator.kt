package com.github.grishberg.cad3d.util

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.vrl.Polygon

class LineKey(a: V3d, b: V3d) {
    // Направляющий вектор прямой (нормализованный)
    private val direction = (b.add(a.inverse())).let { 
        val mag = it.magnitude()
        if (mag > 1e-10) V3d(it.x / mag, it.y / mag, it.z / mag) else V3d.ZERO
    }
    // Точка на прямой (берем первую)
    private val point = a
    
    override fun equals(other: Any?): Boolean {
        if (other !is LineKey) return false
        // Две прямые одинаковы, если их направляющие векторы коллинеарны 
        // и одна точка лежит на другой прямой
        val cross = direction.cross(other.direction)
        val crossMag = cross.magnitude()
        if (crossMag > 1e-6) return false // не коллинеарны
        
        // Проверяем, что точка лежит на прямой
        val toOther = other.point.add(point.inverse())
        val crossToOther = direction.cross(toOther)
        return crossToOther.magnitude() < 1e-6
    }
    
    override fun hashCode(): Int {
        // Для коллинеарных векторов должен быть одинаковый хэш
        // Используем направление с положительной первой ненулевой координатой
        val normalized = when {
            kotlin.math.abs(direction.x) > 1e-10 -> if (direction.x > 0) direction else direction.inverse()
            kotlin.math.abs(direction.y) > 1e-10 -> if (direction.y > 0) direction else direction.inverse()
            kotlin.math.abs(direction.z) > 1e-10 -> if (direction.z > 0) direction else direction.inverse()
            else -> direction
        }
        return normalized.hashCode()
    }
}

class PolygonValidator {

    fun fixPolygons(polygons: List<Polygon>): List<Polygon> {
        // Группируем рёбра по LineKey (прямой)
        val lineToEdges = mutableMapOf<LineKey, MutableList<EdgeInfo>>()
        
        for (polygon in polygons) {
            val verts = polygon.vertices
            for (i in verts.indices) {
                val a = verts[i]
                val b = verts[(i + 1) % verts.size]
                val key = LineKey(a, b)
                lineToEdges.computeIfAbsent(key) { mutableListOf() }
                    .add(EdgeInfo(polygon, i, a, b))
            }
        }

        val polygonToNewVerts = polygons.associateWith { it.vertices.toMutableList() }
        
        // Для каждой прямой собираем все точки и добавляем недостающие
        for ((_, edges) in lineToEdges) {
            if (edges.size < 2) continue // нет смежных рёбер
            
            // Собираем все уникальные точки на этой прямой
            val allPointsOnLine = mutableSetOf<V3d>()
            for (edge in edges) {
                allPointsOnLine.add(edge.start)
                allPointsOnLine.add(edge.end)
            }
            
            // Для каждого ребра добавляем недостающие точки
            for (edge in edges) {
                val verts = polygonToNewVerts[edge.polygon] ?: continue
                val edgeStart = edge.start
                val edgeEnd = edge.end
                
                // Находим точки, которые лежат между началом и концом этого ребра
                val pointsToAdd = allPointsOnLine.filter { point ->
                    point != edgeStart && point != edgeEnd && 
                    isPointBetween(edgeStart, edgeEnd, point)
                }
                
                if (pointsToAdd.isNotEmpty()) {
                    // Сортируем точки по расстоянию от начала ребра
                    val sortedPoints = pointsToAdd.sortedBy { it.distance(edgeStart) }
                    
                    // Вставляем точки после начальной вершины ребра
                    verts.addAll(edge.startIndex + 1, sortedPoints)
                }
            }
        }
        
        // Создаём новые полигоны с добавленными точками
        return polygons.map { orig ->
            val newVerts = polygonToNewVerts[orig] ?: orig.vertices
            if (newVerts.size == orig.vertices.size) orig
            else Polygon.fromPolygons(newVerts, orig.color)
        }
    }
    
    // Проверяет, лежит ли точка point между start и end на одной прямой
    private fun isPointBetween(start: V3d, end: V3d, point: V3d): Boolean {
        val startToPoint = point.add(start.inverse())
        val startToEnd = end.add(start.inverse())
        
        // Проверяем коллинеарность
        val cross = startToPoint.cross(startToEnd)
        if (cross.magnitude() > 1e-6) return false
        
        // Проверяем, что точка между start и end
        val dot = startToPoint.dot(startToEnd)
        val endMagSq = startToEnd.dot(startToEnd)
        
        return dot >= 0 && dot <= endMagSq
    }
}

data class EdgeInfo(
    val polygon: Polygon,
    val startIndex: Int,
    val start: V3d,
    val end: V3d
)
