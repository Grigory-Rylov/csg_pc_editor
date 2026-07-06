package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import com.github.grishberg.cad3d.pccase.SceneConfig
import eu.printingin3d.javascad.vrl.VertexHolder

class PcCaseSceneBuilder : SceneBuilder {

    private val buffers = mutableListOf<VertexHolder>()
    private var listener: SceneBuilder.ReadyListener? = null
    var motherboardIndex: Int = -1
        private set
    var motherboardX: Float = 0f
        private set
    var motherboardY: Float = 0f
        private set
    var motherboardZ: Float = 0f
        private set
    private var currentConfig = SceneConfig.DEFAULT

    override fun setConfig(cfg: KeyboardConfig) {
        // no op
    }

    override fun setListener(listener: SceneBuilder.ReadyListener) {
        this.listener = listener
    }

    fun updateConfig(config: SceneConfig) {
        currentConfig = config
        requestBuffers()
    }

    override fun requestBuffers() {
        buffers.clear()
        motherboardIndex = -1

        val models = PcCaseModelFactory.buildAll(currentConfig)
        var idx = 0
        for ((name, csg) in models) {
            buffers.add(csg.getVerticesAndColorsAsFloatArray())
            if (name == "motherboard") {
                motherboardIndex = idx
            }
            idx++
        }

        val mb = currentConfig.components.firstOrNull { it.type == "motherboard" }
        if (mb != null) {
            motherboardX = mb.x.toFloat()
            motherboardY = mb.y.toFloat()
            motherboardZ = mb.z.toFloat()
        }

        listener?.onReady(buffers)
    }
}
