package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;

public class EdgeSpanningCmd implements DebugCmd {
    private final V3d currentVertex;
    private final V3d newVertex;
    private final V3d spanningVertex;
    private final SplitStartedCmd splitStartedCmd;

    public EdgeSpanningCmd(V3d currentVertex, V3d newVertex, V3d spanningVertex, SplitStartedCmd splitStartedCmd) {
        this.currentVertex = currentVertex;
        this.newVertex = newVertex;
        this.spanningVertex = spanningVertex;
        this.splitStartedCmd = splitStartedCmd;
    }

    @Override
    public String getDescription() {
        return "Edge spanning - Current: (" + currentVertex +
            "), New: (" + newVertex +
            "), Spanning: (" + spanningVertex + ")";
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
            debugVisualizer.drawDebugLine(currentVertex, newVertex, 1.0, Color.ORANGE);

        // Рисуем линии от spanning вершины к остальным вершинам голубым цветом
        debugVisualizer.drawDebugLine(spanningVertex, currentVertex, 1.0, Color.CYAN);
            debugVisualizer.drawDebugLine(spanningVertex, newVertex, 1.0, Color.CYAN);
    }
} 
