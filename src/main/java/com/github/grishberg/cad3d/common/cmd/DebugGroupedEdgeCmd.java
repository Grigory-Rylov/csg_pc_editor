package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import com.github.grishberg.cad3d.util.PolygonValidator;

import java.util.List;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Polygon;

public class DebugGroupedEdgeCmd implements DebugCmd {
    private Color[] colors = {
        new Color(255, 175, 175), // pink (ваш пример)
        new Color(255, 0, 0),     // red
        new Color(0, 255, 0),     // green
        new Color(0, 0, 255),     // blue
        new Color(255, 255, 0),   // yellow
        new Color(255, 0, 255),   // magenta
        new Color(0, 255, 255),   // cyan
        new Color(255, 165, 0),   // orange
        new Color(128, 0, 128),   // purple
        new Color(255, 192, 203), // pink (light)
        new Color(165, 42, 42),   // brown
        new Color(0, 128, 0),     // green (dark)
        new Color(0, 0, 128),     // navy
        new Color(255, 215, 0),   // gold
        new Color(75, 0, 130),    // indigo
        new Color(255, 105, 180), // hot pink
        new Color(50, 205, 50),   // lime green
        new Color(139, 69, 19),   // saddle brown
        new Color(128, 128, 0),   // olive
        new Color(0, 191, 255)    // deep sky blue
    };


    private final PolygonValidator.LineKey key;
    private final List<PolygonValidator.PolygonEdge> polygons;

    public DebugGroupedEdgeCmd(PolygonValidator.LineKey key, List<PolygonValidator.PolygonEdge> polygons) {
        this.key = key;
        this.polygons = polygons;
    }

    @Override
    public String getDescription() {
        return "DebugGroupedEdgeCmd edge: polygons =" + polygons.size() +" point = " + key.getPointOnLine() + ", direction = " + key.getDirectionUnitVector();
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {

        double length = 30;
        // Половина длины отрезка
        double halfLength = length / 2.0;

        // Вычисляем смещение по направлению вектора
        // Так как вектор единичный, умножение дает вектор длины halfLength
        double dx = key.getDirectionUnitVector().x * halfLength;
        double dy = key.getDirectionUnitVector().y * halfLength;
        double dz = key.getDirectionUnitVector().z * halfLength;

        // Точка p0: от pointOnLine на halfLength в направлении, противоположном directionUnitVector
        V3d p0 = new V3d(key.getPointOnLine().x - dx, key.getPointOnLine().y - dy, key.getPointOnLine().z - dz);

        // Точка p1: от pointOnLine на halfLength в направлении directionUnitVector
        V3d p1 = new V3d(key.getPointOnLine().x + dx, key.getPointOnLine().y + dy, key.getPointOnLine().z + dz);
        debugVisualizer.drawDebugLine(p0, p1, 2.1, Color.CYAN);

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i).polygon;
            Color color = colors[i % colors.length]; // Циклический выбор цвета
            debugVisualizer.drawDebugPolygon(polygon.getVertices(), 0.8, color);
        }
    }
}
