package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.Utils.hull
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.EdgeType
import eu.printingin3d.javascad.models.surfaces.SmoothSurface
import eu.printingin3d.javascad.models.surfaces.bicubic.BicubicSurfaceSpline

class Walls(
    private val cfg: KeyboardConfig,
    private val keyPlace: KeyPlace,
    private val thumbKeyPlace: ThumbKeyPlace,
    private val topEdgeOffsetZ: Double,
) {

    private val wallVerticalOffset = 5.0
    private val wallHorizontalOffset = 10.0
    private val models = ArrayList<Abstract3dModel>()
    var bordersOffset = cfg.bordersOffset
    val thumbRightOffset = 4.0
    val thumbOuterRightOffset = 7.0
    val firstColunsBordersOffset = 4.0
    val horizontalOffset = 8.0
    val borderZOffset = -2.0

    fun createBorders(borderThickness: Double = 1.5, borderHeight: Double = 4.0): Abstract3dModel {
        models.clear()

        //columns
        thumbBorders(
            InnerBordersBuilder(
                thumbKeyPlace = thumbKeyPlace,
                rightOffset = 4.0,
                borderThickness = borderThickness,
                borderHeight = borderHeight
            ), InnerCorners(rightOffset = 4.0, borderThickness = borderThickness, borderHeight = borderHeight)
        )

        matrixBorders(
            InnerBordersBuilder(
                thumbKeyPlace = thumbKeyPlace, borderThickness = borderThickness, borderHeight = borderHeight
            ), InnerCorners(borderThickness = borderThickness, borderHeight = borderHeight)
        )

        betweenThumbAndMatrixBorders(borderThickness, borderHeight)

        return Utils.union(models)
    }

    fun createWalls(
        borderThickness: Double = 1.5,
        borderHeight: Double = 4.0,
        bottomBorderHeight: Double,
    ): Abstract3dModel {
        models.clear()

        val bottomEdgePatcher = CircleBottomEdgePatcher(
            thickness = 1.5,
            objectHeight = bottomBorderHeight,
            radiusX = 100.0,
            radiusY = 80.0,
            centerY = -30.0,
        )

        matrixBorders(
            OuterWallsBuilder(
                topEdgeOffsetZ = topEdgeOffsetZ,
                bottomBorderHeight = bottomBorderHeight,
                verticalOffset = wallVerticalOffset,
                borderHeight = borderHeight,
                horizontalOffset = wallHorizontalOffset,
                borderThickness = borderThickness,
                bottomEdgePatcher = bottomEdgePatcher
            ), OuterCornersWallBuilder(
                topEdgeOffsetZ = topEdgeOffsetZ,
                bottomBorderHeight = bottomBorderHeight,
                verticalOffset = wallVerticalOffset,
                borderHeight = borderHeight,
                leftOffset = wallHorizontalOffset,
                rightOffset = wallHorizontalOffset,
                borderThickness = borderThickness,
                bottomEdgePatcher = bottomEdgePatcher
            ), isWallMode = true
        )

        thumbWalls(
            OuterWallsBuilder(
                topEdgeOffsetZ = topEdgeOffsetZ,
                bottomBorderHeight = bottomBorderHeight,
                verticalOffset = wallVerticalOffset,
                horizontalOffset = wallHorizontalOffset,
                borderThickness = borderThickness,
                borderHeight = borderHeight
            ),
            OuterCornersWallBuilder(
                topEdgeOffsetZ = topEdgeOffsetZ,
                bottomBorderHeight = bottomBorderHeight,
                verticalOffset = wallVerticalOffset,
                leftOffset = wallHorizontalOffset,
                rightOffset = thumbRightOffset,
                borderThickness = borderThickness,
                borderHeight = borderHeight,
                outerRightOffset = thumbOuterRightOffset
            ),
            verticalOffset = wallVerticalOffset,
            leftOffset = wallHorizontalOffset,
            rightOffset = thumbRightOffset,
            outerRightOffset = thumbOuterRightOffset,
            bottomEdgePatcher = bottomEdgePatcher,
        )

        return Utils.union(models)
    }

    private fun matrixBorders(
        wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder, isWallMode: Boolean = false
    ) {
        // corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> keyPlace.place(0, 0, obj) })
        //left front
        //models.add(cornerWallBuilder.frontLeft { obj -> keyPlace.place(0, cfg.lastRow, obj) })
        // right back
        models.add(cornerWallBuilder.backRight { obj -> keyPlace.place(cfg.lastCol, 0, obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> keyPlace.place(cfg.lastCol, cfg.lastRow, obj) })

        for (column in 0 until cfg.columnsCount) {

            // back columns
            models.add(wallsBuilder.backWall { obj -> keyPlace.place(column, 0, obj) })
            if (column < 2) {
                continue
            }

            if (isWallMode) {
                continue
            }
            //front columns
            models.add(wallsBuilder.frontWall { obj -> keyPlace.place(column, cfg.lastRow, obj) })

        }
        for (column in 0 until cfg.columnsCount - 1) {
            // back diagonals
            models.add(
                wallsBuilder.backMidWall(
                    leftPlace = { obj -> keyPlace.place(column, 0, obj) },
                    rightPlace = { obj -> keyPlace.place(column + 1, 0, obj) },
                )
            )
            if (column < 2) {
                continue
            }
            if (isWallMode) {
                continue
            }
            // front diagonals
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { obj -> keyPlace.place(column, cfg.lastRow, obj) },
                    rightPlace = { obj -> keyPlace.place(column + 1, cfg.lastRow, obj) },
                )
            )

        }

        for (row in 0 until cfg.rowsCount) {
            //left
            models.add(wallsBuilder.leftWall { obj -> keyPlace.place(0, row, obj) })
            //right
            models.add(wallsBuilder.rightWall { obj -> keyPlace.place(cfg.lastCol, row, obj) })

        }
        for (row in 0 until cfg.rowsCount - 1) {
            models.add(
                wallsBuilder.leftMidWall(
                    leftPlace = { obj -> keyPlace.place(0, row, obj) },
                    rightPlace = { obj -> keyPlace.place(0, row + 1, obj) },
                )
            )

            models.add(
                wallsBuilder.rightMidWall(
                    backPlace = { obj -> keyPlace.place(cfg.lastCol, row, obj) },
                    frontPlace = { obj -> keyPlace.place(cfg.lastCol, row + 1, obj) },
                )
            )
        }

        if (isWallMode) {
            // front diagonals
            models.add(
                wallsBuilder.frontMidWall(
                    leftOffset = -4.0,
                    rightOffset = -4.0,
                    leftPlace = { obj -> keyPlace.place(4, cfg.lastRow, obj) },
                    rightPlace = { obj -> keyPlace.place(5, cfg.lastRow, obj) },
                )
            )

            models.add(wallsBuilder.frontWall { obj -> keyPlace.place(4, cfg.lastRow, obj) })

            models.add(wallsBuilder.frontWall(
                leftOffset = -4.0,
            ) { obj -> keyPlace.place(5, cfg.lastRow, obj) })
        }
    }

    private fun thumbBorders(
        wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder, isWallMode: Boolean = false
    ) {
        //corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> thumbKeyPlace.placeL(obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> thumbKeyPlace.placeL(obj) })
        // right back
        models.add(cornerWallBuilder.backRight { obj -> thumbKeyPlace.placeR(obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> thumbKeyPlace.placeR(obj) })

        //models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeR(o) })
        //models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.backWall { o -> thumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.frontWall { o -> thumbKeyPlace.placeR(o) })
        models.add(wallsBuilder.frontWall { o -> thumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.frontWall { o -> thumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.leftWall { o -> thumbKeyPlace.placeL(o) })
        models.add(wallsBuilder.rightWall { o -> thumbKeyPlace.placeR(o) })

        if (!isWallMode) {
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { o -> thumbKeyPlace.placeM(o) },
                    rightPlace = { o -> thumbKeyPlace.placeR(o) },
                )
            )
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { o -> thumbKeyPlace.placeL(o) },
                    rightPlace = { o -> thumbKeyPlace.placeM(o) },
                )
            )

            models.add(
                wallsBuilder.backMidWall(
                    leftPlace = { o -> thumbKeyPlace.placeL(o) },
                    rightPlace = { o -> thumbKeyPlace.placeM(o) },
                )
            )
        }

        models.add(
            wallsBuilder.midEdge(
                midPlace = { o -> thumbKeyPlace.placeM(o) },
                leftPlace = { o -> thumbKeyPlace.placeL(o) },
                rightPlace = { o -> thumbKeyPlace.placeR(o) },
            )
        )
    }

    private fun thumbWalls(
        wallsBuilder: WallsBuilder,
        cornerWallBuilder: CornerWallBuilder,
        borderThickness: Double = 1.5,
        borderHeight: Double = 4.0,
        verticalOffset: Double = 4.0,
        leftOffset: Double = 8.0,
        rightOffset: Double = 8.0,
        borderZOffset: Double = -2.0,

        outerVerticalOffset: Double = 10.0,
        outerLeftOffset: Double = 15.0,
        outerRightOffset: Double = 15.0,
        outerBorderZOffset: Double = -6.0,
        bottomEdgePatcher: WallBottomEdgePatcher,
    ) {

        //corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> thumbKeyPlace.placeL(obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> thumbKeyPlace.placeL(obj) })
        // right front
        models.add(
            cornerWallBuilder.frontRightToMatrix(
                keyPlace = { obj -> thumbKeyPlace.placeR(obj) },
                matrixOuterPlace = { o -> keyPlace.place(4, cfg.lastRow, o) },
                matrixInnerPlace = { o -> keyPlace.place(3, cfg.lastRow, o) },
            )
        )

        models.add(wallsBuilder.backWall(onlyBorder = true) { o -> thumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.frontWall { o -> thumbKeyPlace.placeR(o) })
        //models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.frontWall { o -> thumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.leftWall { o -> thumbKeyPlace.placeL(o) })


        models.add(
            wallsBuilder.midEdge(
                midPlace = { o -> thumbKeyPlace.placeM(o) },
                leftPlace = { o -> thumbKeyPlace.placeL(o) },
                rightPlace = { o -> thumbKeyPlace.placeR(o) },
            )
        )

        val topInnerPoint = keyPlace.place(
            0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(-leftOffset, 0.0, borderZOffset)
        )

        val topOuterPoint = keyPlace.place(
            0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(-outerLeftOffset, 0.0, outerBorderZOffset)
        )

        models.add(
            hull(
                verticalCube(
                    thumbKeyPlace.placeL(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    thumbKeyPlace.placeL(
                        KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset)
                    ), borderThickness, borderHeight
                ),
                thumbKeyPlace.placeL(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset)
                ),

                verticalCube(
                    topInnerPoint, borderThickness, borderHeight
                ),

                topOuterPoint,
            )
        )

        models.add(
            hull(
                topInnerPoint,

                thumbKeyPlace.placeL(
                    KeyPlaceholder.placeHolderTopRight().move(0.0, outerVerticalOffset, outerBorderZOffset)
                ),

                verticalCube(
                    thumbKeyPlace.placeL(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset + 2, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )

        models.add(
            hull(
                topOuterPoint,
                bottomEdgePatcher.projection(topOuterPoint),
                bottomEdgePatcher.projection(
                    thumbKeyPlace.placeL(
                        KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset)
                    ),
                ),
            )
        )
    }

    private fun betweenThumbAndMatrixBorders(borderThickness: Double, borderHeight: Double) {
        val verticalOffset = 4.0
        val leftOffset = -8.0
        val rightOffset = 4.0
        val borderZOffset = -2.0

        // edge
        models.add(
            hull(
                thumbKeyPlace.placeM(KeyPlaceholder.placeHolderTopLeft()),

                verticalCube(
                    thumbKeyPlace.placeM(
                        KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),

                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft()),

                verticalCube(
                    keyPlace.place(
                        0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(leftOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                )
            )
        )

        // thumb mid
        models.add(
            hull(
                thumbKeyPlace.placeM(KeyPlaceholder.placeHolderTop()),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottom()),
            )
        )

        // mid

        models.add(
            hull(
                thumbKeyPlace.placeM(KeyPlaceholder.placeHolderTopRight()),
                thumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopLeft()),

                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )

        // thumb R

        models.add(
            hull(
                thumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopLeft()),
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )
        models.add(
            hull(
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft()),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )
        val firstPointMatrix = keyPlace.place(
            3, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
        )
        models.add(
            hull(
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),

                verticalCube(
                    firstPointMatrix, borderThickness, borderHeight
                ),
            )
        )

        //row 3 bottom
        models.add(
            hull(
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
                verticalCube(
                    firstPointMatrix, borderThickness, borderHeight
                ),

                verticalCube(
                    keyPlace.place(
                        2, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    keyPlace.place(
                        2,
                        cfg.lastRow,
                        KeyPlaceholder.placeHolderBottomRight().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),

                )
        )

        models.add(
            hull(
                thumbKeyPlace.placeR(KeyPlaceholder.placeHolderTop()),
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )

        models.add(
            hull(
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    firstPointMatrix, borderThickness, borderHeight
                ),
            )
        )

        models.add(
            hull(
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    thumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderBottomRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    firstPointMatrix, borderThickness, borderHeight
                ),
            )
        )
    }

    private fun verticalCube(obj: Abstract3dModel, borderThickness: Double, borderHeight: Double): Abstract3dModel {
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).move(obj.move)
    }
}
