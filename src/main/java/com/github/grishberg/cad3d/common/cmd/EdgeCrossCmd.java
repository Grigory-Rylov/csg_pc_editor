package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;

import java.util.ArrayList;
import java.util.List;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.VertexPosition;

public class EdgeCrossCmd implements DebugCmd {
    private final V3d currentVertex;
    private final V3d newVertex;
    private final V3d spanningVertex;
    private final SplitStartedCmd splitStartedCmd;
	private final List<V3d> list;
	private final VertexPosition vp;

    public EdgeCrossCmd(List<V3d> list,
                        V3d currentVertex,
                        V3d newVertex,
                        V3d spanningVertex,
                        VertexPosition vp,
                        SplitStartedCmd splitStartedCmd) {
        this.currentVertex = currentVertex;
        this.newVertex = newVertex;
        this.spanningVertex = spanningVertex;
        this.splitStartedCmd = splitStartedCmd;
        this.list = new ArrayList<>(list);
		this.vp = vp;
    }

    @Override
    public String getDescription() {
        return "Edge cross - Current: (" + currentVertex +
            "), New: (" + newVertex +
            "), Cross: (" + spanningVertex + "), vert pos = " + vp;
    }

	@Override
    public void render(DebugVisualizer debugVisualizer) {
        if (splitStartedCmd != null) {
            splitStartedCmd.render(debugVisualizer);
        }
        // Рисуем текущую вершину оранжевым цветом
        debugVisualizer.drawDebugPoint(currentVertex, 1.5, Color.ORANGE);

        // Рисуем следующую вершину фиолетовым цветом
        debugVisualizer.drawDebugPoint(newVertex, 1.5, Color.MAGENTA);

        // Рисуем spanning вершину голубым цветом
        debugVisualizer.drawDebugPoint(spanningVertex, 2.0, Color.CYAN);

        // Рисуем линию между текущей и следующей вершиной оранжевым цветом
        debugVisualizer.drawDebugLine(currentVertex, newVertex, 1.0, Color.GREEN);

        // Рисуем линии от spanning вершины к остальным вершинам голубым цветом
        debugVisualizer.drawDebugLine(spanningVertex, currentVertex, 1.0, Color.CYAN);
        debugVisualizer.drawDebugLine(spanningVertex, newVertex, 1.0, Color.CYAN);
		
		for(V3d p : list){
			debugVisualizer.drawDebugPoint(p, 2.2, Color.WHITE);
		}
    }
} 
