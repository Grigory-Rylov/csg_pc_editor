package com.github.grishberg.cad3d.keyboard

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.vrl.VertexHolder

data class ModelHolder(val model: Abstract3dModel, val vertexHolders: List<VertexHolder>) { constructor(
    model: Abstract3dModel, vararg vertexHolders: VertexHolder
) : this(model, vertexHolders.asList())
}
