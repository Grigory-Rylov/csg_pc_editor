package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.utils.PolygonValidatorMultithreading.LineKey
import eu.printingin3d.javascad.utils.PolygonValidatorMultithreading.PolygonEdge
import eu.printingin3d.javascad.vrl.Polygon
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CommonPolygonFinderMultitrading(progressObserver: ProgressObserver) : EdgesFinder {

    override suspend fun getCommonPolygons(
        polygons: List<Polygon>,
    ): Map<LineKey, List<PolygonEdge>> = withContext(Dispatchers.Default) {

        val numChunks = 10

        val chunks = polygons.chunked(
            if (polygons.size < numChunks) 1
            else max(1, polygons.size / numChunks)
        )

        // ConcurrentHashMap для потокобезопасного накопления результатов
        val resultMap = ConcurrentHashMap<LineKey, MutableList<PolygonEdge>>()

        // Создаем 10 (или меньше, если chunks.size < 10) параллельных задач
        val deferredResults = chunks.map { chunk ->
            async {
                processPolygonChunk(chunk)
            }
        }

        // Собираем результаты по мере завершения задач
        var completedChunks = 0
        for (deferred in deferredResults) {
            try {
                val chunkResult = deferred.await()
                mergeChunkResultIntoMap(chunkResult, resultMap)

                completedChunks++
            } catch (e: Exception) {
                // Логируем ошибку, но продолжаем обработку других чанков
                println("Error processing polygon chunk: ${e.message}")
                e.printStackTrace()
            }
        }

        // Преобразуем результат в неизменяемый Map
        resultMap.mapValues { it.value.toList() }.toMap()
    }

    /**
     * Обрабатывает один чанк полигонов и возвращает карту рёбер для этого чанка.
     */
    private fun processPolygonChunk(chunk: List<Polygon>): Map<LineKey, List<PolygonEdge>> {
        val localMap = mutableMapOf<LineKey, MutableList<PolygonEdge>>()

        for (polygon in chunk) {
            val vertices = polygon.getVertices()
            for (i in vertices.indices) {
                val a = vertices[i]
                val b = vertices[(i + 1) % vertices.size]
                val key: LineKey? = LineKey.fromSegment(a, b)
                if (key != null) {
                    val currentList = localMap.computeIfAbsent(key) { mutableListOf() }
                    currentList.add(PolygonEdge(polygon, a, b, i))
                } else {
                    // println("Found close points $a - $b") // Опционально, может быть много логов
                }
            }
        }

        return localMap
    }

    /**
     * Потокобезопасно объединяет результат обработки одного чанка в общий результат.
     */
    private fun mergeChunkResultIntoMap(
        chunkResult: Map<LineKey, List<PolygonEdge>>, resultMap: ConcurrentHashMap<LineKey, MutableList<PolygonEdge>>
    ) {
        for ((key, edges) in chunkResult) {
            resultMap.compute(key) { _, existingList ->
                val list = existingList ?: mutableListOf()
                synchronized(list) { // Дополнительная синхронизация для addAll
                    list.addAll(edges)
                }
                list
            }
        }
    }
}
