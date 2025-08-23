package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.vrl.Const
import eu.printingin3d.javascad.vrl.Polygon
import java.util.Collections
import java.util.Objects
import java.util.TreeMap
import java.util.function.ToDoubleFunction
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Утилита для исправления проблем в полигонах
 */
class PolygonValidatorMultithreading {

    /**
     * Исправляет проблемы в полигонах: коллинеарные точки, близкие вершины, naked edges
     */
    @JvmOverloads
    fun fixPolygons(
        polygons: List<Polygon>, progressObserver: ProgressObserver = ProgressObserver.Companion.STUB
    ): List<Polygon> {
        val startTime = System.currentTimeMillis()
        val edges = getCommonPolygons(polygons, progressObserver)

        val mergedPoints = HashMap<Polygon, MutableSet<PointInsert>>()

        var iter = 0
        var percent = 0
        var percentF: Float

        for (entry in edges.entries) {
            val newPoints = findNewPoints(entry.value)

            // Объединяем новые точки с общим результатом
            for (newEntry in newPoints.entries) {
                val polygon = newEntry.key
                val points = newEntry.value

                val dst = mergedPoints.computeIfAbsent(polygon) { k: Polygon -> HashSet() }
                dst.addAll(points)
            }

            percentF = ((iter++).toFloat() / polygons.size.toFloat()) * 50f
            val newPercent = percentF.toInt()
            if (newPercent > percent) {
                progressObserver.onProgress(newPercent + 50)
            }
            percent = newPercent
        }

        val result = addPolygonNewVertices(mergedPoints)
        println(
            "Polygon validator duration = " + (System.currentTimeMillis() - startTime) + " ms"
        )
        return result
    }

    private fun getCommonPolygons(polygons: List<Polygon>): Map<LineKey, List<PolygonEdge>> {
        return getCommonPolygons(polygons, ProgressObserver.Companion.STUB)
    }

    private fun getCommonPolygons(
        polygons: List<Polygon>, progressObserver: ProgressObserver
    ): Map<LineKey, List<PolygonEdge>> {
        val map = HashMap<LineKey, MutableList<PolygonEdge>>()

        var percent = 0
        var percentF = 0f

        var iter = 0
        for (polygon in polygons) {
            val vertices = polygon.getVertices()
            for (i in vertices.indices) {
                val a = vertices.get(i)
                val b = vertices.get((i + 1) % vertices.size)
                val key: LineKey? = LineKey.Companion.fromSegment(a, b)
                if (key != null) {
                    val currentList = map.computeIfAbsent(key) { k: LineKey -> ArrayList<PolygonEdge>() }
                    currentList.add(PolygonEdge(polygon, a, b, i))
                } else {
                    println("Found closes points " + a + " - " + b)
                }
            }

            percentF = (iter.toFloat() / polygons.size.toFloat()) * 50f
            val newPercent = percentF.toInt()
            if (newPercent > percent) {
                progressObserver.onProgress(newPercent)
            }
            percent = newPercent
            iter++
        }

        return map
    }

    private fun findNewPoints(polygons: List<PolygonEdge>): Map<Polygon, Set<PointInsert>> {
        val result = HashMap<Polygon, MutableSet<PointInsert>>()

        if (polygons.size < 2) {
            result.put(polygons[0].polygon, mutableSetOf())
            return result
        }
        for (i in 0..<polygons.size - 1) {
            val currentPolygon = polygons[i]
            for (j in i + 1..<polygons.size) {
                val otherPolygon = polygons[j]
                val a1 = currentPolygon.p0
                val b1 = currentPolygon.p1

                val a2 = otherPolygon.p0
                val b2 = otherPolygon.p1

                // should insert points into current
                val currentPolygonToBeAdded =
                    result.computeIfAbsent(currentPolygon.polygon) { k: Polygon -> HashSet<PointInsert>() }

                if (CrossEdgeValidator.isPointBetween(a2, a1, b1)) {
                    currentPolygonToBeAdded.add(
                        PointInsert(
                            a2, currentPolygon.firstPointIndex
                        )
                    )
                }
                if (CrossEdgeValidator.isPointBetween(b2, a1, b1)) {
                    currentPolygonToBeAdded.add(
                        PointInsert(
                            b2, currentPolygon.firstPointIndex
                        )
                    )
                }

                // should insert points into other
                val otherPolygonToBeAdded =
                    result.computeIfAbsent(otherPolygon.polygon) { k: Polygon -> HashSet<PointInsert>() }
                if (CrossEdgeValidator.isPointBetween(a1, a2, b2)) {
                    otherPolygonToBeAdded.add(PointInsert(a1, otherPolygon.firstPointIndex))
                }
                if (CrossEdgeValidator.isPointBetween(b1, a2, b2)) {
                    otherPolygonToBeAdded.add(PointInsert(b1, otherPolygon.firstPointIndex))
                }
            }
        }
        return result
    }

