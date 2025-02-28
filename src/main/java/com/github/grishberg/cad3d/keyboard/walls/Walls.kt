package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.Utils.hull
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel

class Walls(private val cfg: KeyboardConfig, private val keyPlace: KeyPlace) {

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
            InnerBordersBuilder(rightOffset = 4.0, borderThickness = borderThickness, borderHeight = borderHeight),
            InnerCorners(rightOffset = 4.0, borderThickness = borderThickness, borderHeight = borderHeight)
        )

        matrixBorders(
            InnerBordersBuilder(borderThickness = borderThickness, borderHeight = borderHeight),
            InnerCorners(borderThickness = borderThickness, borderHeight = borderHeight)
        )
        //matrixBorders(InnerBordersBuilder(borderThickness = borderThickness), OuterCornersWallBuilder(borderThickness = borderThickness))

        betweenThumbAndMatrixBorders(borderThickness, borderHeight)

        return Utils.union(models)
    }

    fun createWalls(borderThickness: Double = 1.5, borderHeight: Double = 4.0): Abstract3dModel {
        models.clear()

        //columns
        //thumbBorders(InnerBordersBuilder(borderThickness = borderThickness), InnerCorners(borderThickness = borderThickness))

        //matrixBorders(InnerBordersBuilder(borderThickness = borderThickness), InnerCorners(borderThickness = borderThickness))
        matrixBorders(
            OuterWallsBuilder(borderThickness = borderThickness),
            OuterCornersWallBuilder(borderThickness = borderThickness),
            isWallMode = true
        )

        thumbWalls(
            OuterWallsBuilder(borderThickness = borderThickness, borderHeight = borderHeight),
            OuterCornersWallBuilder(
                borderThickness = borderThickness,
                borderHeight = borderHeight,
                rightOffset = thumbRightOffset,
                outerRightOffset = thumbOuterRightOffset
            ),
            rightOffset = thumbRightOffset,
            outerRightOffset = thumbOuterRightOffset,
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

            if (isWallMode && column < 4) {
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
            if (isWallMode && column < 3) {
                continue
            }
            // front diagonals
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { obj -> keyPlace.place(column, cfg.lastRow, obj) },
                    rightPlace = { obj -> keyPlace.place(column + 1, cfg.lastRow, obj) },
                )
            )
            bordersOffset = cfg.bordersOffset


            for (row in 0 until cfg.rowsCount) {
                //left
                models.add(wallsBuilder.leftWall { obj -> keyPlace.place(0, row, obj) })
                //right
                models.add(wallsBuilder.rightWall { obj -> keyPlace.place(cfg.lastCol, row, obj) })

            }
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
    }

    private fun thumbBorders(
        wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder, isWallMode: Boolean = false
    ) {
        //corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> ThumbKeyPlace.placeL(obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> ThumbKeyPlace.placeL(obj) })
        // right back
        models.add(cornerWallBuilder.backRight { obj -> ThumbKeyPlace.placeR(obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> ThumbKeyPlace.placeR(obj) })

        //models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeR(o) })
        //models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeR(o) })
        models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.leftWall { o -> ThumbKeyPlace.placeL(o) })
        models.add(wallsBuilder.rightWall { o -> ThumbKeyPlace.placeR(o) })

        if (!isWallMode) {
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { o -> ThumbKeyPlace.placeM(o) },
                    rightPlace = { o -> ThumbKeyPlace.placeR(o) },
                )
            )
            models.add(
                wallsBuilder.frontMidWall(
                    leftPlace = { o -> ThumbKeyPlace.placeL(o) },
                    rightPlace = { o -> ThumbKeyPlace.placeM(o) },
                )
            )

            models.add(
                wallsBuilder.backMidWall(
                    leftPlace = { o -> ThumbKeyPlace.placeL(o) },
                    rightPlace = { o -> ThumbKeyPlace.placeM(o) },
                )
            )
        }

        models.add(
            wallsBuilder.midEdge(
                midPlace = { o -> ThumbKeyPlace.placeM(o) },
                leftPlace = { o -> ThumbKeyPlace.placeL(o) },
                rightPlace = { o -> ThumbKeyPlace.placeR(o) },
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
    ) {

        //corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> ThumbKeyPlace.placeL(obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> ThumbKeyPlace.placeL(obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> ThumbKeyPlace.placeR(obj) })

        //models.add(wallsBuilder.backWall { o -> ThumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeR(o) })
        //models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeM(o) })
        models.add(wallsBuilder.frontWall { o -> ThumbKeyPlace.placeL(o) })

        models.add(wallsBuilder.leftWall { o -> ThumbKeyPlace.placeL(o) })


        models.add(
            wallsBuilder.midEdge(
                midPlace = { o -> ThumbKeyPlace.placeM(o) },
                leftPlace = { o -> ThumbKeyPlace.placeL(o) },
                rightPlace = { o -> ThumbKeyPlace.placeR(o) },
            )
        )

        models.add(
            hull(
                ThumbKeyPlace.placeL(KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)),
                ThumbKeyPlace.placeL(KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)),
                ThumbKeyPlace.placeL(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, outerVerticalOffset, outerBorderZOffset)
                ),

                verticalCube(
                    keyPlace.place(
                        0,
                        cfg.lastRow,
                        KeyPlaceholder.placeHolderBottomLeft().move(-leftOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),

                keyPlace.place(
                    0,
                    cfg.lastRow,
                    KeyPlaceholder.placeHolderBottomLeft().move(-outerLeftOffset, 0.0, outerBorderZOffset)
                )
            )
        )

        //right thumb wall
        val rightThumbRPoint = ThumbKeyPlace.placeR(
            KeyPlaceholder.placeHolderBottomRight().move(outerRightOffset, 0.0, outerBorderZOffset)
        )
        val midCasePoint = keyPlace.place(
            3,
            cfg.lastRow,
            KeyPlaceholder.placeHolderBottomLeft().move(outerRightOffset, -outerVerticalOffset, outerBorderZOffset)
        )
        models.add(
            hull(
                verticalCube(
                    ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderBottomRight().move(rightOffset, 0.0, borderZOffset)),
                    borderThickness,
                    borderHeight
                ),

                rightThumbRPoint,

                projection(rightThumbRPoint, borderThickness, borderHeight),

                verticalCube(
                    keyPlace.place(
                        3,
                        cfg.lastRow,
                        KeyPlaceholder.placeHolderBottomLeft().move(rightOffset, -verticalOffset - 5, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                midCasePoint,
                projection(midCasePoint, borderThickness, borderHeight),

                )
        )

        //mid front wall

        models.add(wallsBuilder.frontWall(leftOffset = 5.0) { o -> keyPlace.place(3, cfg.lastRow, o) })

    }

    private fun projection(obj: Abstract3dModel, borderThickness: Double, borderHeight: Double): Abstract3dModel {
        val point = obj.move
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).move(V3d(point.x, point.y, 0.0))
    }

    private fun betweenThumbAndMatrixBorders(borderThickness: Double, borderHeight: Double) {
        val verticalOffset = 4.0
        val leftOffset = -8.0
        val rightOffset = 4.0
        val borderZOffset = -2.0

        // edge
        models.add(
            Utils.hull(
                ThumbKeyPlace.placeM(KeyPlaceholder.placeHolderTopLeft()),

                verticalCube(
                    ThumbKeyPlace.placeM(
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
            Utils.hull(
                ThumbKeyPlace.placeM(KeyPlaceholder.placeHolderTop()),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottom()),
            )
        )

        // mid

        models.add(
            Utils.hull(
                ThumbKeyPlace.placeM(KeyPlaceholder.placeHolderTopRight()),
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopLeft()),

                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )

        // thumb R

        models.add(
            Utils.hull(
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopLeft()),
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )
        models.add(
            Utils.hull(
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft()),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
            )
        )
        models.add(
            Utils.hull(
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),

                verticalCube(
                    keyPlace.place(
                        3, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )

        //row 3 bottom
        models.add(
            Utils.hull(
                keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
                verticalCube(
                    keyPlace.place(
                        3, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
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
            Utils.hull(
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTop()),
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )

        models.add(
            Utils.hull(
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    keyPlace.place(
                        3, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )

        models.add(
            Utils.hull(
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderTopRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderBottomRight().move(rightOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    keyPlace.place(
                        3, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)
                    ), borderThickness, borderHeight
                ),
            )
        )
    }

    private fun verticalCube(obj: Abstract3dModel, borderThickness: Double, borderHeight: Double): Abstract3dModel {
        return KeyPlaceholder.placeCube(borderThickness, borderHeight).move(obj.move)
    }
}
