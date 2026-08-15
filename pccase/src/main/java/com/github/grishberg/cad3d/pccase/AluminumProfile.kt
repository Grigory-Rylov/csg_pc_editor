package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube

class AluminumProfile private constructor(
    val length: Double,
    val orientation: Orientation
) {
    enum class Orientation { VERTICAL, HORIZONTAL_X, HORIZONTAL_Z }

    companion object {
        const val PROFILE_SIZE = 20.0

        private val cuts = mutableListOf<AluminumProfile>()

        fun reset() {
            cuts.clear()
        }

        fun vertical(height: Double): Abstract3dModel {
            cuts.add(AluminumProfile(height, Orientation.VERTICAL))
            return Cube(PROFILE_SIZE, height, PROFILE_SIZE)
        }

        fun horizontalX(length: Double): Abstract3dModel {
            cuts.add(AluminumProfile(length, Orientation.HORIZONTAL_X))
            return Cube(length, PROFILE_SIZE, PROFILE_SIZE)
        }

        fun horizontalZ(length: Double): Abstract3dModel {
            cuts.add(AluminumProfile(length, Orientation.HORIZONTAL_Z))
            return Cube(PROFILE_SIZE, PROFILE_SIZE, length)
        }

        // Формат отчёта 1-в-1 с web-версией (pc_viewer_3d profiles.ts generateReport)
        fun generateReport(): String {
            val axisOf = { o: Orientation ->
                when (o) {
                    Orientation.HORIZONTAL_X -> "X"
                    Orientation.VERTICAL -> "Y"
                    Orientation.HORIZONTAL_Z -> "Z"
                }
            }

            // тот же набор кусков, что собирает PcFrame: 4 стойки по Z + слои (низ/верх) 2xX+2xY
            // + bottomBeams по Y + front/backEdge вдоль X + left/rightEdge вдоль Y
            val grouped = cuts.groupBy { it.length to axisOf(it.orientation) }
                .mapValues { (_, v) -> v.size }
                .entries.map { (key, count) -> Triple(key.first, key.second, count) }
                    .sortedWith(compareBy({ it.first }, { it.second }))

            val lines = mutableListOf<String>()
            lines.add("=== Спецификация профиля ===")
            lines.add("")

            var totalLength = 0.0
            var totalPieces = 0
            for ((length, axis, count) in grouped) {
                val total = length * count
                totalLength += total
                totalPieces += count
                lines.add("  По оси $axis: ${fmt(length)}мм x ${count}шт = ${fmt(total)}мм")
            }

            lines.add("")
            lines.add("  Всего кусков: $totalPieces")
            lines.add("  Всего резов: $totalPieces")
            lines.add("  Общая длина: ${fmt(totalLength)}мм (${ "%.1f".format(totalLength / 1000.0) }м)")
            lines.add("  Профиль: 20x20мм")
            lines.add("=========================================")
            return lines.joinToString("\n")
        }

        private fun fmt(n: Double): String {
            val r = Math.round(n * 1000.0) / 1000.0
            return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
        }
    }
}
