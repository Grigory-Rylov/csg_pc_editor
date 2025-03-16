package com.github.grishberg.cad3d.keyboard.casebody.controllers.switcher

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig

class SwitcherFactory(
    private val cfg: KeyboardConfig,
) {

    fun createSwitcher(): Switcher {
        return RoundSwitcher()
    }
}
