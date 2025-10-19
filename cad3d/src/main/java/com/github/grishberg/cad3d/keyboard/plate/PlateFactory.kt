package com.github.grishberg.cad3d.keyboard.plate

import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.screws.ScrewBase
import com.github.grishberg.cad3d.keyboard.screws.ScrewWallPlaces
import com.github.grishberg.cad3d.plugin.cfg.ThumbClusterMode

class PlateFactory(
    private val cfg: KeyboardConfig,
    private val bottomPoints: BottomPoints,
    private val screwWallPlaces: ScrewWallPlaces,
    private val screwBase: ScrewBase,
) {

    fun create(): ModelHolder {
        val delegate = when (cfg.thumbClusterSettings.type) {
            ThumbClusterMode.SingleColumn3Buttons -> SingleRow3ThumbsPlate(
                cfg, bottomPoints, screwWallPlaces, screwBase
            )

            ThumbClusterMode.SingleColumn4Buttons -> SingleRow3ThumbsPlate(
                cfg, bottomPoints, screwWallPlaces, screwBase
            )

            ThumbClusterMode.TwoRows5Buttons -> TwoRows5ThumbsPlate(cfg, bottomPoints, screwWallPlaces, screwBase)
        }
        return delegate.create()
    }
}
