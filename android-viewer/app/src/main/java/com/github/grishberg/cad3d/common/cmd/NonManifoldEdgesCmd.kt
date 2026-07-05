package com.github.grishberg.cad3d.common.cmd

import com.github.grishberg.cad3d.common.DebugCmd
import com.github.grishberg.cad3d.ui.DebugVisualizer
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.Polygon

class NonManifoldEdgesCmd(private val p0: V3d, private val p1: V3d, private val polygons: List<Polygon>) : DebugCmd {
    override fun getDescription(): String {
        return "NonManifoldEdgesCmd p0 = ${p0.toJson()}, p1 = ${p1.toJson()} , polygons = ${polygons.joinToString(",")}"
    }

    override fun render(debugVisualizer: DebugVisualizer) {
        var index = 0
        polygons.forEach {
            val color = DbgConfig.COLORS[index++ % DbgConfig.COLORS.size]
            debugVisualizer.drawDebugPolygon(it, DbgConfig.LINE_THICKNESS, DbgConfig.POINT_THICKNESS, color, Color.CYAN)
        }

        debugVisualizer.drawDebugLine(p0, p1, DbgConfig.LINE_THICKNESS_1, Color.RED)
        debugVisualizer.drawDebugPoint(p0, DbgConfig.POINT_THICKNESS_1, Color.BLUE)
        debugVisualizer.drawDebugPoint(p1, DbgConfig.POINT_THICKNESS_1, Color.GREEN)
    }
}
