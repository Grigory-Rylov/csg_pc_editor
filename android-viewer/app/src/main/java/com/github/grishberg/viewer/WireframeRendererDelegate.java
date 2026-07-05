package com.github.grishberg.viewer;

import android.opengl.GLES20;

import com.github.grishberg.cad3d.util.BuffersContainer;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Делегат для режима wireframe рендеринга
 */
public class WireframeRendererDelegate extends BaseRendererDelegate {
    
    @Override
    public void drawObjects(List<BuffersContainer> targetBuffers, float[] modelMatrix, float[] viewMatrix, 
                          float[] projectionMatrix, float[] lightPosInEyeSpace) {
        for (BuffersContainer buffersContainer : targetBuffers) {
            drawWireframe(buffersContainer, modelMatrix, viewMatrix, projectionMatrix);
        }
    }
    
    private void drawWireframe(BuffersContainer buffersContainer, float[] modelMatrix, float[] viewMatrix, 
                             float[] projectionMatrix) {
        // Сбрасываем состояние атрибутов для предотвращения конфликтов
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        
        GLES20.glLineWidth(5f); // Толщина линий
        final FloatBuffer aTriangleBuffer = buffersContainer.verticesBuffers;
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Цвет задаём белым для wireframe
        float[] white = {1f, 1f, 1f, 1f};
        GLES20.glVertexAttrib4fv(mColorHandle, white, 0);
        // Отключаем нормали
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        // Устанавливаем матрицы
        setupMatrices(modelMatrix, viewMatrix, projectionMatrix);
        // Рисуем рёбра
        int vertexCount = buffersContainer.vertexCount;
        for (int i = 0; i < vertexCount; i += 3) {
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, i, 3);
        }
    }
} 
