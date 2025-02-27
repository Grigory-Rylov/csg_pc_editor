package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.models.Abstract3dModel

class Walls(private val cfg: KeyboardConfig, private val keyPlace: KeyPlace) {

    private val models = ArrayList<Abstract3dModel>()
    var bordersOffset = cfg.bordersOffset
    val firstColunsBordersOffset = 4.0
    val horizontalOffset = 8.0
    val borderZOffset = -2.0

    fun createBorders(borderThickness: Double = 1.5, borderHeight: Double = 4.0): Abstract3dModel {
        models.clear()

        //columns
        thumbBorders(
            InnerBordersBuilder(borderThickness = borderThickness, borderHeight = borderHeight),
            InnerCorners(borderThickness = borderThickness, borderHeight = borderHeight)
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

        //betweenThumbAndMatrixBorders(borderThickness)

        return Utils.union(models)
    }

    private fun matrixBorders(
        wallsBuilder: WallsBuilder,
        cornerWallBuilder: CornerWallBuilder,
        isWallMode: Boolean = false
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

            if (isWallMode && column < 3) {
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

    private fun thumbBorders(wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder) {
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

        models.add(
            wallsBuilder.midEdge(
                midPlace = { o -> ThumbKeyPlace.placeM(o) },
                leftPlace = { o -> ThumbKeyPlace.placeL(o) },
                rightPlace = { o -> ThumbKeyPlace.placeR(o) },
            )
        )
    }

    private fun betweenThumbAndMatrixBorders(borderThickness: Double, borderHeight: Double) {
        val verticalOffset = 4.0
        val horizontalOffset = 8.0
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
                        0,
                        cfg.lastRow,
                        KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
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
                        KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset)
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
                        KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset)
                    ), borderThickness, borderHeight
                ),
                verticalCube(
                    ThumbKeyPlace.placeR(
                        KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
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
