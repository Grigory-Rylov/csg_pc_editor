package com.github.grishberg.cad3d.util;

import java.nio.FloatBuffer;

public class BuffersContainer {

    public final int vertexCount;
    public final FloatBuffer verticesBuffers;
    public final FloatBuffer normals;

    public BuffersContainer(int vertexCount, FloatBuffer verticesBuffers, FloatBuffer mCubeNormals) {
        this.vertexCount = vertexCount;
        this.verticesBuffers = verticesBuffers;
        this.normals = mCubeNormals;
    }
} 