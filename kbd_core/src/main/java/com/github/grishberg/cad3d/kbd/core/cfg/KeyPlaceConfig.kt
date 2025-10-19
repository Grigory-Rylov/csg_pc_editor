package com.github.grishberg.cad3d.kbd.core.cfg

import com.github.grishberg.cad3d.plugin.cfg.KeyPlaceholderType

data class KeyPlaceConfig(
    val plateThickness: Double,
    val saProfileKeyHeight: Double,
    val keyswitchWidth: Double,
    val horizontalExtraSpace: Double,
    val verticalExtraSpace: Double,
    val extraHeight: Double,
    val extraWidth: Double,
    val columnCurvature: Double,
    val keyswitchHeight: Double,
    val plateZOffset: Double,
    val rowCurvature: Double,
    val tentingAngle: Double,
    val centerCol: Int,
    val rowsCount: Int,
    val columnsCount: Int,
    val centerRow: Int,
    val keyPlaceHolderWidth: Double,
    val keyPlaceHolderDepth: Double,
    val keyPlaceHolderHeight: Double,
    val isLowProfile: Boolean,
    val zAngleProvider: KeyZAngleProvider, //Не нужно сериализовывать
    val columnOffsetProvider: KeyOffsetProvider, //Не нужно сериализовывать
    val isHasHotswap: Boolean,
    val keyPlaceholderType: KeyPlaceholderType,
) {

    val lastCol: Int
        get() = columnsCount - 1
    val lastRow: Int
        get() = rowsCount - 1

}
