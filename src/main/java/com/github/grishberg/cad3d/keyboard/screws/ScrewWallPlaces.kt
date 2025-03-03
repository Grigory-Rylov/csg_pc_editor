package com.github.grishberg.cad3d.keyboard.screws

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

class ScrewWallPlaces(
    private val cfg: KeyboardConfig,
    private val keyPlace: KeyPlace,
    private val thumbKeyPlace: ThumbKeyPlace,

    // TODO: move to cfg
    private val outerVerticalOffset: Double = 10.0,
    private val outerHorizontalOffset: Double = 15.0,
    private val outerBorderZOffset: Double = -6.0,

    ) {

    fun place(o: Abstract3dModel): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        val cube = Cube(1.0)
        models.add(placeLeftTop(o, keyPlace.place(0, 0, cube), offsetX = 0.2))

        models.add(placeRightTop(o, keyPlace.place(cfg.lastCol, 0, cube), offsetY = 9.0))
        models.add(placeRightBottom(o, keyPlace.place(cfg.lastCol, cfg.lastRow, cube), offsetY = -4.0))

        models.add(placeBottom(o, keyPlace.place(3, cfg.lastRow, cube), offsetY = -23.5))

        models.add(placeTop(o, keyPlace.place(3, 0, cube), offsetY = 12.0))

        models.add(placeBottom(o, thumbKeyPlace.placeL(cube), offsetX = -8.0, offsetY = -4.0))
        return Union(models)
    }

    private fun placeLeftTop(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(-outerHorizontalOffset, outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeLeft(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(-outerHorizontalOffset, 0.0, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeRight(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(outerHorizontalOffset, 0.0, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeRightTop(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(outerHorizontalOffset, outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeRightBottom(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(outerHorizontalOffset, -outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeTop(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(0.0, outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))

    }

    private fun placeBottom(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(0.0, -outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

}
