package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.Facet;
import java.util.List;

public class EndFacetsCmd implements DebugCmd {
    private final List<Facet> facets;


    public EndFacetsCmd(List<Facet> facets) {
        this.facets = facets;
    }

    @Override
    public String getDescription() {
        return "End - facets: " + facets.size();
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {

        // Рисуем фасеты с циклической сменой цветов
        for (int i = 0; i < facets.size(); i++) {
            Facet p = facets.get(i);
            Color color = Colors.COLORS[i % Colors.COLORS.length]; // Циклический выбор цвета
            debugVisualizer.drawDebugPolygon(p.getTriangle().getPoints(), 0.8, color);
        }
    }
}
