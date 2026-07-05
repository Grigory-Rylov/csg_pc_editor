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

        fun generateReport(): String {
            val grouped = cuts.groupBy { it.length to it.orientation }
                .mapValues { (_, v) -> v.size }
                .entries.sortedBy { it.key.first }

            val sb = StringBuilder()
            sb.appendLine("=== Aluminum Profile Bill of Materials ===")
            sb.appendLine()

            var totalLength = 0.0
            for ((key, count) in grouped) {
                val (length, orientation) = key
                val label = when (orientation) {
                    Orientation.VERTICAL -> "Vertical (Y)"
                    Orientation.HORIZONTAL_X -> "Horizontal (X)"
                    Orientation.HORIZONTAL_Z -> "Horizontal (Z)"
                }
                val total = length * count
                totalLength += total
                sb.appendLine("  $label: ${length}mm x ${count}pcs = ${total}mm")
            }

            sb.appendLine()
            sb.appendLine("  Total: ${totalLength}mm (${"%.1f".format(totalLength / 1000.0)}m)")
            sb.appendLine("  Profile: 20x20mm aluminum extrusion")
            sb.appendLine("=========================================")
            return sb.toString()
        }
    }
}
