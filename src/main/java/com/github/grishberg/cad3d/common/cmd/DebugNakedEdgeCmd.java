package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;

public class DebugNakedEdgeCmd implements DebugCmd {

    private final Polygon polygon;
    private final V3d nakedEdgeA;
    private final V3d nakedEdgeB;
    private final Facet facet;

    public DebugNakedEdgeCmd(Polygon polygon, V3d nakedEdgeA, V3d nakedEdgeB, Facet facet) {
        this.polygon = polygon;
        this.nakedEdgeA = nakedEdgeA;
        this.nakedEdgeB = nakedEdgeB;
        this.facet = facet;
    }

    @Override
    public String getDescription() {
        String verticesInfo = polygon != null ? EndCmd.verticesToString(polygon.getVertices()) :
            EndCmd.verticesToString(facet.getTriangle().getPoints());
        return "DebugNakedEdgeCmd polygon = " + verticesInfo + ", A = " + nakedEdgeA + ", B = " + nakedEdgeB;
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {
        if (polygon != null) {
            debugVisualizer.drawDebugPolygon(polygon.getVertices(), 0.7, Color.YELLOW, Color.BLUE);
        }
        debugVisualizer.drawDebugPoint(nakedEdgeA, 2.1, Color.GREEN);
        debugVisualizer.drawDebugPoint(nakedEdgeB, 2.1, Color.RED);
        debugVisualizer.drawDebugPolygon(facet.getTriangle().getPoints(), 0.8, Color.RED);
    }
}
