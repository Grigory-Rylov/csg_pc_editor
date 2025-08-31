package com.github.grishberg.cad3d

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import com.github.grishberg.cad3d.plugin.Config
import com.github.grishberg.cad3d.plugin.ResultListener
import kotlinx.coroutines.Dispatchers

class KeyboardBuilderPlugin : Cad3dPlugin {

    private val keyboardBuilder = KeyboardBuilder(
        mainThreadDispatcher = Dispatchers.Main,
    )

    override fun requestModels(
        config: Config, listener: ResultListener
    ) {
        if (config !is KeyboardConfig) {
            return
        }
        keyboardBuilder.rebuildModels(config, listener)
    }

    override val version: Long = 6

    override val name: String = "Keyboard builder $version"
}
