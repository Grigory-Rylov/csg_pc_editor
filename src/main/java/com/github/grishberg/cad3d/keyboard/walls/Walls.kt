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
        betweenThumbAndMatrixBorders()
        return Utils.union(models)
    }

    private fun matrixBorders(wallsBuilder: WallsBuilder, cornerWallBuilder: CornerWallBuilder) {
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
    }

    private fun betweenThumbAndMatrixBorders() {
        val verticalOffset = 4.0
        val horizontalOffset = 8.0
        val borderZOffset = -2.0

        // edge
        models.add(
            Utils.hull(
                ThumbKeyPlace.placeM(KeyPlaceholder.placeHolderTopLeft()), ThumbKeyPlace.placeM(
                    KeyPlaceholder.placeHolderTopLeft().move(0.0, verticalOffset, borderZOffset)
                ), keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft()),

                keyPlace.place(
                    0, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft().move(-horizontalOffset, 0.0, borderZOffset)
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
                //keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottomLeft())
            )
        )

        // thumb R

        models.add(
            Utils.hull(
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopLeft()),
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)),
                keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderBottomRight()),
                //keyPlace.place(1, cfg.lastRow, KeyPlaceholder.placeHolderBottom()),
            )
        )

        models.add(
            Utils.hull(
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTop()),
                ThumbKeyPlace.placeR(KeyPlaceholder.placeHolderTopRight().move(0.0, verticalOffset, borderZOffset)),
            )
        )
    }

    private class InnerBordersBuilder(
        private val verticalOffset: Double = 4.0,
        private val horizontalOffset: Double = 8.0,
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
        private val verticalOffset: Double = 4.0,
        private val horizontalOffset: Double = 8.0,
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
