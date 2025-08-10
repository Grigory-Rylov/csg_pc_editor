package com.github.grishberg.cad3d.ui;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.util.BuffersContainer;

import java.util.List;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

/**
 * Интерфейс для визуализации отладочных состояний
 */
public interface DebugVisualizer {
    /**
     * Применяет визуализацию для отладочной команды
     * @param cmd отладочная команда
     */
    void applyDebugVisualization(DebugCmd cmd);
    
    /**
     * Очищает все визуальные эффекты
     */
    void clearVisualization();
    
    /**
     * Рисует отладочный полигон
     * @param vertices список вершин полигона
     * @param lineColor цвет полигона
     */
    void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor);
    /**
     * Рисует отладочный полигон
     * @param vertices список вершин полигона
     * @param lineColor цвет полигона
     * @param vertexColor цвет вершины
     */
    void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor,  Color vertexColor);

    /**
     * Рисует отладочную линию
     * @param p0 начальная точка
     * @param p1 конечная точка
     * @param color цвет линии
     */
    void drawDebugLine(V3d p0, V3d p1, double thickness, Color color);
    
    /**
     * Рисует отладочную точку (куб размером 1)
     * @param p позиция точки
     * @param color цвет точки
     */
    void drawDebugPoint(V3d p, double thickness, Color color);

    /**
     * Возвращает список буферов для отрисовки отладочных примитивов
     * @return список буферов
     */
    List<BuffersContainer> getDebugBuffers();
}
