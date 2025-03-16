package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig

class ControllerFactory(private val cfg: KeyboardConfig, private val controllerPlace: ControllerPlace) {

    fun createController(): Controller {
        return SuperMiniNRF52840(cfg, controllerPlace)
    }
}
