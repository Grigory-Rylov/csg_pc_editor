package com.github.grishberg.viewer;

import com.github.grishberg.cad3d.util.BuffersContainer;

import java.util.List;

/**
 * Интерфейс для делегатов рендеринга
 */
public interface RendererDelegate {

    /**
     * Отрисовывает основные объекты
     */
    void drawObjects(List<BuffersContainer> targetBuffers, float[] modelMatrix, float[] viewMatrix, 
                    float[] projectionMatrix, float[] lightPosInEyeSpace);

    /**
     * Устанавливает OpenGL handles
     */
    void setHandles(int positionHandle, int colorHandle, int normalHandle,
                   int mvpMatrixHandle, int mvMatrixHandle, int lightPosHandle);
} 
