package com.github.grishberg.cad3d.keyboard.casebody

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel

interface WallBottomEdgePatcher {
    fun backPoint(o: Abstract3dModel): Abstract3dModel
    fun leftPoint(o: Abstract3dModel): Abstract3dModel = o
    fun rightPoint(o: Abstract3dModel): Abstract3dModel = o
    fun frontPoint(o: Abstract3dModel): Abstract3dModel = o
    fun projection(o: Abstract3dModel): Abstract3dModel

    fun verticalPoint(src: V3d): V3d
    fun horizontalPoint(src: V3d): V3d
}
