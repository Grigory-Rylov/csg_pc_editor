package com.github.grishberg.cad3d.ui;

import eu.printingin3d.javascad.coords.V3d;
import java.util.List;

/**
 * Класс для хранения данных отладочного полигона
 */
public class DebugPolygonData {
    public enum PolygonType {
        CURRENT("current"),
        OTHER("other"),
        FRONT("front"),
        BACK("back"),
        COPLANAR("coplanar"),
        INTERSECTION("intersection");
        
        private final String type;
        
        PolygonType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    private final List<V3d> vertices;
    private final V3d normal;
    private final PolygonType type;
    private final float[] color;
    private final int id;
    
    public DebugPolygonData(List<V3d> vertices, V3d normal, PolygonType type, float[] color, int id) {
        this.vertices = vertices;
        this.normal = normal;
        this.type = type;
        this.color = color;
        this.id = id;
    }
    
    public DebugPolygonData(List<V3d> vertices, float[] color) {
        this.vertices = vertices;
        this.normal = null;
        this.type = PolygonType.CURRENT;
        this.color = color;
        this.id = 0;
    }
    
    public List<V3d> getVertices() {
        return vertices;
    }
    
    public V3d getNormal() {
        return normal;
    }
    
    public PolygonType getType() {
        return type;
    }
    
    public float[] getColor() {
        return color;
    }
    
    public int getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return "DebugPolygonData{" +
                "vertices=" + vertices.size() +
                ", type=" + type +
                ", id=" + id +
                '}';
    }
} 