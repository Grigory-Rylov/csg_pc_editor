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

    fun borders(): Abstract3dModel {
        models.clear()

        //columns
        thumbBorders(InnerBordersBuilder(), InnerCorners())
        matrixBorders(InnerBordersBuilder(), InnerCorners())
        return Utils.union(models)
    }

    private fun matrixBorders(wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder) {
        // corners
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> keyPlace.place(0, 0, obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> keyPlace.place(0, cfg.lastRow, obj) })
        // right back
        models.add(cornerWallBuilder.backRight { obj -> keyPlace.place(cfg.lastCol, 0, obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> keyPlace.place(cfg.lastCol, cfg.lastRow, obj) })

        var bordersOffset = bordersOffset



        for (column in 0 until cfg.columnsCount) {
            bordersOffset = cfg.bordersOffset

            // back columns
            models.add(wallsBuilder.backWall { obj -> keyPlace.place(column, 0, obj) })
//            addHull(
//                keyPlace.place(column, 0, KeyPlaceholder.placeHolderTop()),
//                keyPlace.place(column, 0, KeyPlaceholder.placeHolderTop().move(0.0, bordersOffset, borderZOffset))
//            )
            if (column < 2) {
                bordersOffset = firstColunsBordersOffset
            }

            //front columns
            models.add(wallsBuilder.frontWall { obj -> keyPlace.place(column, cfg.lastRow, obj) })
//            addHull(
//                keyPlace.place(column, cfg.lastRow, KeyPlaceholder.placeHolderBottom()), keyPlace.place(
//                    column, cfg.lastRow, KeyPlaceholder.placeHolderBottom().move(0.0, -bordersOffset, borderZOffset)
//                )
//            )
        }
        for (column in 0 until cfg.columnsCount - 1) {
            bordersOffset = cfg.bordersOffset
            // back diagonals
            models.add(wallsBuilder.backMidWall(
                leftPlace = { obj -> keyPlace.place(column, 0, obj) },
                rightPlace = { obj -> keyPlace.place(column + 1, 0, obj) },
            ))
//            addHull(
//                keyPlace.place(column, 0, KeyPlaceholder.placeHolderTopRight()),
//                keyPlace.place(column + 1, 0, KeyPlaceholder.placeHolderTopLeft()),
//                keyPlace.place(
//                    column, 0, KeyPlaceholder.placeHolderTopRight().move(0.0, bordersOffset, borderZOffset)
//                ),
//                keyPlace.place(
//                    column + 1, 0, KeyPlaceholder.placeHolderTopLeft().move(0.0, bordersOffset, borderZOffset)
//                )
//            )
            if (column < 2) {
                bordersOffset = firstColunsBordersOffset
            }
            // front diagonals
            models.add(wallsBuilder.frontMidWall(
                leftPlace = { obj -> keyPlace.place(column, cfg.lastRow, obj) },
                rightPlace = { obj -> keyPlace.place(column + 1, cfg.lastRow, obj) },
            ))
//            addHull(
//                keyPlace.place(column, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
//                keyPlace.place(column + 1, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft()),
//                keyPlace.place(
//                    column,
//                    cfg.lastRow,
//                    KeyPlaceholder.placeHolderBottomRight().move(0.0, -bordersOffset, borderZOffset)
//                ),
//                keyPlace.place(
//                    column + 1,
//                    cfg.lastRow,
//                    KeyPlaceholder.placeHolderBottomLeft().move(0.0, -bordersOffset, borderZOffset)
//                )
//            )
            bordersOffset = cfg.bordersOffset


            for (row in 0 until cfg.rowsCount) {
                //left
                models.add(wallsBuilder.leftWall { obj -> keyPlace.place(0, row, obj) })
//                addHull(
//                    keyPlace.place(0, row, KeyPlaceholder.placeHolderLeft()),
//                    keyPlace.place(0, row, KeyPlaceholder.placeHolderLeft().move(-horizontalOffset, 0.0, borderZOffset))
//                )
                //right
                models.add(wallsBuilder.rightWall { obj -> keyPlace.place(cfg.lastCol, row, obj) })
//                addHull(
//                    keyPlace.place(cfg.lastCol, row, KeyPlaceholder.placeHolderRight()),
//                    keyPlace.place(
//                        cfg.lastCol, row, KeyPlaceholder.placeHolderRight().move(horizontalOffset, 0.0, borderZOffset)
//                    )
//                )
            }
        }
        for (row in 0 until cfg.rowsCount - 1) {
            models.add(
                wallsBuilder.leftMidWall(
                    leftPlace = { obj -> keyPlace.place(0, row, obj) },
                    rightPlace = { obj -> keyPlace.place(0, row + 1, obj) },
                )
            )
//            addHull(
//                keyPlace.place(0, row, KeyPlaceholder.placeHolderBottomLeft()),
//                keyPlace.place(0, row + 1, KeyPlaceholder.placeHolderTopLeft()),
//                keyPlace.place(
//                    0, row, KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
//                ),
//                keyPlace.place(
//                    0, row + 1, KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset)
//                )
//            )
            models.add(
                wallsBuilder.rightMidWall(
                    backPlace = { obj -> keyPlace.place(cfg.lastCol, row, obj) },
                    frontPlace = { obj -> keyPlace.place(cfg.lastCol, row + 1, obj) },
                )
            )
//            addHull(
//                keyPlace.place(cfg.lastCol, row, KeyPlaceholder.placeHolderBottomRight()),
//                keyPlace.place(cfg.lastCol, row + 1, KeyPlaceholder.placeHolderTopRight()),
//                keyPlace.place(
//                    cfg.lastCol, row, KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)
//                ),
//                keyPlace.place(
//                    cfg.lastCol,
//                    row + 1,
//                    KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset)
//                )
//            )
        }
    }

    private fun thumbBorders(wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder) {
        //left back
        models.add(cornerWallBuilder.backLeft { obj -> ThumbKeyPlace.place3(obj) })
        //left front
        models.add(cornerWallBuilder.frontLeft { obj -> ThumbKeyPlace.place3(obj) })
        // right back
        models.add(cornerWallBuilder.backRight { obj -> ThumbKeyPlace.place1(obj) })
        // right front
        models.add(cornerWallBuilder.frontRight { obj -> ThumbKeyPlace.place1(obj) })

        bordersOffset = 4.0
        /*
        //corners
        //left front
        cornerWallBuilder.frontLeft()
        addHull(
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -bordersOffset, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottomLeft())
        )

        //left back
        cornerWallBuilder.backLeft()
        addHull(
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTopLeft().move(0.0, bordersOffset, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTopLeft())
        )

        //right front
        cornerWallBuilder.frontRight()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottomRight().move(0.0, -bordersOffset, borderZOffset)),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottomRight().move(bordersOffset, 0.0, borderZOffset)),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottomRight())
        )

        //right back
        cornerWallBuilder.backRight()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTopRight().move(0.0, bordersOffset, borderZOffset)),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTopRight().move(bordersOffset, 0.0, borderZOffset)),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTopRight())
        )

        // thumb borders;
        wallsBuilder.backWall()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTop()),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTop().move(0.0, bordersOffset, borderZOffset))
        )

        wallsBuilder.backWall()
        addHull(
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTop()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTop().move(0.0, bordersOffset, borderZOffset))
        )

        wallsBuilder.backWall()
        addHull(
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTop()),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTop().move(0.0, bordersOffset, borderZOffset))
        )

        wallsBuilder.frontWall()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottom()),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottom().move(0.0, -bordersOffset, borderZOffset))
        )
        wallsBuilder.frontWall()
        addHull(
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottom()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottom().move(0.0, -bordersOffset, borderZOffset))
        )
        wallsBuilder.frontWall()
        addHull(
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottom()),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottom().move(0.0, -bordersOffset, borderZOffset))
        )

        // right
        wallsBuilder.rightWall()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderRight()),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderRight().move(bordersOffset, 0.0, borderZOffset))
        )

        // left
        wallsBuilder.leftWall()
        addHull(
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderLeft()),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderLeft().move(-horizontalOffset, 0.0, borderZOffset))
        )

        //middle front
        wallsBuilder.frontMidWall()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottomLeft()),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -bordersOffset, borderZOffset)),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottomRight()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottomRight().move(0.0, -bordersOffset, borderZOffset))
        )
        wallsBuilder.frontMidWall()
        addHull(
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottomLeft()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -bordersOffset, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottomRight()),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderBottomRight().move(0.0, -bordersOffset, borderZOffset))
        )

        // middle back
        wallsBuilder.backMidWall()
        addHull(
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTopLeft()),
            ThumbKeyPlace.place1(KeyPlaceholder.placeHolderTopLeft().move(0.0, bordersOffset, borderZOffset)),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTopRight()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTopRight().move(0.0, bordersOffset, borderZOffset))
        )
        wallsBuilder.backMidWall()
        addHull(
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTopLeft()),
            ThumbKeyPlace.place2(KeyPlaceholder.placeHolderTopLeft().move(0.0, bordersOffset, borderZOffset)),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTopRight()),
            ThumbKeyPlace.place3(KeyPlaceholder.placeHolderTopRight().move(0.0, bordersOffset, borderZOffset))
        )

 */
    }

    private class InnerBordersBuilder(
        private val verticalOffset: Double = 8.0,
        private val horizontalOffset: Double = 6.0,
        private val borderZOffset: Double = -2.0,
    ) : WallsBuilder {

        override fun backWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return keyPlace(
                Utils.hull(
                    KeyPlaceholder.placeHolderTop(),
                    KeyPlaceholder.placeHolderTop().move(0.0, verticalOffset, borderZOffset)
                )
            )
        }

        override fun backMidWall(
            leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
        ): Abstract3dModel {
            return Utils.hull(
                leftPlace.invoke(KeyPlaceholder.placeHolderTopRight()),
                rightPlace.invoke(KeyPlaceholder.placeHolderTopLeft()),
                leftPlace.invoke(
                    KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)
                ),
                rightPlace.invoke(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
                )
            )
        }

        override fun leftWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return keyPlace(
                Utils.hull(
                    (KeyPlaceholder.placeHolderLeft()),
                    KeyPlaceholder.placeHolderLeft().move(-horizontalOffset, 0.0, borderZOffset)
                )
            )
        }

        override fun leftMidWall(
            leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
        ): Abstract3dModel {
            return Utils.hull(
                leftPlace(KeyPlaceholder.placeHolderBottomLeft()),
                rightPlace(KeyPlaceholder.placeHolderTopLeft()),
                leftPlace(KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)),
                rightPlace(KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset))
            )
        }

        override fun frontWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return keyPlace(
                Utils.hull(
                    KeyPlaceholder.placeHolderBottom(),
                    KeyPlaceholder.placeHolderBottom().move(0.0, -verticalOffset, borderZOffset)
                )
            )
        }

        override fun frontMidWall(
            leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
        ): Abstract3dModel {
            return Utils.hull(
                leftPlace(KeyPlaceholder.placeHolderBottomRight()),
                rightPlace(KeyPlaceholder.placeHolderBottomLeft()),
                leftPlace(KeyPlaceholder.placeHolderBottomRight().move(0.0, -verticalOffset, borderZOffset)),
                rightPlace(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset))
            )
        }

        override fun rightWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return keyPlace(
                Utils.hull(
                    KeyPlaceholder.placeHolderRight(),
                    KeyPlaceholder.placeHolderRight().move(horizontalOffset, 0.0, borderZOffset),
                )
            )
        }

        override fun rightMidWall(
            backPlace: (Abstract3dModel) -> Abstract3dModel, frontPlace: (Abstract3dModel) -> Abstract3dModel
        ): Abstract3dModel {
            return Utils.hull(
                backPlace(KeyPlaceholder.placeHolderBottomRight()),
                frontPlace(KeyPlaceholder.placeHolderTopRight()),
                backPlace(KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)),
                frontPlace(KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset))
            )
        }
    }

    private class InnerCorners(
        private val verticalOffset: Double = 8.0,
        private val horizontalOffset: Double = 6.0,
        private val borderZOffset: Double = -2.0,
    ) : CornerWallBuilder {

        override fun backLeft(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return Utils.hull(
                keyPlace(KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderTopLeft().move(-horizontalOffset, 0.0, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderTopLeft())
            )
        }

        override fun backRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return Utils.hull(
                keyPlace(KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderTopRight().move(horizontalOffset, 0.0, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderTopRight())
            )
        }

        override fun frontLeft(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return Utils.hull(
                //keyPlace(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -firstColunsBordersOffset, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderBottomLeft().move(0.0, -verticalOffset, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderBottomLeft())
            )
        }

        override fun frontRight(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
            return Utils.hull(
                keyPlace(KeyPlaceholder.placeHolderBottomRight().move(0.0, -verticalOffset, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderBottomRight().move(horizontalOffset, 0.0, borderZOffset)),
                keyPlace(KeyPlaceholder.placeHolderBottomRight())
            )
        }
    }
}
