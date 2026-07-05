package com.github.grishberg.viewer;

import android.opengl.GLES20;

import com.github.grishberg.cad3d.util.BuffersContainer;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Делегат для нормального рендеринга
 */
public class NormalRendererDelegate extends BaseRendererDelegate {
    
    @Override
    public void drawObjects(List<BuffersContainer> targetBuffers, float[] modelMatrix, float[] viewMatrix, 
                          float[] projectionMatrix, float[] lightPosInEyeSpace) {
        for (BuffersContainer buffersContainer : targetBuffers) {
            drawTriangle(buffersContainer, modelMatrix, viewMatrix, projectionMatrix, lightPosInEyeSpace);
        }
    }
    
    private void drawTriangle(BuffersContainer buffersContainer, float[] modelMatrix, float[] viewMatrix, 
                            float[] projectionMatrix, float[] lightPosInEyeSpace) {
        // Сбрасываем состояние атрибутов для предотвращения конфликтов
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        
        final FloatBuffer aTriangleBuffer = buffersContainer.verticesBuffers;
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, buffersContainer.normals
        );
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
        // Устанавливаем позицию света
        GLES20.glUniform3fv(mLightPosHandle, 1, lightPosInEyeSpace, 0);
        
        // Устанавливаем матрицы
        setupMatrices(modelMatrix, viewMatrix, projectionMatrix);
        
        // Рисуем треугольники
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffersContainer.vertexCount);
    }
} 
