package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import eu.printingin3d.javascad.vrl.VertexHolder

class PcCaseSceneBuilder : SceneBuilder {

    private val buffers = mutableListOf<VertexHolder>()
    private var listener: SceneBuilder.ReadyListener? = null

    override fun setConfig(cfg: KeyboardConfig) {
        // no op
    }

    override fun setListener(listener: SceneBuilder.ReadyListener) {
        this.listener = listener
    }

    override fun requestBuffers() {
        buffers.clear()

        val models = PcCaseModelFactory.buildAll()
        for ((_, csg) in models) {
            buffers.add(csg.getVerticesAndColorsAsFloatArray())
        }

        listener?.onReady(buffers)
    }
}
