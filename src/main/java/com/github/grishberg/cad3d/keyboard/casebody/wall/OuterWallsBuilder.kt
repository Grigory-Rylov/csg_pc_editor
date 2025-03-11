package com.github.grishberg.cad3d.keyboard.casebody.wall

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.Utils.hull
import com.github.grishberg.cad3d.keyboard.Utils.union
import com.github.grishberg.cad3d.keyboard.casebody.DefaultBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.casebody.WallBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.casebody.WallsBuilder
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.EdgeType
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.models.surfaces.S12x3
import eu.printingin3d.javascad.models.surfaces.SmoothSurface
import eu.printingin3d.javascad.models.surfaces.VoronoiSurface
import eu.printingin3d.javascad.tranzitions.Union
import java.util.*
import java.util.function.Consumer


class OuterWallsBuilder(
    private val isSkeletonMode: Boolean,
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
        borderThickness, bottomBorderHeight
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
        if (isSkeletonMode) {
            return Union(
                border,
                // connection
                hull(
                    right, bottomEdgePatcher.backPoint(right)
                ),
                // bottom edge
                hull(
                    bottomEdgePatcher.backPoint(left),
                    bottomEdgePatcher.backPoint(right),
                )
            )
        }
        val wall = hull(
            left, right, bottomEdgePatcher.backPoint(left), bottomEdgePatcher.backPoint(right)
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
        if (isSkeletonMode) {
            return union(
                border, hull(
                    bottomEdgePatcher.backPoint(left),
                    bottomEdgePatcher.backPoint(right),
                )
            )
        }
        val wall = hull(
            left, right,
            bottomEdgePatcher.backPoint(left),
            bottomEdgePatcher.backPoint(right),
        )
        return Union(border, wall)
    }

    override fun leftWall(
        topOffset: Double, bottomOffset: Double, keyPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val top =
            keyPlace(KeyPlaceholder.placeHolderTopLeft().move(-outerHorizontalOffset, topOffset, outerBorderZOffset))
        val bottom = keyPlace(
            KeyPlaceholder.placeHolderBottomLeft().move(-outerHorizontalOffset, bottomOffset, outerBorderZOffset)
        )

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

        if (isSkeletonMode) {
            return union(
                border, hull(
                    bottomEdgePatcher.leftPoint(top), bottomEdgePatcher.leftPoint(bottom)
                )
            )
        }

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

        if (isSkeletonMode) {
            return union(
                border, hull(
                    bottomEdgePatcher.leftPoint(top), bottomEdgePatcher.leftPoint(bottom)
                )
            )
        }

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

        if (isSkeletonMode) {
            return union(
                border, hull(left, bottomEdgePatcher.frontPoint(left)), hull(
                    bottomEdgePatcher.frontPoint(left),
                    bottomEdgePatcher.frontPoint(right),
                )
            )
        }

        val wall = hull(
            left, right, bottomEdgePatcher.frontPoint(left), bottomEdgePatcher.frontPoint(right)
        )

        return Union(border, wall)
    }

    override fun frontMidWall(
        leftOffset: Double,
        rightOffset: Double,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val left = leftPlace(
            KeyPlaceholder.placeHolderBottomRight().move(leftOffset, -outerVerticalOffset, outerBorderZOffset)
        )
        val right = rightPlace(
            KeyPlaceholder.placeHolderBottomLeft().move(rightOffset, -outerVerticalOffset, outerBorderZOffset)
        )

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

        if (isSkeletonMode) {
            return union(
                border, hull(
                    bottomEdgePatcher.frontPoint(left),
                    bottomEdgePatcher.frontPoint(right),
                )
            )
        }

        val wall = hull(
            left, right,
            bottomEdgePatcher.frontPoint(left),
            bottomEdgePatcher.frontPoint(right),
        )

        return Union(border, wall)
    }

    override fun rightWall(
        topOffset: Double, bottomOffset: Double, keyPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        val top =
            keyPlace(KeyPlaceholder.placeHolderTopRight().move(outerHorizontalOffset, topOffset, outerBorderZOffset))
        val bottom = keyPlace(
            KeyPlaceholder.placeHolderBottomRight().move(outerHorizontalOffset, bottomOffset, outerBorderZOffset)
        )

        val border = hull(
            top, bottom,
            verticalCube(
                keyPlace(
                    KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
                )
            ),
            verticalCube(keyPlace(KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset))),
        )

        if (isSkeletonMode) {
            return union(
                border,
                hull(
                    bottomEdgePatcher.rightPoint(top),
                    bottomEdgePatcher.rightPoint(bottom),
                )
            )
        }

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


        if (isSkeletonMode) {
            return union(
                border,
                hull(
                    bottomEdgePatcher.rightPoint(top),
                    bottomEdgePatcher.rightPoint(bottom),
                )
            )
        }

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

        if (isSkeletonMode) {
            return union(
                border, hull(
                    bottomEdgePatcher.projection(left),
                    bottomEdgePatcher.projection(right),
                )
            )
        }

        val wall = hull(
            left, right,
            bottomEdgePatcher.projection(left),
            bottomEdgePatcher.projection(right),
        )
        return Union(border, wall)
    }

    fun curveWall(
        cfg: KeyboardConfig,
        keyPlace: KeyPlace,
        thumbKeyPlace: ThumbKeyPlace,
    ): Abstract3dModel {
        val topEdgePoints = mutableListOf<V3d>()
        val topBorderPoints = mutableListOf<V3d>()
        val bottomEdgePoints = mutableListOf<V3d>()

        for (column in 0 until cfg.columnsCount) {
            val topKeyPlace = { obj: Abstract3dModel ->
                keyPlace.place(
                    column, 0, obj
                )
            }

            val leftTopEdge = topKeyPlace(
                KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
            )

            val rightTopEdge = topKeyPlace(
                KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
            )

            topEdgePoints.add(leftTopEdge.move)
            topEdgePoints.add(rightTopEdge.move)

            val leftBorderEdge =
                topKeyPlace(KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset))
            val rightBorderEdge =
                topKeyPlace(KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset))

            topBorderPoints.add(leftBorderEdge.move)
            topBorderPoints.add(rightBorderEdge.move)

            val bottomLeft = bottomEdgePatcher.backPoint(leftTopEdge)
            val bottomRight = bottomEdgePatcher.backPoint(rightTopEdge)


            bottomEdgePoints.add(bottomLeft.move)
            bottomEdgePoints.add(bottomRight.move)
        }

        val controlPoints = arrayOf(
            bottomEdgePoints.toTypedArray(),
            topBorderPoints.toTypedArray(),
            topEdgePoints.toTypedArray(),
        )


        // Создание случайных сайтов
        val sites: MutableList<V3d> = ArrayList()
        val rand: Random = Random()
        for (i in 0..9) {
            sites.add(
                V3d(
                    rand.nextDouble() * 100 - 50,
                    rand.nextDouble() * 20,
                    rand.nextDouble() * 50
                )
            )
        }

        val vs = VoronoiSurface(
            controlPoints,
            sites,
            2.5 // Ширина ребер
        )
        val contours = vs.calculateVoronoiEdges(4)


        val holes = mutableListOf<Abstract3dModel>()
        // Визуализация контуров
        for (contour in contours) {
            val points = mutableListOf<Abstract3dModel>()
            for (point in contour) {
                points.add(Sphere(Radius.fromDiameter(1.0)).move(point))
            }
            //holes.add(Hull(points))
            holes.addAll(points)
        }
        /*val surfaceBuilder: Abstract3dModel = SmoothSurface(
            BicubicSurfaceSpline.bSplineSurface(controlPoints, 10),
            borderHeight,
            EdgeType.Normal,
            EdgeType.Normal,
            EdgeType.Normal,
            EdgeType.Normal
        )
*/
        val surface = SmoothSurface(
            S12x3.create(controlPoints).buildSurfaceStrategy(5),
            borderThickness * 2,
            EdgeType.Vertical,
            EdgeType.Normal,
            EdgeType.HorizontalY,
            EdgeType.Normal
        );
        return Union(holes)
    }

    private fun verticalCube(obj: Abstract3dModel): Abstract3dModel {
        return borderObject(borderThickness, borderHeight).moveZ(topEdgeOffsetZ).move(obj.move)
    }

    private fun borderObject(thickness: Double, height: Double): Abstract3dModel {
        return Utils.cylinder(thickness, height)
    }
}
