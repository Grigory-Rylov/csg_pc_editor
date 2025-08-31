package com.github.grishberg.cad3d.keyboard.screws

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.PlacePointType
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.casebody.controllers.ControllerHolderDimensions
import com.github.grishberg.cad3d.keyboard.casebody.wall.ControllerHolderWall
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union

/**
 * Установка
 */
class ScrewWallPlaces(
    private val cfg: KeyboardConfig,
    private val wallsSettings: WallsSettings,
    private val keyPlace: KeyPlace,
    private val thumbKeyPlace: ThumbKeyPlace,
    private val controllerHolderWall: ControllerHolderWall,
    private val controllerHolderDimensions: ControllerHolderDimensions,
) {

    fun place(o: Abstract3dModel, heightMode: HeightMode): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        val cube = Cube(1.0)

        models.add(placeControllerScrews(o, heightMode, ControllerMode.All))

        models.add(placeRightBack(o, keyPlace.place(cfg.lastCol, 0, cube), offsetY = 10.0))
        models.add(placeRightFront(o, keyPlace.place(cfg.lastCol, cfg.lastRow, cube), offsetY = -4.0))

        models.add(placeFrontCenter(o, keyPlace.place(3, cfg.lastRow, cube), offsetY = -23.5))

        if (!cfg.isSkeletonMode) {
            models.add(placeTop(o, keyPlace.place(3, 0, cube), offsetY = 12.0))
        }

        models.add(placeFrontCenter(o, thumbKeyPlace.placeL(cube), offsetX = -0.5, offsetY = -10.0))
        return Union(models)
    }

    fun placeControllerScrews(o: Abstract3dModel, heightMode: HeightMode, mode: ControllerMode): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        val horizontalOffset = controllerHolderDimensions.distanceBetweenControllerHolderMountX
        val verticalOffset = controllerHolderDimensions.distanceBetweenControllerHolderMountY

        val controllerOffsetZ = when (heightMode) {
            HeightMode.Walls -> cfg.controllerPlateHeight
            else -> 0.0
        }

        val screwOffset = wallsSettings.borderThickness + cfg.screwHolderWallhickness + cfg.screwNutHoleDiameter / 2
        val calculatePlacePoint = keyPlace.calculatePlacePoint(
            0, 0, PlacePointType.BackLeftBottom, -wallsSettings.outerLeftOffset, wallsSettings.outerBorderZOffset
        )
        val wallLeftOffset = calculatePlacePoint.x
        val backWallPoint = V3d(
            wallLeftOffset + screwOffset, controllerHolderWall.getWallPoint(
                keyPlace.calculatePlacePoint(
                    0, 0, PlacePointType.BackLeftBottom
                )
            ).y - screwOffset, controllerOffsetZ
        )

        if (mode == ControllerMode.All || mode == ControllerMode.Back || mode == ControllerMode.Side) {
            models.add(o.move(backWallPoint))
        }

        if (mode == ControllerMode.All || mode == ControllerMode.Back) {
            models.add(
                o.move(
                    V3d(backWallPoint.x + horizontalOffset, backWallPoint.y, controllerOffsetZ)
                )
            )
        }

        if (mode == ControllerMode.All || mode == ControllerMode.Side) {
            models.add(
                o.move(
                    V3d(backWallPoint.x, backWallPoint.y - verticalOffset, controllerOffsetZ)
                )
            )
        }

        return Union(models)
    }

    private fun placeRight(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(wallsSettings.outerHorizontalOffset, 0.0, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeRightBack(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(wallsSettings.outerHorizontalOffset, wallsSettings.outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y + 5.0, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    private fun placeRightFront(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(wallsSettings.outerHorizontalOffset, -wallsSettings.outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY - 1.5, offsetZ)))
    }

    private fun placeTop(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(0.0, wallsSettings.outerVerticalOffset, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))

    }

    private fun placeFrontCenter(
        obj: Abstract3dModel,
        place: Abstract3dModel,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0
    ): Abstract3dModel {
        val p = place.move(0.0, -wallsSettings.outerVerticalOffset - 1.5, 0.0).move
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(offsetX, offsetY, offsetZ)))
    }

    enum class HeightMode { Walls, Plate, ControllerHolder,
    }

    enum class ControllerMode { All, Back, Side, }
}
