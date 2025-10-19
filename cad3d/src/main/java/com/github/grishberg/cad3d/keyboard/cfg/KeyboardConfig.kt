package com.github.grishberg.cad3d.keyboard.cfg

import com.github.grishberg.cad3d.kbd.core.cfg.KeyOffsetProvider
import com.github.grishberg.cad3d.kbd.core.cfg.KeyPlaceConfig
import com.github.grishberg.cad3d.kbd.core.cfg.KeyZAngleProvider
import com.github.grishberg.cad3d.plugin.cfg.BatteryType
import com.github.grishberg.cad3d.plugin.cfg.ControllerType
import com.github.grishberg.cad3d.plugin.cfg.KeyboardPart
import com.github.grishberg.cad3d.plugin.cfg.PowerSwitcherType
import com.github.grishberg.cad3d.plugin.cfg.SettingsContainer
import com.github.grishberg.cad3d.plugin.cfg.ThumbClusterSettings
import com.github.grishberg.cad3d.plugin.cfg.TrackballConfig

data class KeyboardConfig(
    val fn: Int,
    val stlFn: Int,
    val powerSwitcherType: PowerSwitcherType,
    val isMagneticWristRestHolder: Boolean,
    val bordersOffset: Double,
    val visibleKeyboardParts: Set<KeyboardPart>,
    val modifiedKeyboardParts: Set<KeyboardPart>,
    val thumbClusterSettings: ThumbClusterSettings,
    val screwNutHoleDiameter: Double,
    val screwHeadDiameter: Double = 6.0,
    val screwHeadHeight: Double = 1.7,
    val screwHolderWallhickness: Double,
    val screwBoltDiameter: Double = 3.0,
    val isSkeletonMode: Boolean,
    val trackball: TrackballConfig,
    val wallsSettings: WallsSettings,
    val controllerPlateHeight: Double = 1.5,
    val controllerType: ControllerType,
    val innerBatteryType: BatteryType,
    val keyPlaceConfig: KeyPlaceConfig,
) {

    val lastCol: Int
        get() = keyPlaceConfig.lastCol
    val lastRow: Int
        get() = keyPlaceConfig.lastRow

    val batteryType: BatteryType
        get() = when (controllerType) {
            ControllerType.SuperMiniNRF52840 -> batteryType
            else -> BatteryType.None
        }

    companion object {

        fun SettingsContainer.getKeyboardConfig(modifiedKeyboardParts: Set<KeyboardPart>): KeyboardConfig =
            KeyboardConfig(
                fn = keyboardSettings.fn,
                stlFn = keyboardSettings.stlFn,

                powerSwitcherType = keyboardSettings.powerSwitcherType,

                isMagneticWristRestHolder = keyboardSettings.isMagneticWristRestHolder,
                bordersOffset = keyboardSettings.bordersOffset,
                visibleKeyboardParts = assemblySettings.toKeyboardPartsList(),
                modifiedKeyboardParts = modifiedKeyboardParts,
                thumbClusterSettings = thumbClusterSettings,
                screwNutHoleDiameter = keyboardSettings.screwNutHoleDiameter,
                screwHolderWallhickness = keyboardSettings.screwHolderWallhickness,
                isSkeletonMode = keyboardSettings.isSkeletonMode,
                trackball = trackballSettings,
                wallsSettings = WallsSettings(),

                controllerType = keyboardSettings.controllerType,
                innerBatteryType = keyboardSettings.batteryType,
                keyPlaceConfig = KeyPlaceConfig(
                    plateZOffset = keyboardSettings.plateZOffset,
                    rowCurvature = keyboardSettings.rowCurvature,
                    tentingAngle = keyboardSettings.tentingAngle,
                    columnCurvature = keyboardSettings.columnCurvature,
                    keyswitchHeight = 18.0,
                    keyswitchWidth = 18.0,
                    extraWidth = 2.5,
                    extraHeight = 1.0,
                    keyPlaceHolderWidth = 15.7,
                    keyPlaceHolderDepth = 15.7,
                    keyPlaceHolderHeight = 4.0,
                    horizontalExtraSpace = keyboardSettings.horizontalExtraSpace,
                    verticalExtraSpace = keyboardSettings.verticalExtraSpace,
                    zAngleProvider = KeyZAngleProvider(),
                    columnOffsetProvider = KeyOffsetProvider(),
                    plateThickness = keyboardSettings.plateThickness,
                    saProfileKeyHeight = keyboardSettings.saProfileKeyHeight,
                    columnsCount = keyboardSettings.columnsCount,
                    rowsCount = keyboardSettings.rowsCount,
                    centerCol = keyboardSettings.centerCol,
                    centerRow = keyboardSettings.centerRow,
                    isLowProfile = keyboardSettings.isLowProfile,
                    isHasHotswap = keyboardSettings.isHasHotswap,
                    keyPlaceholderType = keyboardSettings.keyPlaceholderType,
                ),
            )
    }
}

