package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.common.DebugRecorder
import com.github.grishberg.cad3d.pccase.PcCaseBuilder
import com.github.grishberg.cad3d.util.SceneBuilder.ReadyListener
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.VertexHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PcCaseSceneBuilder(
    private val debugRecorder: DebugRecorder,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SceneBuilder {

    private var listener: ReadyListener? = null
    private val mutex = Mutex()

    override fun setConfig(cfg: com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig) {
        // unused
    }

    override fun setListener(listener: ReadyListener) {
        this.listener = listener
    }

    override fun requestBuffers() {
        create3dModels()
    }

    private fun create3dModels() {
        coroutineScope.launch {
            val pcCaseBuilder = PcCaseBuilder()
            val models = pcCaseBuilder.build()

            val buffers = mutableListOf<VertexHolder>()
            for (pcModel in models) {
                val vh = VertexHolder.fromModel(pcModel.model, pcModel.color, 8)
                buffers.add(vh)
            }

            listener?.onReady(buffers)
        }
    }
}
