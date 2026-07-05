package com.github.grishberg.cad3d.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import eu.printingin3d.javascad.vrl.VertexHolder;

public class BuffersContainer {

    public static final int mBytesPerFloat = 4;
    public final int vertexCount;
    public final FloatBuffer verticesBuffers;
    public final FloatBuffer normals;

    public BuffersContainer(int vertexCount, FloatBuffer verticesBuffers, FloatBuffer mCubeNormals) {
        this.vertexCount = vertexCount;
        this.verticesBuffers = verticesBuffers;
        this.normals = mCubeNormals;
    }

    public static BuffersContainer fromVertexHolder(VertexHolder vertex) {
        int vertexCount = vertex.getVerticesCount();

        // Initialize the buffers.
        FloatBuffer vertexAndColor =
            ByteBuffer.allocateDirect(vertex.getVertex().length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexAndColor.put(vertex.getVertex()).position(0);

        FloatBuffer normals = ByteBuffer.allocateDirect(vertex.getNormals().length * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();
        normals.put(vertex.getNormals()).position(0);

        return new BuffersContainer(vertexCount, vertexAndColor, normals);
    }
} 