package com.github.grishberg.cad3d.keyboard

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.plugin.VertexHolder
import com.github.grishberg.cad3d.util.fromModel
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

data class ModelHolder(
    /**
     * Model for printing
     */
    val model: Abstract3dModel,
    /**
     * Models for rendering in viewer
     */
    val vertexHolders: List<VertexHolder>
) {

    constructor(
        model: Abstract3dModel, vararg vertexHolders: VertexHolder
    ) : this(model, vertexHolders.asList())

    constructor(
        model: Abstract3dModel, vertexHoldersList: List<VertexHolder>, vararg vertexHolders: VertexHolder
    ) : this(model, vertexHoldersList + vertexHolders.asList())

    constructor(
        cfg: KeyboardConfig, model: Abstract3dModel, partialModelsList: List<Abstract3dModel>, vararg partialModels: Abstract3dModel,
    ) : this(model, (partialModelsList + partialModels.asList()).map { fromModel(it, cfg.fn) })
}
