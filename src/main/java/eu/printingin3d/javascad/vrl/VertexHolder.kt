package eu.printingin3d.javascad.vrl

import eu.printingin3d.javascad.models.IModel
import eu.printingin3d.javascad.utils.Color

data class VertexHolder(
    val facets: List<Facet>, val vertex: FloatArray, val normals: FloatArray, val verticesCount: Int
) {

    companion object {

        fun createVertexHolder(model: IModel, color: Color, fn: Int): VertexHolder {
            val context: FacetGenerationContext = ColorFacetGenerationContext(color)
            context.setFn(fn)
            val csg = model.toCSG(context)
            val vertex = csg.verticesAndColorsAsFloatArray
            return vertex
        }
    }
}
