package org.example.debug;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

import java.util.List;

/**
 * Класс для хранения отладочных объектов
 */
public class DebugObject {
    public enum Type {
        POINT, LINE, POLYGON
    }
    
    private final Type type;
    private final List<V3d> vertices;
    private final double thickness;
    private final Color color;
    private final Color vertexColor; // Дополнительный цвет для вершин
    
    public DebugObject(Type type, List<V3d> vertices, double thickness, Color color) {
        this(type, vertices, thickness, color, null);
    }
    
    public DebugObject(Type type, List<V3d> vertices, double thickness, Color color, Color vertexColor) {
        this.type = type;
        this.vertices = vertices;
        this.thickness = thickness;
        this.color = color;
        this.vertexColor = vertexColor;
    }
    
    public Type getType() {
        return type;
    }
    
    public List<V3d> getVertices() {
        return vertices;
    }
    
    public double getThickness() {
        return thickness;
    }
    
    public Color getColor() {
        return color;
    }
    
    public Color getVertexColor() {
        return vertexColor;
    }
    
    public boolean hasVertexColor() {
        return vertexColor != null;
    }
} 