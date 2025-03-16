package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.KeyPlace
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel

/**
 * Places controller and controller holder.
 */
class ControllerPlace(private val keyPlace: KeyPlace) {

    fun place(
        obj: Abstract3dModel,
    ): Abstract3dModel {
        val p = keyPlace.calculateCoordinates(0, 0)
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(1.0, 13.2, 4.0)))
    }
}
