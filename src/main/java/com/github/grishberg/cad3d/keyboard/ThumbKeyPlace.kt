package com.github.grishberg.cad3d.keyboard

import com.github.grishberg.cad3d.keyboard.cfg.ThumbClusterSettings
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel

class ThumbKeyPlace(val thumbConfig: ThumbClusterSettings) {

    fun thumbPlace(obj: Abstract3dModel): Abstract3dModel {
        return placeR(obj).addModel(placeM(obj)).addModel(placeL(obj))
    }

    fun placeR(obj: Abstract3dModel): Abstract3dModel {
        return place(
            obj,
            14.0,
            -40.0,
            10.0,
            V3d((-5 + thumbConfig.xOffset), (-10 + thumbConfig.yOffset), (5 + thumbConfig.zOffset))
        )
    }

    fun placeM(obj: Abstract3dModel): Abstract3dModel {
        return place(
            obj,
            12.0,
            -39.0,
            22.0,
            V3d((-22 + thumbConfig.xOffset), (-14 + thumbConfig.yOffset), (-7 + thumbConfig.zOffset))
        )
    }

    fun placeL(obj: Abstract3dModel): Abstract3dModel {
        return place(
            obj,
            8.0,
            -44.0,
            28.0,
            V3d(-35.0 + thumbConfig.xOffset, -20.5 + thumbConfig.yOffset, (-21 + thumbConfig.zOffset))
        )
    }

    private fun place(
        obj: Abstract3dModel, xAngle: Double, yAngle: Double, zAngle: Double, offset: V3d
    ): Abstract3dModel {
        return obj.rotate(xAngle, yAngle, zAngle).move(offset)
    }

}
