package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import eu.printingin3d.javascad.vrl.VertexHolder

class PcCaseSceneBuilder : SceneBuilder {

    private val buffers = mutableListOf<VertexHolder>()
    private var listener: SceneBuilder.ReadyListener? = null
    var motherboardIndex: Int = -1
        private set

    override fun setConfig(cfg: KeyboardConfig) {
        // no op
    }

    override fun setListener(listener: SceneBuilder.ReadyListener) {
        this.listener = listener
    }

    override fun requestBuffers() {
        buffers.clear()
        motherboardIndex = -1

        val models = PcCaseModelFactory.buildAll()
        var idx = 0
        for ((name, csg) in models) {
            buffers.add(csg.getVerticesAndColorsAsFloatArray())
            if (name == "motherboard") {
                motherboardIndex = idx
            }
            idx++
        }

        listener?.onReady(buffers)
    }
}
