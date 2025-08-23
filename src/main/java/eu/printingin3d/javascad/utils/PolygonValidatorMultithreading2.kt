package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.vrl.Const
import eu.printingin3d.javascad.vrl.Polygon
import java.util.Collections
import java.util.Objects
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.function.ToDoubleFunction
import java.util.stream.Collectors
import kotlin.Pair
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Утилита для исправления проблем в полигонах с поддержкой многопоточности
 */
class PolygonValidatorMultithreading2(
    private val parallelismThreshold: Int = 50,
    private val threadPoolSize: Int = Runtime.getRuntime().availableProcessors()
) {

    // Пользовательский ForkJoinPool для контроля количества потоков
    private val customPool = ForkJoinPool(threadPoolSize)

    /**
     * Исправляет проблемы в полигонах: коллинеарные точки, близкие вершины, naked edges
     */
    @JvmOverloads
    fun fixPolygons(
        polygons: List<Polygon>, progressObserver: ProgressObserver = ProgressObserver.STUB
    ): List<Polygon> {
        val startTime = System.currentTimeMillis()
        val edges = getCommonPolygonsParallel(polygons, progressObserver)

        val mergedPoints = ConcurrentHashMap<Polygon, MutableSet<PointInsert>>()

        // Параллельная обработка групп рёбер с использованием настраиваемого пула потоков
        val edgesList = edges.entries.toList()

        customPool.submit {
            edgesList.parallelStream().forEach { entry ->
                val newPoints = findNewPoints(entry.value)

                // Объединяем новые точки с общим результатом
                newPoints.forEach { (polygon, points) ->
                    mergedPoints.computeIfAbsent(polygon) { ConcurrentHashMap.newKeySet() }.addAll(points)
                }
            }
        }.get()

        progressObserver.onProgress(75)

        val result = addPolygonNewVerticesParallel(mergedPoints)
        progressObserver.onProgress(100)

        println(
            "Polygon validator duration = " + (System.currentTimeMillis() - startTime) + " ms"
        )
        return result
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

    private fun getCommonPolygonsParallel(
        polygons: List<Polygon>, progressObserver: ProgressObserver
    ): Map<LineKey, List<PolygonEdge>> {
        val map = ConcurrentHashMap<LineKey, MutableList<PolygonEdge>>()

        // Параллельная обработка полигонов с использованием настраиваемого пула потоков
        customPool.submit {
            polygons.parallelStream().forEach { polygon ->
                val vertices = polygon.getVertices()
                for (i in vertices.indices) {
                    val a = vertices[i]
                    val b = vertices[(i + 1) % vertices.size]
                    val key: LineKey? = LineKey.fromSegment(a, b)
                    if (key != null) {
                        val currentList = map.computeIfAbsent(key) {
                            Collections.synchronizedList(ArrayList<PolygonEdge>())
                        }
                        currentList.add(PolygonEdge(polygon, a, b, i))
                    } else {
                        println("Found closes points $a - $b")
                    }
                }
            }
        }.get()

        progressObserver.onProgress(50)
        return map
    }

    private fun findNewPoints(polygons: List<PolygonEdge>): Map<Polygon, Set<PointInsert>> {
        if (polygons.size < 2) {
            return mapOf(polygons[0].polygon to emptySet())
        }

        // Для небольших групп используем последовательный алгоритм
        if (polygons.size < parallelismThreshold) {
            return findNewPointsSequential(polygons)
        }

        // Для больших групп используем параллельный алгоритм
        return findNewPointsParallel(polygons)
    }

    private fun findNewPointsSequential(polygons: List<PolygonEdge>): Map<Polygon, Set<PointInsert>> {
        val result = HashMap<Polygon, MutableSet<PointInsert>>()

        for (i in 0..<polygons.size - 1) {
            val currentPolygon = polygons[i]
            for (j in i + 1..<polygons.size) {
                val otherPolygon = polygons[j]
                processPolygonPair(currentPolygon, otherPolygon, result)
            }
        }
        return result
    }

    private fun findNewPointsParallel(polygons: List<PolygonEdge>): Map<Polygon, Set<PointInsert>> {
        val result = ConcurrentHashMap<Polygon, MutableSet<PointInsert>>()

        // Создаём список пар для параллельной обработки
        val pairs = mutableListOf<Pair<PolygonEdge, PolygonEdge>>()
        for (i in 0..<polygons.size - 1) {
            for (j in i + 1..<polygons.size) {
                pairs.add(Pair(polygons[i], polygons[j]))
            }
        }

        // Параллельно обрабатываем все пары с использованием настраиваемого пула потоков
        customPool.submit {
            pairs.parallelStream().forEach { (current, other) ->
                processPolygonPairConcurrent(current, other, result)
            }
        }.get()

        return result
    }

    private fun processPolygonPair(
        currentPolygon: PolygonEdge, otherPolygon: PolygonEdge, result: MutableMap<Polygon, MutableSet<PointInsert>>
    ) {
        val a1 = currentPolygon.p0
        val b1 = currentPolygon.p1
        val a2 = otherPolygon.p0
        val b2 = otherPolygon.p1

        // should insert points into current
        val currentPolygonToBeAdded = result.computeIfAbsent(currentPolygon.polygon) { HashSet<PointInsert>() }

        if (CrossEdgeValidator.isPointBetween(a2, a1, b1)) {
            currentPolygonToBeAdded.add(PointInsert(a2, currentPolygon.firstPointIndex))
        }
        if (CrossEdgeValidator.isPointBetween(b2, a1, b1)) {
            currentPolygonToBeAdded.add(PointInsert(b2, currentPolygon.firstPointIndex))
        }

        // should insert points into other
        val otherPolygonToBeAdded = result.computeIfAbsent(otherPolygon.polygon) { HashSet<PointInsert>() }
        if (CrossEdgeValidator.isPointBetween(a1, a2, b2)) {
            otherPolygonToBeAdded.add(PointInsert(a1, otherPolygon.firstPointIndex))
        }
        if (CrossEdgeValidator.isPointBetween(b1, a2, b2)) {
            otherPolygonToBeAdded.add(PointInsert(b1, otherPolygon.firstPointIndex))
        }
    }

    private fun processPolygonPairConcurrent(
        currentPolygon: PolygonEdge,
        otherPolygon: PolygonEdge,
        result: ConcurrentHashMap<Polygon, MutableSet<PointInsert>>
    ) {
        val a1 = currentPolygon.p0
        val b1 = currentPolygon.p1
        val a2 = otherPolygon.p0
        val b2 = otherPolygon.p1

        // should insert points into current
        val currentPolygonToBeAdded = result.computeIfAbsent(currentPolygon.polygon) { ConcurrentHashMap.newKeySet() }

        if (CrossEdgeValidator.isPointBetween(a2, a1, b1)) {
            currentPolygonToBeAdded.add(PointInsert(a2, currentPolygon.firstPointIndex))
        }
        if (CrossEdgeValidator.isPointBetween(b2, a1, b1)) {
            currentPolygonToBeAdded.add(PointInsert(b2, currentPolygon.firstPointIndex))
        }

        // should insert points into other
        val otherPolygonToBeAdded = result.computeIfAbsent(otherPolygon.polygon) { ConcurrentHashMap.newKeySet() }
        if (CrossEdgeValidator.isPointBetween(a1, a2, b2)) {
            otherPolygonToBeAdded.add(PointInsert(a1, otherPolygon.firstPointIndex))
        }
        if (CrossEdgeValidator.isPointBetween(b1, a2, b2)) {
            otherPolygonToBeAdded.add(PointInsert(b1, otherPolygon.firstPointIndex))
        }
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

    private fun addPolygonNewVerticesParallel(newVerticesInfo: Map<Polygon, Set<PointInsert>>): List<Polygon> {
        return customPool.submit<List<Polygon>> {
            newVerticesInfo.entries.parallelStream().map { entry ->
                    processPolygonVertices(entry.key, entry.value)
                }.filter { it != null }.collect(Collectors.toList()) as List<Polygon>
        }.get()
    }

    private fun processPolygonVertices(currentPolygon: Polygon, pointInserts: Set<PointInsert>): Polygon? {
        val currentPolygonVertices = ArrayList<V3d>(currentPolygon.getVertices())
        val groupedPoints = TreeMap<Int, MutableSet<V3d>>(Collections.reverseOrder<Int>())

        // Группируем вершины
        for (pointInsert in pointInserts) {
            val pointsOfGroup = groupedPoints.computeIfAbsent(pointInsert.position) { HashSet<V3d>() }
            pointsOfGroup.add(pointInsert.point)
        }

        // Итерация от большего ключа к меньшему
        for (verticesEntry in groupedPoints.entries) {
            val key: Int = verticesEntry.key
            val points = verticesEntry.value
            val sortedPoints = sortedPoints(currentPolygonVertices[key], points)

            if (key == currentPolygonVertices.size - 1) {
                for (i in sortedPoints.indices.reversed()) {
                    currentPolygonVertices.add(sortedPoints[i])
                }
            } else {
                for (pointToBeAdded in sortedPoints) {
                    currentPolygonVertices.add(key + 1, pointToBeAdded)
                }
            }
        }

        return if (Polygon.isValid(
                currentPolygonVertices, currentPolygon.getNormal(), currentPolygon.getDist()
            )
        ) {
            Polygon.fromPolygons(
                currentPolygonVertices, currentPolygon.getNormal(), currentPolygon.getColor()
            )
        } else {
            println("Invalid triangle")
            null
        }
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

    /**
     * Закрывает пул потоков. Вызывайте после завершения работы с валидатором.
     */
    fun shutdown() {
        customPool.shutdown()
    }

    /**
     * Принудительно закрывает пул потоков.
     */
    fun shutdownNow() {
        customPool.shutdownNow()
    }

    companion object {

        /**
         * Создаёт валидатор с оптимальными настройками для текущей системы
         */
        fun createOptimized(): PolygonValidatorMultithreading2 {
            val cores = Runtime.getRuntime().availableProcessors()
            // Используем немного больше потоков для IO-bound операций
            val threadCount = (cores * 1.5).toInt().coerceAtLeast(cores)
            return PolygonValidatorMultithreading2(
                parallelismThreshold = 100, threadPoolSize = threadCount
            )
        }

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
