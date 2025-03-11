package eu.printingin3d.javascad.vrl;

import java.util.List;

public class VertexHolder {

    private final float[] vertex;
    private final float[] normals;
    private final int verticesCount;
    private final List<Facet> facets;

    public VertexHolder(List<Facet> facets, float[] vertex, float[] normals, int verticesCount) {
        this.facets = facets;
        this.vertex = vertex;
        this.normals = normals;
        this.verticesCount = verticesCount;
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

    public List<Facet> getFacets() {
        return facets;
    }
}