    private fun addPolygonNewVertices(newVerticesInfo: Map<Polygon, Set<PointInsert>>): List<Polygon> {
        val polygons = ArrayList<Polygon>()
        for (entry in newVerticesInfo.entries) {
            val currentPolygon: Polygon = entry.key
            val currentPolygonVertices = ArrayList<V3d>(currentPolygon.getVertices())

            val groupedPoints = TreeMap<Int, MutableSet<V3d>>(Collections.reverseOrder<Int>())

            // group vertices
            for (pointInsert in entry.value) {
                val pointsOfGroup = groupedPoints.computeIfAbsent(pointInsert!!.position) { k: Int -> HashSet<V3d>() }
                pointsOfGroup.add(pointInsert.point)
            }

            // Теперь итерация будет от большего ключа к меньшему
            for (verticesEntry in groupedPoints.entries) {
                val key: Int = verticesEntry.key
                val points = verticesEntry.value
                val sortedPoints = sortedPoints(currentPolygonVertices.get(key), points)

                if (key == currentPolygonVertices.size - 1) {
                    for (i in sortedPoints.indices.reversed()) {
                        currentPolygonVertices.add(sortedPoints.get(i))
                    }
                } else {
                    for (pointToBeAdded in sortedPoints) {
                        currentPolygonVertices.add(key + 1, pointToBeAdded)
                    }
                }
            }
            if (Polygon.isValid(
                    currentPolygonVertices, currentPolygon.getNormal(), currentPolygon.getDist()
                )
            ) {
                polygons.add(
                    Polygon.fromPolygons(
                        currentPolygonVertices, currentPolygon.getNormal(), currentPolygon.getColor()
                    )
                )
            } else {
                println("Invalid triangle")
            }
        }
        return polygons
    }

    private fun sortedPoints(startPoint: V3d, newPoints: Set<V3d>): List<V3d> {
        val sortedNewPoints = ArrayList<V3d>(newPoints)
        sortedNewPoints.sortWith(Comparator.comparingDouble(ToDoubleFunction { p: V3d ->
            -distanceSquared(
                startPoint, p
            )
        }))
        return sortedNewPoints
    }

