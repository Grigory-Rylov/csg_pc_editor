package com.github.grishberg.viewer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Базовый класс для делегатов рендеринга
 */
public abstract class BaseRendererDelegate implements RendererDelegate {

    // OpenGL handles
    protected int mPositionHandle;
    protected int mColorHandle;
    protected int mNormalHandle;
    protected int mMVPMatrixHandle;
    protected int mMVMatrixHandle;
    protected int mLightPosHandle;

    // Константы
    protected final int mBytesPerFloat = 4;
    protected final int mStrideBytes = 7 * mBytesPerFloat;
    protected final int mPositionOffset = 0;
    protected final int mPositionDataSize = 3;
    protected final int mColorOffset = 3;
    protected final int mColorDataSize = 4;
    protected final int mNormalDataSize = 3;

    // Временные матрицы
    protected final float[] mMVPMatrix = new float[16];
    protected final float[] mTemporaryMatrix = new float[16];

    @Override
    public void setHandles(int positionHandle, int colorHandle, int normalHandle,
                          int mvpMatrixHandle, int mvMatrixHandle, int lightPosHandle) {
        this.mPositionHandle = positionHandle;
        this.mColorHandle = colorHandle;
        this.mNormalHandle = normalHandle;
        this.mMVPMatrixHandle = mvpMatrixHandle;
        this.mMVMatrixHandle = mvMatrixHandle;
        this.mLightPosHandle = lightPosHandle;
    }

    /**
     * Устанавливает матрицы для отрисовки
     */
    protected void setupMatrices(float[] modelMatrix, float[] viewMatrix, float[] projectionMatrix) {
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mTemporaryMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    /**
     * Создаёт FloatBuffer из массива float
     */
    protected FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }
}
