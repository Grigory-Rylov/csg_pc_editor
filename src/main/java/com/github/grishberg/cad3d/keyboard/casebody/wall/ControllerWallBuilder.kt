package com.github.grishberg.cad3d.keyboard.casebody.wall

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.casebody.DefaultBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.casebody.WallBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.tranzitions.Union

class ControllerWallBuilder(
    private val controllerHolderWall: ControllerHolderWall,
    private val keyPlace: KeyPlace,
    private val isSkeletonMode: Boolean,
    private val topEdgeOffsetZ: Double,
    private val cfg: WallsSettings,
    private val bottomEdgePatcher: WallBottomEdgePatcher = DefaultBottomEdgePatcher(
        cfg.borderThickness, cfg.bottomBorderHeight
    ),

    ) {

    fun createWall(): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        models.add(backLeftCorner())

        val left = keyPlace.place(
            0,
            0,
            KeyPlaceholder.placeHolderTopLeft().move(-cfg.outerLeftOffset, 0.0, cfg.outerBorderZOffset)
        )

        val right = keyPlace.place(
            2,
            0,
            KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )

        val mid = keyPlace.place(
            1, 0,
            KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )

        val midLeft = keyPlace.place(
            0, 0,
            KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )


        val backControllerLeft = controllerHolderWall.getWallPoint(left.move)
        val backControllerRight = controllerHolderWall.getWallPoint(right.move)
        val backControllerMid = controllerHolderWall.getWallPoint(mid.move)
        val backControllerMidLeft = controllerHolderWall.getWallPoint(midLeft.move)


        if (isSkeletonMode) {
            //top edge
            val sphere = Sphere(Radius.fromRadius(cfg.borderThickness))
            models.add(
                Hull(
                    sphere.move(backControllerLeft), sphere.move(backControllerRight)
                )
            )

            //bottom edge

            models.add(
                Hull(
                    bottomBorderObject().move(backControllerRight.projectionZ(cfg.borderThickness)),
                    bottomBorderObject().move(backControllerLeft.projectionZ(cfg.borderThickness))
                )
            )

            //left vertical
            models.add(Hull(
                sphere.move(backControllerLeft),
                bottomBorderObject().move(backControllerLeft.projectionZ(cfg.borderThickness)),
            ))

            //left corner - bottom
            models.add(Hull(
                left,
                bottomEdgePatcher.leftPoint(left),
            ))

            //left corner - back top controller
            models.add(Hull(
                left,
                sphere.move(backControllerLeft),
            ))

            //mid - back top controller
            models.add(Hull(
                mid,
                sphere.move(backControllerMid),
            ))

            //mid left - back top controller
            models.add(Hull(
                midLeft,
                sphere.move(backControllerMidLeft),
            ))

            models.add(Hull(
                bottomBorderObject().move(backControllerLeft.projectionZ(cfg.borderThickness)),
                bottomEdgePatcher.leftPoint(left),
            ))

            // center left
            val centerLeft = keyPlace.place(
                2, 0,
                KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
            )

            models.add(Hull(
                centerLeft,
                bottomEdgePatcher.backPoint(centerLeft),
            ))
        }

        //models.add(backWall(0))
        //models.add(backWall(1))

        return Union(models)
    }

    private fun backWall(column: Int): Abstract3dModel {
        val left = keyPlace.place(
            column,
            0,
            KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )
        val right = keyPlace.place(
            column,
            0,
            KeyPlaceholder.placeHolderTopRight().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )

        val backControllerRight = controllerHolderWall.getWallPoint(right.move)
        val backControllerLeft = controllerHolderWall.getWallPoint(left.move)

        if (isSkeletonMode) {
            val objects = mutableListOf<Abstract3dModel>()

            val sphere = Sphere(Radius.fromRadius(cfg.borderThickness))
            objects.add(Utils.hull(left, sphere.move(backControllerLeft)))
            objects.add(Utils.hull(right, sphere.move(backControllerRight)))
            objects.add(
                Utils.hull(
                    sphere.move(backControllerLeft),
                    sphere.move(backControllerRight),
                )
            )

            val bottomZOffset = cfg.borderThickness
            objects.add(
                Utils.hull(
                    sphere.move(backControllerRight),
                    bottomBorderObject().move(backControllerRight.projectionZ(bottomZOffset))
                )
            )

            objects.add(
                Utils.hull(
                    sphere.move(backControllerLeft.projectionZ(bottomZOffset)),
                    bottomBorderObject().move(backControllerRight.projectionZ(bottomZOffset))
                )
            )

            objects.add(
                Utils.hull(
                    left,
                    left.moveY(-2.0),
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.leftPoint(left.moveY(-2.0)),
                )
            )
            objects.add(
                Utils.hull(
                    right,
                    bottomEdgePatcher.backPoint(right),
                )
            )
            objects.add(
                Utils.hull(
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.backPoint(right),
                )
            )
            return Union(objects)
        }

        val wall = Utils.hull(
            left, right,
            bottomEdgePatcher.leftPoint(left),
            bottomEdgePatcher.backPoint(right),
        )
        return Union(wall)
    }

    private fun backLeftCorner(): Abstract3dModel {

        val back =
            keyPlace.place(0, 0, KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset))
        val left =
            keyPlace.place(0, 0, KeyPlaceholder.placeHolderTopLeft().move(-cfg.outerLeftOffset, 0.0, cfg.outerBorderZOffset))

        val backControllerBack = controllerHolderWall.getWallPoint(back.move)
        val backControllerLeft = controllerHolderWall.getWallPoint(left.move)

        val border = Utils.hull(
            verticalCube(
                keyPlace.place(
                    0,
                    0,
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(
                keyPlace.place(
                    0,
                    0,
                    KeyPlaceholder.placeHolderTopLeft().move(-cfg.leftOffset, 0.0, cfg.borderZOffset)
                )
            ),
            back,
            left,
        )

        if (isSkeletonMode) {
            val objects = mutableListOf<Abstract3dModel>()
            objects.add(border)

            val sphere = Sphere(Radius.fromRadius(cfg.borderThickness))
            /*
            objects.add(Utils.hull(left, sphere.move(backControllerLeft)))
            objects.add(Utils.hull(back, sphere.move(backControllerBack)))
            objects.add(
                Utils.hull(
                    sphere.move(backControllerLeft),
                    sphere.move(backControllerBack),
                )
            )

            objects.add(
                Utils.hull(
                    sphere.move(backControllerLeft),
                    bottomBorderObject().move(backControllerLeft.projectionZ(borderThickness))
                )
            )
            objects.add(
                Utils.hull(
                    sphere.move(backControllerBack),
                    bottomBorderObject().move(backControllerBack.projectionZ(borderThickness))
                )
            )

            objects.add(
                Utils.hull(
                    sphere.move(backControllerLeft.projectionZ(borderThickness)),
                    bottomBorderObject().move(backControllerBack.projectionZ(borderThickness))
                )
            )

            objects.add(
                Utils.hull(
                    left,
                    left.moveY(-2.0),
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.leftPoint(left.moveY(-2.0)),
                )
            )
*/

            return Union(objects)
        }

        val wall = Utils.hull(
            left, back,
            bottomEdgePatcher.leftPoint(left),
            bottomEdgePatcher.backPoint(back),
        )
        return Union(border, wall)
    }

    private fun verticalCube(obj: Abstract3dModel): Abstract3dModel {
        return borderObject(cfg.borderThickness, cfg.borderHeight).moveZ(topEdgeOffsetZ).move(obj.move)
    }

    private fun bottomBorderObject(): Abstract3dModel {
        return borderObject(cfg.borderThickness, cfg.bottomBorderHeight)
    }

    private fun borderObject(thickness: Double, height: Double): Abstract3dModel {
        return Utils.cylinder(thickness, height)
    }

}
