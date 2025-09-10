package com.github.grishberg.cad3d.keyboard.casebody.wall

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.Utils.hull
import com.github.grishberg.cad3d.keyboard.casebody.CornerWallBuilder
import com.github.grishberg.cad3d.keyboard.casebody.DefaultBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.casebody.WallBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color

class OuterCornersWallBuilder(
    private val isSkeletonMode: Boolean,
    private val topEdgeOffsetZ: Double,
    private val cfg: WallsSettings,
    private val bottomEdgePatcher: WallBottomEdgePatcher = DefaultBottomEdgePatcher(
        cfg.borderThickness, cfg.bottomBorderHeight
    ),
    private val isThumb: Boolean = false,
) : CornerWallBuilder {

    override fun backLeft(
        keyPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val back =
            keyPlace(KeyPlaceholder.placeHolderBackLeft().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset))
        val left =
            keyPlace(KeyPlaceholder.placeHolderBackLeft().move(-cfg.outerLeftOffset, 0.0, cfg.outerBorderZOffset))
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBackLeft().move(0.0, cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderBackLeft().move(-cfg.leftOffset, 0.0, cfg.borderZOffset))),
            topBorderObj(back),
            topBorderObj(left),
        )

        if (isSkeletonMode) {
            return Union(
                border, hull(
                    left,
                    left.moveY(-2.0),
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.leftPoint(left.moveY(-2.0)),
                ), hull(
                    back,
                    bottomEdgePatcher.backPoint(back),
                ), hull(
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.backPoint(back),
                )
            )
        }

        val wall = hull(
            topBorderObj(left), topBorderObj(back),
            bottomEdgePatcher.leftPoint(left),
            bottomEdgePatcher.backPoint(back),
        )
        return Union(border, wall)
    }

    override fun backRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): List<Abstract3dModel> {
        if (isThumb) {
            return listOf(backRightThumb(keyPlace))
        }
        val count: Int = 20
        val back =
            keyPlace(KeyPlaceholder.placeHolderBackRight().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset))
        val right =
            keyPlace(KeyPlaceholder.placeHolderBackRight().move(cfg.outerRightOffset, 0.0, cfg.outerBorderZOffset))

        val start = right.move
        val end = back.move

        var lastTop = bottomEdgePatcher.backPoint(right)
        var lastBottom = bottomEdgePatcher.rightPoint(right)

        val models = mutableListOf<Abstract3dModel>()

        // Генерируем промежуточные точки
        for (i in 0..count) {
            val t: Double = i.toDouble() / count
            val intermediatePoint: V3d = start.lerp(end, t)
            val topObject = topBorderObj(intermediatePoint)
            val bottomObject = bottomEdgePatcher.backPoint(topObject)
            models.add(hull(topObject, bottomObject, lastTop, lastBottom))
            lastTop = topObject
            lastBottom = bottomObject
        }

        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBackRight().move(0.0, cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderBackRight().move(cfg.rightOffset, 0.0, cfg.borderZOffset))),
            topBorderObj(back),
            topBorderObj(right),
        )
        models.add(border)

        models.add(
            hull(
                topBorderObj(right),
                bottomCylinder(right.move),
                bottomEdgePatcher.backPoint(right),
            )
        )

        if (isSkeletonMode) {
            return listOf(
                border,
                hull(
                    right,
                    right.moveY(-2.0),
                    bottomEdgePatcher.rightPoint(right),
                    bottomEdgePatcher.rightPoint(right.moveY(-2.0)),
                ),
                hull(back, bottomEdgePatcher.backPoint(back)),
                hull(bottomEdgePatcher.backPoint(back), bottomEdgePatcher.rightPoint(right))
            )
        }
        return models
    }

    private fun backRightThumb(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val back =
            keyPlace(KeyPlaceholder.placeHolderBackRight().move(0.0, cfg.outerVerticalOffset, cfg.outerBorderZOffset))
        val right =
            keyPlace(KeyPlaceholder.placeHolderBackRight().move(cfg.outerRightOffset, 0.0, cfg.outerBorderZOffset))
        val right2 =
            keyPlace(KeyPlaceholder.placeHolderBackRight().move(cfg.outerRightOffset, -2.0, cfg.outerBorderZOffset))
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBackRight().move(0.0, cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderBackRight().move(cfg.rightOffset, 0.0, cfg.borderZOffset))),
            topBorderObj(back),
            topBorderObj(right),
        )
        if (isSkeletonMode) {
            return Union(
                border,
                hull(
                    right,
                    right.moveY(-2.0),
                    bottomEdgePatcher.rightPoint(right),
                    bottomEdgePatcher.rightPoint(right.moveY(-2.0)),
                ),
                hull(back, bottomEdgePatcher.backPoint(back)),
                hull(bottomEdgePatcher.backPoint(back), bottomEdgePatcher.rightPoint(right))
            )
        }
        val wall = hull(
            topBorderObj(back),
            topBorderObj(right),
            bottomEdgePatcher.backPoint(back),
            bottomEdgePatcher.rightPoint(right)
        )
        return Union(border, wall)
    }

    override fun frontLeft(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val left = keyPlace(
            KeyPlaceholder.placeHolderFrontLeft().move(-cfg.outerLeftOffset, 0.0, cfg.outerBorderZOffset)
        )
        val front = keyPlace(
            KeyPlaceholder.placeHolderFrontLeft().move(0.0, -cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderFrontLeft().move(0.0, -cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderFrontLeft().move(-cfg.leftOffset, 0.0, cfg.borderZOffset)
                )
            ),
            topBorderObj(front),
            topBorderObj(left),
        )

        if (isSkeletonMode) {
            return Union(
                border,
                hull(left, bottomEdgePatcher.leftPoint(left)),
                hull(front, bottomEdgePatcher.leftPoint(front)),
                hull(
                    bottomEdgePatcher.leftPoint(left),
                    bottomEdgePatcher.frontPoint(front),
                )
            )
        }

        val wall = hull(
            topBorderObj(left), topBorderObj(front),
            bottomEdgePatcher.leftPoint(left),
            bottomEdgePatcher.frontPoint(front),
        )
        return Union(border, wall)
    }

    override fun frontRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val front = keyPlace(
            KeyPlaceholder.placeHolderFrontRight().move(0.0, -cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )
        val right = keyPlace(
            KeyPlaceholder.placeHolderFrontRight().move(cfg.outerRightOffset, 0.0, cfg.outerBorderZOffset)
        )
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderFrontRight().move(0.0, -cfg.verticalOffset, cfg.borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderFrontRight().move(cfg.rightOffset, 0.0, cfg.borderZOffset)
                )
            ),
            topBorderObj(front),
            topBorderObj(right),
        )

        if (isSkeletonMode) {
            return Union(
                border,
                hull(front, bottomEdgePatcher.frontPoint(front)),
                hull(right, bottomEdgePatcher.rightPoint(right)),
                hull(
                    bottomEdgePatcher.frontPoint(front),
                    bottomEdgePatcher.rightPoint(right),
                )
            )
        }

        val wall = hull(
            topBorderObj(front), topBorderObj(right),
            bottomEdgePatcher.frontPoint(front),
            bottomEdgePatcher.rightPoint(right),
        )
        return Union(border, wall)
    }

    override fun frontRightToMatrix(
        ThumbR: (Abstract3dModel) -> Abstract3dModel,
        matrixOuterPlace: (Abstract3dModel) -> Abstract3dModel,
        matrixInnerPlace: (Abstract3dModel) -> Abstract3dModel,
    ): List<Abstract3dModel> {
        val thumbFrontRight = ThumbR(
            KeyPlaceholder.placeHolderFrontRight().move(2.0, 0.0, cfg.borderZOffset)
        )
        val thumbBackRight = ThumbR(
            KeyPlaceholder.placeHolderBackRight().move(2.0, -3.0, cfg.borderZOffset)
        )
        val thumbVertexFront = ThumbR(
            KeyPlaceholder.placeHolderFrontRight().move(0.0, -cfg.verticalOffset, cfg.borderZOffset)
        )
        val frontOuter = ThumbR(
            KeyPlaceholder.placeHolderFrontRight().move(0.0, -cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )

        val innerLeft = matrixInnerPlace(
            KeyPlaceholder.placeHolderFrontLeft().move(0.0, -cfg.verticalOffset, cfg.borderZOffset)
        )

        val innerRight = matrixInnerPlace(
            KeyPlaceholder.placeHolderFrontRight().move(0.0, -cfg.verticalOffset, cfg.borderZOffset)
        )

        val outerLeft = matrixOuterPlace(
            KeyPlaceholder.placeHolderFrontLeft().move(0.0, -cfg.outerVerticalOffset, cfg.outerBorderZOffset)
        )

        val wall1 = hull(
            verticalCube(thumbFrontRight),
            verticalCube(thumbBackRight),
            verticalCube(innerLeft),
            verticalCube(outerLeft),
        ).withColor(Color.PINK)

        val bottomBorder = hull(
            verticalCube(innerLeft),
            verticalCube(innerRight),
            verticalCube(outerLeft),
        ).withColor(Color.BROWN)

        val topBorder = hull(
            verticalCube(thumbFrontRight),
            verticalCube(thumbVertexFront),
            topBorderObj(frontOuter),
            bottomEdgePatcher.projection(frontOuter),
            bottomEdgePatcher.projection(outerLeft),
            verticalCube(outerLeft),
        ).withColor(Color.RED)

        if (isSkeletonMode) {
            return listOf(
                topBorder, hull(
                    bottomEdgePatcher.projection(frontOuter),
                    bottomEdgePatcher.projection(outerLeft),
                )
            )
        }

        return listOf(
            wall1,
            bottomBorder,
            topBorder,
        )
    }

    private fun verticalCube(obj: Abstract3dModel): Abstract3dModel {
        return borderObject(cfg.borderThickness, cfg.borderHeight).moveZ(topEdgeOffsetZ).move(obj.move)
    }

    private fun bottomCylinder(point: V3d): Abstract3dModel {
        return Utils.cylinder(
            cfg.borderThickness, cfg.bottomBorderHeight
        ).move(point.projectionZ(cfg.bottomBorderHeight / 2))
    }

    private fun borderObject(thickness: Double, height: Double): Abstract3dModel {
        return Utils.cylinder(thickness, height)
    }

    private fun topBorderObj(obj: Abstract3dModel): Abstract3dModel {
        return Utils.sphere(cfg.borderThickness / 2.0).move(obj.move)
    }

    private fun topBorderObj(point: V3d): Abstract3dModel {
        return Utils.sphere(cfg.borderThickness / 2.0).move(point)
    }
}
