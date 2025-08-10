package com.github.grishberg.cad3d.common.cmd;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.utils.Color;
import eu.printingin3d.javascad.vrl.VertexPosition;

public class ClassifyAndSplitVertexCmd implements DebugCmd {

    private final V3d currentVertex;
    private final V3d nextVertex;
    private final VertexPosition position;
    private final SplitStartedCmd splitStartedCmd;

    public ClassifyAndSplitVertexCmd(
        V3d currentVertex, V3d nextVertex,
        VertexPosition position,
        SplitStartedCmd splitStartedCmd
    ) {
        this.currentVertex = currentVertex;
        this.nextVertex = nextVertex;
        this.position = position;
        this.splitStartedCmd = splitStartedCmd;
    }

    @Override
    public String getDescription() {
        return "Classify and split vertex - Current: (" +
            String.format(
                "%.1f, %.1f, %.1f",
                currentVertex.getX(),
                currentVertex.getY(),
                currentVertex.getZ()
            ) +
            "), Next: (" +
            String.format(
                "%.1f, %.1f, %.1f",
                nextVertex.getX(),
                nextVertex.getY(),
                nextVertex.getZ()
            ) + "), position = " + position;
    }

    @Override
    public void render(DebugVisualizer debugVisualizer) {
        if (splitStartedCmd != null) {
            splitStartedCmd.render(debugVisualizer);
        }

        // Рисуем текущую вершину желтым цветом
        debugVisualizer.drawDebugPoint(currentVertex, 1.5, Color.YELLOW);

        // Рисуем следующую вершину зеленым цветом
        debugVisualizer.drawDebugPoint(nextVertex, 1.5, Color.GREEN);

        // Рисуем линию между вершинами желтым цветом
        debugVisualizer.drawDebugLine(currentVertex, nextVertex, 1.0, Color.YELLOW);
    }
} 
