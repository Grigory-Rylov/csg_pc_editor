package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.Utils.hull
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class OuterWallsBuilder(
    private val topEdgeOffsetZ: Double,
    private val borderThickness: Double = 1.5,
    private val borderHeight: Double = 4.0,
    private val bottomBorderHeight: Double,
    private val verticalOffset: Double = 5.0,
    private val horizontalOffset: Double = 8.0,
    private val borderZOffset: Double = -2.0,

    private val outerVerticalOffset: Double = 10.0,
    private val outerHorizontalOffset: Double = 15.0,
    private val outerBorderZOffset: Double = -6.0,
    private val bottomEdgePatcher: WallBottomEdgePatcher = DefaultBottomEdgePatcher(
        borderThickness,
        bottomBorderHeight
    ),
) : WallsBuilder {

    override fun backWall(
        onlyBorder: Boolean, keyPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val left = keyPlace(KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset))
        val right = keyPlace(KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset))

        val border = hull(
            left, right,

            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
                )
            ),

            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                )
            )
        )
        if (onlyBorder) {
            return border
        }
        val wall = hull(
            left, right,
            bottomEdgePatcher.backPoint(left),
            bottomEdgePatcher.backPoint(right)
        )
        return Union(border, wall)
    }

    override fun backMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val left =
            leftPlace.invoke(KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset))
        val right =
            rightPlace.invoke(KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset))
        val border = hull(
            left, right, verticalCube(
                leftPlace.invoke(
                    KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                )
            ), verticalCube(
                rightPlace.invoke(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
                )
            )
        )
        val wall = hull(
            left, right,
            bottomEdgePatcher.backPoint(left),
            bottomEdgePatcher.backPoint(right),
        )
        return Union(border, wall)
    }

    override fun leftWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val top = keyPlace(KeyPlaceholder.placeHolderTopLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset))
        val bottom =
            keyPlace(KeyPlaceholder.placeHolderBottomLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset))
        val border = hull(
            top, bottom,
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            ),
        )

        val wall = hull(
            top, bottom,
            bottomEdgePatcher.leftPoint(top),
            bottomEdgePatcher.leftPoint(bottom),
        )
        return Union(border, wall)
    }

    override fun leftMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val top =
            leftPlace(KeyPlaceholder.placeHolderBottomLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset))
        val bottom =
            rightPlace(KeyPlaceholder.placeHolderTopLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset))
        val border = hull(
            top,
            bottom,
            verticalCube(
                leftPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            ),
            verticalCube(
                rightPlace(
                    KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            ),
        )

        val wall = hull(
            top, bottom, bottomEdgePatcher.leftPoint(top), bottomEdgePatcher.leftPoint(bottom)
        )

        return Union(border, wall)
    }

    override fun frontWall(
        leftOffset: Double, rightOffset: Double,
        onlyBorder: Boolean,
        keyPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel {
        val left =
            keyPlace(KeyPlaceholder.placeHolderBottomLeft().move(leftOffset, -outerVerticalOffset, outerBorderZOffset))
        val right = keyPlace(
            KeyPlaceholder.placeHolderBottomRight().move(rightOffset, -outerVerticalOffset, outerBorderZOffset)
        )

        val border = hull(
            left, right,
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(leftOffset, -verticalOffset, borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(rightOffset, -verticalOffset, borderZOffset)
                )
            ),
        )
        if (onlyBorder) {
            return border
        }
        val wall = hull(
            left, right, bottomEdgePatcher.frontPoint(left), bottomEdgePatcher.frontPoint(right)
        )

        return Union(border, wall)
    }

    override fun frontMidWall(
        leftOffset: Double,
        rightOffset: Double,
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val left =
            leftPlace(KeyPlaceholder.placeHolderBottomRight().move(leftOffset, -outerVerticalOffset, outerBorderZOffset))
        val right =
            rightPlace(KeyPlaceholder.placeHolderBottomLeft().move(rightOffset, -outerVerticalOffset, outerBorderZOffset))
        val border = hull(
            left,
            right,
            verticalCube(
                leftPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(leftOffset, -verticalOffset, borderZOffset)
                )
            ),
            verticalCube(
                rightPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(rightOffset, -verticalOffset, borderZOffset)
                )
            ),
        )

        val wall = hull(
            left, right,
            bottomEdgePatcher.frontPoint(left),
            bottomEdgePatcher.frontPoint(right),
        )

        return Union(border, wall)
    }

    override fun rightWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val top = keyPlace(KeyPlaceholder.placeHolderTopRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset))
        val bottom =
            keyPlace(KeyPlaceholder.placeHolderBottomRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset))

        val border = hull(
            top, bottom,
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
                )
            ),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset))),
        )

        val wall = hull(
            top, bottom,
            bottomEdgePatcher.rightPoint(top),
            bottomEdgePatcher.rightPoint(bottom),
        )

        return Union(border, wall)
    }

    override fun rightMidWall(
        backPlace: (Abstract3dModel) -> Abstract3dModel, frontPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val top = frontPlace(KeyPlaceholder.placeHolderTopRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset))
        val bottom =
            backPlace(KeyPlaceholder.placeHolderBottomRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset))

        val border = hull(
            bottom, top, verticalCube(
                backPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
                )
            ), verticalCube(
                frontPlace(
                    KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset)
                )
            )
        )

        val wall = hull(
            top, bottom,
            bottomEdgePatcher.rightPoint(top),
            bottomEdgePatcher.rightPoint(bottom),
        )
        return Union(border, wall)
    }

    override fun midEdge(
        midPlace: (Abstract3dModel) -> Abstract3dModel,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val left =
            leftPlace(KeyPlaceholder.placeHolderBottomRight().move(0.0, -outerVerticalOffset, outerBorderZOffset))
        val right =
            rightPlace(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -outerVerticalOffset, outerBorderZOffset))

        val border = hull(
            left, right,
            verticalCube(leftPlace(KeyPlaceholder.placeHolderBottomRight().move(0.0, -verticalOffset, borderZOffset))),
            verticalCube(rightPlace(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset))),
        )

        val wall = hull(
            left, right,
            bottomEdgePatcher.projection(left),
            bottomEdgePatcher.projection(right),
        )
        return Union(border, wall)
    }

    private fun verticalCube(obj: Abstract3dModel): Abstract3dModel {
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).moveZ(topEdgeOffsetZ).move(obj.move)
    }
}
