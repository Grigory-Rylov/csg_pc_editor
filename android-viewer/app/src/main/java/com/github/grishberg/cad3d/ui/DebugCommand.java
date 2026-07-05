package com.github.grishberg.cad3d.ui;

import com.github.grishberg.viewer.RendererDelegate;

/**
 * Базовый интерфейс для команд отладки
 */
public interface DebugCommand {
    
    /**
     * Возвращает описание команды для отображения в UI
     */
    String getDescription();
    
    /**
     * Выполняет отрисовку текущего состояния отладки
     * @param renderer Рендерер для отрисовки
     * @param modelMatrix Матрица модели
     * @param viewMatrix Матрица вида
     * @param projectionMatrix Матрица проекции
     */
    void render(RendererDelegate renderer, float[] modelMatrix, float[] viewMatrix, float[] projectionMatrix);
} 