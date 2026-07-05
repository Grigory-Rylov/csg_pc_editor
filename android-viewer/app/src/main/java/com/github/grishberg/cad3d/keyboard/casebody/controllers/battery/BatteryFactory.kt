package com.github.grishberg.cad3d.keyboard.casebody.controllers.battery

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig

class BatteryFactory(private val cfg: KeyboardConfig) {

    fun create(): Battery {
        //return SquareBattery()
        return RoundBattery18650()
    }
}
