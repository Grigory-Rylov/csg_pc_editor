package eu.printingin3d.javascad.utils

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.vrl.Polygon
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

class STLParser {
    fun parseBinarySTL(filePath: String): List<Polygon> {
        val polygons = mutableListOf<Polygon>()

        DataInputStream(FileInputStream(filePath)).use { dis ->
            // Пропускаем заголовок (80 байт)
            val header = ByteArray(80)
            dis.readFully(header)

            // Читаем количество треугольников (4 байта) - используем исправленный метод
            val numberOfTriangles = readLittleEndianInt(dis)

            // Проверяем корректность количества треугольников
            if (numberOfTriangles <= 0 || numberOfTriangles > 10_000_000) {
                throw IllegalArgumentException("Invalid number of triangles: $numberOfTriangles")
            }

            println("Reading $numberOfTriangles triangles...")

            for (i in 0 until numberOfTriangles) {
                try {
                    val polygon = readTriangle(dis)
                    polygons.add(polygon)
                } catch (e: Exception) {
                    println("Error reading triangle $i: ${e.message}")
                    // В зависимости от требований, можно либо прервать, либо пропустить
                    // break // Закомментировано, чтобы продолжать чтение
                }

                // Прогресс для больших файлов
                if (i % 1000 == 0 && i > 0) {
                    println("Processed $i triangles...")
                }
            }
        }

        return polygons
    }

    private fun readLittleEndianInt(dis: DataInputStream): Int {
        val bytes = ByteArray(4)
        dis.readFully(bytes)
        return ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
    }

    // Используем ByteBuffer для чтения float в Little-Endian
    private fun readLittleEndianFloat(dis: DataInputStream): Float {
        val bytes = ByteArray(4)
        dis.readFully(bytes)
        return ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .float
    }

    private fun readTriangle(dis: DataInputStream): Polygon {
        // Читаем нормаль (3 * 4 байта = 12 байт)
        val normal = readVector3D(dis)

        // Читаем 3 вершины (3 * 3 * 4 байта = 36 байт)
        val vertices = listOf(
            readVector3D(dis),
            readVector3D(dis),
            readVector3D(dis)
        )

        // Читаем атрибуты (2 байта)
        // Используем readUnsignedShort() или читаем как байты, если нужен signed short
        val attributesBytes = ByteArray(2)
        dis.readFully(attributesBytes)
        val attributes = ByteBuffer.wrap(attributesBytes).order(ByteOrder.LITTLE_ENDIAN).short

        // Проверяем корректность нормали (должна быть единичной)
        val length = sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z)
        if (abs(length - 1.0f) > 0.1f && length > 0f) {
            // Ненормализованная нормаль - можно нормализовать здесь если нужно
            println("Warning: Non-unit normal vector in triangle (length: $length)")
        }

        return Polygon.fromPolygons(vertices, Color.GRAY)
    }

    private fun readVector3D(dis: DataInputStream): V3d {
        // Используем исправленный метод для чтения float
        val x = readLittleEndianFloat(dis).toDouble()
        val y = readLittleEndianFloat(dis).toDouble()
        val z = readLittleEndianFloat(dis).toDouble()
        return V3d(x, y, z)
    }

    // Альтернативная версия с ByteBuffer для большей производительности (остается без изменений)
    fun parseBinarySTLWithByteBuffer(filePath: String): List<Polygon> {
        val polygons = mutableListOf<Polygon>()

        FileInputStream(filePath).use { fis ->
            val channel = fis.channel
            val fileSize = channel.size()

            if (fileSize < 84) { // 80 + 4 байта
                throw IllegalArgumentException("File too small to be a valid STL")
            }

            // Читаем весь файл в память (для больших файлов лучше использовать mmap)
            val buffer = ByteBuffer.allocate(fileSize.toInt())
            channel.read(buffer)
            buffer.flip()
            buffer.order(ByteOrder.LITTLE_ENDIAN) // STL использует little-endian

            // Пропускаем заголовок
            buffer.position(80)

            // Читаем количество треугольников
            val numberOfTriangles = buffer.int
            val expectedSize = 84L + numberOfTriangles * 50L // 50 байт на треугольник

            if (fileSize != expectedSize) {
                println(
                    "Warning: File size doesn't match expected size. " +
                        "Expected: $expectedSize, Actual: $fileSize"
                )
            }

            println("Reading $numberOfTriangles triangles with ByteBuffer...")

            for (i in 0 until numberOfTriangles) {
                if (buffer.remaining() < 50) {
                    println("Not enough data for triangle $i")
                    break
                }

                val polygon = readTriangleFromBuffer(buffer)
                polygons.add(polygon)
            }
        }

        return polygons
    }

    private fun readTriangleFromBuffer(buffer: ByteBuffer): Polygon {
        val normal = readVector3DFromBuffer(buffer)

        val vertices = listOf(
            readVector3DFromBuffer(buffer),
            readVector3DFromBuffer(buffer),
            readVector3DFromBuffer(buffer)
        )

        val attributes = buffer.short

        return Polygon.fromPolygons(vertices, Color.red)
    }

    private fun readVector3DFromBuffer(buffer: ByteBuffer): V3d {
        val x = buffer.float
        val y = buffer.float
        val z = buffer.float
        return V3d(x.toDouble(), y.toDouble(), z.toDouble())
    }

    // Валидация STL файла (остается без изменений)
    fun validateSTLFile(filePath: String): Boolean {
        return try {
            FileInputStream(filePath).use { fis ->
                val buffer = ByteArray(84) // 80 + 4 байта
                fis.read(buffer)

                val bb = ByteBuffer.wrap(buffer, 80, 4)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                val numberOfTriangles = bb.int

                val fileSize = File(filePath).length()
                val expectedSize = 84L + numberOfTriangles * 50L

                fileSize == expectedSize && numberOfTriangles > 0
            }
        } catch (e: Exception) {
            false
        }
    }
}

// Функции расширения для удобства (остаются без изменений)
fun List<Polygon>.calculateBounds(): Pair<V3d, V3d> {
    if (isEmpty()) return Pair(V3d(0.0, 0.0, 0.0), V3d(0.0, 0.0, 0.0))

    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE
    var minZ = Double.MAX_VALUE
    var maxX = Double.MIN_VALUE
    var maxY = Double.MIN_VALUE
    var maxZ = Double.MIN_VALUE

    for (polygon in this) {
        for (vertex in polygon.vertices) {
            minX = minOf(minX, vertex.x)
            minY = minOf(minY, vertex.y)
            minZ = minOf(minZ, vertex.z)
            maxX = maxOf(maxX, vertex.x)
            maxY = maxOf(maxY, vertex.y)
            maxZ = maxOf(maxZ, vertex.z)
        }
    }

    return Pair(V3d(minX, minY, minZ), V3d(maxX, maxY, maxZ))
}

fun List<Polygon>.getVertexCount(): Int = sumOf { it.vertices.size }
