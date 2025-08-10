package eu.printingin3d.javascad.vrl

import eu.printingin3d.javascad.coords.Triangulator
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

        fun createVertexHolder(polygons: List<Polygon>, color: Color): VertexHolder {
            val facets = mutableListOf<Facet>()
            for (polygon in polygons) {
                facets.addAll(
                    Triangulator.triangulate(polygon.vertices).map {
                        Facet(it, polygon.normal, color)
                    })
            }
            return getVerticesAndColorsAsFloatArray(facets)
        }

        fun getVerticesAndColorsAsFloatArray(facets: List<Facet>): VertexHolder {
            var verticesCount = 0
            verticesCount = facets.size * 3

            val verticesArray = FloatArray(verticesCount * 7)
            val normalsArray = FloatArray(verticesCount * 3)

            var normalArrayIndex = 0
            var vertexArrayIndex = 0
            for (facet in facets) {
                val facetColor = facet.getColor()
                val normal = facet.getNormal()
                val triangle3d = facet.getTriangle()
                for (vertex in triangle3d.getPoints()) {
                    // X, Y, Z,
                    // R, G, B, A
                    verticesArray[vertexArrayIndex++] = vertex.getX().toFloat()
                    verticesArray[vertexArrayIndex++] = vertex.getY().toFloat()
                    verticesArray[vertexArrayIndex++] = vertex.getZ().toFloat()
                    verticesArray[vertexArrayIndex++] = facetColor.getRed() / 255f
                    verticesArray[vertexArrayIndex++] = facetColor.getGreen() / 255f
                    verticesArray[vertexArrayIndex++] = facetColor.getBlue() / 255f
                    verticesArray[vertexArrayIndex++] = facetColor.getAlpha() / 255f

                    normalsArray[normalArrayIndex++] = normal.getX().toFloat()
                    normalsArray[normalArrayIndex++] = normal.getY().toFloat()
                    normalsArray[normalArrayIndex++] = normal.getZ().toFloat()
                }
            }

            return VertexHolder(facets, verticesArray, normalsArray, verticesCount)
        }
    }
}
