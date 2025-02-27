package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.Utils.hull
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class OuterCornersWallBuilder(
    private val borderThickness: Double = 1.5,
    private val borderHeight: Double = 4.0,
    private val verticalOffset: Double = 4.0,
    private val horizontalOffset: Double = 8.0,
    private val borderZOffset: Double = -2.0,

    private val outerVerticalOffset: Double = 10.0,
    private val outerHorizontalOffset: Double = 15.0,
    private val outerBorderZOffset: Double = -6.0,
) : CornerWallBuilder {

    private val offset = 2.0

    override fun backLeft(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val back = keyPlace(KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset))
        val left = keyPlace(KeyPlaceholder.placeHolderTopLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset))
        val border = hull(
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset))),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset))),
            back,
            left,
        )

        val wall = hull(
            left, back,
            projection(left),
            projection(back),
        )
        return Union(border, wall)
    }

    override fun backRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val back = keyPlace(KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset))
        val right = keyPlace(KeyPlaceholder.placeHolderTopRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset))
        val border = hull(
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset))),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset))),
            back,
            right,
        )
        val wall = hull(
            back, right, projection(back), projection(right)
        )
        return Union(border, wall)
    }

    override fun frontLeft(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val left = keyPlace(
            KeyPlaceholder.placeHolderBottomLeft().move(-outerHorizontalOffset, 0.0, outerBorderZOffset)
        )
        val front = keyPlace(
            KeyPlaceholder.placeHolderBottomLeft().move(0.0, -outerVerticalOffset, outerBorderZOffset)
        )
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            ),
            front,
            left,
        )

        val wall = hull(
            left, front,
            projection(left),
            projection(front),
        )
        return Union(border, wall)
    }

    override fun frontRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        val front = keyPlace(
            KeyPlaceholder.placeHolderBottomRight().move(0.0, -outerVerticalOffset, outerBorderZOffset)
        )
        val right = keyPlace(
            KeyPlaceholder.placeHolderBottomRight().move(outerHorizontalOffset, 0.0, outerBorderZOffset)
        )
        val border = hull(
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(0.0, -verticalOffset, borderZOffset)
                )
            ),
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
                )
            ),
            front,
            right,
        )

        val wall = hull(
            front, right,
            projection(front),
            projection(right),
        )
        return Union(border, wall)
    }

    private fun verticalCube(obj: Abstract3dModel): Abstract3dModel {
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).move(obj.move)
    }

    private fun projection(obj: Abstract3dModel): Abstract3dModel {
        val point = obj.move
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).move(V3d(point.x, point.y, 0.0))
    }
}
