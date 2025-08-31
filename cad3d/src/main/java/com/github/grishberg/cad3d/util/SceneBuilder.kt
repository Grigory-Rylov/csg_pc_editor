package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.plugin.VertexHolder

interface SceneBuilder {

    fun rebuildModels(cfg: KeyboardConfig)

    fun setListener(listener: ReadyListener?)

    interface ReadyListener {

        fun onReady(buffers: List<VertexHolder>)
    }
}
