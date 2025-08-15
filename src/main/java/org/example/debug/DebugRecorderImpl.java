package org.example.debug;

import com.github.grishberg.cad3d.common.DebugCmd;
import com.github.grishberg.cad3d.common.DebugRecorder;
import com.github.grishberg.cad3d.common.cmd.AddVertexNextPoint;
import com.github.grishberg.cad3d.common.cmd.ClassifyAndSplitVertexCmd;
import com.github.grishberg.cad3d.common.cmd.DebugGroupedEdgeCmd;
import com.github.grishberg.cad3d.common.cmd.DebugNakedEdgeCmd;
import com.github.grishberg.cad3d.common.cmd.DebugPolygonTriangulationCmd;
import com.github.grishberg.cad3d.common.cmd.EdgeCrossCmd;
import com.github.grishberg.cad3d.common.cmd.EdgeSpanningCmd;
import com.github.grishberg.cad3d.common.cmd.EndCmd;
import com.github.grishberg.cad3d.common.cmd.EndFacetsCmd;
import com.github.grishberg.cad3d.common.cmd.LogCmd;
import com.github.grishberg.cad3d.common.cmd.SplitPolygonCompletedCmd;
import com.github.grishberg.cad3d.common.cmd.SplitStartedCmd;
import eu.printingin3d.javascad.utils.PolygonValidator;
import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;
import eu.printingin3d.javascad.vrl.VertexPosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация DebugRecorder для записи отладочных команд в OpenGL проекте
 */
public class DebugRecorderImpl implements DebugRecorder {

    private final List<DebugCmd> commands = new ArrayList<>();
    private volatile SplitStartedCmd splitStartedCmd = null;

    @Override
    public List<DebugCmd> getCommands() {
        return new ArrayList<>(commands);
    }

    @Override
    public void clear() {
        commands.clear();
    }

    @Override
    public int getCommandCount() {
        return commands.size();
    }

    @Override
    public DebugCmd getCommand(int index) {
        if (index >= 0 && index < commands.size()) {
            return commands.get(index);
        }
        return null;
    }

    @Override
    public void onSplitPolygonStarted(
        Polygon polygon,
        Polygon polygon1,
        VertexPosition polygonType
    ) {
        SplitStartedCmd cmd = new SplitStartedCmd(polygon, polygon1, polygonType);
        splitStartedCmd = cmd;
        commands.add(cmd);
    }

    @Override
    public void onClassifyAndSplitVertex(
        V3d currentVertex,
        V3d nextVertex,
        VertexPosition position
    ) {
        ClassifyAndSplitVertexCmd cmd =
            new ClassifyAndSplitVertexCmd(currentVertex, nextVertex, position, splitStartedCmd);
        commands.add(cmd);
    }

    @Override
    public void onEdgeSpanning(V3d currentVertex, V3d newVertex, V3d v) {
        EdgeSpanningCmd cmd = new EdgeSpanningCmd(currentVertex, newVertex, v, splitStartedCmd);
        commands.add(cmd);
    }

    @Override
    public void onEdgeCross(
        List<V3d> list,
        V3d currentVertex,
        V3d newVertex,
        V3d cross,
        VertexPosition vp
    ) {
        EdgeCrossCmd cmd =
            new EdgeCrossCmd(list, currentVertex, newVertex, cross, vp, splitStartedCmd);
        commands.add(cmd);
    }

    @Override
    public void onAddVertexNextPoint(
        List<V3d> list, V3d lastVertex, V3d newVertex, V3d prev, V3d curr,
        VertexPosition vp
    ) {
        AddVertexNextPoint cmd = new AddVertexNextPoint(
            list, lastVertex, newVertex, prev, curr, vp,
            splitStartedCmd
        );
        commands.add(cmd);
    }

    @Override
    public void onSplitPolygonCompleted(List<V3d> front, List<V3d> back) {
        commands.add(new SplitPolygonCompletedCmd(front, back, splitStartedCmd));
    }

    @Override
    public void onEnd(List<Polygon> polygons) {
        commands.add(new EndCmd(polygons));
    }

    @Override
    public void onEnd(Polygon polygon) {
        commands.add(new EndCmd(polygon));
    }

    @Override
    public void onEndFacets(List<Facet> facets) {
        commands.add(new EndFacetsCmd(facets));
    }

    @Override
    public void onLogEvent(String message) {
        commands.add(new LogCmd(message, splitStartedCmd));
    }

    @Override
    public void onLogEvent(String message, List<V3d> points) {
        commands.add(new LogCmd(message, points, splitStartedCmd));
    }

    @Override
    public void onDebugNakedEdge(Polygon polygon, V3d nakedEdgeA, V3d nakedEdgeB, Facet facet) {
        commands.add(new DebugNakedEdgeCmd(polygon, nakedEdgeA, nakedEdgeB, facet));
    }

    @Override
    public void onDebugPolygonTriangulation(Polygon polygon, List<Facet> facets) {
        commands.add(new DebugPolygonTriangulationCmd(polygon, facets));
    }

    @Override
    public void onGroupedEdge(
        PolygonValidator.LineKey key,
        List<PolygonValidator.PolygonEdge> polygons
    ) {
        commands.add(new DebugGroupedEdgeCmd(key, polygons));
    }
} 