    class LineKey private constructor(// Геттеры (опционально, если нужно получить данные из ключа)
        val pointOnLine: V3d, // Возвращаем копию, если V3d не иммутабельный
        val directionUnitVector: V3d
    ) {// Возвращаем копию, если V3d не иммутабельный

        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || javaClass != o.javaClass) {
                return false
            }
            val lineKey = o as LineKey
            return pointOnLine == lineKey.pointOnLine && directionUnitVector == lineKey.directionUnitVector
        }

        override fun hashCode(): Int {
            // Используем Objects.hash для комбинирования хэш-кодов
            return Objects.hash(pointOnLine, directionUnitVector)
        }

        override fun toString(): String {
            return "LineKey{" + "pointOnLine=" + pointOnLine + ", directionUnitVector=" + directionUnitVector + '}'
        }

        companion object {

            /**
             * Создает LineKey из двух точек, определяющих отрезок.
             *
             * @param p0 Первая точка отрезка.
             * @param p1 Вторая точка отрезка.
             * @return LineKey, представляющий прямую, проходящую через p0 и p1.
             * @throws IllegalArgumentException если p0 и p1 совпадают.
             */
            fun fromSegment(p0: V3d, p1: V3d): LineKey? {
                if (p0 == p1) {
                    return null
                }

                // Вычисляем направляющий вектор
                val directionVector = V3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z)

                // Нормализуем направляющий вектор
                val length =
                    sqrt(directionVector.x * directionVector.x + directionVector.y * directionVector.y + directionVector.z * directionVector.z)

                require(!(length < Const.EPSILON)) { "Точки p0 и p1 совпадают (длина вектора близка к нулю)." }

                var unitDirection = V3d(
                    directionVector.x / length, directionVector.y / length, directionVector.z / length
                )

                // Вычисляем точку на прямой, ближайшую к началу координат (точка перпендикуляра из
                // (0,0,0) на прямую)
                // Параметр t для точки на прямой P(t) = p0 + t * (p1 - p0) ближайшей к (0,0,0):
                // t = - (p0 . (p1 - p0)) / |p1 - p0|^2
                val dotProduct = p0.x * directionVector.x + p0.y * directionVector.y + p0.z * directionVector.z
                val t = -dotProduct / (length * length)

                val closestPoint = V3d(
                    p0.x + t * directionVector.x, p0.y + t * directionVector.y, p0.z + t * directionVector.z
                )

                // Для обеспечения канонического представления, убедимся, что направляющий вектор
                // имеет положительную компоненту X (или Y, если X=0, или Z, если X=Y=0)
                // Это предотвратит ситуацию, когда (p0,p1) и (p1,p0) дадут разные ключи.
                if (needsToFlipDirection(unitDirection)) {
                    unitDirection = V3d(-unitDirection.x, -unitDirection.y, -unitDirection.z)
                    // Точка closestPoint остается той же, так как она определяет положение прямой
                }

                return LineKey(closestPoint, unitDirection)
            }

            /**
             * Определяет, нужно ли инвертировать направляющий вектор для канонического представления.
             *
             * @param unitDirection Единичный направляющий вектор.
             * @return true, если вектор нужно инвертировать.
             */
            private fun needsToFlipDirection(unitDirection: V3d): Boolean {
                // Стандартный способ: выбрать направление, при котором первая ненулевая компонента
                // положительна
                if (abs(unitDirection.x) > Const.EPSILON) {
                    return unitDirection.x < 0
                }
                if (abs(unitDirection.y) > Const.EPSILON) {
                    return unitDirection.y < 0
                }
                if (abs(unitDirection.z) > Const.EPSILON) {
                    return unitDirection.z < 0
                }
                // Все компоненты нулевые (не должно происходить для единичного вектора)
                return false
            }
        }
    }

    class PointInsert(val point: V3d, val position: Int) : Comparable<PointInsert> {

        override fun equals(o: Any?): Boolean {
            if (o == null || javaClass != o.javaClass) {
                return false
            }
            val that = o as PointInsert
            return position == that.position && point == that.point
        }

        override fun hashCode(): Int {
            return Objects.hash(point, position)
        }

        // Реализация метода compareTo для сортировки по position по УБЫВАНИЮ
        override fun compareTo(other: PointInsert): Int {
            return other.position.compareTo(this.position)
        }
    }

    class PolygonEdge(val polygon: Polygon, val p0: V3d, val p1: V3d, val firstPointIndex: Int) {

        override fun equals(o: Any?): Boolean {
            if (o == null || javaClass != o.javaClass) {
                return false
            }
            val that = o as PolygonEdge
            return firstPointIndex == that.firstPointIndex && polygon == that.polygon && p0 == that.p0 && p1 == that.p1
        }

        override fun hashCode(): Int {
            return Objects.hash(polygon, p0, p1, firstPointIndex)
        }
    }

    interface ProgressObserver {

        fun onProgress(progress: Int)

        companion object {

            val STUB: ProgressObserver = object : ProgressObserver {
                override fun onProgress(progress: Int) {
                    // no op
                }
            }
        }
    }

    companion object {

        /**
         * Вычисляет квадрат расстояния между двумя точками (для избежания вычисления sqrt).
         */
        private fun distanceSquared(a: V3d, b: V3d): Double {
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dz = b.z - a.z
            return dx * dx + dy * dy + dz * dz
        }
    }
}
