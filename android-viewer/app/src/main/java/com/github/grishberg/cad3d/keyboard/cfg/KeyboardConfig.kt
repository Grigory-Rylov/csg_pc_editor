package com.github.grishberg.cad3d.keyboard.cfg

data class KeyboardConfig(
    val fn: Int = 20,
    val stlFn: Int = 50,
    val plateZOffset: Double = 12.0,
    val rowCurvature: Double = 12.0,
    val tentingAngle: Double = 16.0,
    val columnCurvature: Double = 15.0,
    val keyswitchHeight: Double = 18.0,
    val keyswitchWidth: Double = 18.0,
    val extraWidth: Double = 2.5,
    val extraHeight: Double = 1.0,
    val plateThickness: Double = 4.0,
    val saProfileKeyHeight: Double = 6.5,
    val centerCol: Int = 2,
    val rowsCount: Int = 3,
    val columnsCount: Int = 6,
    val centerRow: Int = 1,
    val keyPlaceHolderWidth: Double = 15.7,
    val keyPlaceHolderDepth: Double = 15.7,
    val keyPlaceHolderHeight: Double = 4.0,
    val isLowProfile: Boolean = true,
    val zAngleProvider: KeyZAngleProvider = KeyZAngleProvider(), //Не нужно сериализовывать
    val columnOffsetProvider: KeyOffsetProvider = KeyOffsetProvider(), //Не нужно сериализовывать
    val powerSwitcherType: PowerSwitcherType = PowerSwitcherType.None,
    val isHasHotswap: Boolean = false,
    val isMagneticWristRestHolder: Boolean = false,
    val bordersOffset: Double = 2.0,
    @field:Volatile var assemblySettings: AssemblySettings = AssemblySettings(),
    val thumbClusterSettings: ThumbClusterSettings = ThumbClusterSettings(),
    val screwNutHoleDiameter: Double = 6.0,
    val screwHolderWallhickness: Double = 1.5,
    val screwBoltDiameter: Double = 3.0,
    val isSkeletonMode: Boolean = true,
    val trackball: TrackballConfig = TrackballConfig(TrackballMode.None, 34.0, 2.2),
    val wallsSettings: WallsSettings = WallsSettings(),
    val controllerPlateHeight: Double = 1.5,
    val keyPlaceholderType: KeyPlaceholderType = KeyPlaceholderType.AmoebaSu120,
    val horizontalExtraSpace: Double = 1.0,
    val verticalExtraSpace: Double = 1.0,
) {

    val lastCol: Int
        get() = columnsCount - 1
    val lastRow: Int
        get() = rowsCount - 1
}

