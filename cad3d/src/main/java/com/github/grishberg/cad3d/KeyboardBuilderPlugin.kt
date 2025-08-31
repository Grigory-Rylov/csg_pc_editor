package com.github.grishberg.cad3d

import com.github.grishberg.cad3d.keyboard.ControlPointsController
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import com.github.grishberg.cad3d.plugin.Config
import com.github.grishberg.cad3d.plugin.VertexHolder

class KeyboardBuilderPlugin : Cad3dPlugin {

    private val keyboardBuilder = KeyboardBuilder()

    override fun requestModels(config: Config, ResultListener listener) {
        if (config !is KeyboardConfig) {
            return emptyList()
        }
    }

    override fun getName(): String = "Keyboard builder"

    override fun getVersion(): Long = 1
}
