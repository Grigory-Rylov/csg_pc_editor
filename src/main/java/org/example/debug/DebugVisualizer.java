package org.example.debug;

import com.github.grishberg.cad3d.common.DebugCmd;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

import java.util.List;

/**
 * Интерфейс для визуализации отладочных состояний в OpenGL
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
     * @param thickness толщина линии
     * @param lineColor цвет полигона
     */
    void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor);
    
    /**
     * Рисует отладочный полигон с отдельными цветами для линий и вершин
     * @param vertices список вершин полигона
     * @param thickness толщина линии
     * @param lineColor цвет полигона
     * @param vertexColor цвет вершины
     */
    void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor, Color vertexColor);

    /**
     * Рисует отладочную линию
     * @param p0 начальная точка
     * @param p1 конечная точка
     * @param thickness толщина линии
     * @param color цвет линии
     */
    void drawDebugLine(V3d p0, V3d p1, double thickness, Color color);
    
    /**
     * Рисует отладочную точку (куб размером thickness)
     * @param p позиция точки
     * @param thickness размер точки
     * @param color цвет точки
     */
    void drawDebugPoint(V3d p, double thickness, Color color);

    /**
     * Отрисовывает все накопленные debug объекты
     */
    void renderDebugObjects();
} 