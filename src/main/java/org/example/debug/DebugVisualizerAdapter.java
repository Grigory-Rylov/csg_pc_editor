package org.example.debug;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.util.BuffersContainer;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для интеграции нашего DebugVisualizer с DebugCmd из Android проекта
 */
public class DebugVisualizerAdapter implements com.github.grishberg.cad3d.ui.DebugVisualizer {
    private final org.example.debug.DebugVisualizer actualVisualizer;
    
    public DebugVisualizerAdapter(org.example.debug.DebugVisualizer actualVisualizer) {
        this.actualVisualizer = actualVisualizer;
    }
    
    @Override
    public void applyDebugVisualization(DebugCmd cmd) {
        cmd.render(this);
        System.out.println(cmd.getDescription());
    }
    
    @Override
    public void clearVisualization() {
        actualVisualizer.clearVisualization();
    }
    
    @Override
    public void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor) {
        actualVisualizer.drawDebugPolygon(vertices, thickness, lineColor);
    }
    
    @Override
    public void drawDebugPolygon(List<V3d> vertices, double thickness, Color lineColor, Color vertexColor) {
        actualVisualizer.drawDebugPolygon(vertices, thickness, lineColor, vertexColor);
    }
    
    @Override
    public void drawDebugLine(V3d p0, V3d p1, double thickness, Color color) {
        actualVisualizer.drawDebugLine(p0, p1, thickness, color);
    }
    
    @Override
    public void drawDebugPoint(V3d p, double thickness, Color color) {
        actualVisualizer.drawDebugPoint(p, thickness, color);
    }
    
    @Override
    public List<BuffersContainer> getDebugBuffers() {
        // Для OpenGL версии нам не нужны буферы, так как мы рендерим напрямую
        return new ArrayList<>();
    }
} 
