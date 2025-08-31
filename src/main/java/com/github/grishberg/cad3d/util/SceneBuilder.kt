package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.vrl.VertexHolder

interface SceneBuilder {

    fun rebuildModels(cfg: KeyboardConfig)

    fun setListener(listener: ReadyListener?)

    interface ReadyListener {

        fun onReady(buffers: List<VertexHolder>)
    }
}
