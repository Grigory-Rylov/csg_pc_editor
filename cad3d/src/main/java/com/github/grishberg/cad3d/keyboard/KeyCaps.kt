package com.github.grishberg.cad3d.keyboard

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.util.fromModel
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.utils.Color

class KeyCaps(private val cfg: KeyboardConfig) {

    private val width = 18.5
    fun create(): ModelHolder {
        val model = createModel()
        return ModelHolder(model, fromModel(model, Color.PINK, 10))
    }

    private fun createModel(): Abstract3dModel {
        return Cube(
            width, width, cfg.keyPlaceConfig.saProfileKeyHeight + cfg.keyPlaceConfig.saProfileKeyHeight / 2
        ).move(0.0, 0.0, 7.5)
    }
}
