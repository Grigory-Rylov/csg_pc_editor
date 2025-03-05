package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel

class DefaultBottomEdgePatcher(
    private val thickness: Double,
    private val objectHeight: Double,
) : WallBottomEdgePatcher {
    override fun backPoint(o: Abstract3dModel): Abstract3dModel {
        return projection(o)
    }

    override fun projection(obj: Abstract3dModel): Abstract3dModel {
        val point = obj.move
        return KeyPlaceholder.placeCube(thickness, objectHeight).move(V3d(point.x, point.y, objectHeight + objectHeight/2))
    }

    override fun leftPoint(o: Abstract3dModel): Abstract3dModel = projection(o)

    override fun rightPoint(o: Abstract3dModel): Abstract3dModel = projection(o)

    override fun frontPoint(o: Abstract3dModel): Abstract3dModel = projection(o)
}
