package com.github.grishberg.cad3d.keyboard.casebody.controllers.battery

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.plugin.cfg.BatteryType

class BatteryFactory(private val cfg: KeyboardConfig) {

    fun create(): Battery {
        return when (cfg.batteryType) {
            BatteryType.Bt18650 -> RoundBattery18650()
            BatteryType.None -> NoBattery
        }
    }
}
