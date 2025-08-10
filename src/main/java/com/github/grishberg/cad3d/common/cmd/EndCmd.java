package com.github.grishberg.cad3d.common.cmd;


import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Polygon;
import java.util.ArrayList;
import java.util.List;

public class EndCmd implements DebugCmd {

    private final List<Polygon> polygons;


    public EndCmd(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public EndCmd(Polygon p) {
        this.polygons = new ArrayList<>();
        polygons.add(p);
    }

    @Override
    public String getDescription() {
        return "End - polygons: vertex =" + polygons.get(0).getVertices().size() + " , " + verticesToString(polygons.get(0).getVertices());
    }

    public static String verticesToString(List<V3d> vertices) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(vertices.get(i));
        }
        return sb.toString();
    }

    public void render(DebugVisualizer debugVisualizer) {

        // Рисуем полигоны с циклической сменой цветов
        for (int i = 0; i < polygons.size(); i++) {
            Polygon p = polygons.get(i);
            Color color = Colors.COLORS[i % Colors.COLORS.length]; // Циклический выбор цвета
            debugVisualizer.drawDebugPolygon(p.getVertices(), 0.3, color, Color.BLUE);
        }
    }
}
