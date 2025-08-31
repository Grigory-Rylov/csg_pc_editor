package com.github.grishberg.cad3d.keyboard.cfg

import com.github.grishberg.cad3d.keyboard.KeyboardPart

data class KeyboardConfig(
    val fn: Int,
    val stlFn: Int,
    val plateZOffset: Double,
    val rowCurvature: Double,
    val tentingAngle: Double,
    val columnCurvature: Double,
    val keyswitchHeight: Double,
    val keyswitchWidth: Double,
    val extraWidth: Double,
    val extraHeight: Double,
    val plateThickness: Double,
    val saProfileKeyHeight: Double,
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
    val powerSwitcherType: PowerSwitcherType,
    val isHasHotswap: Boolean,
    val isMagneticWristRestHolder: Boolean,
    val bordersOffset: Double,
    val visibleKeyboardParts: Set<KeyboardPart>,
    val modifiedKeyboardParts: Set<KeyboardPart>,
    val thumbClusterSettings: ThumbClusterSettings,
    val screwNutHoleDiameter: Double,
    val screwHolderWallhickness: Double,
    val screwBoltDiameter: Double = 3.0,
    val isSkeletonMode: Boolean,
    val trackball: TrackballConfig,
    val wallsSettings: WallsSettings,
    val controllerPlateHeight: Double = 1.5,
    val keyPlaceholderType: KeyPlaceholderType,
    val horizontalExtraSpace: Double,
    val verticalExtraSpace: Double,
) {

    val lastCol: Int
        get() = columnsCount - 1
    val lastRow: Int
        get() = rowsCount - 1
}

