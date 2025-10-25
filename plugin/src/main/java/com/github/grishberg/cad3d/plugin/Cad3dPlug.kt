package com.github.grishberg.cad3d.plugin

import com.github.grishberg.cad3d.plugin.cfg.KeyboardPart
import com.github.grishberg.cad3d.plugin.cfg.SettingsContainer

interface Cad3dPlugin {

    fun requestModels(
        config: SettingsContainer, modifiedKeyboardParts: Set<KeyboardPart>, listener: ResultListener
    )

    fun exportStl(
        config: SettingsContainer,
        listener: StlExportListener
    )

    fun onUnload()

    val name: String

    val version: Long
}
