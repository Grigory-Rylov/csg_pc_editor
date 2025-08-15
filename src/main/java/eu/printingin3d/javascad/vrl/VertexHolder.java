package eu.printingin3d.javascad.vrl;

import eu.printingin3d.javascad.coords.Triangle3d;
import eu.printingin3d.javascad.coords.Triangulator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.models.IModel;
import eu.printingin3d.javascad.utils.Color;
import java.util.ArrayList;
import java.util.List;

public class VertexHolder {

    private final float[] vertex;
    private final float[] normals;
    private final int verticesCount;

    public VertexHolder(float[] vertex, float[] normals, int verticesCount) {
        this.vertex = vertex;
        this.normals = normals;
        this.verticesCount = verticesCount;
    }

    public static VertexHolder fromModel(IModel model, Color color, int fn) {
        FacetGenerationContext context = new ColorFacetGenerationContext(color);
        context.setFn(fn);
        CSG csg = model.toCSG(context);
        return getVerticesAndColorsAsFloatArray(csg.toFacets());
    }

    public float[] getVertex() {
        return vertex;
    }

    public float[] getNormals() {
        return normals;
    }

    public int getVerticesCount() {
        return verticesCount;
    }

    public static VertexHolder fromPolygons(List<Polygon> polygons, Color color) {
        List<Facet> facets = new ArrayList<>();
        for (Polygon p : polygons) {
            List<Triangle3d> triangle3ds = Triangulator.triangulate(p.getVertices(), p.getNormal());
            for (Triangle3d t : triangle3ds) {
                facets.add(new Facet(t, p.getNormal(), color));
            }
        }

        return getVerticesAndColorsAsFloatArray(facets);
    }


    private static VertexHolder getVerticesAndColorsAsFloatArray(List<Facet> facets) {

        int verticesCount = 0;
        verticesCount = facets.size() * 3;

        float[] verticesArray = new float[verticesCount * 7];
        float[] normalsArray = new float[verticesCount * 3];

        int normalArrayIndex = 0;
        int vertexArrayIndex = 0;
        for (Facet facet : facets) {
            final Color facetColor = facet.getColor();
            final V3d normal = facet.getNormal();
            Triangle3d triangle3d = facet.getTriangle();
            for (V3d vertex : triangle3d.getPoints()) {
                // X, Y, Z,
                // R, G, B, A
                verticesArray[vertexArrayIndex++] = (float) vertex.getX();
                verticesArray[vertexArrayIndex++] = (float) vertex.getY();
                verticesArray[vertexArrayIndex++] = (float) vertex.getZ();
                verticesArray[vertexArrayIndex++] = facetColor.getRed() / 255f;
                verticesArray[vertexArrayIndex++] = facetColor.getGreen() / 255f;
                verticesArray[vertexArrayIndex++] = facetColor.getBlue() / 255f;
                verticesArray[vertexArrayIndex++] = facetColor.getAlpha() / 255f;

                normalsArray[normalArrayIndex++] = (float) normal.getX();
                normalsArray[normalArrayIndex++] = (float) normal.getY();
                normalsArray[normalArrayIndex++] = (float) normal.getZ();
            }
        }

        return new VertexHolder(verticesArray, normalsArray, verticesCount);
    }
}
