package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;
import java.util.List;

public class DebugPolygonTriangulationCmd implements DebugCmd {

    private final Polygon polygon;
    private final List<Facet> facets;

    public DebugPolygonTriangulationCmd(Polygon polygon, List<Facet> facets) {
        this.polygon = polygon;
        this.facets = facets;
    }

    @Override
    public String getDescription() {
        String verticesInfo = EndCmd.verticesToString(polygon.getVertices());
        return "DebugPolygonTriangulationCmd polygon = " + verticesInfo;
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {
        debugVisualizer.drawDebugPolygon(polygon.getVertices(), 0.7, Color.CYAN, Color.BLUE);

        for (int i = 0; i < facets.size(); i++) {
            Facet facet = facets.get(i);
            debugVisualizer.drawDebugPolygon(
                facet.getTriangle().getPoints(),
                0.8,
                Colors.COLORS[i % Colors.COLORS.length]
            );
            for (V3d vertex : facet.getTriangle().getPoints()) {
                Color color = Colors.COLORS[i % Colors.COLORS.length]; // Циклический выбор цвета
                debugVisualizer.drawDebugPoint(vertex, 2.0, color);
            }
        }
    }
}
