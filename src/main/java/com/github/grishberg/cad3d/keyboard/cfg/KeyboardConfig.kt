package com.github.grishberg.cad3d.keyboard.cfg

data class KeyboardConfig(
    val fn: Int,
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
    @field:Volatile var assemblySettings: AssemblySettings,
    val thumbClusterSettings: ThumbClusterSettings,
    val screwNutHoleDiameter: Double,
    val screwHolderWallhickness: Double,
    val isSkeletonMode: Boolean,
) {

    val lastCol: Int
        get() = columnsCount - 1
    val lastRow: Int
        get() = rowsCount - 1
}

