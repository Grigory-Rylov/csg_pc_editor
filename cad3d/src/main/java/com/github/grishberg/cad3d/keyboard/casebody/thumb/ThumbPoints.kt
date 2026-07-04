package com.github.grishberg.cad3d.keyboard.casebody.thumb

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.models.Abstract3dModel

/**
 * FR - Front Right Corner
 * FL - Front Left Corner
 * BR - Back Right Corner
 * BL - Back Left Corner
 */
class ThumbPoints(
    private val cfg: KeyboardConfig,
    keyPlace: KeyPlace,
    thumbKeyPlace: ThumbKeyPlace,
) {

    val row2RightFRRightInner = safeThumbModel(thumbKeyPlace.placeR2(
        KeyPlaceholder.placeHolderFrontRight()
            .move(cfg.wallsSettings.verticalOffset + 0.0, 0.0, cfg.wallsSettings.borderZOffset)
    ))

    val row2RightFRRightOuter = topBorderObj(safeThumbModel(thumbKeyPlace.placeR2(
        KeyPlaceholder.placeHolderFrontRight()
            .move(0.0, -cfg.wallsSettings.outerVerticalOffset, cfg.wallsSettings.outerBorderZOffset)
    )))

    val row1RightFRInner = thumbKeyPlace.placeR(
        KeyPlaceholder.placeHolderFrontRight().move(2.0, 0.0, cfg.wallsSettings.borderZOffset)
    )
    val row1RightBRInner = thumbKeyPlace.placeR(
        KeyPlaceholder.placeHolderBackRight().move(2.2, -3.0, cfg.wallsSettings.borderZOffset)
    )
    val col4LFOut = keyPlace.place(
        4, cfg.lastRow, KeyPlaceholder.placeHolderFrontLeft().move(
            0.0, -cfg.wallsSettings.outerVerticalOffset, cfg.wallsSettings.outerBorderZOffset
        )
    )

    private fun topBorderObj(obj: Abstract3dModel): Abstract3dModel {
        return Utils.sphere(cfg.wallsSettings.borderThickness / 2.0).move(obj.move)
    }

    companion object {
        private val EMPTY_MODEL = eu.printingin3d.javascad.models.Cube(0.1, 0.1, 0.1)
        fun safeThumbModel(model: Abstract3dModel): Abstract3dModel = model ?: EMPTY_MODEL
    }
}
